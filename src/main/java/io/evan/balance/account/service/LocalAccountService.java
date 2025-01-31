package io.evan.balance.account.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.evan.balance.account.AccountErrorCode;
import io.evan.balance.account.domain.BalanceLog;
import io.evan.balance.account.domain.AccountRepository;
import io.evan.balance.account.domain.BalanceLogRepository;
import io.evan.balance.common.Result;

@Component
public class LocalAccountService implements AccountService {
    private final AccountRepository accountRepository;
    private final BalanceLogRepository balanceLogRepository;

    public LocalAccountService(final AccountRepository accountRepository,
                               final BalanceLogRepository balanceLogRepository) {
        this.accountRepository = accountRepository;
        this.balanceLogRepository = balanceLogRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<AccountInfo, AccountErrorCode> addBalance(final String transactionId, final String accountNumber, final BigDecimal amount, final String reason) {
        return accountRepository.findByAccountNumberForUpdate(accountNumber)
                .map(account -> {
                    account.setBalance(account.getBalance().add(amount));
                    accountRepository.save(account);

                    BalanceLog balanceLog = BalanceLog.builder()
                            .accountNumber(accountNumber)
                            .transactionId(transactionId)
                            .amount(amount)
                            .type(BalanceLog.BalanceLogType.ADD)
                            .balanceBefore(account.getBalance().subtract(amount))
                            .balanceAfter(account.getBalance())
                            .frozenBefore(account.getFrozeBalance())
                            .frozenAfter(account.getFrozeBalance())
                            .createTime(LocalDateTime.now())
                            .status(BalanceLog.BalanceLogStatus.COMMITED)
                            .build();

                    balanceLogRepository.save(balanceLog);

                    return Result.<AccountInfo, AccountErrorCode>success(new AccountInfo(account.getAccountNumber(), account.getBalance()));
                })
                .orElse(Result.error(AccountErrorCode.ACCOUNT_NOT_FOUND));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<AccountInfo, AccountErrorCode> confirmFreeze(final String transactionId, final String accountNumber) {
        return accountRepository.findByAccountNumberForUpdate(accountNumber)
                .map(account -> balanceLogRepository.findByTransactionIdAndAccountNumberForUpdate(transactionId, accountNumber)
                        .filter(balanceLog -> balanceLog.getType() == BalanceLog.BalanceLogType.FREEZE)
                        .map(balanceLog -> {
                            BigDecimal amount = balanceLog.getAmount();
                            account.setBalance(account.getBalance().subtract(amount));
                            account.setFrozeBalance(account.getFrozeBalance().subtract(amount));
                            accountRepository.save(account);

                            balanceLog.setStatus(BalanceLog.BalanceLogStatus.COMMITED);
                            balanceLogRepository.save(balanceLog);
                            return Result.<AccountInfo, AccountErrorCode>success(new AccountInfo(account.getAccountNumber(), account.getBalance()));
                        })
                        .orElse(Result.error(AccountErrorCode.TRANSACTION_NOT_FOUND))
                )
                .orElse(Result.error(AccountErrorCode.ACCOUNT_NOT_FOUND));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<AccountInfo, AccountErrorCode> freeze(final String transactionId,
                                                        final String accountNumber,
                                                        final BigDecimal amount,
                                                        final String reason) {
        return accountRepository.findByAccountNumberForUpdate(accountNumber)
                .map(account -> {
                    final BigDecimal frozeBalance = account.getFrozeBalance();
                    if (account.getBalance().subtract(frozeBalance).compareTo(amount) < 0) {
                        return Result.<AccountInfo, AccountErrorCode>error(AccountErrorCode.INSUFFICIENT_BALANCE);
                    }

                    account.setFrozeBalance(frozeBalance.add(amount));
                    accountRepository.save(account);

                    BalanceLog balanceLog = BalanceLog.builder()
                            .accountNumber(accountNumber)
                            .transactionId(transactionId)
                            .amount(amount)
                            .type(BalanceLog.BalanceLogType.FREEZE)
                            .balanceBefore(account.getBalance())
                            .balanceAfter(account.getBalance())
                            .frozenBefore(frozeBalance)
                            .frozenAfter(frozeBalance.add(amount))
                            .createTime(LocalDateTime.now())
                            .build();

                    balanceLogRepository.save(balanceLog);

                    return Result.<AccountInfo, AccountErrorCode>success(new AccountInfo(account.getAccountNumber(), account.getBalance()));
                })
                .orElse(Result.error(AccountErrorCode.ACCOUNT_NOT_FOUND));
    }

    @Override
    public Result<AccountInfo, AccountErrorCode> getAccount(final String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(account -> new AccountInfo(account.getAccountNumber(), account.getBalance()))
                .map(Result::<AccountInfo, AccountErrorCode>success)
                .orElse(Result.error(AccountErrorCode.ACCOUNT_NOT_FOUND));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<AccountInfo, AccountErrorCode> unfreeze(final String transactionId, final String accountNumber) {
        return accountRepository.findByAccountNumberForUpdate(accountNumber)
                .map(account -> balanceLogRepository.findByTransactionIdAndAccountNumberForUpdate(transactionId, accountNumber)
                        .filter(balanceLog -> balanceLog.getType() == BalanceLog.BalanceLogType.FREEZE)
                        .map(balanceLog -> {
                            BigDecimal amount = balanceLog.getAmount();
                            account.setFrozeBalance(account.getFrozeBalance().subtract(amount));
                            accountRepository.save(account);

                            balanceLog.setStatus(BalanceLog.BalanceLogStatus.ROLLBACK);
                            balanceLogRepository.save(balanceLog);

                            return Result.<AccountInfo, AccountErrorCode>success(new AccountInfo(account.getAccountNumber(), account.getBalance()));
                        })
                        .orElse(Result.error(AccountErrorCode.TRANSACTION_NOT_FOUND))
                )
                .orElse(Result.error(AccountErrorCode.ACCOUNT_NOT_FOUND));
    }
}
