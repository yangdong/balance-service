package io.evan.balance.account.service;

import java.math.BigDecimal;

import io.evan.balance.account.AccountErrorCode;
import io.evan.balance.common.Result;

public interface AccountService {
    /**
     * Adds a specified amount to the account balance.
     *
     * @param transactionId the transaction identifier
     * @param accountNumber the account number
     * @param amount the amount to add
     * @param reason the reason for adding the balance
     * @return the result containing account information or an error code
     */
    Result<AccountInfo, AccountErrorCode> addBalance(String transactionId, String accountNumber, BigDecimal amount, String reason);

    /**
     * Confirms a previously frozen amount, deducting it from the available balance.
     *
     * @param transactionId the transaction identifier
     * @param accountNumber the account number
     * @return the result containing account information or an error code
     */
    Result<AccountInfo, AccountErrorCode> confirmFreeze(String transactionId, String accountNumber);

    /**
     * Freezes a specified amount in the account, making it unavailable for transactions.
     *
     * @param transactionId the transaction identifier
     * @param accountNumber the account number
     * @param amount the amount to freeze
     * @param reason the reason for freezing the balance
     * @return the result containing account information or an error code
     */
    Result<AccountInfo, AccountErrorCode> freeze(String transactionId, String accountNumber, BigDecimal amount, String reason);

    /**
     * Retrieves account information based on the account number.
     *
     * @param accountNumber the account number
     * @return the result containing account information or an error code
     */
    Result<AccountInfo, AccountErrorCode> getAccount(String accountNumber);

    /**
     * Unfreezes a previously frozen amount, making it available again.
     *
     * @param transactionId the transaction identifier
     * @param accountNumber the account number
     * @return the result containing account information or an error code
     */
    Result<AccountInfo, AccountErrorCode> unfreeze(String transactionId, String accountNumber);
}
