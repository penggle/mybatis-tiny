package com.penglecode.codeforce.mybatistiny.examples.test;

import com.penglecode.codeforce.mybatistiny.examples.config.MybatisConfiguration;
import com.penglecode.codeforce.mybatistiny.examples.dal.mapper.ComponentMetaMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Jackson2TypeHandler测试
 *
 * @author pengpeng
 * @version 1.0
 */
@SpringJUnitConfig(MybatisConfiguration.class)
public class TypeHandlerTestBySpring extends TypeHandlerTestCase {

    @Autowired
    private ComponentMetaMapper componentMetaMapper;

    /**
     * 在insert操作中测试Jackson2TypeHandler
     */
    @Test
    public void createComponentMetaTest() {
        createComponentMeta();
    }

    /**
     * 在update操作中测试Jackson2TypeHandler
     */
    @Test
    public void updateComponentMetaTest() {
        updateComponentMeta();
    }

    /**
     * 在select操作中测试Jackson2TypeHandler
     */
    @Test
    public void selectComponentMetaTest() {
        selectComponentMeta();
    }

    @Override
    public ComponentMetaMapper getComponentMetaMapper() {
        return componentMetaMapper;
    }

}
