package com.penglecode.codeforce.mybatistiny.dialect;

import com.penglecode.codeforce.mybatistiny.support.AdditionalParameter;
import com.penglecode.codeforce.mybatistiny.support.RewriteSql;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLServer数据库方言
 *
 * @author pengpeng
 * @version 1.0
 */
public class SQLServerDialect implements Dialect {

    @Override
    public RewriteSql getPageSql(String sql, int offset, int limit) {
        String upperSql = sql.toUpperCase();
        String finalSql = sql;
        List<AdditionalParameter> additionalParameters = new ArrayList<>();
        if(upperSql.startsWith("SELECT")) {
            finalSql = sql + " OFFSET " + SQL_PARAM_MARKER + " ROWS FETCH NEXT " + SQL_PARAM_MARKER + " ROWS ONLY";
            additionalParameters.add(new AdditionalParameter(genAdditionalParamName(1), offset, Integer.class));
            additionalParameters.add(new AdditionalParameter(genAdditionalParamName(2), limit, Integer.class));
        }
        return new RewriteSql(finalSql, additionalParameters);
    }

    @Override
    public RewriteSql getLimitSql(String sql, int limit) {
        String upperSql = sql.toUpperCase();
        StringBuilder finalSql = new StringBuilder(sql);
        List<AdditionalParameter> additionalParameters = new ArrayList<>();
        if(upperSql.startsWith("SELECT")) {
            finalSql.insert(6, " TOP (" + SQL_PARAM_MARKER + ") ");
            additionalParameters.add(new AdditionalParameter(true, genAdditionalParamName(1), limit, Integer.class));
        }
        return new RewriteSql(finalSql.toString(), additionalParameters);
    }

}
