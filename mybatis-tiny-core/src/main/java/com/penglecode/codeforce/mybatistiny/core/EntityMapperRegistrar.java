package com.penglecode.codeforce.mybatistiny.core;

import com.penglecode.codeforce.common.domain.EntityObject;
import com.penglecode.codeforce.common.util.ClassUtils;
import com.penglecode.codeforce.mybatistiny.dsl.QueryCriteria;
import com.penglecode.codeforce.mybatistiny.exception.MapperParseException;
import com.penglecode.codeforce.mybatistiny.interceptor.DomainObjectQueryInterceptor;
import com.penglecode.codeforce.mybatistiny.interceptor.PageLimitInterceptor;
import com.penglecode.codeforce.mybatistiny.mapper.BaseEntityMapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
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
import java.nio.charset.StandardCharsets;
import java.util.List;

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
     * @param entityMapperClass
     */
    public String registerEntityMapper(Class<BaseEntityMapper<? extends EntityObject>> entityMapperClass) {
        Assert.isTrue(entityMapperClass.isInterface(), String.format("Parameter 'entityMapperClass'(%s) must be an interface!", entityMapperClass));
        //创建实体元数据
        EntityMeta<? extends EntityObject> entityMeta = createEntityMeta(entityMapperClass);
        //为动态生成的实体XxxMapper.xml创建所需的模板参数
        EntityMapperTemplateParameter templateParameter = getXmlMapperTemplateParameterFactory().createTemplateParameter(entityMapperClass, entityMeta);

        freemarker.template.Configuration configuration = new freemarker.template.Configuration(freemarker.template.Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        Class<?> resourceLoadClass = BaseEntityMapper.class;
        configuration.setClassForTemplateLoading(resourceLoadClass, "/" + resourceLoadClass.getPackage().getName().replace(".", "/"));
        String xmlMapperLocation = entityMapperClass.getName().replace(".", "/") + ".xml";
        try {
            Template xmlMapperTemplate = configuration.getTemplate("BaseEntityMapper.ftl");
            StringWriter xmlMapperWriter = new StringWriter();
            xmlMapperTemplate.process(templateParameter, xmlMapperWriter);
            String xmlMapperContent = xmlMapperWriter.toString();
            LOGGER.debug("<-----------------------------【{}】----------------------------->\n{}", xmlMapperLocation, xmlMapperContent);
            registerXmlMapper(new ByteArrayResource(xmlMapperContent.getBytes(StandardCharsets.UTF_8), xmlMapperLocation), xmlMapperLocation);
        } catch (IOException | TemplateException e) {
            throw new MapperParseException("Failed to parse 'BaseEntityMapper.ftl'", e);
        }
        return xmlMapperLocation;
    }



    /**
     * 创建实体元数据
     *
     * @param entityMapperClass
     * @return
     */
    protected EntityMeta<? extends EntityObject> createEntityMeta(Class<BaseEntityMapper<? extends EntityObject>> entityMapperClass) {
        Class<? extends EntityObject> entityClass = ClassUtils.getSuperGenericType(entityMapperClass, BaseEntityMapper.class, 0);
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
            throw new MapperParseException("Failed to parse mapping resource: '" + xmlMapperResource + "'", e);
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
