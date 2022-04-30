package com.penglecode.codeforce.mybatistiny.support;

/**
 * BaseEntityMapper的XML-Mapper中的顶级标签元素类型枚举
 *
 * @author pengpeng
 * @version 1.0
 */
public enum XmlMapperElementType {

    RESULT_MAP("resultMap", false),
    SQL("sql", true),
    INSERT("insert", true),
    UPDATE("update", true),
    DELETE("delete", true),
    SELECT("select", true);

    /** 元素ID */
    private final String name;

    /** 是否区分databaseId */
    private final boolean divByDatabaseId;

    XmlMapperElementType(String name, boolean divByDatabaseId) {
        this.name = name;
        this.divByDatabaseId = divByDatabaseId;
    }

    public String getName() {
        return name;
    }

    public boolean isDivByDatabaseId() {
        return divByDatabaseId;
    }

    public static XmlMapperElementType typeOf(String name) {
        for(XmlMapperElementType type : values()) {
            if(type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }

    public static boolean contains(String name) {
        for(XmlMapperElementType type : values()) {
            if(type.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

}
