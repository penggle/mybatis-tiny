package com.penglecode.codeforce.mybatistiny.dialect;

import com.penglecode.codeforce.mybatistiny.support.AdditionalParameter;
import com.penglecode.codeforce.mybatistiny.support.RewriteSql;

import java.util.ArrayList;
import java.util.List;

/**
 * DB2数据库方言
 *
 * @author pengpeng
 * @version 1.0
 */
public class DB2Dialect implements Dialect {

    private static final String DEFAULT_PAGING_SQL_FORMAT = "SELECT * FROM (SELECT page_temp_table.*,ROWNUMBER() OVER() AS ROW_ID FROM ( %s ) AS page_temp_table) page_temp_table WHERE ROW_ID BETWEEN %S AND %s";

    @Override
    public RewriteSql getPageSql(String sql, int offset, int limit) {
        String upperSql = sql.toUpperCase();
        String finalSql = sql;
        List<AdditionalParameter> additionalParameters = new ArrayList<>();
        if(upperSql.startsWith("SELECT")) {
            finalSql = String.format(DEFAULT_PAGING_SQL_FORMAT, sql, SQL_PARAM_MARKER, SQL_PARAM_MARKER);
            additionalParameters.add(new AdditionalParameter(genAdditionalParamName(1), offset + 1, Integer.class));
            additionalParameters.add(new AdditionalParameter(genAdditionalParamName(2), offset + limit, Integer.class));
        }
        return new RewriteSql(finalSql, additionalParameters);
    }

    @Override
    public RewriteSql getLimitSql(String sql, int limit) {
        String upperSql = sql.toUpperCase();
        String finalSql = sql;
        List<AdditionalParameter> additionalParameters = new ArrayList<>();
        if(upperSql.startsWith("SELECT")) {
            finalSql = sql + " FETCH FIRST " + SQL_PARAM_MARKER + " ROWS ONLY";
            additionalParameters.add(new AdditionalParameter(genAdditionalParamName(1), limit, Integer.class));
        }
        return new RewriteSql(finalSql, additionalParameters);
    }

}
