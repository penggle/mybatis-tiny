package com.penglecode.codeforce.mybatistiny.dialect;

/**
 * 框架内目前支持的数据库枚举
 *
 * @author pengpeng
 * @version 1.0
 */
public enum SupportedDatabase {

    MYSQL("mysql", "MySQL"),
    MARIADB("mariadb", "MariaDB"),
    ORACLE("oracle", "Oracle"),
    DB2("db2", "DB2"),
    SQLSERVER("sqlserver", "SQL Server"),
    POSTGRESQL("postgresql", "PostgreSQL"),
    H2("h2", "H2"),
    HSQL("hsql", "HSQL"),
    SQLITE("sqlite", "Sqlite"),
    CLICKHOUSE("clickhouse", "ClickHouse");

    private final String databaseId;

    private final String productName;

    SupportedDatabase(String databaseId, String productName) {
        this.databaseId = databaseId;
        this.productName = productName;
    }

    public String getDatabaseId() {
        return databaseId;
    }

    public String getProductName() {
        return productName;
    }

    public static SupportedDatabase of(String databaseId) {
        for(SupportedDatabase em : values()) {
            if(em.getDatabaseId().equals(databaseId)) {
                return em;
            }
        }
        return null;
    }

}
