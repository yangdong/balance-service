package io.evan.balance.transaction.service;

import io.evan.balance.common.Result;
import io.evan.balance.transaction.domain.Transaction;
import io.evan.balance.transaction.error.TransactionErrorCode;

public interface TransactionService {
    /**
     * 执行转账交易
     *
     * @param request 转账请求参数
     * @return 交易结果，包含成功/失败状态和错误码
     */
    Result<Transaction, TransactionErrorCode> transfer(TransferRequest request);
}