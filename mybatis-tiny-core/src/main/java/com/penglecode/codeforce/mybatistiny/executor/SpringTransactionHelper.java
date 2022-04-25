package com.penglecode.codeforce.mybatistiny.executor;

import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Spring事务帮助类
 *
 * @author pengpeng
 * @version 1.0
 */
public class SpringTransactionHelper {

    /**
     * 当前是否存在事务
     *
     * @return
     */
    public static boolean isActualTransactionActive() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

}
