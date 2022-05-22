package com.penglecode.codeforce.mybatistiny.support;

import com.penglecode.codeforce.common.domain.EntityObject;
import com.penglecode.codeforce.common.model.Page;
import com.penglecode.codeforce.mybatistiny.dsl.QueryCriteria;
import com.penglecode.codeforce.mybatistiny.executor.JdbcBatchOperation;
import com.penglecode.codeforce.mybatistiny.mapper.BaseEntityMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.RowBounds;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * {@link BaseEntityMapper}的辅助类
 *
 * @author pengpeng
 * @version 1.0
 */
public class EntityMapperHelper {

    /**
     * 默认的JDBC-Batch操作批量大小
     */
    public static final Integer DEFAULT_JDBC_BATCH_SIZE = 5000;

    /**
     * 默认的in SQL语句限制大小
     */
    public static final int DEFAULT_IN_SQL_LIMITS = 500;

    private EntityMapperHelper() {}

    /**
     * 执行Mybatis分页查询
     * (总记录数totalRowCount将会回设进入Page对象中)
     *
     * @param entityMybatisMapper   - 实体对象的Mybatis-Mapper接口
     * @param criteria              - 查询条件
     * @param page                  - 分页参数
     * @param <T>
     * @return 返回分页结果列表
     */
    public static <T extends EntityObject> List<T> selectEntityObjectListByPage(BaseEntityMapper<T> entityMybatisMapper, QueryCriteria<T> criteria, Page page) {
        List<T> pageList = null;
        int totalRowCount = entityMybatisMapper.selectPageCountByCriteria(criteria);
        if(totalRowCount > 0) {
            pageList = entityMybatisMapper.selectPageListByCriteria(criteria, new RowBounds(page.offset(), page.limit()));
        }
        page.setTotalRowCount(totalRowCount);
        return ObjectUtils.defaultIfNull(pageList, Collections.emptyList());
    }

    /**
     * 执行JDBC批量更新操作
     *
     * @param entityObjects         - 被批量更新的实体对象集合
     * @param updateOperation       - 具体执行更新的操作
     * @param entityMybatisMapper   - 实体对象的MybatisMapper
     * @param <T>
     */
    public static <T extends EntityObject> int batchUpdateEntityObjects(List<T> entityObjects, Consumer<T> updateOperation, BaseEntityMapper<T> entityMybatisMapper) {
        return batchUpdateEntityObjects(entityObjects, DEFAULT_JDBC_BATCH_SIZE, updateOperation, entityMybatisMapper);
    }

    /**
     * 执行JDBC批量更新操作
     *
     * @param entityObjects         - 被批量更新的实体对象集合
     * @param batchSize             - 每批次处理多少条
     * @param updateOperation       - 具体执行更新的操作
     * @param entityMybatisMapper   - 实体对象的MybatisMapper
     * @param <T>
     */
    public static <T extends EntityObject> int batchUpdateEntityObjects(List<T> entityObjects, int batchSize, Consumer<T> updateOperation, BaseEntityMapper<T> entityMybatisMapper) {
        return executeBatchUpdateEntityObjects(entityObjects, batchSize, updateOperation, entityMybatisMapper);
    }

    /**
     * 执行JDBC批量删除操作
     *
     * @param ids                   - 被批量删除的ID集合
     * @param entityMybatisMapper   - 实体对象的MybatisMapper
     * @param <T>
     */
    public static <T extends Serializable, D extends EntityObject> int batchDeleteEntityObjects(List<T> ids, BaseEntityMapper<D> entityMybatisMapper) {
        return batchDeleteEntityObjects(ids, DEFAULT_JDBC_BATCH_SIZE, entityMybatisMapper);
    }

    /**
     * 执行JDBC批量删除操作
     *
     * @param ids                   - 被批量删除的ID集合
     * @param batchSize             - 每批次处理多少条
     * @param entityMybatisMapper   - 实体对象的MybatisMapper
     * @param <T>                   - 操作目标
     * @param <D>                   - 操作实体对象
     */
    public static <T extends Serializable, D extends EntityObject> int batchDeleteEntityObjects(List<T> ids, int batchSize, BaseEntityMapper<D> entityMybatisMapper) {
        if(ids.size() > DEFAULT_IN_SQL_LIMITS) { //如果ids过大，则走原生JDBC-Batch
            return executeBatchUpdateEntityObjects(ids, batchSize, entityMybatisMapper::deleteById, entityMybatisMapper);
        } else {
            return entityMybatisMapper.deleteByIds(ids);
        }
    }

    /**
     * 执行JDBC批量更新操作
     *
     * @param targetList            - 被批量更新的目标集合
     * @param batchSize             - 每批次处理多少条
     * @param updateOperation       - 具体执行更新的操作
     * @param entityMybatisMapper   - 实体对象的MybatisMapper
     * @param <T>                   - 操作目标
     * @param <D>                   - 操作实体对象
     */
    protected static <T extends Serializable, D extends EntityObject> int executeBatchUpdateEntityObjects(List<T> targetList, int batchSize, Consumer<T> updateOperation, BaseEntityMapper<D> entityMybatisMapper) {
        batchSize = batchSize > 0 ? batchSize : DEFAULT_JDBC_BATCH_SIZE;
        int updateCounts = 0;
        try(JdbcBatchOperation batchOperation = new JdbcBatchOperation()) {
            for(int i = 0, size = targetList.size(); i < size; i++) {
                T target = targetList.get(i);
                if(target != null) {
                    updateOperation.accept(target);
                    if(size > batchSize && ((i + 1) % batchSize == 0)) {
                        List<BatchResult> results = entityMybatisMapper.flushStatements();
                        updateCounts += collectUpdateCounts(results);
                    }
                }
            }
            List<BatchResult> results = entityMybatisMapper.flushStatements();
            updateCounts += collectUpdateCounts(results);
        }
        return updateCounts;
    }

    private static int collectUpdateCounts(List<BatchResult> results) {
        int updateCounts = 0;
        for(BatchResult result : results) {
            for(int updateCount : result.getUpdateCounts()) {
                updateCounts += updateCount;
            }
        }
        return updateCounts;
    }

}
