<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${mapperNamespace}">

    <sql id="MergeUpdateColumnsClause">
        <trim suffixOverrides=",">
            <#list updateColumns as column>
                <if test="@${mapperHelperClass}@containsColumn(updateColumns, '${column.fieldName}')">
                    ${column.columnName} = <#noparse>#{</#noparse>mergeEntity.${column.fieldName}, <#if column.typeHandler != "org.apache.ibatis.type.UnknownTypeHandler">javaType=${column.fieldType}, typeHandler=${column.typeHandler}<#else>jdbcType=${column.jdbcTypeName}</#if><#noparse>}</#noparse>,
                </if>
            </#list>
        </trim>
    </sql>

    <update id="merge" parameterType="java.util.Map" statementType="PREPARED" databaseId="mysql">
        INSERT INTO ${tableName}(
        <#list insertColumns as column>
            ${column.columnName}<#if column_has_next>,</#if>
        </#list>
        ) VALUES (
        <#list insertColumns as column>
            <#noparse>#{</#noparse>mergeEntity.${column.fieldName}, <#if column.typeHandler != "org.apache.ibatis.type.UnknownTypeHandler">javaType=${column.fieldType}, typeHandler=${column.typeHandler}<#else>jdbcType=${column.jdbcTypeName}</#if><#noparse>}</#noparse><#if column_has_next>,</#if>
        </#list>
        ) ON DUPLICATE KEY UPDATE <include refid="MergeUpdateColumnsClause"/>
    </update>

    <delete id="deleteById" parameterType="java.util.Map" statementType="PREPARED">
        DELETE t FROM ${tableName} t
        WHERE <#list idColumns as column>t.${column.columnName} = <#noparse>#{</#noparse><#if (idColumns?size == 1)>id<#else>id.${column.fieldName}</#if>, jdbcType=${column.jdbcTypeName}<#noparse>}</#noparse><#if column_has_next> AND </#if></#list>
    </delete>

    <update id="replace" parameterType="${entityName}" statementType="PREPARED" databaseId="mysql">
        REPLACE INTO ${tableName}(
        <#list insertColumns as column>
            ${column.columnName}<#if column_has_next>,</#if>
        </#list>
        ) VALUES (
        <#list insertColumns as column>
            <#noparse>#{</#noparse>${column.fieldName}, <#if column.typeHandler != "org.apache.ibatis.type.UnknownTypeHandler">javaType=${column.fieldType}, typeHandler=${column.typeHandler}<#else>jdbcType=${column.jdbcTypeName}</#if><#noparse>}</#noparse><#if column_has_next>,</#if>
        </#list>
        )
    </update>

</mapper>