package io.evan.balance.account;

import io.evan.balance.common.ErrorCode;

public enum AccountErrorCode implements ErrorCode {
    ACCOUNT_NOT_FOUND("Account not found"),
    INSUFFICIENT_BALANCE("Insufficient balance"),
    TRANSACTION_NOT_FOUND("Transaction not found");

    private final String message;

    AccountErrorCode(String message) {
        this.message = message;
    }

    public String code() {
        return name().replace("_", ".").toLowerCase();
    }

    @Override
    public String message() {
        return this.message;
    }
}
