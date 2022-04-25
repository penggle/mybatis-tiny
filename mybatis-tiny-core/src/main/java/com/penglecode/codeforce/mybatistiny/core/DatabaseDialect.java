package com.penglecode.codeforce.mybatistiny.core;

/**
 * 数据库方言
 *
 * @author pengpeng
 * @version 1.0
 */
public interface DatabaseDialect {

    /**
     * 根据原始查询sql语句及分页参数获取分页sql,
     * (注意：如果SQL语句中使用了left join、right join查询一对多的结果集时,请不要使用该分页处理机制,得另寻他法)
     * @param sql
     * @param offset	- 起始记录行数(从0开始)
     * @param limit		- 从起始记录行数offset开始获取limit行记录
     * @return
     */
    String getPageSql(String sql, int offset, int limit);

    /**
     * 根据原始查询sql语句及limit参数获取限制查询返回记录数的sql
     *
     * @param sql
     * @param limit
     * @return
     */
    String getLimitSql(String sql, int limit);

    /**
     * DELETE语句别名方言
     *
     * @return
     */
    default String getDeleteTargetAlias() {
        return "";
    }

}
