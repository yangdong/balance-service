package io.evan.balance.transaction.service.tcc.domain;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TCCTransactionRepository extends JpaRepository<TCCTransaction, String> {
    Optional<TCCTransaction> findByTransactionId(String transactionId);
}
