package com.penglecode.codeforce.mybatistiny.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MybatisTiny自动配置
 *
 * @author pengpeng
 * @version 1.0
 */
@Configuration
public class MybatisTinyConfiguration {

    @Bean
    public MybatisBeanPostProcessor mybatisBeanPostProcessor() {
        return new MybatisBeanPostProcessor();
    }

}
