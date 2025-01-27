package io.evan.balance.transaction.service.tcc;

import java.math.BigDecimal;

import io.evan.balance.transaction.domain.Transaction;
import io.evan.balance.transaction.service.tcc.domain.TCCTransaction;
import lombok.Getter;

@Getter
public class TransactionContext {
    private final String transactionId;
    private Transaction transaction;
    private TCCTransaction tccTransaction;

    public TransactionContext(final String transactionId) {
        this.transactionId = transactionId;
    }

    public void attachTCCTransaction(final TCCTransaction transaction) {
        this.tccTransaction = transaction;
    }

    public void attachTransaction(final Transaction transaction) {
        this.transaction = transaction;
    }
}
