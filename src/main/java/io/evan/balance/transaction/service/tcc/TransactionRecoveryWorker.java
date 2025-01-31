package io.evan.balance.transaction.service.tcc;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;

import io.evan.balance.common.LockService;
import io.evan.balance.transaction.error.TransactionException;
import io.evan.balance.transaction.service.TransactionRecoveryMessage;

@RocketMQMessageListener(
        topic = "transaction_recovery",
        consumerGroup = "${spring.application.name}_consumer"
)
public class TransactionRecoveryWorker implements RocketMQListener<TransactionRecoveryMessage> {
    private final TccTransactionService tccTransactionService;
    private final LockService lockService;

    public TransactionRecoveryWorker(final TccTransactionService tccTransactionService, final LockService lockService) {
        this.tccTransactionService = tccTransactionService;
        this.lockService = lockService;
    }

    @Override
    public void onMessage(final TransactionRecoveryMessage message) {
        final String key = "Transaction:" + message.getTransactionId();
        try {
            if (!lockService.tryLock(key)) {
                return;
            }
            this.tccTransactionService.finishTransaction(message.getTransactionId());
        } catch (TransactionException e) {
            throw new RuntimeException(e);
        } finally {
            this.lockService.unlock(key);
        }
    }
}
