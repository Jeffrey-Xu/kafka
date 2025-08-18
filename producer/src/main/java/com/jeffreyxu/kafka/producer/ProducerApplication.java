package com.jeffreyxu.kafka.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Spring Boot application for Kafka message producer.
 * 
 * This application provides REST APIs for sending messages to Kafka topics
 * and tracks statistics in MySQL database.
 */
@SpringBootApplication(scanBasePackages = {
    "com.jeffreyxu.kafka.producer",
    "com.jeffreyxu.kafka.common"
})
@EnableKafka
@EnableJpaRepositories(basePackages = "com.jeffreyxu.kafka.producer.repository")
@EntityScan(basePackages = "com.jeffreyxu.kafka.producer.entity")
public class ProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProducerApplication.class, args);
    }
}
