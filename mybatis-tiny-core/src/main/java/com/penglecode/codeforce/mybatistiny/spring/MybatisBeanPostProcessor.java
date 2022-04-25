package com.penglecode.codeforce.mybatistiny.spring;

import com.penglecode.codeforce.common.util.ReflectionUtils;
import com.penglecode.codeforce.mybatistiny.core.DecoratedConfiguration;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Mybatis基于Spring框架的{@link BeanPostProcessor}后置处理程序，用于替换{@link SqlSessionFactory}中的{@link Configuration}为{@link DecoratedConfiguration}
 * 从而使得传统搭配(Mybatis + Spring或SpringBoot)的应用无需过多配置，只需@Import(MybatisTinyConfiguration.class)即可集成MybatisTiny
 *
 * @author pengpeng
 * @version 1.0
 */
public class MybatisBeanPostProcessor implements BeanPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MybatisBeanPostProcessor.class);

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof SqlSessionFactory) {
            if(bean instanceof DefaultSqlSessionFactory) {
                DefaultSqlSessionFactory sqlSessionFactory = (DefaultSqlSessionFactory) bean;
                decorateConfiguration(sqlSessionFactory);
            } else {
                LOGGER.warn("Can not decorate 'configuration' of SqlSessionFactory[{}], expected sqlSessionFactory is the type of {}!", bean, DefaultSqlSessionFactory.class);
            }
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

    /**
     * 装饰DefaultSqlSessionFactory中Configuration并替换之
     *
     * @param sqlSessionFactory
     */
    protected void decorateConfiguration(DefaultSqlSessionFactory sqlSessionFactory) {
        Configuration configuration = sqlSessionFactory.getConfiguration();
        if(!(configuration instanceof DecoratedConfiguration)) { //避免可能出现的重复decorate
            DecoratedConfiguration decoratedConfiguration = new DecoratedConfiguration(configuration);
            ReflectionUtils.setFinalFieldValue(sqlSessionFactory, "configuration", decoratedConfiguration);
            LOGGER.info(">>> Successfully decorate 'configuration' of DefaultSqlSessionFactory[{}]", sqlSessionFactory);
        }
    }

}
