package com.penglecode.codeforce.mybatistiny.core;

import com.penglecode.codeforce.mybatistiny.support.RewriteSql;

/**
 * 数据库方言
 *
 * @author pengpeng
 * @version 1.0
 */
public interface DatabaseDialect {

    /**
     * SQL参数占位符
     */
    String SQL_PARAM_MARKER = "?";

    /**
     * 生成第{index}个参数名
     *
     * @param index     - 参数index，从1开始
     * @return 参数
     */
    default String genAdditionalParamName(int index) {
        return "addMybatisTinyParam" + index;
    }

    /**
     * 根据原始查询sql语句及分页参数获取分页sql,
     * (注意：如果SQL语句中使用了left join、right join查询一对多的结果集时,请不要使用该分页处理机制,得另寻他法)
     * @param sql       - 原始SQL
     * @param offset	- 起始记录行数(从0开始)
     * @param limit		- 从起始记录行数offset开始获取limit行记录
     * @return 返回重写的SQL
     */
    RewriteSql getPageSql(String sql, int offset, int limit);

    /**
     * 根据原始查询sql语句及limit参数获取限制查询返回记录数的sql
     *
     * @param sql       - 原始SQL
     * @param limit     - limit值
     * @return 返回重写的SQL
     */
    RewriteSql getLimitSql(String sql, int limit);

    /**
     * DELETE语句别名方言
     *
     * @return
     */
    default String getDeleteTargetAlias() {
        return "";
    }

}
