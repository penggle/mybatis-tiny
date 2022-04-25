package com.penglecode.codeforce.mybatistiny.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 映射数据库中的ID字段
 *
 * @author pengpeng
 * @version 1.0
 */
@Documented
@Retention(RUNTIME)
@Target({FIELD})
public @interface Id {

    /**
     * 主键生成策略
     */
    GenerationType strategy() default GenerationType.NONE;

    /**
     * 1.当主键生成策略为{@link GenerationType#SEQUENCE}时，该字段值为序列名称
     */
    String generator() default "";

    /**
     * 当前被注释的字段是否包含在UPDATE列中? 默认true
     */
    boolean updatable() default false;

}
