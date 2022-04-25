package com.penglecode.codeforce.mybatistiny.examples.domain.model;

import com.penglecode.codeforce.common.domain.DomainObject;
import com.penglecode.codeforce.common.domain.EntityObject;
import com.penglecode.codeforce.mybatistiny.annotations.Column;
import com.penglecode.codeforce.mybatistiny.annotations.GenerationType;
import com.penglecode.codeforce.mybatistiny.annotations.Id;
import com.penglecode.codeforce.mybatistiny.annotations.Table;
import com.penglecode.codeforce.mybatistiny.type.Jackson2TypeHandler;

import java.util.List;
import java.util.Map;

/**
 * 组件元数据信息
 *
 * @author pengpeng
 * @version 1.0
 */
@Table("t_component_meta")
public class ComponentMeta implements EntityObject {

    private static final long serialVersionUID = 1L;

    /** 组件代码 */
    @Id(strategy=GenerationType.NONE)
    private String componentCode;

    /** 组件名称 */
    private String componentName;

    /** 组件类型*/
    private String componentType;

    /** 组件属性 */
    //字段类型带泛型,需要对Jackson2TypeHandler进行扩展,以期在编译期就能确定泛型的类型
    @Column(typeHandler=ComponentPropsTypeHandler.class)
    private Map<String,Object> componentProps;

    /** 组件API列表 */
    //字段类型带泛型,需要对Jackson2TypeHandler进行扩展,以期在编译期就要确定泛型的类型
    @Column(typeHandler=ComponentApisTypeHandler.class)
    private List<ComponentApiMeta> componentApis;

    /** 组件API列表 */
    //字段类型不带泛型,直接用Jackson2TypeHandler就可以了
    @Column(typeHandler=Jackson2TypeHandler.class)
    private ComponentDocMeta componentDoc;

    /** 创建时间 */
    @Column(updatable=false, select="DATE_FORMAT({name}, '%Y-%m-%d %T')")
    private String createTime;

    /** 最近修改时间 */
    @Column(select="DATE_FORMAT({name}, '%Y-%m-%d %T')")
    private String updateTime;

    public String getComponentCode() {
        return componentCode;
    }

    public void setComponentCode(String componentCode) {
        this.componentCode = componentCode;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public Map<String, Object> getComponentProps() {
        return componentProps;
    }

    public void setComponentProps(Map<String, Object> componentProps) {
        this.componentProps = componentProps;
    }

    public List<ComponentApiMeta> getComponentApis() {
        return componentApis;
    }

    public void setComponentApis(List<ComponentApiMeta> componentApis) {
        this.componentApis = componentApis;
    }

    public ComponentDocMeta getComponentDoc() {
        return componentDoc;
    }

    public void setComponentDoc(ComponentDocMeta componentDoc) {
        this.componentDoc = componentDoc;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String identity() {
        return componentCode;
    }

    @Override
    public String toString() {
        return "ComponentMeta{" +
                "componentCode='" + componentCode + '\'' +
                ", componentName='" + componentName + '\'' +
                ", componentType='" + componentType + '\'' +
                ", componentProps=" + componentProps +
                ", componentApis=" + componentApis +
                ", componentDoc=" + componentDoc +
                ", createTime='" + createTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                '}';
    }

    public static class ComponentPropsTypeHandler extends Jackson2TypeHandler<Map<String,Object>> {

        public ComponentPropsTypeHandler(Class<Map<String, Object>> javaType) {
            super(javaType);
        }

    }

    public static class ComponentApisTypeHandler extends Jackson2TypeHandler<List<ComponentApiMeta>> {

        public ComponentApisTypeHandler(Class<List<ComponentApiMeta>> javaType) {
            super(javaType);
        }

    }

    public static class ComponentApiMeta implements DomainObject {

        private String apiName;

        private String apiMethod;

        private String apiUrl;

        public ComponentApiMeta() {
        }

        public ComponentApiMeta(String apiName, String apiMethod, String apiUrl) {
            this.apiName = apiName;
            this.apiMethod = apiMethod;
            this.apiUrl = apiUrl;
        }

        public String getApiName() {
            return apiName;
        }

        public void setApiName(String apiName) {
            this.apiName = apiName;
        }

        public String getApiMethod() {
            return apiMethod;
        }

        public void setApiMethod(String apiMethod) {
            this.apiMethod = apiMethod;
        }

        public String getApiUrl() {
            return apiUrl;
        }

        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }
    }

    public static class ComponentDocMeta {

        private String description;

        private String url;

        public ComponentDocMeta() {
        }

        public ComponentDocMeta(String description, String url) {
            this.description = description;
            this.url = url;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

    }

}
