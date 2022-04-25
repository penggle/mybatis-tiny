package com.penglecode.codeforce.mybatistiny.interceptor;

import com.penglecode.codeforce.common.domain.DomainObject;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;

import java.sql.Statement;
import java.util.List;

/**
 * 领域对象查询拦截器
 * 对查询出来的领域对象执行其process()方法
 *
 * @author pengpeng
 * @version 1.0
 */
@SuppressWarnings("unchecked")
@Intercepts({@Signature(type=ResultSetHandler.class, method="handleResultSets", args={Statement.class})})
public class DomainObjectQueryInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object resultObject = invocation.proceed();
        if(resultObject instanceof List) {
            List<Object> resultSets = (List<Object>) resultObject;
            resultSets.forEach(element -> {
                if(element instanceof DomainObject) {
                    ((DomainObject) element).processOutbound();
                }
            });
        } else if(resultObject instanceof DomainObject) {
            ((DomainObject) resultObject).processOutbound();
        }
        return resultObject;
    }

}
