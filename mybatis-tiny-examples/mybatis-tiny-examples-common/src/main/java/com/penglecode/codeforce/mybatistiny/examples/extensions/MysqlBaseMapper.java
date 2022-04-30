package com.penglecode.codeforce.mybatistiny.examples.extensions;

import com.penglecode.codeforce.common.domain.EntityObject;
import com.penglecode.codeforce.mybatistiny.dsl.QueryColumns;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;

/**
 * 基于MySQL数据库的EnhancedBaseMapper扩展
 *
 * @author pengpeng
 * @version 1.0
 */
public interface MysqlBaseMapper<T extends EntityObject> extends EnhancedBaseMapper<T> {

    /**
     * 注意这个覆盖实现的XML-Mapper方法上带了databaseId="mysql"
     * 见MysqlBaseMapper.ftl
     */
    @Override
    int merge(@Param("mergeEntity") T mergeEntity, @Param("updateColumns") QueryColumns... updateColumns);

    /**
     * 重写delete语句，使用MySQL另类的DELETE语句表别名~~
     * @param id		- 主键ID
     * @return
     */
    @Override
    int deleteById(@Param("id") Serializable id);

    /**
     * MYSQL的replace into语句来更新实体对象
     *
     * @param entity
     * @return
     */
    int replace(T entity);

}
