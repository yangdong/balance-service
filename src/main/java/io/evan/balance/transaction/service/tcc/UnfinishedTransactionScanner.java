package io.evan.balance.transaction.service.tcc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import io.evan.balance.transaction.domain.TransactionIdWithRetryCount;
import io.evan.balance.transaction.domain.TransactionRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UnfinishedTransactionScanner {
    // 分页大小
    private static final int PAGE_SIZE = 100;
    // 一次最多处理多少条
    private static final int MAX_UNFINISHED_TRANSACTIONS = 1000;
    // 超时时间（分钟）
    private static final int TIMEOUT_MINUTES = 30;
    private final TransactionRepository transactionRepository;

    public UnfinishedTransactionScanner(final TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<TransactionIdWithRetryCount> scanUnfinishedTransactions() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES);
        List<TransactionIdWithRetryCount> unfinishedTransactionIds = new ArrayList<>();

        try {
            // 使用分页查询
            int pageNumber = 0;
            while (true) {
                Page<TransactionIdWithRetryCount> page = transactionRepository.findUnfinishedTransactionIdsWithRetryCount(
                        timeoutThreshold,
                        PageRequest.of(pageNumber, PAGE_SIZE)
                );

                unfinishedTransactionIds.addAll(page.toList());

                if (!page.hasNext() || unfinishedTransactionIds.size() >= MAX_UNFINISHED_TRANSACTIONS) {
                    break;
                }

                pageNumber++;
            }
        } catch (Exception e) {
            log.error("Error scanning unfinished transactions", e);
        }

        return unfinishedTransactionIds;
    }
}
