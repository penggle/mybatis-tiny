package com.penglecode.codeforce.mybatistiny.support;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 重写SQL语句
 *
 * @author pengpeng
 * @version 1.0
 */
public class RewriteSql {

    /**
     * 重写后的SQL
     */
    private final String sql;

    /**
     * 重写后新增的额外参数
     */
    private final List<AdditionalParameter> additionalParameters;

    public RewriteSql(String sql, List<AdditionalParameter> additionalParameters) {
        this.sql = sql;
        this.additionalParameters = Collections.unmodifiableList(additionalParameters == null ? Collections.emptyList() : additionalParameters);
    }

    public String getSql() {
        return sql;
    }

    public List<AdditionalParameter> getAdditionalParameters() {
        return additionalParameters;
    }

    /**
     * 重写原始SQL
     *
     * @param configuration     - Mybatis的全局Configuration
     * @param boundSql          - 原始SQL
     */
    @SuppressWarnings("unchecked")
    public void rewriteSql(Configuration configuration, BoundSql boundSql) {
        MetaObject boundSqlMetaObject = SystemMetaObject.forObject(boundSql);
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        Map<String, Object> additionalParameters = (Map<String, Object>) boundSqlMetaObject.getValue("additionalParameters");
        for(AdditionalParameter additionalParameter : this.additionalParameters) {
            if(additionalParameter.isAddFirst()) { //在原SQL参数前加入
                parameterMappings.add(0, new ParameterMapping.Builder(configuration, additionalParameter.getParamName(), additionalParameter.getParamType()).build());
            } else { //在原SQL参数后加入
                parameterMappings.add(new ParameterMapping.Builder(configuration, additionalParameter.getParamName(), additionalParameter.getParamType()).build());
            }
            additionalParameters.put(additionalParameter.getParamName(), additionalParameter.getParamValue());
        }
        boundSqlMetaObject.setValue("sql", getSql());
    }

    public static class AdditionalParameter {

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

}
