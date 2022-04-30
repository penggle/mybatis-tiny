package com.penglecode.codeforce.mybatistiny.support;

/**
 * BaseEntityMapper的XML-Mapper中的顶级标签元素枚举
 *
 * @author pengpeng
 * @version 1.0
 */
public enum XmlMapperElementEnum {

    SELECT_BASE_RESULT_MAP("SelectBaseResultMap", "resultMap"),
    SELECT_BASE_COLUMNS_CLAUSE("SelectBaseColumnsClause", "sql"),
    UPDATE_DYNAMIC_COLUMNS_CLAUSE("UpdateDynamicColumnsClause", "sql"),
    INSERT("insert", "insert"),
    UPDATE_BY_ID("updateById", "update"),
    UPDATE_BY_CRITERIA("updateByCriteria", "update"),
    DELETE_BY_ID("deleteById", "delete"),
    DELETE_BY_IDS("deleteByIds", "delete"),
    DELETE_BY_CRITERIA("deleteByCriteria", "delete"),
    SELECT_BY_ID("selectById", "select"),
    SELECT_BY_CRITERIA("selectByCriteria", "select"),
    SELECT_COUNT_BY_CRITERIA("selectCountByCriteria", "select"),
    SELECT_LIST_BY_IDS("selectListByIds", "select"),
    SELECT_ALL_LIST("selectAllList", "select"),
    SELECT_ALL_COUNT("selectAllCount", "select"),
    SELECT_LIST_BY_CRITERIA("selectListByCriteria", "select"),
    SELECT_PAGE_LIST_BY_CRITERIA("selectPageListByCriteria", "select"),
    SELECT_PAGE_COUNT_BY_CRITERIA("selectPageCountByCriteria", "select"),;

    /** 元素ID */
    private final String id;

    /** 元素名称 */
    private final String name;

    XmlMapperElementEnum(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static XmlMapperElementEnum of(String id) {
        for(XmlMapperElementEnum element : values()) {
            if(element.getId().equals(id)) {
                return element;
            }
        }
        return null;
    }

}
