package com.penglecode.codeforce.mybatistiny.support;


import com.penglecode.codeforce.mybatistiny.dsl.QueryColumns;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Mybatis的XML-Mapper配置文件辅助类
 *
 * @author pengpeng
 * @version 1.0
 */
public class XmlMapperHelper {

    private XmlMapperHelper() {}

    public static boolean isEmpty(Object paramObj) {
        return ObjectUtils.isEmpty(paramObj);
    }

    public static boolean isNotEmpty(Object paramObj) {
        return !ObjectUtils.isEmpty(paramObj);
    }

    public static boolean isArrayOrCollection(Object paramObj) {
        if (paramObj == null) {
            return false;
        }
        return paramObj instanceof Collection || paramObj.getClass().isArray();
    }
    
    public static boolean containsColumn(Map<String,Object> columnNames, String columnName) {
    	if(columnNames != null) {
    		return columnNames.containsKey(columnName);
    	}
    	return false;
    }

    public static boolean containsColumn(QueryColumns[] columnArray, String columnName) {
        boolean selected = true;
        QueryColumns queryColumns = (columnArray != null && columnArray.length > 0) ? columnArray[0] : null;
        if(queryColumns != null) {
            Set<String> selectColumns = queryColumns.getColumns();
            if(!CollectionUtils.isEmpty(selectColumns)) {
                for(String selectColumn : selectColumns) {
                    if(selectColumn.equals(columnName)) {
                        return true;
                    }
                }
                selected = false;
            }
            if(queryColumns.getPredicate() != null) {
                return queryColumns.getPredicate().test(columnName);
            }
        }
        return selected;
    }

}
