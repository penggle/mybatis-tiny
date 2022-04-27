package com.penglecode.codeforce.mybatistiny.dialect;

import com.penglecode.codeforce.mybatistiny.support.AdditionalParameter;
import com.penglecode.codeforce.mybatistiny.support.RewriteSql;

import java.util.ArrayList;
import java.util.List;

/**
 * ClickHouse数据库方言
 *
 * @author pengpeng
 * @version 1.0
 */
public class ClickHouseDialect implements Dialect {

    @Override
    public RewriteSql getPageSql(String sql, int offset, int limit) {
        String upperSql = sql.toUpperCase();
        String finalSql = sql;
        List<AdditionalParameter> additionalParameters = new ArrayList<>();
        if(upperSql.startsWith("SELECT")) {
            finalSql = sql + " LIMIT " + SQL_PARAM_MARKER + ", " + SQL_PARAM_MARKER;
            additionalParameters.add(new AdditionalParameter(genAdditionalParamName(1), offset, Integer.class));
            additionalParameters.add(new AdditionalParameter(genAdditionalParamName(2), limit, Integer.class));
        }
        return new RewriteSql(finalSql, additionalParameters);
    }

    @Override
    public RewriteSql getLimitSql(String sql, int limit) {
        String upperSql = sql.toUpperCase();
        String finalSql = sql;
        List<AdditionalParameter> additionalParameters = new ArrayList<>();
        if(upperSql.startsWith("SELECT")) {
            finalSql = sql + " LIMIT " + SQL_PARAM_MARKER;
            additionalParameters.add(new AdditionalParameter(genAdditionalParamName(1), limit, Integer.class));
        }
        return new RewriteSql(finalSql, additionalParameters);
    }

}
