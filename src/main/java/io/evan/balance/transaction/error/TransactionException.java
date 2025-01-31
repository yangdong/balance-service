package io.evan.balance.transaction.error;

import lombok.Getter;

@Getter
public class TransactionException extends Exception {
    private final TransactionErrorCode errorCode;

    public TransactionException(final TransactionErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
