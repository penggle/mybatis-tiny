package com.penglecode.codeforce.mybatistiny.support;

import com.penglecode.codeforce.common.util.StringUtils;

import java.util.Objects;

/**
 * BaseEntityMapper的XML-Mapper中的顶级标签元素的唯一标识
 *
 * @author pengpeng
 * @version 1.0
 */
public class XmlMapperElementKey {

    private final String id;

    private final String name;

    private final String databaseId;

    public XmlMapperElementKey(String id, String name, String databaseId) {
        this.id = id;
        this.name = name;
        this.databaseId = StringUtils.defaultIfBlank(databaseId, "");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDatabaseId() {
        return databaseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof XmlMapperElementKey)) return false;
        XmlMapperElementKey that = (XmlMapperElementKey) o;
        return id.equals(that.id) && name.equals(that.name) && databaseId.equals(that.databaseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, databaseId);
    }

}
