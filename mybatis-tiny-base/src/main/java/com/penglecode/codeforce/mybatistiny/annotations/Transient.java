package com.penglecode.codeforce.mybatistiny.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 被注解的字段，将不会参与数据库字段映射(非持久化字段)
 *
 * @author pengpeng
 * @version 1.0
 */
@Documented
@Retention(RUNTIME)
@Target({FIELD})
public @interface Transient {
}
