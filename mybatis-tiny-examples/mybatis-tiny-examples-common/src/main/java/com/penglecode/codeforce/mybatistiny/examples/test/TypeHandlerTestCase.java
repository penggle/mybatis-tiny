package com.penglecode.codeforce.mybatistiny.examples.test;

import com.penglecode.codeforce.common.support.MapLambdaBuilder;
import com.penglecode.codeforce.common.util.DateTimeUtils;
import com.penglecode.codeforce.mybatistiny.dsl.LambdaQueryCriteria;
import com.penglecode.codeforce.mybatistiny.dsl.QueryCriteria;
import com.penglecode.codeforce.mybatistiny.examples.dal.mapper.ComponentMetaMapper;
import com.penglecode.codeforce.mybatistiny.examples.domain.model.ComponentMeta;
import com.penglecode.codeforce.mybatistiny.examples.domain.model.ComponentMeta.ComponentApiMeta;
import com.penglecode.codeforce.mybatistiny.examples.domain.model.ComponentMeta.ComponentDocMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Jackson2TypeHandler测试用例
 *
 * @author pengpeng
 * @version 1.0
 */
public abstract class TypeHandlerTestCase {

    /**
     * 在insert操作中测试Jackson2TypeHandler
     */
    protected void createComponentMeta() {
        ComponentMeta componentMeta = new ComponentMeta();
        componentMeta.setComponentCode("StaffSelector");
        componentMeta.setComponentName("人员选择器");
        componentMeta.setComponentType("业务组件");

        Map<String,Object> properties = new HashMap<>();
        properties.put("rootOrgId", 1024);
        properties.put("pageSize", 50);
        componentMeta.setComponentProps(properties);

        List<ComponentApiMeta> apiList = new ArrayList<>();
        apiList.add(new ComponentApiMeta("queryOrgTree", "GET", "/api/org/tree"));
        apiList.add(new ComponentApiMeta("queryStaffList", "GET", "/api/staff/list"));
        componentMeta.setComponentApis(apiList);

        componentMeta.setComponentDoc(new ComponentDocMeta("MybatisTiny", "https://github.com/penggle/mybatis-tiny"));
        componentMeta.setCreateTime(DateTimeUtils.formatNow());
        componentMeta.setUpdateTime(componentMeta.getCreateTime());

        getComponentMetaMapper().insert(componentMeta);
    }

    /**
     * 在update操作中测试Jackson2TypeHandler
     */
    protected void updateComponentMeta() {
        ComponentMeta componentMeta = new ComponentMeta();
        componentMeta.setComponentCode("StaffSelector");
        componentMeta.setComponentName("人员选择器(Enhanced)");
        componentMeta.setComponentType("业务组件");

        Map<String,Object> properties = new HashMap<>();
        properties.put("rootOrgId", 1024);
        properties.put("staffRoleId", "ADMIN");
        properties.put("pageSize", 50);
        componentMeta.setComponentProps(properties);

        List<ComponentApiMeta> apiList = new ArrayList<>();
        apiList.add(new ComponentApiMeta("queryRoleList", "GET", "/api/role/list"));
        apiList.add(new ComponentApiMeta("queryOrgTree", "GET", "/api/org/tree"));
        apiList.add(new ComponentApiMeta("queryStaffList", "GET", "/api/staff/list"));
        componentMeta.setComponentApis(apiList);

        componentMeta.setComponentDoc(new ComponentDocMeta("OpenAPI规范参考文档", "https://github.com/OAI/OpenAPI-Specification/blob/3.0.1/versions/3.0.1.md"));
        componentMeta.setCreateTime(DateTimeUtils.formatNow());
        componentMeta.setUpdateTime(componentMeta.getCreateTime());

        Map<String,Object> updateColumns = MapLambdaBuilder.of(componentMeta)
                .with(ComponentMeta::getComponentName)
                .with(ComponentMeta::getComponentType)
                .with(ComponentMeta::getComponentProps)
                .with(ComponentMeta::getComponentApis)
                .with(ComponentMeta::getComponentDoc)
                .build();
        getComponentMetaMapper().updateById(componentMeta.identity(), updateColumns);
    }

    /**
     * 在select操作中测试Jackson2TypeHandler
     */
    protected void selectComponentMeta() {
        ComponentMeta componentMeta = getComponentMetaMapper().selectById("StaffSelector");
        System.out.println(componentMeta);

        QueryCriteria<ComponentMeta> queryCriteria = LambdaQueryCriteria.ofSupplier(ComponentMeta::new)
                .eq(ComponentMeta::getComponentCode, "StaffSelector");
        List<ComponentMeta> componentList = getComponentMetaMapper().selectListByCriteria(queryCriteria);
        if(componentList != null) {
            componentList.forEach(System.out::println);
        }
    }

    public abstract ComponentMetaMapper getComponentMetaMapper();

}
