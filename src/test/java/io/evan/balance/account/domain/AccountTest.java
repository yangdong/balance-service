package io.evan.balance.account.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .accountNumber("acc_123456")
                .balance(new BigDecimal("1000.00"))
                .frozeBalance(new BigDecimal("0.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testAccountNumber() {
        assertEquals("acc_123456", account.getAccountNumber());
        account.setAccountNumber("acc_654321");
        assertEquals("acc_654321", account.getAccountNumber());
    }

    @Test
    void testBalance() {
        assertEquals(new BigDecimal("1000.00"), account.getBalance());
        account.setBalance(new BigDecimal("2000.00"));
        assertEquals(new BigDecimal("2000.00"), account.getBalance());
    }

    @Test
    void testFrozeBalance() {
        assertEquals(new BigDecimal("0.00"), account.getFrozeBalance());
        account.setFrozeBalance(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("100.00"), account.getFrozeBalance());
    }

    @Test
    void testCreatedAt() {
        assertNotNull(account.getCreatedAt());
    }

    @Test
    void testUpdatedAt() {
        assertNotNull(account.getUpdatedAt());
        LocalDateTime newTime = LocalDateTime.now();
        account.setUpdatedAt(newTime);
        assertEquals(newTime, account.getUpdatedAt());
    }
}