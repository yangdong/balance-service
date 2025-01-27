package io.evan.balance.transaction.service;

import lombok.Getter;

@Getter
public class TransactionRecoveryMessage {
    private final String transactionId;
    private final int retryCount;

    public TransactionRecoveryMessage(final String transactionId, final int retryCount) {
        this.transactionId = transactionId;
        this.retryCount = retryCount;
    }
}
