package com.penglecode.codeforce.mybatistiny.support;

import org.apache.ibatis.mapping.VendorDatabaseIdProvider;

import java.util.Properties;

/**
 * 默认的DatabaseIdProvider
 *
 * @author pengpeng
 * @version 1.0
 */
public class DefaultDatabaseIdProvider extends VendorDatabaseIdProvider {

    public DefaultDatabaseIdProvider() {
        Properties properties = new Properties();
        properties.put("MySQL", "mysql");
        properties.put("Oracle", "oracle");
        properties.put("DB2", "db2");
        properties.put("SQL Server", "sqlserver");
        properties.put("PostgreSQL", "postgresql");
        properties.put("H2", "h2");
        setProperties(properties);
    }

}
