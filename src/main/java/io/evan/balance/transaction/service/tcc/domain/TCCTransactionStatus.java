package io.evan.balance.transaction.service.tcc.domain;

public enum TCCTransactionStatus {
    /**
     * Try阶段 - 预留资源
     */
    TRYING,

    CONFIRMING,
    /**
     * Confirm阶段 - 确认完成
     */
    CONFIRMED,

    CANCELLING,
    /**
     * Cancel阶段 - 取消回滚
     */
    CANCELLED
}