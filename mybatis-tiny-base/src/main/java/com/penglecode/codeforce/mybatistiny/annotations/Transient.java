package com.penglecode.codeforce.mybatistiny.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 表示实体类中不需要持久化的字段
 *
 * @author pengpeng
 * @version 1.0
 */
@Documented
@Retention(RUNTIME)
@Target({FIELD})
public @interface Transient {
}
