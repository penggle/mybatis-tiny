package com.penglecode.codeforce.mybatistiny.support;

/**
 * 重写SQL后新增的额外参数
 *
 * @author pengpeng
 * @version 1.0
 */
public class AdditionalParameter {

    /** 添加在BoundSql.parameterMappings的头部? */
    private final boolean addFirst;

    /** 参数名称 */
    private final String paramName;

    /** 参数类型 */
    private final Object paramValue;

    /** 参数类型 */
    private final Class<?> paramType;

    public AdditionalParameter(String paramName, Object paramValue, Class<?> paramType) {
        this(false, paramName, paramValue, paramType);
    }

    public AdditionalParameter(boolean addFirst, String paramName, Object paramValue, Class<?> paramType) {
        this.addFirst = addFirst;
        this.paramName = paramName;
        this.paramValue = paramValue;
        this.paramType = paramType;
    }

    public boolean isAddFirst() {
        return addFirst;
    }

    public String getParamName() {
        return paramName;
    }

    public Object getParamValue() {
        return paramValue;
    }

    public Class<?> getParamType() {
        return paramType;
    }

}