package io.evan.balance.account.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BalanceLogTest {

    @Test
    void testBalanceLogBuilder() {
        LocalDateTime now = LocalDateTime.now();
        BalanceLog balanceLog = BalanceLog.builder()
                .accountNumber("acc_123456")
                .transactionId("txn_123456")
                .amount(new BigDecimal("100.00"))
                .type(BalanceLog.BalanceLogType.ADD)
                .status(BalanceLog.BalanceLogStatus.INIT)
                .balanceBefore(new BigDecimal("1000.00"))
                .balanceAfter(new BigDecimal("1100.00"))
                .frozenBefore(new BigDecimal("0.00"))
                .frozenAfter(new BigDecimal("0.00"))
                .createTime(now)
                .build();

        assertNotNull(balanceLog);
        assertEquals("acc_123456", balanceLog.getAccountNumber());
        assertEquals("txn_123456", balanceLog.getTransactionId());
        assertEquals(new BigDecimal("100.00"), balanceLog.getAmount());
        assertEquals(BalanceLog.BalanceLogType.ADD, balanceLog.getType());
        assertEquals(BalanceLog.BalanceLogStatus.INIT, balanceLog.getStatus());
        assertEquals(new BigDecimal("1000.00"), balanceLog.getBalanceBefore());
        assertEquals(new BigDecimal("1100.00"), balanceLog.getBalanceAfter());
        assertEquals(new BigDecimal("0.00"), balanceLog.getFrozenBefore());
        assertEquals(new BigDecimal("0.00"), balanceLog.getFrozenAfter());
        assertEquals(now, balanceLog.getCreateTime());
    }
}