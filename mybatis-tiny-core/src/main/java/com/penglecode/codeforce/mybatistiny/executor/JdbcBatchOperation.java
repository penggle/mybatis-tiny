package com.penglecode.codeforce.mybatistiny.executor;

import org.apache.ibatis.session.ExecutorType;

/**
 * Mybatis更新操作走JDBC-Batch特性的优雅辅助类
 * 使用方式：
 *      try(JdbcBatchOperation operation = new JdbcBatchOperation()) { //进入try块中,底层真正的Executor将切换为BatchExecutor
 *          for(int i = 0; i < modelList.size(); i++) {
 *              MyModel model = modelList.get(i);
 *              xxxMapper.insertModel(model);
 *              if((i + 1) % 5000 == 0) {
 *                  xxxMapper.flushStatements(); //5000条提交一次
 *              }
 *          }
 *          xxxMapper.flushStatements(); //最后一波提交(别漏了)
 *      } //退出try块后底层真正的Executor将切换为默认的Executor(取决于你程序的设置)
 *
 * @author pengpeng
 * @version 1.0
 */
public final class JdbcBatchOperation implements AutoCloseable {

    public JdbcBatchOperation() {
        ExecutorSynchronizationManager.setCurrentExecutorType(ExecutorType.BATCH);
    }

    @Override
    public void close() {
        ExecutorSynchronizationManager.resetCurrentExecutorType();
    }

}
