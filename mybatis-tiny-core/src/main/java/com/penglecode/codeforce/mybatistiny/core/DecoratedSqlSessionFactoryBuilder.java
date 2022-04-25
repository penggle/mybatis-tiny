package com.penglecode.codeforce.mybatistiny.core;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

/**
 * 自定义的SqlSessionFactoryBuilder，用于装饰{@link Configuration}
 *
 * @author pengpeng
 * @version 1.0
 */
public class DecoratedSqlSessionFactoryBuilder extends SqlSessionFactoryBuilder {

    @Override
    public SqlSessionFactory build(Configuration configuration) {
        return super.build(new DecoratedConfiguration(configuration));
    }

}
