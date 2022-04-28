package com.penglecode.codeforce.mybatistiny.dialect;

import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据库方言管理器，类似于java.sql.DriverManager
 *
 *
 * @author pengpeng
 * @version 1.0
 */
public final class DialectManager {

    /**
     * 已注册的数据库方言
     */
    private static final Map<String,Dialect> REGISTERED_DIALECTS = new HashMap<>();

    static {
        initDialects();
    }

    private DialectManager() {}

    /**
     * 初始化主流数据库方言
     */
    private static void initDialects() {
        //MySQL系列
        Dialect mysqlDialect = new MySQLDialect();
        regDialect(SupportedDatabase.MYSQL.getDatabaseId(), mysqlDialect);
        regDialect(SupportedDatabase.MARIADB.getDatabaseId(), mysqlDialect);
        //PG系列
        Dialect pgDialect = new PostgreSQLDialect();
        regDialect(SupportedDatabase.H2.getDatabaseId(), pgDialect);
        regDialect(SupportedDatabase.HSQL.getDatabaseId(), pgDialect);
        regDialect(SupportedDatabase.SQLITE.getDatabaseId(), pgDialect);
        regDialect(SupportedDatabase.POSTGRESQL.getDatabaseId(), pgDialect);
        //Oracle
        Dialect oracleDialect = new OracleDialect();
        regDialect(SupportedDatabase.ORACLE.getDatabaseId(), oracleDialect);
        //DB2
        Dialect db2Dialect = new DB2Dialect();
        regDialect(SupportedDatabase.DB2.getDatabaseId(), db2Dialect);
        //SQLServer
        Dialect sqlserverDialect = new SQLServerDialect();
        regDialect(SupportedDatabase.SQLSERVER.getDatabaseId(), sqlserverDialect);
        //ClickHouse
        Dialect clickhouseDialect = new ClickHouseDialect();
        regDialect(SupportedDatabase.CLICKHOUSE.getDatabaseId(), clickhouseDialect);
    }

    /**
     * 注册额外的数据库方言，方言扩展入口
     *
     * @param databaseId    - 数据库ID，例如mysql,oracle等，不区分大小写
     * @param dialect       - 方言实现
     */
    public synchronized static void regDialect(String databaseId, Dialect dialect) {
        REGISTERED_DIALECTS.put(databaseId.toLowerCase(), dialect);
    }

    /**
     * 根据databaseId获取数据库方言
     *
     * @param databaseId    - 数据库ID，例如mysql,oracle等，不区分大小写
     * @return 返回数据库方言
     */
    public static Dialect getDialect(String databaseId) {
        Dialect dialect = REGISTERED_DIALECTS.get(databaseId.toLowerCase());
        Assert.notNull(dialect, String.format("No suitable DatabaseDialect found for databaseId(%s)!", databaseId));
        return dialect;
    }

    /**
     * 判断指定的databaseId对应的方言是否已经注册了
     *
     * @param databaseId    - 数据库ID，例如mysql,oracle等，不区分大小写
     * @return 是否存在指定数据库的方言
     */
    public static boolean hasDialect(String databaseId) {
        return REGISTERED_DIALECTS.containsKey(databaseId.toLowerCase());
    }

}
