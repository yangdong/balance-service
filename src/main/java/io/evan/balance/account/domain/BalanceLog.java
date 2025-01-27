package io.evan.balance.account.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "balance_logs", indexes = {
        @Index(name = "idx_transaction_id", columnList = "transaction_id"),
        @Index(name = "idx_account_number", columnList = "account_number"),
})
@Builder
@Getter
@Setter
public class BalanceLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", length = 32, nullable = false)
    private String accountNumber;

    @Column(name = "transaction_id", length = 100, nullable = false)
    private String transactionId;

    @Column(name = "amount", precision = 20, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "type", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private BalanceLogType type;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private BalanceLogStatus status;

    @Column(name = "balance_before", precision = 20, scale = 2, nullable = false)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", precision = 20, scale = 2, nullable = false)
    private BigDecimal balanceAfter;

    @Column(name = "frozen_before", precision = 20, scale = 2, nullable = false)
    private BigDecimal frozenBefore;

    @Column(name = "frozen_after", precision = 20, scale = 2, nullable = false)
    private BigDecimal frozenAfter;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    public enum BalanceLogType {
        FREEZE, ADD
    }

    public enum BalanceLogStatus {
        INIT, ROLLBACK, COMMITED
    }
}