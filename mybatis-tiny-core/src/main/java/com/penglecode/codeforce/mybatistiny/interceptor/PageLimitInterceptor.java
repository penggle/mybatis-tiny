package com.penglecode.codeforce.mybatistiny.interceptor;

import com.penglecode.codeforce.mybatistiny.dialect.Dialect;
import com.penglecode.codeforce.mybatistiny.dialect.DialectManager;
import com.penglecode.codeforce.mybatistiny.dsl.QueryCriteria;
import com.penglecode.codeforce.mybatistiny.support.MybatisTinyHelper;
import com.penglecode.codeforce.mybatistiny.support.RewriteSql;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
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

	private volatile Dialect dialect;

	public Object intercept(Invocation invocation) throws Throwable {
		StatementHandler statementHandler = (StatementHandler)invocation.getTarget();
		BoundSql boundSql = statementHandler.getBoundSql(); //获取绑定sql
		MetaObject statementHandlerMetaObject = SystemMetaObject.forObject(statementHandler);
		Configuration configuration = (Configuration) statementHandlerMetaObject.getValue("delegate.configuration");
		Dialect dialect = getDialect(configuration);
		//delegate指的是RoutingStatementHandler.delegate
		RowBounds rowBounds = (RowBounds) statementHandlerMetaObject.getValue("delegate.rowBounds");
		if(rowBounds == null || rowBounds == RowBounds.DEFAULT) { //如果当前不分页则需要处理QueryCriteria#limit(int)条件
			//开始处理QueryCriteria.limit(xx)逻辑
			Integer limit = MybatisTinyHelper.getQueryCriteria(boundSql).map(QueryCriteria::getLimit).orElse(null);
			if(limit != null && limit > 0) {
				RewriteSql rewriteSql = dialect.getLimitSql(boundSql.getSql(), limit);
				rewriteSql.reboundSql(configuration, boundSql); //重写SQL
			}
		} else {
			//反之，如果当前存在分页，则忽略QueryCriteria#limit(int)条件
			//开始处理分页逻辑
			RewriteSql rewriteSql = dialect.getPageSql(boundSql.getSql(), rowBounds.getOffset(), rowBounds.getLimit());
			rewriteSql.reboundSql(configuration, boundSql); //重写SQL
			//metaObject.setValue("delegate.rowBounds", RowBounds.DEFAULT); //不能重置rowBounds引用为DEFAULT(应该使用下面方式设置offset和limit)，否则会出现结果集为0的问题
			statementHandlerMetaObject.setValue("delegate.rowBounds.offset", RowBounds.NO_ROW_OFFSET);
			statementHandlerMetaObject.setValue("delegate.rowBounds.limit", RowBounds.NO_ROW_LIMIT);
		}
		return invocation.proceed();
	}

	protected Dialect getDialect(Configuration configuration) {
		if(dialect == null) {
			synchronized (this) {
				if(dialect == null) {
					dialect = initDialect(configuration);
				}
			}
		}
		return dialect;
	}

	protected Dialect initDialect(Configuration configuration) {
		String databaseId = configuration.getDatabaseId();
		return DialectManager.getDialect(databaseId);
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

}
