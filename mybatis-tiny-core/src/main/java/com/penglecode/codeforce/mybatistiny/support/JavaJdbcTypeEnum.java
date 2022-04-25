package com.penglecode.codeforce.mybatistiny.support;

import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.time.chrono.JapaneseDate;

/**
 * 常用Java类型的默认JDBC类型的映射枚举
 *
 * @author pengpeng
 * @version 1.0
 */
public enum JavaJdbcTypeEnum {

    BOOLEAN(JdbcType.BOOLEAN, boolean.class, Boolean.class),
    BYTE(JdbcType.TINYINT, byte.class, Byte.class),
    SHORT(JdbcType.SMALLINT, short.class, Short.class),
    INTEGER(JdbcType.INTEGER, int.class, Integer.class),
    LONG(JdbcType.BIGINT, long.class, Long.class),
    FLOAT(JdbcType.FLOAT, float.class, Float.class),
    DOUBLE(JdbcType.DOUBLE, double.class, Double.class),

    CHARACTER(JdbcType.CHAR, char.class, Character.class),
    STRING(JdbcType.VARCHAR, String.class),

    BIGINTEGER(JdbcType.BIGINT, BigInteger.class),
    BIGDECIMAL(JdbcType.DECIMAL, BigDecimal.class),

    BYTES(JdbcType.BLOB, byte[].class, Byte[].class),

    DATE_UTIL(JdbcType.TIMESTAMP, java.util.Date.class),
    DATE_SQL(JdbcType.DATE, java.sql.Date.class),
    TIME(JdbcType.TIME, java.sql.Time.class),
    TIMESTAMP(JdbcType.TIMESTAMP, java.sql.Timestamp.class),
    INSTANT(JdbcType.TIMESTAMP, Instant.class),
    LOCAL_DATETIME(JdbcType.TIMESTAMP, LocalDateTime.class),
    LOCAL_DATE(JdbcType.DATE, LocalDate.class),
    LOCAL_TIME(JdbcType.TIME, LocalTime.class),
    OFFSET_DATETIME(JdbcType.TIMESTAMP, OffsetDateTime.class),
    OFFSET_TIME(JdbcType.TIME, OffsetTime.class),
    ZONED_DATETIME(JdbcType.TIMESTAMP, ZonedDateTime.class),
    MONTH(JdbcType.INTEGER, Month.class),
    YEARMONTH(JdbcType.VARCHAR, YearMonth.class),
    JAPANESE_DATE(JdbcType.DATE, JapaneseDate.class),

    OBJECT(JdbcType.OTHER, Object.class);

    private final JdbcType jdbcType;

    private final Class<?>[] javaTypes;

    JavaJdbcTypeEnum(JdbcType jdbcType, Class<?>... javaTypes) {
        this.javaTypes = javaTypes;
        this.jdbcType = jdbcType;
    }

    public JdbcType getJdbcType() {
        return jdbcType;
    }

    public Class<?>[] getJavaTypes() {
        return javaTypes;
    }

    public static JdbcType getJdbcType(Class<?> javaType) {
        for(JavaJdbcTypeEnum em : values()) {
            for(Class<?> type : em.getJavaTypes()) {
                if(type.equals(javaType)) {
                    return em.getJdbcType();
                }
            }
        }
        return JdbcType.UNDEFINED;
    }

}
