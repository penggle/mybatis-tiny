package com.penglecode.codeforce.mybatistiny.core;

import com.penglecode.codeforce.common.domain.EntityObject;
import com.penglecode.codeforce.common.util.ClassUtils;
import com.penglecode.codeforce.common.util.CollectionUtils;
import com.penglecode.codeforce.mybatistiny.dsl.QueryCriteria;
import com.penglecode.codeforce.mybatistiny.exception.MapperTemplateException;
import com.penglecode.codeforce.mybatistiny.exception.MapperXmlParseException;
import com.penglecode.codeforce.mybatistiny.interceptor.DomainObjectQueryInterceptor;
import com.penglecode.codeforce.mybatistiny.interceptor.PageLimitInterceptor;
import com.penglecode.codeforce.mybatistiny.mapper.BaseEntityMapper;
import com.penglecode.codeforce.mybatistiny.support.XmlMapperElementKey;
import com.penglecode.codeforce.mybatistiny.support.XmlMapperElementNode;
import com.penglecode.codeforce.mybatistiny.support.XmlMapperElementType;
import com.penglecode.codeforce.mybatistiny.support.XmlMapperHelper;
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
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    /**
     * 创建Freemarker模板参数
     *
     * @param configuration
     * @return
     */
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
        //获取实体元数据
        EntityMeta entityMeta = getEntityMeta(entityMapperClass);
        //为动态生成的实体XxxMapper.xml创建所需的模板参数
        EntityMapperTemplateParameter templateParameter = getXmlMapperTemplateParameterFactory().createTemplateParameter(entityMapperClass, entityMeta);
        String entityXmlMapperLocation = entityMapperClass.getName().replace(".", "/") + ".xml"; //虚拟的位置
        String entityXmlMapperContent = determineEntityXmlMapper(entityMapperClass, entityXmlMapperLocation, templateParameter);
        LOGGER.debug("<-----------------------------【{}】----------------------------->\n{}", entityXmlMapperLocation, entityXmlMapperContent);
        registerXmlMapper(new ByteArrayResource(entityXmlMapperContent.getBytes(StandardCharsets.UTF_8), entityXmlMapperLocation), entityXmlMapperLocation);
        return entityXmlMapperLocation;
    }

    /**
     * 最终确定指定实体对象的XML-Mapper内容
     *
     * @param entityMapperClass         - 实体对象Mapper接口的类型
     * @param entityXmlMapperLocation   - 实体对象Mapper.xml的位置(虚拟的位置)
     * @param templateParameter         - Freemarker模板渲染所需参数
     * @return
     */
    protected String determineEntityXmlMapper(Class<BaseEntityMapper<?>> entityMapperClass, String entityXmlMapperLocation, EntityMapperTemplateParameter templateParameter) {
        //首先加载BaseEntityMapper.ftl对应的XML-Mapper内容
        Class<BaseEntityMapper<?>> baseEntityMapperClass = (Class<BaseEntityMapper<?>>) ClassUtils.resolveClassName(BaseEntityMapper.class.getName(), ClassUtils.getDefaultClassLoader());
        String entityXmlMapperContent = processBaseMapperTemplate(baseEntityMapperClass, templateParameter);
        //处理继承BaseEntityMapper扩展公共方法的情况开始
        List<Class<BaseEntityMapper<?>>> customBaseEntityMapperClasses = getCustomBaseEntityMapperClasses(entityMapperClass);
        if(!CollectionUtils.isEmpty(customBaseEntityMapperClasses)) { //是否存在这种扩展?
            //如果存在则需要尝试将对应的自定义Mapper方法的xml合并到BaseEntityMapper.xml中去
            for(Class<BaseEntityMapper<?>> customBaseEntityMapperClass : customBaseEntityMapperClasses) {
                Set<Method> customBaseEntityMapperMethods = getCustomBaseEntityMapperMethods(customBaseEntityMapperClass);
                if(!CollectionUtils.isEmpty(customBaseEntityMapperMethods)) { //自定义BaseEntityMapper接口中声明了接口方法?(不包括静态方法和default方法)
                    if(isBaseMapperTemplateExists(customBaseEntityMapperClass)) { //如果存在自定义BaseEntityMapper的对应freemarker模板?
                        String customXmlMapperContent = processBaseMapperTemplate(customBaseEntityMapperClass, templateParameter);
                        entityXmlMapperContent = mergeEntityXmlMapper(customBaseEntityMapperClass, customXmlMapperContent, entityXmlMapperContent);
                    } else {
                        LOGGER.warn(">>> Found customized {}({}), but no corresponding Freemarker-Template({}) was found!", BaseEntityMapper.class.getSimpleName(), customBaseEntityMapperClass.getName(), getBaseEntityMapperTemplateLocation(customBaseEntityMapperClass));
                    }
                }
            }
        }
        return entityXmlMapperContent;
    }

    /**
     * 将customXmlMapperContent中的内容合并到entityXmlMapperContent中去
     * 合并原则：只有标签元素的id、name、databaseId一样时才会覆盖，否则添加
     *
     * @param customBaseEntityMapperClass   - 自定义的BaseEntityMapper类型
     * @param customXmlMapperContent        - 自定义BaseEntityMapper接口对应XML-Mapper内容
     * @param entityXmlMapperContent        - 实体对象的完整XML-Mapper内容
     * @return
     */
    protected String mergeEntityXmlMapper(Class<BaseEntityMapper<?>> customBaseEntityMapperClass, String customXmlMapperContent, String entityXmlMapperContent) {
        LOGGER.info(">>> Found customized {}({}), perform XML-Mapper content merge immediately!", BaseEntityMapper.class.getSimpleName(), customBaseEntityMapperClass.getName());
        Document entityXmlMapperDocument = XmlMapperHelper.readAsDocument(entityXmlMapperContent);
        Document customXmlMapperDocument = XmlMapperHelper.readAsDocument(customXmlMapperContent);

        Map<XmlMapperElementKey,XmlMapperElementNode> entityXmlMapperElements = XmlMapperHelper.getAllXmlMapperElements(entityXmlMapperDocument);
        Map<XmlMapperElementKey,XmlMapperElementNode> customXmlMapperElements = XmlMapperHelper.getAllXmlMapperElements(customXmlMapperDocument);

        entityXmlMapperElements.putAll(customXmlMapperElements); //执行合并
        //按XmlMapperElementType中定义的元素顺序排序(保证合并后的XML-Mapper文档的标签顺序)
        List<Node> finalXmlMapperElements = entityXmlMapperElements.values()
                .stream()
                .sorted(Comparator.comparing(this::indexOfXmlMapperElement, Integer::compare))
                .map(XmlMapperElementNode::getNode)
                .collect(Collectors.toList());

        XmlMapperHelper.clearXmlMapperElements(entityXmlMapperDocument); //先清空原来的
        XmlMapperHelper.appendXmlMapperElements(entityXmlMapperDocument, finalXmlMapperElements); //再重新填充已排序的最终版
        return XmlMapperHelper.writeAsString(entityXmlMapperDocument);
    }

    /**
     * 获取排序源
     *
     * @param element
     * @return
     */
    private int indexOfXmlMapperElement(XmlMapperElementNode element) {
        XmlMapperElementType elementType = XmlMapperElementType.typeOf(element.getKey().getName());
        return elementType == null ? Integer.MAX_VALUE : elementType.ordinal();
    }

    /**
     * 指定的自定义BaseEntityMapper是否存在对应的Freemarker模板
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
     * 尝试获取某实体对象XxxMapper的自定义BaseEntityMapper，也就是获取XxxMapper的父接口(不包括BaseEntityMapper本身)
     *
     * 例如：
     * public interface CustomBaseMapper<T> extends BaseEntityMapper<T extends EntityObject> {
     *     int mergeByUniqueKey(T entity);
     *     T selectByUniqueKey(@Param("id") Serializable id);
     * }
     *
     * public interface StudentMapper extends CustomBaseMapper<Student> { }
     *
     * 调用该方法：getCustomBaseEntityMappers(StudentMapper.class) ==> [CustomBaseMapper.class]
     *
     * @param entityMapperClass     - 某个具体实体对象Mapper类型
     * @return 返回已经排过序的列表(远亲父接口靠前,近亲父接口靠后)
     */
    protected List<Class<BaseEntityMapper<?>>> getCustomBaseEntityMapperClasses(Class<BaseEntityMapper<?>> entityMapperClass) {
        Set<Class<BaseEntityMapper<?>>> customBaseMapperClasses = new LinkedHashSet<>();
        collectCustomBaseEntityMappers(entityMapperClass, customBaseMapperClasses::add);
        List<Class<BaseEntityMapper<?>>> resultList = new ArrayList<>(customBaseMapperClasses);
        Collections.reverse(resultList);
        return resultList;
    }

    /**
     * 递归遍历指定entityMapperClass的祖先
     *
     * @param baseEntityMapperClass
     * @param consumer
     */
    private void collectCustomBaseEntityMappers(Class<BaseEntityMapper<?>> baseEntityMapperClass, Consumer<Class<BaseEntityMapper<?>>> consumer) {
        if(!BaseEntityMapper.class.equals(baseEntityMapperClass)) {
            Class<BaseEntityMapper<?>>[] superInterfaces = (Class<BaseEntityMapper<?>>[]) baseEntityMapperClass.getInterfaces();
            for(Class<BaseEntityMapper<?>> superInterface : superInterfaces) {
                if(!BaseEntityMapper.class.equals(superInterface)) {
                    Class<?> superInterfaceGeneric0 = ClassUtils.getClassGenericType(superInterface, 0);
                    if(superInterfaceGeneric0 != null && EntityObject.class.isAssignableFrom(superInterfaceGeneric0)) { //校验泛型
                        consumer.accept(superInterface);
                        collectCustomBaseEntityMappers(superInterface, consumer);
                    }
                }
            }
        }
    }

    /**
     * 获取指定自定义BaseEntityMapper的声明方法(不包括父接口中的，不包括静态方法，不包括default方法)
     *
     * @param customEntityMapperClass
     * @return
     */
    protected Set<Method> getCustomBaseEntityMapperMethods(Class<BaseEntityMapper<?>> customEntityMapperClass) {
        return Stream.of(customEntityMapperClass.getDeclaredMethods()).filter(method -> !method.isDefault() && !Modifier.isStatic(method.getModifiers())).collect(Collectors.toSet());
    }

    /**
     * 获取实体元数据
     *
     * @param entityMapperClass
     * @return
     */
    protected EntityMeta getEntityMeta(Class<BaseEntityMapper<?>> entityMapperClass) {
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
