<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${mapperNamespace}">

    <!-- Auto-Generation Code Start -->
    <!--
        每个继承BaseEntityMapper的Mybatis-Mapper接口都会自动生成对应的如下XML-Mapper
    -->

    <resultMap id="SelectBaseResultMap" type="${entityClass}">
    <#list selectColumns as column>
        <#if column.idColumn>
        <id column="${column.fieldName}" jdbcType="${column.jdbcTypeName}" property="${column.fieldName}"/>
        <#else>
        <result column="${column.fieldName}" jdbcType="${column.jdbcTypeName}" property="${column.fieldName}" <#if column.typeHandler != "org.apache.ibatis.type.UnknownTypeHandler">javaType="${column.fieldType}" typeHandler="${column.typeHandler}"</#if>/>
        </#if>
    </#list>
    </resultMap>

    <sql id="SelectBaseColumnsClause">
        <trim suffixOverrides=",">
        <#list selectColumns as column>
            <if test="@${mapperHelperClass}@containsColumn(columns, '${column.fieldName}')">
                ${column.selectClause}   ${column.fieldName},
            </if>
        </#list>
        </trim>
    </sql>

    <sql id="UpdateDynamicColumnsClause">
        <trim suffixOverrides=",">
        <#list updateColumns as column>
            <if test="@${mapperHelperClass}@containsColumn(columns, '${column.fieldName}')">
                ${tableAlias}.${column.columnName} = <#noparse>#{</#noparse>columns.${column.fieldName}, <#if column.typeHandler != "org.apache.ibatis.type.UnknownTypeHandler">javaType=${column.fieldType}, typeHandler=${column.typeHandler}<#else>jdbcType=${column.jdbcTypeName}</#if><#noparse>}</#noparse>,
            </if>
        </#list>
        </trim>
    </sql>

    <insert id="insert" <#if idStrategy == "IDENTITY">keyProperty="${idColumns[0].fieldName}"</#if> parameterType="${entityName}" statementType="PREPARED"<#if idStrategy != "NONE"> useGeneratedKeys="true"</#if>>
    <#if idStrategy == "SEQUENCE">
        <selectKey resultType="${idColumns[0].fieldType}" order="BEFORE" keyProperty="${idColumns[0].fieldName}">
            SELECT ${idGenerator}.NEXTVAL AS ${idColumns[0].fieldName} FROM dual
        </selectKey>
    </#if>
        INSERT INTO ${tableName}(
        <#list insertColumns as column>
            ${column.columnName}<#if column_has_next>,</#if>
        </#list>
        ) VALUES (
        <#list insertColumns as column>
            <#noparse>#{</#noparse>${column.fieldName}, <#if column.typeHandler != "org.apache.ibatis.type.UnknownTypeHandler">javaType=${column.fieldType}, typeHandler=${column.typeHandler}<#else>jdbcType=${column.jdbcTypeName}</#if><#noparse>}</#noparse><#if column_has_next>,</#if>
        </#list>
        )
    </insert>

    <update id="updateById" parameterType="java.util.Map" statementType="PREPARED">
        UPDATE ${tableName} ${tableAlias}
           SET <include refid="UpdateDynamicColumnsClause"/>
         WHERE <#list idColumns as column>${tableAlias}.${column.columnName} = <#noparse>#{</#noparse><#if (idColumns?size == 1)>id<#else>id.${column.fieldName}</#if>, jdbcType=${column.jdbcTypeName}<#noparse>}</#noparse><#if column_has_next> AND </#if></#list>
    </update>

    <update id="updateByCriteria" parameterType="java.util.Map" statementType="PREPARED">
        UPDATE ${tableName} ${tableAlias}
           SET <include refid="UpdateDynamicColumnsClause"/>
        <include refid="CommonMybatisMapper.CommonWhereCriteriaClause"/>
    </update>

    <delete id="deleteById" parameterType="java.util.Map" statementType="PREPARED">
        DELETE ${deleteTargetAlias} FROM ${tableName} ${tableAlias}
         WHERE <#list idColumns as column>${tableAlias}.${column.columnName} = <#noparse>#{</#noparse><#if (idColumns?size == 1)>id<#else>id.${column.fieldName}</#if>, jdbcType=${column.jdbcTypeName}<#noparse>}</#noparse><#if column_has_next> AND </#if></#list>
    </delete>

    <delete id="deleteByIds" parameterType="java.util.Map" statementType="PREPARED">
    <#if (idColumns?size == 1)>
        DELETE ${deleteTargetAlias} FROM ${tableName} ${tableAlias}
         WHERE ${tableAlias}.${idColumns[0].columnName} in
        <foreach collection="ids" index="index" item="id" open="(" separator="," close=")">
            <#noparse>#{</#noparse>id, jdbcType=${idColumns[0].jdbcTypeName}<#noparse>}</#noparse>
        </foreach>
    <#else>
        DELETE ${deleteTargetAlias} FROM ${tableName} ${tableAlias}
         WHERE <foreach collection="ids" index="index" item="id" open="" separator=" OR " close="">(<#list idColumns as column>${tableAlias}.${column.columnName} = <#noparse>#{</#noparse>id.${column.fieldName}, jdbcType=${column.jdbcTypeName}<#noparse>}</#noparse><#if column_has_next> AND </#if></#list>)</foreach>
    </#if>
    </delete>

    <delete id="deleteByCriteria" parameterType="java.util.Map" statementType="PREPARED">
        DELETE ${deleteTargetAlias} FROM ${tableName} ${tableAlias}
        <include refid="CommonMybatisMapper.CommonWhereCriteriaClause"/>
    </delete>

    <select id="selectById" parameterType="java.util.Map" resultMap="SelectBaseResultMap" statementType="PREPARED">
        SELECT <include refid="SelectBaseColumnsClause"/>
          FROM ${tableName} ${tableAlias}
         WHERE <#list idColumns as column>${tableAlias}.${column.columnName} = <#noparse>#{</#noparse><#if (idColumns?size == 1)>id<#else>id.${column.fieldName}</#if>, jdbcType=${column.jdbcTypeName}<#noparse>}</#noparse><#if column_has_next> AND </#if></#list>
    </select>

    <select id="selectByCriteria" parameterType="java.util.Map" resultMap="SelectBaseResultMap" statementType="PREPARED">
        SELECT <include refid="SelectBaseColumnsClause"/>
          FROM ${tableName} ${tableAlias}
        <include refid="CommonMybatisMapper.CommonWhereCriteriaClause"/>
    </select>

    <select id="selectCountByCriteria" parameterType="java.util.Map" resultType="java.lang.Integer" statementType="PREPARED">
        SELECT COUNT(*)
          FROM ${tableName} ${tableAlias}
        <include refid="CommonMybatisMapper.CommonWhereCriteriaClause"/>
    </select>

    <select id="selectListByIds" parameterType="java.util.Map" resultMap="SelectBaseResultMap" statementType="PREPARED">
    <#if (idColumns?size == 1)>
        SELECT <include refid="SelectBaseColumnsClause"/>
          FROM ${tableName} ${tableAlias}
         WHERE ${tableAlias}.${idColumns[0].columnName} in
        <foreach collection="ids" index="index" item="id" open="(" separator="," close=")">
            <#noparse>#{</#noparse>id, jdbcType=${idColumns[0].jdbcTypeName}<#noparse>}</#noparse>
        </foreach>
    <#else>
        SELECT <include refid="SelectBaseColumnsClause"/>
          FROM ${tableName} ${tableAlias}
         WHERE <foreach collection="ids" index="index" item="id" open="" separator=" OR " close="">(<#list idColumns as column>${tableAlias}.${column.columnName} = <#noparse>#{</#noparse>id.${column.fieldName}, jdbcType=${column.jdbcTypeName}<#noparse>}</#noparse><#if column_has_next> AND </#if></#list>)</foreach>
    </#if>
    </select>

    <select id="selectAllList" parameterType="java.util.Map" resultMap="SelectBaseResultMap" resultSetType="FORWARD_ONLY" statementType="PREPARED">
        SELECT <include refid="SelectBaseColumnsClause"/>
          FROM ${tableName} ${tableAlias}
    </select>

    <select id="selectAllCount" parameterType="java.util.Map" resultType="java.lang.Integer" statementType="PREPARED">
        SELECT COUNT(*) FROM ${tableName} ${tableAlias}
    </select>

    <select id="selectListByCriteria" parameterType="java.util.Map" resultMap="SelectBaseResultMap" statementType="PREPARED">
        SELECT <include refid="SelectBaseColumnsClause"/>
          FROM ${tableName} ${tableAlias}
        <include refid="CommonMybatisMapper.CommonWhereCriteriaClause"/>
        <include refid="CommonMybatisMapper.CommonOrderByCriteriaClause"/>
    </select>

    <select id="selectPageListByCriteria" parameterType="java.util.Map" resultMap="SelectBaseResultMap" statementType="PREPARED">
        SELECT <include refid="SelectBaseColumnsClause"/>
          FROM ${tableName} ${tableAlias}
        <include refid="CommonMybatisMapper.CommonWhereCriteriaClause"/>
        <include refid="CommonMybatisMapper.CommonOrderByCriteriaClause"/>
    </select>

    <select id="selectPageCountByCriteria" parameterType="java.util.Map" resultType="java.lang.Integer" statementType="PREPARED">
        SELECT COUNT(*)
          FROM ${tableName} ${tableAlias}
        <include refid="CommonMybatisMapper.CommonWhereCriteriaClause"/>
    </select>
    <!-- Auto-Generation Code End -->

</mapper>