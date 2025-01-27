package io.evan.balance.transaction.service.tcc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import io.evan.balance.transaction.domain.TransactionIdWithRetryCount;
import io.evan.balance.transaction.domain.TransactionRepository;

class UnfinishedTransactionScannerTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private UnfinishedTransactionScanner unfinishedTransactionScanner;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testScanUnfinishedTransactions() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(30);
        TransactionIdWithRetryCount transaction = new TransactionIdWithRetryCount("tx123", 1);
        Page<TransactionIdWithRetryCount> page = new PageImpl<>(Collections.singletonList(transaction));

        when(transactionRepository.findUnfinishedTransactionIdsWithRetryCount(any(), any(PageRequest.class)))
                .thenReturn(page);

        List<TransactionIdWithRetryCount> result = unfinishedTransactionScanner.scanUnfinishedTransactions();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("tx123", result.get(0).getTransactionId());
        assertEquals(1, result.get(0).getRetryCount());
    }
}