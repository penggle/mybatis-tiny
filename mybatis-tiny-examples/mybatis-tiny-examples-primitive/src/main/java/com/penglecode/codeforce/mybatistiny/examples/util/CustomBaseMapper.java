package com.penglecode.codeforce.mybatistiny.examples.util;

import com.penglecode.codeforce.common.domain.EntityObject;
import com.penglecode.codeforce.mybatistiny.mapper.BaseEntityMapper;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.util.Map;

/**
 * @author pengpeng
 * @version 1.0
 */
public interface CustomBaseMapper<T extends EntityObject> extends BaseEntityMapper<T> {

    @Override
    int updateById(@Param("id") Serializable id, @Param("columns") Map<String,Object> columns);

    @Override
    int deleteById(@Param("id") Serializable id);

    int merge(T entity);

    default void flush() {}

    static void sayHello() {
        System.out.println("hello");
    }

}
