package com.penglecode.codeforce.mybatistiny;

import com.penglecode.codeforce.mybatistiny.spring.MybatisTinyConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用MybatisTiny
 *
 * @author pengpeng
 * @version 1.0
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(MybatisTinyConfiguration.class)
public @interface EnableMybatisTiny {

}
