package io.evan.balance.account;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AccountErrorCodeTest {

    @Test
    void test_account_not_found() {
        AccountErrorCode errorCode = AccountErrorCode.ACCOUNT_NOT_FOUND;
        assertEquals("account.not.found", errorCode.code());
        assertEquals("Account not found", errorCode.message());
    }

    @Test
    void test_insufficient_balance() {
        AccountErrorCode errorCode = AccountErrorCode.INSUFFICIENT_BALANCE;
        assertEquals("insufficient.balance", errorCode.code());
        assertEquals("Insufficient balance", errorCode.message());
    }

    @Test
    void test_transaction_not_found() {
        AccountErrorCode errorCode = AccountErrorCode.TRANSACTION_NOT_FOUND;
        assertEquals("transaction.not.found", errorCode.code());
        assertEquals("Transaction not found", errorCode.message());
    }
}