package com.penglecode.codeforce.mybatistiny.core;

import com.penglecode.codeforce.common.util.JdbcUtils;
import com.penglecode.codeforce.common.util.ReflectionUtils;
import com.penglecode.codeforce.common.util.StringUtils;
import com.penglecode.codeforce.mybatistiny.dialect.DialectManager;
import com.penglecode.codeforce.mybatistiny.executor.DynamicExecutor;
import com.penglecode.codeforce.mybatistiny.mapper.BaseEntityMapper;
import com.penglecode.codeforce.mybatistiny.support.DefaultDatabaseIdProvider;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.builder.CacheRefResolver;
import org.apache.ibatis.builder.ResultMapResolver;
import org.apache.ibatis.builder.annotation.MethodResolver;
import org.apache.ibatis.builder.xml.XMLStatementBuilder;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.loader.ProxyFactory;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.io.VFS;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.InterceptorChain;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.LanguageDriverRegistry;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.util.Assert;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 自定义Configuration，用于实现应用程序在第一次通过{@link Configuration#getMapper(Class, SqlSession)}获取某个XxxMapper.class接口的代理实例时，
 * 顺带自动生成其XxxMapper.xml，并将这个自动生成的XxxMapper.xml加载到{@link Configuration}中，最终就是运行时动态生成了许多{@link MappedStatement}。
 * 这个契机(Configuration#getMapper(..))是从根儿上的唯一扎口，不论你用何种框架来集成Mybatis
 *
 * @author pengpeng
 * @version 1.0
 */
@SuppressWarnings("unchecked")
public class DecoratedConfiguration extends Configuration {

    /**
     * 被代理的Mybatis配置
     */
    private final Configuration delegate;

    /**
     * 当前Configuration上下文下的所有实体元数据信息
     */
    private final Map<Class<?>, EntityMeta> allEntityMetas = new HashMap<>();

    /**
     * 实体对象的XML-Mapper注册器
     */
    private final EntityMapperRegistrar entityMapperRegistrar;

    /**
     * 实体对象的XML-Mapper注册信息
     */
    private final ConcurrentMap<Class<BaseEntityMapper<?>>,String> entityMapperRegistries;

    public DecoratedConfiguration(Configuration delegate) {
        this.delegate = delegate;
        this.initDatabaseId();
        this.initInterceptorChain();
        this.entityMapperRegistrar = new EntityMapperRegistrar(this);
        this.entityMapperRegistries = new ConcurrentHashMap<>();
    }

    /**
     * 初始化databaseId，如果需要的话
     */
    protected void initDatabaseId() {
        String databaseId = delegate.getDatabaseId();
        if(StringUtils.isNotBlank(databaseId)) { //如果应用程序设置了databaseId，则需要检查其对应的方言在DatabaseDialectEnum中是否注册
            Assert.isTrue(DialectManager.hasDialect(databaseId), String.format("Unsupported databaseId(%s) in your Mybatis Configuration!", databaseId));
        } else {
            databaseId = getDatabaseIdByJdbcUrl();
            if(StringUtils.isBlank(databaseId)) {
                databaseId = getDatabaseIdByProvider();
            }
            Assert.isTrue(DialectManager.hasDialect(databaseId), String.format("Unsupported databaseId(%s), No suitable DatabaseDialect found!", databaseId));
            delegate.setDatabaseId(databaseId);
        }
    }

    private String getDatabaseIdByJdbcUrl() {
        try {
            try (Connection connection = delegate.getEnvironment().getDataSource().getConnection()) {
                return JdbcUtils.getDbType(connection.getMetaData().getURL());
            }
        } catch (SQLException e) {
            throw new IllegalStateException(String.format("Can't obtain databaseId: %s", e.getMessage()), e);
        }
    }

    private String getDatabaseIdByProvider() {
        DatabaseIdProvider databaseIdProvider = new DefaultDatabaseIdProvider();
        try {
            return databaseIdProvider.getDatabaseId(delegate.getEnvironment().getDataSource());
        } catch (SQLException e) {
            throw new IllegalStateException(String.format("Can't obtain databaseId: %s", e.getMessage()), e);
        }
    }

    /**
     * 将delegate.interceptorChain设置到当前对象上来,在下面newExecutor(..)时要用到
     */
    protected void initInterceptorChain() {
        String fieldName = "interceptorChain";
        InterceptorChain interceptorChain = ReflectionUtils.getFieldValue(delegate, fieldName);
        ReflectionUtils.setFinalFieldValue(this, fieldName, interceptorChain);
    }

    @Override
    public String getLogPrefix() {
        return delegate.getLogPrefix();
    }

    @Override
    public void setLogPrefix(String logPrefix) {
        delegate.setLogPrefix(logPrefix);
    }

    @Override
    public Class<? extends Log> getLogImpl() {
        return delegate.getLogImpl();
    }

    @Override
    public void setLogImpl(Class<? extends Log> logImpl) {
        delegate.setLogImpl(logImpl);
    }

    @Override
    public Class<? extends VFS> getVfsImpl() {
        return delegate.getVfsImpl();
    }

    @Override
    public void setVfsImpl(Class<? extends VFS> vfsImpl) {
        delegate.setVfsImpl(vfsImpl);
    }

    @Override
    public boolean isCallSettersOnNulls() {
        return delegate.isCallSettersOnNulls();
    }

    @Override
    public void setCallSettersOnNulls(boolean callSettersOnNulls) {
        delegate.setCallSettersOnNulls(callSettersOnNulls);
    }

    @Override
    public boolean isUseActualParamName() {
        return delegate.isUseActualParamName();
    }

    @Override
    public void setUseActualParamName(boolean useActualParamName) {
        delegate.setUseActualParamName(useActualParamName);
    }

    @Override
    public boolean isReturnInstanceForEmptyRow() {
        return delegate.isReturnInstanceForEmptyRow();
    }

    @Override
    public void setReturnInstanceForEmptyRow(boolean returnEmptyInstance) {
        delegate.setReturnInstanceForEmptyRow(returnEmptyInstance);
    }

    @Override
    public boolean isShrinkWhitespacesInSql() {
        return delegate.isShrinkWhitespacesInSql();
    }

    @Override
    public void setShrinkWhitespacesInSql(boolean shrinkWhitespacesInSql) {
        delegate.setShrinkWhitespacesInSql(shrinkWhitespacesInSql);
    }

    @Override
    public String getDatabaseId() {
        return delegate.getDatabaseId();
    }

    @Override
    public void setDatabaseId(String databaseId) {
        delegate.setDatabaseId(databaseId);
    }

    @Override
    public Class<?> getConfigurationFactory() {
        return delegate.getConfigurationFactory();
    }

    @Override
    public void setConfigurationFactory(Class<?> configurationFactory) {
        delegate.setConfigurationFactory(configurationFactory);
    }

    @Override
    public boolean isSafeResultHandlerEnabled() {
        return delegate.isSafeResultHandlerEnabled();
    }

    @Override
    public void setSafeResultHandlerEnabled(boolean safeResultHandlerEnabled) {
        delegate.setSafeResultHandlerEnabled(safeResultHandlerEnabled);
    }

    @Override
    public boolean isSafeRowBoundsEnabled() {
        return delegate.isSafeRowBoundsEnabled();
    }

    @Override
    public void setSafeRowBoundsEnabled(boolean safeRowBoundsEnabled) {
        delegate.setSafeRowBoundsEnabled(safeRowBoundsEnabled);
    }

    @Override
    public boolean isMapUnderscoreToCamelCase() {
        return delegate.isMapUnderscoreToCamelCase();
    }

    @Override
    public void setMapUnderscoreToCamelCase(boolean mapUnderscoreToCamelCase) {
        delegate.setMapUnderscoreToCamelCase(mapUnderscoreToCamelCase);
    }

    @Override
    public void addLoadedResource(String resource) {
        delegate.addLoadedResource(resource);
    }

    @Override
    public boolean isResourceLoaded(String resource) {
        return delegate.isResourceLoaded(resource);
    }

    @Override
    public Environment getEnvironment() {
        return delegate.getEnvironment();
    }

    @Override
    public void setEnvironment(Environment environment) {
        delegate.setEnvironment(environment);
    }

    @Override
    public AutoMappingBehavior getAutoMappingBehavior() {
        return delegate.getAutoMappingBehavior();
    }

    @Override
    public void setAutoMappingBehavior(AutoMappingBehavior autoMappingBehavior) {
        delegate.setAutoMappingBehavior(autoMappingBehavior);
    }

    @Override
    public AutoMappingUnknownColumnBehavior getAutoMappingUnknownColumnBehavior() {
        return delegate.getAutoMappingUnknownColumnBehavior();
    }

    @Override
    public void setAutoMappingUnknownColumnBehavior(AutoMappingUnknownColumnBehavior autoMappingUnknownColumnBehavior) {
        delegate.setAutoMappingUnknownColumnBehavior(autoMappingUnknownColumnBehavior);
    }

    @Override
    public boolean isLazyLoadingEnabled() {
        return delegate.isLazyLoadingEnabled();
    }

    @Override
    public void setLazyLoadingEnabled(boolean lazyLoadingEnabled) {
        delegate.setLazyLoadingEnabled(lazyLoadingEnabled);
    }

    @Override
    public ProxyFactory getProxyFactory() {
        return delegate.getProxyFactory();
    }

    @Override
    public void setProxyFactory(ProxyFactory proxyFactory) {
        delegate.setProxyFactory(proxyFactory);
    }

    @Override
    public boolean isAggressiveLazyLoading() {
        return delegate.isAggressiveLazyLoading();
    }

    @Override
    public void setAggressiveLazyLoading(boolean aggressiveLazyLoading) {
        delegate.setAggressiveLazyLoading(aggressiveLazyLoading);
    }

    @Override
    public boolean isMultipleResultSetsEnabled() {
        return delegate.isMultipleResultSetsEnabled();
    }

    @Override
    public void setMultipleResultSetsEnabled(boolean multipleResultSetsEnabled) {
        delegate.setMultipleResultSetsEnabled(multipleResultSetsEnabled);
    }

    @Override
    public Set<String> getLazyLoadTriggerMethods() {
        return delegate.getLazyLoadTriggerMethods();
    }

    @Override
    public void setLazyLoadTriggerMethods(Set<String> lazyLoadTriggerMethods) {
        delegate.setLazyLoadTriggerMethods(lazyLoadTriggerMethods);
    }

    @Override
    public boolean isUseGeneratedKeys() {
        return delegate.isUseGeneratedKeys();
    }

    @Override
    public void setUseGeneratedKeys(boolean useGeneratedKeys) {
        delegate.setUseGeneratedKeys(useGeneratedKeys);
    }

    @Override
    public ExecutorType getDefaultExecutorType() {
        return delegate.getDefaultExecutorType();
    }

    @Override
    public void setDefaultExecutorType(ExecutorType defaultExecutorType) {
        delegate.setDefaultExecutorType(defaultExecutorType);
    }

    @Override
    public boolean isCacheEnabled() {
        return delegate.isCacheEnabled();
    }

    @Override
    public void setCacheEnabled(boolean cacheEnabled) {
        delegate.setCacheEnabled(cacheEnabled);
    }

    @Override
    public Integer getDefaultStatementTimeout() {
        return delegate.getDefaultStatementTimeout();
    }

    @Override
    public void setDefaultStatementTimeout(Integer defaultStatementTimeout) {
        delegate.setDefaultStatementTimeout(defaultStatementTimeout);
    }

    @Override
    public Integer getDefaultFetchSize() {
        return delegate.getDefaultFetchSize();
    }

    @Override
    public void setDefaultFetchSize(Integer defaultFetchSize) {
        delegate.setDefaultFetchSize(defaultFetchSize);
    }

    @Override
    public ResultSetType getDefaultResultSetType() {
        return delegate.getDefaultResultSetType();
    }

    @Override
    public void setDefaultResultSetType(ResultSetType defaultResultSetType) {
        delegate.setDefaultResultSetType(defaultResultSetType);
    }

    @Override
    public boolean isUseColumnLabel() {
        return delegate.isUseColumnLabel();
    }

    @Override
    public void setUseColumnLabel(boolean useColumnLabel) {
        delegate.setUseColumnLabel(useColumnLabel);
    }

    @Override
    public LocalCacheScope getLocalCacheScope() {
        return delegate.getLocalCacheScope();
    }

    @Override
    public void setLocalCacheScope(LocalCacheScope localCacheScope) {
        delegate.setLocalCacheScope(localCacheScope);
    }

    @Override
    public JdbcType getJdbcTypeForNull() {
        return delegate.getJdbcTypeForNull();
    }

    @Override
    public void setJdbcTypeForNull(JdbcType jdbcTypeForNull) {
        delegate.setJdbcTypeForNull(jdbcTypeForNull);
    }

    @Override
    public Properties getVariables() {
        return delegate.getVariables();
    }

    @Override
    public void setVariables(Properties variables) {
        delegate.setVariables(variables);
    }

    @Override
    public TypeHandlerRegistry getTypeHandlerRegistry() {
        return delegate.getTypeHandlerRegistry();
    }

    @Override
    public void setDefaultEnumTypeHandler(Class<? extends TypeHandler> typeHandler) {
        delegate.setDefaultEnumTypeHandler(typeHandler);
    }

    @Override
    public TypeAliasRegistry getTypeAliasRegistry() {
        return delegate.getTypeAliasRegistry();
    }

    @Override
    public MapperRegistry getMapperRegistry() {
        return delegate.getMapperRegistry();
    }

    @Override
    public ReflectorFactory getReflectorFactory() {
        return delegate.getReflectorFactory();
    }

    @Override
    public void setReflectorFactory(ReflectorFactory reflectorFactory) {
        delegate.setReflectorFactory(reflectorFactory);
    }

    @Override
    public ObjectFactory getObjectFactory() {
        return delegate.getObjectFactory();
    }

    @Override
    public void setObjectFactory(ObjectFactory objectFactory) {
        delegate.setObjectFactory(objectFactory);
    }

    @Override
    public ObjectWrapperFactory getObjectWrapperFactory() {
        return delegate.getObjectWrapperFactory();
    }

    @Override
    public void setObjectWrapperFactory(ObjectWrapperFactory objectWrapperFactory) {
        delegate.setObjectWrapperFactory(objectWrapperFactory);
    }

    @Override
    public List<Interceptor> getInterceptors() {
        return delegate.getInterceptors();
    }

    @Override
    public LanguageDriverRegistry getLanguageRegistry() {
        return delegate.getLanguageRegistry();
    }

    @Override
    public void setDefaultScriptingLanguage(Class<? extends LanguageDriver> driver) {
        delegate.setDefaultScriptingLanguage(driver);
    }

    @Override
    public LanguageDriver getDefaultScriptingLanguageInstance() {
        return delegate.getDefaultScriptingLanguageInstance();
    }

    @Override
    public LanguageDriver getLanguageDriver(Class<? extends LanguageDriver> langClass) {
        return delegate.getLanguageDriver(langClass);
    }

    @Override
    @Deprecated
    public LanguageDriver getDefaultScriptingLanuageInstance() {
        return delegate.getDefaultScriptingLanuageInstance();
    }

    @Override
    public MetaObject newMetaObject(Object object) {
        return delegate.newMetaObject(object);
    }

    @Override
    public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        return delegate.newParameterHandler(mappedStatement, parameterObject, boundSql);
    }

    @Override
    public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, RowBounds rowBounds, ParameterHandler parameterHandler, ResultHandler resultHandler, BoundSql boundSql) {
        return delegate.newResultSetHandler(executor, mappedStatement, rowBounds, parameterHandler, resultHandler, boundSql);
    }

    @Override
    public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        return delegate.newStatementHandler(executor, mappedStatement, parameterObject, rowBounds, resultHandler, boundSql);
    }

    @Override
    public Executor newExecutor(Transaction transaction) {
        return newExecutor(transaction, delegate.getDefaultExecutorType());
    }

    @Override
    public Executor newExecutor(Transaction transaction, ExecutorType executorType) {
        executorType = executorType == null ? delegate.getDefaultExecutorType() : executorType;
        executorType = executorType == null ? ExecutorType.SIMPLE : executorType;
        Executor executor = new DynamicExecutor(transaction, executorType, this);
        if (delegate.isCacheEnabled()) {
            executor = new CachingExecutor(executor);
        }
        //由于无法delegate.getInterceptorChain()，所以需要在上面构造器中初始化interceptorChain
        //总之，所有Plugin都得应用到当前Configuration上
        executor = (Executor) interceptorChain.pluginAll(executor);
        return executor;
    }

    @Override
    public void addKeyGenerator(String id, KeyGenerator keyGenerator) {
        delegate.addKeyGenerator(id, keyGenerator);
    }

    @Override
    public Collection<String> getKeyGeneratorNames() {
        return delegate.getKeyGeneratorNames();
    }

    @Override
    public Collection<KeyGenerator> getKeyGenerators() {
        return delegate.getKeyGenerators();
    }

    @Override
    public KeyGenerator getKeyGenerator(String id) {
        return delegate.getKeyGenerator(id);
    }

    @Override
    public boolean hasKeyGenerator(String id) {
        return delegate.hasKeyGenerator(id);
    }

    @Override
    public void addCache(Cache cache) {
        delegate.addCache(cache);
    }

    @Override
    public Collection<String> getCacheNames() {
        return delegate.getCacheNames();
    }

    @Override
    public Collection<Cache> getCaches() {
        return delegate.getCaches();
    }

    @Override
    public Cache getCache(String id) {
        return delegate.getCache(id);
    }

    @Override
    public boolean hasCache(String id) {
        return delegate.hasCache(id);
    }

    @Override
    public void addResultMap(ResultMap rm) {
        delegate.addResultMap(rm);
    }

    @Override
    public Collection<String> getResultMapNames() {
        return delegate.getResultMapNames();
    }

    @Override
    public Collection<ResultMap> getResultMaps() {
        return delegate.getResultMaps();
    }

    @Override
    public ResultMap getResultMap(String id) {
        return delegate.getResultMap(id);
    }

    @Override
    public boolean hasResultMap(String id) {
        return delegate.hasResultMap(id);
    }

    @Override
    public void addParameterMap(ParameterMap pm) {
        delegate.addParameterMap(pm);
    }

    @Override
    public Collection<String> getParameterMapNames() {
        return delegate.getParameterMapNames();
    }

    @Override
    public Collection<ParameterMap> getParameterMaps() {
        return delegate.getParameterMaps();
    }

    @Override
    public ParameterMap getParameterMap(String id) {
        return delegate.getParameterMap(id);
    }

    @Override
    public boolean hasParameterMap(String id) {
        return delegate.hasParameterMap(id);
    }

    @Override
    public void addMappedStatement(MappedStatement ms) {
        delegate.addMappedStatement(ms);
    }

    @Override
    public Collection<String> getMappedStatementNames() {
        return delegate.getMappedStatementNames();
    }

    @Override
    public Collection<MappedStatement> getMappedStatements() {
        return delegate.getMappedStatements();
    }

    @Override
    public Collection<XMLStatementBuilder> getIncompleteStatements() {
        return delegate.getIncompleteStatements();
    }

    @Override
    public void addIncompleteStatement(XMLStatementBuilder incompleteStatement) {
        delegate.addIncompleteStatement(incompleteStatement);
    }

    @Override
    public Collection<CacheRefResolver> getIncompleteCacheRefs() {
        return delegate.getIncompleteCacheRefs();
    }

    @Override
    public void addIncompleteCacheRef(CacheRefResolver incompleteCacheRef) {
        delegate.addIncompleteCacheRef(incompleteCacheRef);
    }

    @Override
    public Collection<ResultMapResolver> getIncompleteResultMaps() {
        return delegate.getIncompleteResultMaps();
    }

    @Override
    public void addIncompleteResultMap(ResultMapResolver resultMapResolver) {
        delegate.addIncompleteResultMap(resultMapResolver);
    }

    @Override
    public void addIncompleteMethod(MethodResolver builder) {
        delegate.addIncompleteMethod(builder);
    }

    @Override
    public Collection<MethodResolver> getIncompleteMethods() {
        return delegate.getIncompleteMethods();
    }

    @Override
    public MappedStatement getMappedStatement(String id) {
        return delegate.getMappedStatement(id);
    }

    @Override
    public MappedStatement getMappedStatement(String id, boolean validateIncompleteStatements) {
        return delegate.getMappedStatement(id, validateIncompleteStatements);
    }

    @Override
    public Map<String, XNode> getSqlFragments() {
        return delegate.getSqlFragments();
    }

    @Override
    public void addInterceptor(Interceptor interceptor) {
        delegate.addInterceptor(interceptor);
    }

    @Override
    public void addMappers(String packageName, Class<?> superType) {
        delegate.addMappers(packageName, superType);
    }

    @Override
    public void addMappers(String packageName) {
        delegate.addMappers(packageName);
    }

    @Override
    public <T> void addMapper(Class<T> type) {
        delegate.addMapper(type);
    }

    @Override
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        T mapperInstance = delegate.getMapper(type, sqlSession);
        if(mapperInstance instanceof BaseEntityMapper) { //如果生成的Mapper实例是mybatis-tiny框架下的BaseEntityMapper
            Class<BaseEntityMapper<?>> entityMapperClass = (Class<BaseEntityMapper<?>>) type;
            entityMapperRegistries.computeIfAbsent(entityMapperClass, entityMapperRegistrar::registerEntityMapper); //进行首次注册
        }
        return mapperInstance;
    }

    @Override
    public boolean hasMapper(Class<?> type) {
        return delegate.hasMapper(type);
    }

    @Override
    public boolean hasStatement(String statementName) {
        return delegate.hasStatement(statementName);
    }

    @Override
    public boolean hasStatement(String statementName, boolean validateIncompleteStatements) {
        return delegate.hasStatement(statementName, validateIncompleteStatements);
    }

    @Override
    public void addCacheRef(String namespace, String referencedNamespace) {
        delegate.addCacheRef(namespace, referencedNamespace);
    }

    public EntityMeta getEntityMeta(Class<?> entityType) {
        return allEntityMetas.get(entityType);
    }

    public void setEntityMeta(Class<?> entityType, EntityMeta entityMeta) {
        allEntityMetas.put(entityType, entityMeta);
    }

    protected Map<Class<?>, EntityMeta> getAllEntityMetas() {
        return allEntityMetas;
    }

    protected Configuration getDelegate() {
        return delegate;
    }

    protected EntityMapperRegistrar getEntityMapperRegistrar() {
        return entityMapperRegistrar;
    }

    protected ConcurrentMap<Class<BaseEntityMapper<?>>, String> getEntityMapperRegistries() {
        return entityMapperRegistries;
    }

}