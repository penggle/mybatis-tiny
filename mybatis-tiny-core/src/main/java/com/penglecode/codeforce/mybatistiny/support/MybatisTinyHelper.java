package com.penglecode.codeforce.mybatistiny.support;

import com.penglecode.codeforce.common.domain.EntityObject;
import com.penglecode.codeforce.mybatistiny.dsl.QueryCriteria;
import com.penglecode.codeforce.mybatistiny.mapper.BaseEntityMapper;
import org.apache.ibatis.mapping.BoundSql;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 通用工具集合
 *
 * @author pengpeng
 * @version 1.0
 */
public class MybatisTinyHelper {

    private static final Set<String> SELECT_CLAUSE_PATTERNS = new LinkedHashSet<>();

    static {
        SELECT_CLAUSE_PATTERNS.add("{name}");
        SELECT_CLAUSE_PATTERNS.add("{columnName}");
    }

    private MybatisTinyHelper() {}

    /**
     * 解析SELECT列字句，例如：DATE_FORMAT({name}, '%Y-%m-%d %T')
     *
     * @param selectClause  - SELECT列字句
     * @param columnName    - 列名
     * @return 返回解析后的SELECT列字句
     */
    public static String parseSelectClause(String selectClause, String columnName) {
        if(selectClause != null) {
            for(String pattern : SELECT_CLAUSE_PATTERNS) {
                if(selectClause.contains(pattern)) {
                    return selectClause.replace(pattern, columnName);
                }
            }
        }
        return columnName;
    }

    /**
     * 如果绑定参数中存在QueryCriteria，则获取之
     *
     * @param boundSql  - 当前绑定的SQL
     * @return 返回查询条件
     */
    public static Optional<QueryCriteria<? extends EntityObject>> getQueryCriteria(BoundSql boundSql) {
        String paramName = BaseEntityMapper.QUERY_CRITERIA_PARAM_NAME;
        Object parameterObject = boundSql.getParameterObject();
        QueryCriteria<? extends EntityObject> queryCriteria = null;
        if(parameterObject instanceof QueryCriteria) {
            queryCriteria = (QueryCriteria<? extends EntityObject>) parameterObject;
        }
        if(parameterObject instanceof Map && ((Map<?, ?>) parameterObject).containsKey(paramName)) {
            Object paramValue = ((Map<?, ?>) parameterObject).get(paramName);
            if(paramValue instanceof QueryCriteria) {
                queryCriteria = (QueryCriteria<? extends EntityObject>) paramValue;
            }
        }
        return Optional.ofNullable(queryCriteria);
    }

}
