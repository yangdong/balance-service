package io.evan.balance.transaction.error;

import io.evan.balance.common.ErrorCode;

public enum TransactionErrorCode implements ErrorCode {
    SUCCESS("Transaction successful"),
    SOURCE_ACCOUNT_NOT_FOUND("Source account not found"),
    TARGET_ACCOUNT_NOT_FOUND("Target account not found"),
    INSUFFICIENT_BALANCE("Insufficient account balance"),
    TRANSACTION_NOT_FOUND("Transaction record not found"),
    TRANSACTION_STATUS_ERROR("Transaction status error"),
    SYSTEM_ERROR("System error");

    private final String message;

    TransactionErrorCode(String message) {
        this.message = message;
    }

    @Override
    public String code() {
        return name().replace("_", ".").toLowerCase();
    }

    @Override
    public String message() {
        return message;
    }
}