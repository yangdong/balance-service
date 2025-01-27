package io.evan.balance.account.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface BalanceLogRepository extends JpaRepository<BalanceLog, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BalanceLog b where b.transactionId = :transactionId and b.accountNumber = :accountNumber")
    Optional<BalanceLog> findByTransactionIdAndAccountNumberForUpdate(@Param("transactionId")String transactionId, @Param("accountNumber") String accountNumber);
}
