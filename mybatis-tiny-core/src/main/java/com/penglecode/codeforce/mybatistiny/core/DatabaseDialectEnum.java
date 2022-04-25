package com.penglecode.codeforce.mybatistiny.core;

import com.penglecode.codeforce.mybatistiny.dsl.QueryCriteria;

import java.util.*;

/**
 * 数据库方言
 *
 * @author pengpeng
 * @version 1.0
 */
public enum DatabaseDialectEnum implements DatabaseDialect {

    /**
     * 基于Oracle数据库的方言
     */
    ORACLE() {

        private static final String DEFAULT_PAGING_SQL_FORMAT = "SELECT *"
                                                               + " FROM (SELECT rownum rn_, page_inner_table.*"
                                                                       + " FROM (%s) page_inner_table"
                                                                      + " WHERE rownum <= %s) page_outer_table"
                                                              + " WHERE page_outer_table.rn_ > %s";

        @Override
        public String getPageSql(String sql, int offset, int limit) {
            String upperSql = sql.toUpperCase();
            if(upperSql.startsWith("SELECT")) {
                return String.format(DEFAULT_PAGING_SQL_FORMAT, sql, offset + limit, offset);
            }
            return sql;
        }

        @Override
        public String getLimitSql(String sql, int limit) {
            String upperSql = sql.toUpperCase();
            if(upperSql.startsWith("SELECT")) {
                return "SELECT * FROM (" + sql + ") WHERE rownum <= " + limit;
            } else if(upperSql.startsWith("UPDATE") || upperSql.startsWith("DELETE")) { //TODO,此分支实现对于复杂SQL可能会存在问题
                if(upperSql.contains(" WHERE ")) {
                    return sql + " AND rownum <= " + limit;
                } else {
                    return sql + " WHERE rownum <= " + limit;
                }
            }
            return sql;
        }

    },

    /**
     * 基于MySQL数据库的方言
     */
    MYSQL() {
        @Override
        public String getPageSql(String sql, int offset, int limit) {
            String upperSql = sql.toUpperCase();
            if(upperSql.startsWith("SELECT")) {
                return sql + " LIMIT " + offset + ", " + limit;
            }
            return sql;
        }

        @Override
        public String getLimitSql(String sql, int limit) {
            String upperSql = sql.toUpperCase();
            if(upperSql.startsWith("SELECT") || upperSql.startsWith("UPDATE") || upperSql.startsWith("DELETE")) {
                return sql + " LIMIT " + limit;
            }
            return sql;
        }

        @Override
        public String getDeleteTargetAlias() {
            return QueryCriteria.TABLE_ALIAS_NAME;
        }
    };

    /**
     * 额外的数据库方言
     */
    private static final Map<String,DatabaseDialect> ADDITIONAL_DIALECTS = new HashMap<>();

    /**
     * 注册额外的数据库方言，方言扩展入口
     *
     * @param databaseId        - 数据库ID，例如mysql,oracle等，不区分大小写
     * @param dialect
     */
    public static void registerDialect(String databaseId, DatabaseDialect dialect) {
        ADDITIONAL_DIALECTS.put(databaseId.toUpperCase(), dialect);
    }

    /**
     * 根据databaseId获取数据库方言
     *
     * @param databaseId    - 数据库ID，例如mysql,oracle等，不区分大小写
     * @return
     */
    public static DatabaseDialectEnum getDialect(String databaseId) {
        DatabaseDialect dialect = ADDITIONAL_DIALECTS.get(databaseId.toUpperCase());
        if(dialect == null) {
            for(DatabaseDialectEnum dbDialect : values()) {
                if(dbDialect.name().equalsIgnoreCase(databaseId)) {
                    return dbDialect;
                }
            }
        }
        throw new IllegalArgumentException(String.format("No suitable DatabaseDialect found for databaseId(%s)!", databaseId));
    }

    /**
     * 判断指定的databaseId对应的方言是否已经注册了
     *
     * @param databaseId    - 数据库ID，例如mysql,oracle等，不区分大小写
     * @return
     */
    public static boolean hasRegisteredDialect(String databaseId) {
        for(DatabaseDialectEnum dbDialect : values()) {
            if(dbDialect.name().equalsIgnoreCase(databaseId)) {
                return true;
            }
        }
        return ADDITIONAL_DIALECTS.containsKey(databaseId.toUpperCase());
    }

}
