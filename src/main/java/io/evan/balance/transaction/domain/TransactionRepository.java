package io.evan.balance.transaction.domain;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    Optional<Transaction> findByTransactionId(String transactionId);

    @Query("SELECT t.transactionId, t.retryCount FROM Transaction t WHERE t.timestamp < :timeoutThreshold " +
            "AND t.status = 'PENDING'")
    Page<TransactionIdWithRetryCount> findUnfinishedTransactionIdsWithRetryCount(
            @Param("timeoutThreshold") LocalDateTime timeoutThreshold,
            Pageable pageable
    );
}