package com.penglecode.codeforce.mybatistiny.core;

import com.penglecode.codeforce.common.util.ClassUtils;
import com.penglecode.codeforce.common.util.ReflectionUtils;
import com.penglecode.codeforce.mybatistiny.dsl.QueryCriteria;
import com.penglecode.codeforce.mybatistiny.exception.MapperTemplateException;
import com.penglecode.codeforce.mybatistiny.exception.MapperXmlParseException;
import com.penglecode.codeforce.mybatistiny.interceptor.DomainObjectQueryInterceptor;
import com.penglecode.codeforce.mybatistiny.interceptor.PageLimitInterceptor;
import com.penglecode.codeforce.mybatistiny.mapper.BaseEntityMapper;
import freemarker.template.Template;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 实体对象的XML-Mapper注册器
 * 通过实体Java-Mapper接口来注册自动生成的XML-Mapper，具体做了下面三件事
 *
 *  1、向{@link Configuration}中注册{@link DomainObjectQueryInterceptor}和{@link PageLimitInterceptor}
 *  2、向{@link Configuration}中注册CommonMybatisMapper.xml
 *  3、向{@link Configuration}中注册自动生成的XxxMapper.xml
 *
 * @author pengpeng
 * @version 1.0
 */
@SuppressWarnings({"unchecked"})
public class EntityMapperRegistrar {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityMapperRegistrar.class);

    private final DecoratedConfiguration configuration;

    private final EntityMapperTemplateParameterFactory entityMapperTemplateParameterFactory;

    public EntityMapperRegistrar(DecoratedConfiguration configuration) {
        this.configuration = configuration;
        this.entityMapperTemplateParameterFactory = createTemplateParameterFactory(configuration);
        this.registerCommonTypeAlias();
        this.registerCommonPlugin();
        this.registerCommonMapper();
    }

    /**
     * 注册CommonMybatisMapper.xml
     * 该公共XML-Mapper每个Configuration仅需注册一次即可
     */
    protected void registerCommonMapper() {
        String baseMapperLocation = BaseEntityMapper.class.getPackage().getName().replace(".", "/") + "/CommonMybatisMapper.xml";
        registerXmlMapper(new ClassPathResource(baseMapperLocation), baseMapperLocation);
    }

    /**
     * 注册公共的类型别名
     */
    protected void registerCommonTypeAlias() {
        configuration.getTypeAliasRegistry().registerAlias(QueryCriteria.class);
    }

    /**
     * 注册公共的插件
     */
    protected void registerCommonPlugin() {
        List<Interceptor> interceptors = configuration.getInterceptors();
        if(CollectionUtils.isEmpty(interceptors) || interceptors.stream().noneMatch(interceptor -> interceptor instanceof DomainObjectQueryInterceptor)) {
            configuration.addInterceptor(new DomainObjectQueryInterceptor());
            LOGGER.info(">>> Dynamically registered interceptor[{}] into {}", DomainObjectQueryInterceptor.class.getName(), configuration);
        }
        if(CollectionUtils.isEmpty(interceptors) || interceptors.stream().noneMatch(interceptor -> interceptor instanceof PageLimitInterceptor)) {
            configuration.addInterceptor(new PageLimitInterceptor());
            LOGGER.info(">>> Dynamically registered interceptor[{}] into {}", PageLimitInterceptor.class.getName(), configuration);
        }
    }

    protected EntityMapperTemplateParameterFactory createTemplateParameterFactory(DecoratedConfiguration configuration) {
        return new EntityMapperTemplateParameterFactory(configuration);
    }

    /**
     * 注册实体对象的Mybatis-Mapper
     *
     * @param entityMapperClass     - 实体对象Mapper的类型
     */
    public String registerEntityMapper(Class<BaseEntityMapper<?>> entityMapperClass) {
        Assert.isTrue(entityMapperClass.isInterface(), String.format("Parameter 'entityMapperClass'(%s) must be an interface!", entityMapperClass));
        //创建实体元数据
        EntityMeta entityMeta = createEntityMeta(entityMapperClass);
        //为动态生成的实体XxxMapper.xml创建所需的模板参数
        EntityMapperTemplateParameter templateParameter = getXmlMapperTemplateParameterFactory().createTemplateParameter(entityMapperClass, entityMeta);
        String entityXmlMapperLocation = entityMapperClass.getName().replace(".", "/") + ".xml"; //虚拟的位置
        String entityXmlMapperContent = determineEntityXmlMapper(entityMapperClass, entityXmlMapperLocation, templateParameter);
        LOGGER.debug("<-----------------------------【{}】----------------------------->\n{}", entityXmlMapperLocation, entityXmlMapperContent);
        registerXmlMapper(new ByteArrayResource(entityXmlMapperContent.getBytes(StandardCharsets.UTF_8), entityXmlMapperLocation), entityXmlMapperLocation);
        return entityXmlMapperLocation;
    }

    /**
     * 最终确定指定实体对象的Mapper.xml内容
     *
     * @param entityMapperClass         - 实体对象Mapper接口的类型
     * @param entityXmlMapperLocation   - 实体对象Mapper.xml的位置(虚拟的位置)
     * @param templateParameter         - Freemarker模板渲染所需参数
     * @return
     */
    protected String determineEntityXmlMapper(Class<BaseEntityMapper<?>> entityMapperClass, String entityXmlMapperLocation, EntityMapperTemplateParameter templateParameter) {
        Class<BaseEntityMapper<?>> baseEntityMapperClass = (Class<BaseEntityMapper<?>>) ClassUtils.resolveClassName(BaseEntityMapper.class.getName(), ClassUtils.getDefaultClassLoader());
        String entityXmlMapperContent = processBaseMapperTemplate(baseEntityMapperClass, templateParameter);
        //处理继承BaseEntityMapper扩展公共方法的情况开始
        Map<Class<BaseEntityMapper<?>>,Set<Method>> customBaseEntityMappers = getCustomBaseEntityMappers(entityMapperClass);
        if(!CollectionUtils.isEmpty(customBaseEntityMappers)) { //是否存在这种扩展?
            //如果存在则需要尝试将对应的自定义Mapper方法的xml合并到BaseEntityMapper.xml中去
            for(Map.Entry<Class<BaseEntityMapper<?>>,Set<Method>> entry : customBaseEntityMappers.entrySet()) {
                Class<BaseEntityMapper<?>> customBaseEntityMapperClass = entry.getKey();
                if(isBaseMapperTemplateExists(customBaseEntityMapperClass)) { //如果存在自定义BaseEntityMapper的对应freemarker模板?

                } else {
                    LOGGER.warn(">>> Found customized {}({}), but no corresponding Freemarker-Template({}) was found!", BaseEntityMapper.class.getSimpleName(), customBaseEntityMapperClass.getName(), getBaseEntityMapperTemplateLocation(customBaseEntityMapperClass));
                }
            }
        }
        return entityXmlMapperContent;
    }

    /**
     * 指定的自定义BaseEntityMapper是佛存在对应的Freemarker模板
     *
     * @param baseMapperClass
     * @return
     */
    protected boolean isBaseMapperTemplateExists(Class<BaseEntityMapper<?>> baseMapperClass) {
        Resource baseMapperFtlResource = new ClassPathResource(getBaseEntityMapperTemplateLocation(baseMapperClass));
        return baseMapperFtlResource.exists();
    }

    /**
     * 渲染指定的BaseEntityMapper对应的Freemarker模板
     *
     * @param baseMapperClass
     * @param templateParameter
     * @return
     */
    protected String processBaseMapperTemplate(Class<BaseEntityMapper<?>> baseMapperClass, EntityMapperTemplateParameter templateParameter) {
        freemarker.template.Configuration configuration = new freemarker.template.Configuration(freemarker.template.Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setClassForTemplateLoading(baseMapperClass, "/" + baseMapperClass.getPackage().getName().replace(".", "/"));
        try {
            Template baseEntityXmlMapperTemplate = configuration.getTemplate(baseMapperClass.getSimpleName() + ".ftl");
            StringWriter baseEntityXmlMapperWriter = new StringWriter();
            baseEntityXmlMapperTemplate.process(templateParameter, baseEntityXmlMapperWriter);
            return baseEntityXmlMapperWriter.toString();
        } catch (Exception e) {
            throw new MapperTemplateException(String.format("Failed to process '%s'", getBaseEntityMapperTemplateLocation(baseMapperClass)));
        }
    }

    /**
     * 基于约定的：BaseEntityMapper及其扩展所对应的Freemarker模板的位置必须是放在同一个package下
     *
     * @param baseMapperClass
     * @return
     */
    protected String getBaseEntityMapperTemplateLocation(Class<BaseEntityMapper<?>> baseMapperClass) {
        return baseMapperClass.getName().replace(".", "/") + ".ftl";
    }

    /**
     * 尝试获取指定的具体实体对象Mapper接口类型的自定义BaseEntityMapper(注意不是这个指定的具体实体对象Mapper接口中定义的方法)
     *
     * 例如：
     * public interface CustomBaseMapper<T> extends BaseEntityMapper<T extends EntityObject> {
     *     int mergeByUniqueKey(T entity);
     *     T selectByUniqueKey(@Param("id") Serializable id);
     * }
     *
     * public interface StudentMapper extends CustomBaseMapper<Student> { }
     *
     * 调用该方法：getCustomBaseEntityMappers(StudentMapper.class) ==> {key=CustomBaseMapper.class, value=[mergeByUniqueKey, selectByUniqueKey]}
     *
     * @param entityMapperClass     - 某个具体实体对象Mapper类型
     * @return
     */
    protected Map<Class<BaseEntityMapper<?>>,Set<Method>> getCustomBaseEntityMappers(Class<BaseEntityMapper<?>> entityMapperClass) {
        Set<Method> customBaseMapperMethods = new HashSet<>();
        Type[] superInterfaces = entityMapperClass.getGenericInterfaces();
        for (Type superInterface : superInterfaces) {
            ParameterizedType superType = (ParameterizedType) superInterface;
            Class<?> superClass = (Class<?>) superType.getRawType();
            ReflectionUtils.doWithMethods(superClass, customBaseMapperMethods::add, method -> {
                Class<?> declaringClass = method.getDeclaringClass();
                /*
                 * 1、排除default方法；
                 * 2、排除静态方法；
                 * 3、不包括BaseEntityMapper中方法
                 */
                return !method.isDefault() && !Modifier.isStatic(method.getModifiers()) && !BaseEntityMapper.class.equals(declaringClass) && BaseEntityMapper.class.isAssignableFrom(declaringClass);
            });
        }
        return customBaseMapperMethods.stream().collect(Collectors.groupingBy(method -> (Class<BaseEntityMapper<?>>)method.getDeclaringClass(), Collectors.toSet()));
    }

    /**
     * 创建实体元数据
     *
     * @param entityMapperClass
     * @return
     */
    protected EntityMeta createEntityMeta(Class<BaseEntityMapper<?>> entityMapperClass) {
        Class<?> entityClass = ClassUtils.getSuperGenericType(entityMapperClass, BaseEntityMapper.class, 0);
        Assert.notNull(entityClass, String.format("Can not resolve parameterized entity class from entity mapper: %s", entityMapperClass));
        return EntityMetaFactory.getEntityMeta(entityClass);
    }

    /**
     * 注册动态生成的实体XxxMapper.xml
     *
     * @param xmlMapperResource
     * @param xmlMapperLocation     - XxxMapper.xml的classpath路径，例如：com/xxx/xxx/XxxMapper.xml
     * @return
     */
    protected void registerXmlMapper(Resource xmlMapperResource, String xmlMapperLocation) {
        try {
            //考虑到开发者可以自定义XxxMapper.xml，所以必须要重命名，否则与mybatis-spring默认加载的有可能重名，导致MappedStatement加载不到Configuration中去
            String xmlMapperResourceName = String.format("Auto-Generated XML-Mapper [%s]", xmlMapperLocation);
            XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(xmlMapperResource.getInputStream(), configuration, xmlMapperResourceName, configuration.getSqlFragments());
            xmlMapperBuilder.parse();
            LOGGER.info(">>> Dynamically registered {} into {}", xmlMapperResourceName, configuration);
        } catch (IOException e) {
            throw new MapperXmlParseException("Failed to parse mapping resource: '" + xmlMapperResource + "'", e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    protected DecoratedConfiguration getConfiguration() {
        return configuration;
    }

    protected EntityMapperTemplateParameterFactory getXmlMapperTemplateParameterFactory() {
        return entityMapperTemplateParameterFactory;
    }

}
