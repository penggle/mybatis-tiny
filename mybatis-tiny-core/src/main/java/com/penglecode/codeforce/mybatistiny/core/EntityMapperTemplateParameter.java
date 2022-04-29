package com.penglecode.codeforce.mybatistiny.core;

import com.penglecode.codeforce.mybatistiny.core.EntityMeta.EntityField;
import com.penglecode.codeforce.mybatistiny.mapper.BaseEntityMapper;
import com.penglecode.codeforce.mybatistiny.support.MybatisTinyHelper;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;

import java.util.List;

/**
 * BaseEntityMapper.ftl模板参数
 *
 * @author pengpeng
 * @version 1.0
 */
public class EntityMapperTemplateParameter {

    /** 实体对象的Mybatis-Mapper接口类型 */
    private Class<BaseEntityMapper<?>> entityMapperClass;

    /** 实体元数据 */
    private EntityMeta entityMeta;

    /** 实体XML-Mapper的namespace */
    private String mapperNamespace;

    /** MapperHelper类全名 */
    private String mapperHelperClass;

    /** 当前数据库ID */
    private String databaseId;

    /** 表名 */
    private String tableName;

    /** 实体名称 */
    private String entityName;

    /** 实体类名 */
    private String entityClass;

    /** ID生成策略(SEQUENCE,IDENTITY,NONE) */
    private String idStrategy;

    /** ID生成器名称，例如当idStrategy=SEQUENCE时，该字段为指定的序列名称 */
    private String idGenerator;

    /** ID列 */
    private List<ColumnParameter> idColumns;

    /** INSERT列 */
    private List<ColumnParameter> insertColumns;

    /** UPDATE列 */
    private List<ColumnParameter> updateColumns;

    /** 全部SELECT列 */
    private List<ColumnParameter> selectColumns;

    /** 全部列 */
    private List<ColumnParameter> allColumns;

    public Class<BaseEntityMapper<?>> getEntityMapperClass() {
        return entityMapperClass;
    }

    public void setEntityMapperClass(Class<BaseEntityMapper<?>> entityMapperClass) {
        this.entityMapperClass = entityMapperClass;
    }

    public EntityMeta getEntityMeta() {
        return entityMeta;
    }

    public void setEntityMeta(EntityMeta entityMeta) {
        this.entityMeta = entityMeta;
    }

    public String getMapperNamespace() {
        return mapperNamespace;
    }

    public void setMapperNamespace(String mapperNamespace) {
        this.mapperNamespace = mapperNamespace;
    }

    public String getMapperHelperClass() {
        return mapperHelperClass;
    }

    public void setMapperHelperClass(String mapperHelperClass) {
        this.mapperHelperClass = mapperHelperClass;
    }

    public String getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(String databaseId) {
        this.databaseId = databaseId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(String entityClass) {
        this.entityClass = entityClass;
    }

    public String getIdStrategy() {
        return idStrategy;
    }

    public void setIdStrategy(String idStrategy) {
        this.idStrategy = idStrategy;
    }

    public String getIdGenerator() {
        return idGenerator;
    }

    public void setIdGenerator(String idGenerator) {
        this.idGenerator = idGenerator;
    }

    public List<ColumnParameter> getIdColumns() {
        return idColumns;
    }

    public void setIdColumns(List<ColumnParameter> idColumns) {
        this.idColumns = idColumns;
    }

    public List<ColumnParameter> getInsertColumns() {
        return insertColumns;
    }

    public void setInsertColumns(List<ColumnParameter> insertColumns) {
        this.insertColumns = insertColumns;
    }

    public List<ColumnParameter> getUpdateColumns() {
        return updateColumns;
    }

    public void setUpdateColumns(List<ColumnParameter> updateColumns) {
        this.updateColumns = updateColumns;
    }

    public List<ColumnParameter> getSelectColumns() {
        return selectColumns;
    }

    public void setSelectColumns(List<ColumnParameter> selectColumns) {
        this.selectColumns = selectColumns;
    }

    public List<ColumnParameter> getAllColumns() {
        return allColumns;
    }

    public void setAllColumns(List<ColumnParameter> allColumns) {
        this.allColumns = allColumns;
    }

    /**
     * 实体字段对应的数据库列参数
     */
    public static class ColumnParameter {

        /** 列名 */
        private final String columnName;

        /** 列对应的JDBC类型 */
        private final Integer jdbcType;

        /** 列对应的JDBC类型 */
        private final String jdbcTypeName;

        /** 列对应的Java字段名 */
        private final String fieldName;

        /** 列对应的Java字段类型 */
        private final String fieldType;

        /** 列对应的TypeHandler */
        private final String typeHandler;

        /** 列对应的select字句，例如：DATE_FORMAT(t.create_time, '%Y-%m-%d %T') */
        private final String selectClause;

        /** 是否是ID列 */
        private final boolean idColumn;

        /** 当前列对应的Java字段 */
        private final EntityField entityField;

        public ColumnParameter(EntityField entityField) {
            this.entityField = entityField;
            this.fieldName = entityField.getFieldName();
            this.fieldType = entityField.getFieldType().getName();
            this.jdbcType = entityField.getJdbcType().TYPE_CODE;
            this.jdbcTypeName = entityField.getJdbcType().name();
            this.columnName = entityField.getColumnName();
            this.idColumn = entityField.getIdAnnotation() != null;
            this.typeHandler = resolveTypeHandler(entityField).getName();
            this.selectClause = resolveSelectClause(entityField);
        }

        public String getColumnName() {
            return columnName;
        }

        public Integer getJdbcType() {
            return jdbcType;
        }

        public String getJdbcTypeName() {
            return jdbcTypeName;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getFieldType() {
            return fieldType;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        public String getSelectClause() {
            return selectClause;
        }

        public boolean isIdColumn() {
            return idColumn;
        }

        public EntityField getEntityField() {
            return entityField;
        }

        @SuppressWarnings({"rawtypes"})
        protected Class<? extends TypeHandler> resolveTypeHandler(EntityField entityField) {
            Class<? extends TypeHandler> typeHandler = entityField.getColumnAnnotation() != null ? entityField.getColumnAnnotation().typeHandler() : null;
            return typeHandler == null ? UnknownTypeHandler.class : typeHandler;
        }

        protected String resolveSelectClause(EntityField entityField) {
            String selectClause = entityField.getColumnAnnotation() != null ? entityField.getColumnAnnotation().select() : null;
            return MybatisTinyHelper.parseSelectClause(selectClause, getColumnName());
        }

    }

}
