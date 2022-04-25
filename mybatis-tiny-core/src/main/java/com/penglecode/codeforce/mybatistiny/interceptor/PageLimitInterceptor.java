package com.penglecode.codeforce.mybatistiny.interceptor;

import com.penglecode.codeforce.mybatistiny.core.DatabaseDialect;
import com.penglecode.codeforce.mybatistiny.core.DatabaseDialectEnum;
import com.penglecode.codeforce.mybatistiny.dsl.QueryCriteria;
import com.penglecode.codeforce.mybatistiny.support.MybatisTinyHelper;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;

import java.sql.Connection;

/**
 * 处理Mybatis分页及基于QueryCriteria#limit(int)实现数据条数限制的拦截器
 *
 * @author pengpeng
 * @version 1.0
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class PageLimitInterceptor implements Interceptor {

	private volatile DatabaseDialect databaseDialect;

	public Object intercept(Invocation invocation) throws Throwable {
		StatementHandler statementHandler = (StatementHandler)invocation.getTarget();
		BoundSql boundSql = statementHandler.getBoundSql(); //获取绑定sql
		MetaObject metaObject = MetaObject.forObject(statementHandler, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(), new DefaultReflectorFactory());
		DatabaseDialect dialect = getDatabaseDialect(metaObject);
		RowBounds rowBounds = (RowBounds) metaObject.getValue("delegate.rowBounds");
		if(rowBounds == null || rowBounds == RowBounds.DEFAULT) { //如果当前不分页则需要处理QueryCriteria#limit(int)条件
			//开始处理QueryCriteria.limit(xx)逻辑
			Integer limit = MybatisTinyHelper.getQueryCriteria(boundSql).map(QueryCriteria::getLimit).orElse(null);
			if(limit != null && limit > 0) {
				String originalSql = boundSql.getSql();
				metaObject.setValue("delegate.boundSql.sql", dialect.getLimitSql(originalSql, limit));
			}
			return invocation.proceed();
		}
		//反之，如果当前存在分页，则忽略QueryCriteria#limit(int)条件
		//开始处理分页逻辑
		String originalSql = boundSql.getSql();
		metaObject.setValue("delegate.boundSql.sql", dialect.getPageSql(originalSql, rowBounds.getOffset(), rowBounds.getLimit()));
		//metaObject.setValue("delegate.rowBounds", RowBounds.DEFAULT); //不能重置rowBounds引用为DEFAULT(应该使用下面方式设置offset和limit)，否则会出现结果集为0的问题
		metaObject.setValue("delegate.rowBounds.offset", RowBounds.NO_ROW_OFFSET);
		metaObject.setValue("delegate.rowBounds.limit", RowBounds.NO_ROW_LIMIT);
		return invocation.proceed();
	}

	protected DatabaseDialect getDatabaseDialect(MetaObject metaObject) {
		if(databaseDialect == null) {
			synchronized (this) {
				if(databaseDialect == null) {
					databaseDialect = initDatabaseDialect(metaObject);
				}
			}
		}
		return databaseDialect;
	}

	protected DatabaseDialect initDatabaseDialect(MetaObject metaObject) {
		Configuration configuration = (Configuration) metaObject.getValue("delegate.configuration");
		String databaseId = configuration.getDatabaseId();
		return DatabaseDialectEnum.getDialect(databaseId);
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

}
