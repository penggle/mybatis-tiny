package com.penglecode.codeforce.mybatistiny.executor;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.*;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 动态Executor，解决在同一个事物上下文中mybatis-spring框架不允许切换Executor的问题
 *
 * @author pengpeng
 * @version 1.0
 */
public class DynamicExecutor implements Executor {

    private final Transaction transaction;

    private final ExecutorType defaultExecutorType;

    private final Configuration configuration;

    private final ConcurrentMap<ExecutorType,BaseExecutor> cachedExecutors = new ConcurrentHashMap<>();

    public DynamicExecutor(Transaction transaction, ExecutorType defaultExecutorType, Configuration configuration) {
        this.transaction = transaction;
        this.defaultExecutorType = defaultExecutorType;
        this.configuration = configuration;
    }

    protected BaseExecutor determineExecutor() {
        ExecutorType currentExecutorType = ExecutorSynchronizationManager.getCurrentExecutorType();
        currentExecutorType = currentExecutorType == null ? defaultExecutorType : currentExecutorType;
        return cachedExecutors.computeIfAbsent(currentExecutorType, this::createExecutor);
    }

    protected BaseExecutor createExecutor(ExecutorType executorType) {
        BaseExecutor executor;
        if (ExecutorType.BATCH == executorType) {
            executor = new BatchExecutor(configuration, transaction);
        } else if (ExecutorType.REUSE == executorType) {
            executor = new ReuseExecutor(configuration, transaction);
        } else {
            executor = new SimpleExecutor(configuration, transaction);
        }
        return executor;
    }

    @Override
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        return determineExecutor().update(ms, parameter);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey cacheKey, BoundSql boundSql) throws SQLException {
        return determineExecutor().query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
        return determineExecutor().query(ms, parameter, rowBounds, resultHandler);
    }

    @Override
    public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {
        return determineExecutor().queryCursor(ms, parameter, rowBounds);
    }

    @Override
    public List<BatchResult> flushStatements() throws SQLException {
        return determineExecutor().flushStatements();
    }

    @Override
    public void commit(boolean required) throws SQLException {
        determineExecutor().commit(required);
    }

    @Override
    public void rollback(boolean required) throws SQLException {
        determineExecutor().rollback(required);
    }

    @Override
    public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
        return determineExecutor().createCacheKey(ms, parameterObject, rowBounds, boundSql);
    }

    @Override
    public boolean isCached(MappedStatement ms, CacheKey key) {
        return determineExecutor().isCached(ms, key);
    }

    @Override
    public void clearLocalCache() {
        determineExecutor().clearLocalCache();
    }

    @Override
    public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType) {
        determineExecutor().deferLoad(ms, resultObject, property, key, targetType);
    }

    @Override
    public Transaction getTransaction() {
        return determineExecutor().getTransaction();
    }

    @Override
    public void close(boolean forceRollback) {
        determineExecutor().close(forceRollback);
    }

    @Override
    public boolean isClosed() {
        return determineExecutor().isClosed();
    }

    @Override
    public void setExecutorWrapper(Executor executor) {
        determineExecutor().setExecutorWrapper(executor);
    }

}
