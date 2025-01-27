package io.evan.balance.transaction.domain;

public class TransactionIdWithRetryCount {
    private String transactionId;
    private int retryCount;

    public TransactionIdWithRetryCount(String transactionId, int retryCount) {
        this.transactionId = transactionId;
        this.retryCount = retryCount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}