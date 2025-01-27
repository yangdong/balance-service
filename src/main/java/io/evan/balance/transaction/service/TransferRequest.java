package io.evan.balance.transaction.service;

import java.math.BigDecimal;

/**
 * 转账交易请求参数值对象
 */
public class TransferRequest {
    private final String sourceAccount;
    private final String targetAccount;
    private final BigDecimal amount;

    public TransferRequest(String sourceAccount, String targetAccount, BigDecimal amount) {
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
        this.amount = amount;
    }

    public String getSourceAccount() {
        return sourceAccount;
    }

    public String getTargetAccount() {
        return targetAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}