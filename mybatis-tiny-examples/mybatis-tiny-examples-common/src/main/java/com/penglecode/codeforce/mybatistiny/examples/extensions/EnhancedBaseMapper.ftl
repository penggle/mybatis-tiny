<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${mapperNamespace}">

    <sql id="MergeUsingColumnsClause">
        <trim suffixOverrides=",">
            <#list selectColumns as column>
                <#noparse>#{mergeEntity.</#noparse>${column.fieldName}, <#if column.typeHandler != "org.apache.ibatis.type.UnknownTypeHandler">javaType=${column.fieldType}, typeHandler=${column.typeHandler}<#else>jdbcType=${column.jdbcTypeName}</#if><#noparse>}</#noparse>  AS  ${column.columnName},
            </#list>
        </trim>
    </sql>

    <sql id="MergeUpdateColumnsClause">
        <trim suffixOverrides=",">
            <#list updateColumns as column>
                <if test="@${mapperHelperClass}@containsColumn(updateColumns, '${column.fieldName}')">
                    t.${column.columnName} = s.${column.columnName},
                </if>
            </#list>
        </trim>
    </sql>

    <update id="merge" parameterType="java.util.Map" statementType="PREPARED">
        MERGE INTO ${tableName} t
        USING (SELECT <include refid="MergeUsingColumnsClause"/> FROM DUAL) s
           ON (<#list idColumns as column>t.${column.columnName} = s.${column.columnName}<#if column_has_next> AND </#if></#list>)
         WHEN MATCHED THEN
              UPDATE SET <include refid="MergeUpdateColumnsClause"/>
         WHEN NOT MATCHED THEN
              INSERT (<#list insertColumns as column>${column.columnName}<#if column_has_next> ,</#if></#list>) VALUES (<#list insertColumns as column>s.${column.columnName}<#if column_has_next> ,</#if></#list>)
    </update>

</mapper>