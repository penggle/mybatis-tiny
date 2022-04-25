package com.penglecode.codeforce.mybatistiny.annotations;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 映射数据库表的列
 *
 * @author pengpeng
 * @version 1.0
 */
@Documented
@Retention(RUNTIME)
@Target({FIELD})
public @interface Column {

    /** 当前字段对应于数据库中的列名 */
    String name() default "";

    /**
     * 当前字段是否包含在INSERT列中? 默认true
     */
    boolean insertable() default true;

    /**
     * 当前字段是否包含在UPDATE列中? 默认true
     */
    boolean updatable() default true;

    /**
     * 当前字段的select字句，例如：DATE_FORMAT({name}, '%Y-%m-%d %T')
     */
    String select() default "";

    /**
     * 当前字段的JDBC类型
     */
    JdbcType jdbcType() default JdbcType.UNDEFINED;

    /**
     * 当前字段的{@link TypeHandler}类型, 默认{@link UnknownTypeHandler}
     */
    Class<? extends TypeHandler> typeHandler() default UnknownTypeHandler.class;

}
