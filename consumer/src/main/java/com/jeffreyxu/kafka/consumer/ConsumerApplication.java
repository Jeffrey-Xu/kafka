package com.jeffreyxu.kafka.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring Boot application for Kafka message consumer.
 * 
 * This application listens to Kafka topics, processes events,
 * and stores processed data in MySQL database.
 */
@SpringBootApplication(scanBasePackages = {
    "com.jeffreyxu.kafka.consumer",
    "com.jeffreyxu.kafka.common"
})
@EnableKafka
@EnableAsync
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.jeffreyxu.kafka.consumer.repository")
@EntityScan(basePackages = "com.jeffreyxu.kafka.consumer.entity")
public class ConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
}
