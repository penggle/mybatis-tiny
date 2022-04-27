package com.penglecode.codeforce.mybatistiny.dialect;

import com.penglecode.codeforce.mybatistiny.support.RewriteSql;
import com.penglecode.codeforce.mybatistiny.support.RewriteSql.AdditionalParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Oracle数据库的方言
 *
 * @author pengpeng
 * @version 1.0
 */
public class OracleDialect implements Dialect {

    private static final String DEFAULT_PAGING_SQL_FORMAT = "SELECT *"
            + " FROM (SELECT rownum rn_, page_inner_table.*"
            + " FROM (%s) page_inner_table"
            + " WHERE rownum <= %s) page_outer_table"
            + " WHERE page_outer_table.rn_ > %s";

    @Override
    public RewriteSql getPageSql(String sql, int offset, int limit) {
        String upperSql = sql.toUpperCase();
        String finalSql = sql;
        List<AdditionalParameter> additionalParameters = new ArrayList<>();
        if(upperSql.startsWith("SELECT")) {
            finalSql = String.format(DEFAULT_PAGING_SQL_FORMAT, sql, SQL_PARAM_MARKER, SQL_PARAM_MARKER);
            additionalParameters.add(new AdditionalParameter(genAdditionalParamName(1), offset + limit, Integer.class));
            additionalParameters.add(new AdditionalParameter(genAdditionalParamName(2), offset, Integer.class));
        }
        return new RewriteSql(finalSql, additionalParameters);
    }

    @Override
    public RewriteSql getLimitSql(String sql, int limit) {
        String upperSql = sql.toUpperCase();
        String finalSql = sql;
        List<AdditionalParameter> additionalParameters = new ArrayList<>();
        if(upperSql.startsWith("SELECT")) {
            finalSql = "SELECT * FROM (" + sql + ") WHERE rownum <= " + SQL_PARAM_MARKER;
            additionalParameters.add(new AdditionalParameter(genAdditionalParamName(1), limit, Integer.class));
        } else if(upperSql.startsWith("UPDATE") || upperSql.startsWith("DELETE")) { //TODO,此分支实现对于复杂SQL可能会存在问题
            if(upperSql.contains(" WHERE ")) {
                finalSql = sql + " AND rownum <= " + SQL_PARAM_MARKER;
            } else {
                finalSql = sql + " WHERE rownum <= " + SQL_PARAM_MARKER;
            }
            additionalParameters.add(new AdditionalParameter(genAdditionalParamName(1), limit, Integer.class));
        }
        return new RewriteSql(finalSql, additionalParameters);
    }

}
