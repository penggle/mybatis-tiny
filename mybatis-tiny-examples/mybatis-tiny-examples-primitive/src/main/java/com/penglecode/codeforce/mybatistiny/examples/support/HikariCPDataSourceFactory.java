package com.penglecode.codeforce.mybatistiny.examples.support;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;

/**
 * 基于HikariCP的外部连接池
 *
 * @author pengpeng
 * @version 1.0
 */
public class HikariCPDataSourceFactory extends UnpooledDataSourceFactory {

    public HikariCPDataSourceFactory() {
        this.dataSource = new HikariDataSource();
    }

}
