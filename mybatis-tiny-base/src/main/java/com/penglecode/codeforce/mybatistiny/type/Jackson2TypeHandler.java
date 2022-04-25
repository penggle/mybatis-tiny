package com.penglecode.codeforce.mybatistiny.type;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.TypeException;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;

/**
 * 基于Jackson2的泛型JsonTypeHandler实现
 *
 * @author pengpeng
 * @version 1.0
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
public class Jackson2TypeHandler<T> extends JsonTypeHandler<T> {

    private static volatile ObjectMapper objectMapper;

    private final TypeReference<T> typeReference;

    public Jackson2TypeHandler(Class<T> javaType) {
        this.typeReference = createTypeReference(javaType);
    }

    @Override
    protected T json2Object(String json) {
        if(StringUtils.isNotBlank(json)) {
            try {
                return getObjectMapper().readValue(json, typeReference);
            } catch (JsonProcessingException e) {
                throw new TypeException(String.format("Reading JSON(%s) as Type[%s] failed!", json, typeReference.getType()), e);
            }
        }
        return null;
    }

    @Override
    protected String object2Json(T object) {
        if(object != null) {
            try {
                return getObjectMapper().writeValueAsString(object);
            } catch (JsonProcessingException e) {
                throw new TypeException(String.format("Writing Type[%s] as String failed!", object), e);
            }
        }
        return null;
    }

    protected TypeReference<T> getTypeReference() {
        return typeReference;
    }

    protected TypeReference<T> createTypeReference(Class<T> javaType) {
        return new TypeReference<T>() {
            @Override
            public Type getType() {
                //如果T不是泛型类型，则javaType与genericType相等
                Type finalType = Jackson2TypeHandler.this.getType();
                if("T".equals(finalType.getTypeName()) && javaType != null) {
                    finalType = javaType;
                }
                return finalType;
            }
        };
    }

    public static ObjectMapper getObjectMapper() {
        if(objectMapper == null) {
            synchronized (Jackson2TypeHandler.class) {
                if(objectMapper == null) {
                    setObjectMapper(createDefaultObjectMapper());
                }
            }
        }
        return objectMapper;
    }

    public static void setObjectMapper(ObjectMapper objectMapper) {
        Jackson2TypeHandler.objectMapper = objectMapper;
    }

    /**
     * 创建默认配置的ObjectMapper
     * @return
     */
    protected static ObjectMapper createDefaultObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        //去掉默认的时间戳格式
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        //设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        //单引号处理,允许单引号
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

}
