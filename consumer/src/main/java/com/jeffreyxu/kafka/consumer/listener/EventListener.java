package com.jeffreyxu.kafka.consumer.listener;

import com.jeffreyxu.kafka.common.model.UserEvent;
import com.jeffreyxu.kafka.common.model.BusinessEvent;
import com.jeffreyxu.kafka.common.model.SystemEvent;
import com.jeffreyxu.kafka.consumer.service.MessageProcessingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka event listeners for processing different types of events.
 * Uses manual acknowledgment for reliable message processing.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventListener {

    private final MessageProcessingService messageProcessingService;

    /**
     * Listen to user events
     */
    @KafkaListener(
        topics = "user-events",
        groupId = "user-events-consumer-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleUserEvent(
            @Payload UserEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            Acknowledgment acknowledgment) {
        
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Received user event: {} from partition {} at offset {}", 
                event.getDescription(), partition, offset);
            
            // Process the event
            messageProcessingService.processUserEvent(event, topic, partition, offset, key);
            
            // Acknowledge successful processing
            acknowledgment.acknowledge();
            
            long processingTime = System.currentTimeMillis() - startTime;
            log.info("Successfully processed user event {} in {}ms", event.getId(), processingTime);
            
        } catch (Exception e) {
            log.error("Failed to process user event: {} from partition {} at offset {}", 
                event.getId(), partition, offset, e);
            
            // In a production system, you might want to:
            // 1. Send to dead letter queue
            // 2. Implement retry logic
            // 3. Alert monitoring systems
            
            // For now, we'll acknowledge to prevent infinite retries
            acknowledgment.acknowledge();
        }
    }

    /**
     * Listen to business events
     */
    @KafkaListener(
        topics = "business-events",
        groupId = "business-events-consumer-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleBusinessEvent(
            @Payload BusinessEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            Acknowledgment acknowledgment) {
        
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Received business event: {} from partition {} at offset {}", 
                event.getDescription(), partition, offset);
            
            // Process the event
            messageProcessingService.processBusinessEvent(event, topic, partition, offset, key);
            
            // Acknowledge successful processing
            acknowledgment.acknowledge();
            
            long processingTime = System.currentTimeMillis() - startTime;
            log.info("Successfully processed business event {} in {}ms", event.getId(), processingTime);
            
        } catch (Exception e) {
            log.error("Failed to process business event: {} from partition {} at offset {}", 
                event.getId(), partition, offset, e);
            
            // Acknowledge to prevent infinite retries
            acknowledgment.acknowledge();
        }
    }

    /**
     * Listen to system events
     */
    @KafkaListener(
        topics = "system-events",
        groupId = "system-events-consumer-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleSystemEvent(
            @Payload SystemEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            Acknowledgment acknowledgment) {
        
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Received system event: {} from partition {} at offset {}", 
                event.getDescription(), partition, offset);
            
            // Process the event
            messageProcessingService.processSystemEvent(event, topic, partition, offset, key);
            
            // Special handling for critical system events
            if (event.isCritical()) {
                log.warn("CRITICAL SYSTEM EVENT: {}", event.getDescription());
                // In production: send alerts, notifications, etc.
            }
            
            // Acknowledge successful processing
            acknowledgment.acknowledge();
            
            long processingTime = System.currentTimeMillis() - startTime;
            log.info("Successfully processed system event {} in {}ms", event.getId(), processingTime);
            
        } catch (Exception e) {
            log.error("Failed to process system event: {} from partition {} at offset {}", 
                event.getId(), partition, offset, e);
            
            // Acknowledge to prevent infinite retries
            acknowledgment.acknowledge();
        }
    }
}
