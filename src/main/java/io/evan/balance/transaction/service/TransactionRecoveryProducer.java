package io.evan.balance.transaction.service;

import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.evan.balance.transaction.service.tcc.UnfinishedTransactionScanner;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TransactionRecoveryProducer {
    private final RocketMQTemplate rocketMQTemplate;
    private final UnfinishedTransactionScanner unfinishedTransactionScanner;

    public TransactionRecoveryProducer(
            RocketMQTemplate rocketMQTemplate,
            final UnfinishedTransactionScanner unfinishedTransactionScanner) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.unfinishedTransactionScanner = unfinishedTransactionScanner;
    }

    @Scheduled(fixedRate = 300000)
    public void scheduleRecovery() {
        this.unfinishedTransactionScanner.scanUnfinishedTransactions().forEach(trans -> {
            sendRecoveryMessage(trans.getTransactionId(), trans.getRetryCount()); // 从第一个延迟级别开始
        });
    }

    private void sendRecoveryMessage(String transactionId, int delayLevel) {
        Message<TransactionRecoveryMessage> rocketMessage = MessageBuilder
                .withPayload(new TransactionRecoveryMessage(transactionId, delayLevel))
                .setHeader(RocketMQHeaders.KEYS, transactionId)
                .setHeader("retryCount", delayLevel)
                .build();

        try {
            rocketMQTemplate.asyncSend("transaction_recovery", rocketMessage, new SendCallback() {
                @Override
                public void onException(Throwable throwable) {
                    log.error("Failed to send recovery message for transaction: {}",
                            transactionId, throwable);
                }

                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("Recovery message sent successfully for transaction: {}, delay level: {}",
                            transactionId, delayLevel);
                }
            }, 3000, delayLevel); // 3秒发送超时
        } catch (Exception e) {
            log.error("Error sending recovery message for transaction: {}",
                    transactionId, e);
        }
    }

}
