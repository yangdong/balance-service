package io.evan.balance.config;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RocketMQConfig {
    @Value("${spring.rocketmq.name-server}")
    private String nameServer;

    @Value("${spring.rocketmq.producer.group}")
    private String producerGroup;

    @Bean(destroyMethod = "shutdown")
    public DefaultMQProducer defaultMQProducer() throws MQClientException {
        log.info("Initializing DefaultMQProducer with nameServer: {} and producerGroup: {}",
                nameServer, producerGroup);
        DefaultMQProducer producer = new DefaultMQProducer();
        producer.setProducerGroup(producerGroup);
        producer.setNamesrvAddr(nameServer);
        log.info("DefaultMQProducer started successfully");
        return producer;
    }

    @Bean(destroyMethod = "destroy")
    @DependsOn("defaultMQProducer")
    public RocketMQTemplate rocketMQTemplate(DefaultMQProducer defaultMQProducer) {
        log.info("Initializing RocketMQTemplate");
        RocketMQTemplate rocketMQTemplate = new RocketMQTemplate();
        rocketMQTemplate.setProducer(defaultMQProducer);
        log.info("RocketMQTemplate initialized successfully");
        return rocketMQTemplate;
    }
}