package io.evan.balance.transaction.service;

import io.evan.balance.transaction.domain.Transaction;
import io.evan.balance.transaction.error.TransactionException;

public interface TransactionService {
    /**
     * 执行转账交易
     *
     * @param request 转账请求参数
     */
    Transaction transfer(TransferRequest request) throws TransactionException;
}