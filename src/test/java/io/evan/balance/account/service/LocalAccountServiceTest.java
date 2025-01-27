package io.evan.balance.account.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.evan.balance.account.AccountErrorCode;
import io.evan.balance.account.domain.Account;
import io.evan.balance.account.domain.AccountRepository;
import io.evan.balance.account.domain.BalanceLog;
import io.evan.balance.account.domain.BalanceLogRepository;
import io.evan.balance.common.Result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LocalAccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BalanceLogRepository balanceLogRepository;

    @InjectMocks
    private LocalAccountService localAccountService;

    private Account account;
    private BalanceLog balanceLog;

    @Test
    void confirm_freeze_transaction_not_found() {
        when(accountRepository.findByAccountNumberForUpdate("123456")).thenReturn(Optional.of(account));
        when(balanceLogRepository.findByTransactionIdAndAccountNumberForUpdate("tx123", "123456")).thenReturn(Optional.empty());

        Result<AccountInfo, AccountErrorCode> result = localAccountService.confirmFreeze("tx123", "123456");

        assertFalse(result.isSuccess());
        assertEquals(AccountErrorCode.TRANSACTION_NOT_FOUND, result.getError());

        verify(accountRepository, times(0)).save(any(Account.class));
        verify(balanceLogRepository, times(0)).save(any(BalanceLog.class));
    }

    @Test
    void freeze_insufficient_balance() {
        when(accountRepository.findByAccountNumberForUpdate("123456")).thenReturn(Optional.of(account));

        Result<AccountInfo, AccountErrorCode> result = localAccountService.freeze("tx123", "123456", BigDecimal.valueOf(2000), "test reason");

        assertFalse(result.isSuccess());
        assertEquals(AccountErrorCode.INSUFFICIENT_BALANCE, result.getError());

        verify(accountRepository, times(0)).save(any(Account.class));
        verify(balanceLogRepository, times(0)).save(any(BalanceLog.class));
    }

    @Test
    void get_account_not_found() {
        when(accountRepository.findByAccountNumber("123456")).thenReturn(Optional.empty());

        Result<AccountInfo, AccountErrorCode> result = localAccountService.getAccount("123456");

        assertFalse(result.isSuccess());
        assertEquals(AccountErrorCode.ACCOUNT_NOT_FOUND, result.getError());
    }

    @Test
    void get_account_success() {
        when(accountRepository.findByAccountNumber("123456")).thenReturn(Optional.of(account));

        Result<AccountInfo, AccountErrorCode> result = localAccountService.getAccount("123456");

        assertTrue(result.isSuccess());
        assertEquals("123456", result.getData().getAccountNumber());
        assertEquals(BigDecimal.valueOf(1000), result.getData().getBalance());
    }

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .accountNumber("123456")
                .balance(BigDecimal.valueOf(1000))
                .frozeBalance(BigDecimal.ZERO)
                .build();

        balanceLog = BalanceLog.builder()
                .accountNumber("123456")
                .transactionId("tx123")
                .amount(BigDecimal.valueOf(100))
                .type(BalanceLog.BalanceLogType.ADD)
                .balanceBefore(BigDecimal.valueOf(1000))
                .balanceAfter(BigDecimal.valueOf(1100))
                .frozenBefore(BigDecimal.ZERO)
                .frozenAfter(BigDecimal.ZERO)
                .createTime(LocalDateTime.now())
                .status(BalanceLog.BalanceLogStatus.COMMITED)
                .build();
    }

    @Test
    void test_add_balance_success() {
        when(accountRepository.findByAccountNumberForUpdate("123456")).thenReturn(Optional.of(account));
        when(balanceLogRepository.save(any(BalanceLog.class))).thenReturn(balanceLog);

        Result<AccountInfo, AccountErrorCode> result = localAccountService.addBalance("tx123", "123456", BigDecimal.valueOf(100), "test reason");

        assertTrue(result.isSuccess());
        assertEquals("123456", result.getData().getAccountNumber());
        assertEquals(BigDecimal.valueOf(1100), result.getData().getBalance());

        verify(accountRepository, times(1)).save(account);
        verify(balanceLogRepository, times(1)).save(any(BalanceLog.class));
    }

    @Test
    void this_is_a_test_add_balance_account_not_found() {
        when(accountRepository.findByAccountNumberForUpdate("123456")).thenReturn(Optional.empty());

        Result<AccountInfo, AccountErrorCode> result = localAccountService.addBalance("tx123", "123456", BigDecimal.valueOf(100), "test reason");

        assertFalse(result.isSuccess());
        assertEquals(AccountErrorCode.ACCOUNT_NOT_FOUND, result.getError());

        verify(accountRepository, times(0)).save(any(Account.class));
        verify(balanceLogRepository, times(0)).save(any(BalanceLog.class));
    }

    @Test
    void unfreeze_transaction_not_found() {
        when(accountRepository.findByAccountNumberForUpdate("123456")).thenReturn(Optional.of(account));
        when(balanceLogRepository.findByTransactionIdAndAccountNumberForUpdate("tx123", "123456")).thenReturn(Optional.empty());

        Result<AccountInfo, AccountErrorCode> result = localAccountService.unfreeze("tx123", "123456");

        assertFalse(result.isSuccess());
        assertEquals(AccountErrorCode.TRANSACTION_NOT_FOUND, result.getError());

        verify(accountRepository, times(0)).save(any(Account.class));
        verify(balanceLogRepository, times(0)).delete(any(BalanceLog.class));
    }
}
