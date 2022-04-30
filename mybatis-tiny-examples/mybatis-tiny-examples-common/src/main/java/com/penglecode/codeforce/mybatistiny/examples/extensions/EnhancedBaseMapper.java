package com.penglecode.codeforce.mybatistiny.examples.extensions;

import com.penglecode.codeforce.common.domain.EntityObject;
import com.penglecode.codeforce.mybatistiny.dsl.QueryColumns;
import com.penglecode.codeforce.mybatistiny.mapper.BaseEntityMapper;
import org.apache.ibatis.annotations.Param;

/**
 * 增强功能的BaseEntityMapper扩展
 *
 * @author pengpeng
 * @version 1.0
 */
public interface EnhancedBaseMapper<T extends EntityObject> extends BaseEntityMapper<T> {

    /**
     * 通过标准MERGE INTO语句来进行合并存储
     *
     * @param mergeEntity       - 被更新的实体对象
     * @param updateColumns     - 可指定被更新的列
     * @return
     */
    int merge(@Param("mergeEntity") T mergeEntity, @Param("updateColumns") QueryColumns... updateColumns);

}
