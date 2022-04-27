package com.penglecode.codeforce.mybatistiny.support;

import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * 默认的DatabaseIdProvider
 *
 * @author pengpeng
 * @version 1.0
 */
public class DefaultDatabaseIdProvider implements DatabaseIdProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDatabaseIdProvider.class);

    private Properties properties;

    public DefaultDatabaseIdProvider() {
        Properties properties = new Properties();
        properties.put("MySQL", "mysql");
        properties.put("MariaDB", "mariadb");
        properties.put("Oracle", "oracle");
        properties.put("DB2", "db2");
        properties.put("SQL Server", "sqlserver");
        properties.put("PostgreSQL", "postgresql");
        properties.put("H2", "h2");
        properties.put("HSQL", "hsql");
        properties.put("Sqlite", "sqlite");
        properties.put("ClickHouse", "clickhouse");
        setProperties(properties);
    }

    @Override
    public String getDatabaseId(DataSource dataSource) {
        if (dataSource == null) {
            throw new NullPointerException("dataSource cannot be null");
        }
        try {
            return getDatabaseName(dataSource);
        } catch (Exception e) {
            LOGGER.error("Could not get a databaseId from dataSource", e);
        }
        return null;
    }

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    protected String getDatabaseName(DataSource dataSource) throws SQLException {
        String productName = getDatabaseProductName(dataSource);
        if (this.properties != null) {
            for (Map.Entry<Object, Object> property : properties.entrySet()) {
                //忽略大小写的contains
                if (productName.toLowerCase().contains(((String) property.getKey()).toLowerCase())) {
                    return (String) property.getValue();
                }
            }
            // no match, return null
            return null;
        }
        return productName;
    }

    protected String getDatabaseProductName(DataSource dataSource) throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            DatabaseMetaData metaData = con.getMetaData();
            return metaData.getDatabaseProductName();
        }
    }

}
