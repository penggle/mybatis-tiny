package com.penglecode.codeforce.mybatistiny.type;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.result.ResultMapException;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeHandler;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 处理JSON数据的TypeHandler
 *
 * @author pengpeng
 * @version 1.0
 */
public abstract class JsonTypeHandler<T> implements TypeHandler<T> {

    private final Type type;

    protected JsonTypeHandler() {
        this.type = getSuperclassTypeParameter(getClass());
    }

    protected Type getType() {
        return type;
    }

    Type getSuperclassTypeParameter(Class<?> clazz) {
        Type genericSuperclass = clazz.getGenericSuperclass();
        if (genericSuperclass instanceof Class) {
            //找到JsonTypeHandler截止
            if (JsonTypeHandler.class != genericSuperclass) {
                return getSuperclassTypeParameter(clazz.getSuperclass());
            }
            throw new TypeException("'" + getClass() + "' extends TypeReference but misses the type parameter. "
                    + "Remove the extension or add a type parameter to it.");
        }
        return ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) {
        if (parameter == null) {
            if (jdbcType == null) {
                throw new TypeException("JDBC requires that the JdbcType must be specified for all nullable parameters.");
            }
            try {
                ps.setNull(i, jdbcType.TYPE_CODE);
            } catch (SQLException e) {
                throw new TypeException("Error setting null for parameter #" + i + " with JdbcType " + jdbcType + " . "
                        + "Try setting a different JdbcType for this parameter or a different jdbcTypeForNull configuration property. "
                        + "Cause: " + e, e);
            }
        } else {
            try {
                setNonNullParameter(ps, i, parameter, jdbcType);
            } catch (Exception e) {
                throw new TypeException("Error setting non null for parameter #" + i + " with JdbcType " + jdbcType + " . "
                        + "Try setting a different JdbcType for this parameter or a different configuration property. "
                        + "Cause: " + e, e);
            }
        }
    }

    @Override
    public T getResult(ResultSet rs, String columnName) {
        try {
            return getNullableResult(rs, columnName);
        } catch (Exception e) {
            throw new ResultMapException("Error attempting to get column '" + columnName + "' from result set.  Cause: " + e, e);
        }
    }

    @Override
    public T getResult(ResultSet rs, int columnIndex) {
        try {
            return getNullableResult(rs, columnIndex);
        } catch (Exception e) {
            throw new ResultMapException("Error attempting to get column #" + columnIndex + " from result set.  Cause: " + e, e);
        }
    }

    @Override
    public T getResult(CallableStatement cs, int columnIndex) {
        try {
            return getNullableResult(cs, columnIndex);
        } catch (Exception e) {
            throw new ResultMapException("Error attempting to get column #" + columnIndex + " from callable statement.  Cause: " + e, e);
        }
    }

    protected void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, object2Json(parameter));
    }

    protected T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        final String json = rs.getString(columnName);
        return StringUtils.isBlank(json) ? null : json2Object(json);
    }

    protected T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        final String json = rs.getString(columnIndex);
        return StringUtils.isBlank(json) ? null : json2Object(json);
    }

    protected T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        final String json = cs.getString(columnIndex);
        return StringUtils.isBlank(json) ? null : json2Object(json);
    }

    /**
     * JSON字符串转对象
     *
     * @param json
     * @return
     */
    protected abstract T json2Object(String json);

    /**
     * 对象转JSON字符串
     *
     * @param object
     * @return
     */
    protected abstract String object2Json(T object);

}
