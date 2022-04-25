package com.penglecode.codeforce.mybatistiny.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 映射数据库的表
 *
 * @author pengpeng
 * @version 1.0
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE})
public @interface Table {

    /**
     * The name of the table.
     */
    String value();

}
