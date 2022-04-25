package com.penglecode.codeforce.mybatistiny.executor;

import org.apache.ibatis.session.ExecutorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NamedThreadLocal;
import org.springframework.util.ClassUtils;

/**
 * Mybatis Executor同步管理器
 *
 * @author pengpeng
 * @version 1.0
 */
public class ExecutorSynchronizationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorSynchronizationManager.class);

    private static final NamedThreadLocal<ExecutorType> currentExecutorType = new NamedThreadLocal<>("The ExecutorType of current thread inuse");

    /**
     * 当前是Mybatis与Spring集成?
     */
    private static final boolean springTransactionPresent;

    static {
        ClassLoader classLoader = ExecutorSynchronizationManager.class.getClassLoader();
        springTransactionPresent = ClassUtils.isPresent("org.mybatis.spring.transaction.SpringManagedTransaction", classLoader);
    }

    private ExecutorSynchronizationManager() {}

    /**
     * 设置当前线程上下文的ExecutorType
     * @param executorType
     */
    public static void setCurrentExecutorType(ExecutorType executorType) {
        ExecutorType currentType = currentExecutorType.get();
        LOGGER.debug("Set current ExecutorType from [{}] to [{}]", currentType == null ? "DEFAULT" : currentType, executorType);
        currentExecutorType.set(executorType);
        if(executorType == ExecutorType.BATCH && springTransactionPresent) {
            //Mybatis与Spring集成时，在不存在Spring事务的情况下，一个操作(XxxMapper的方法)将会打开一个新的SqlSession(底层所持Connection也将是不同的)
            if(!SpringTransactionHelper.isActualTransactionActive()) {
                LOGGER.warn("There is no active transaction found which managed by Spring, activating JDBC batches is also futile!");
            }
        }
    }

    public static ExecutorType getCurrentExecutorType() {
        return currentExecutorType.get();
    }

    public static void resetCurrentExecutorType() {
        LOGGER.debug("Reset current ExecutorType from [{}] to [{}]", currentExecutorType.get(), "DEFAULT");
        currentExecutorType.remove();
    }

}
