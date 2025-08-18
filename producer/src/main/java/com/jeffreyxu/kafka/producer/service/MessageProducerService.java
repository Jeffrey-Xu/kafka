package com.jeffreyxu.kafka.producer.service;

import com.jeffreyxu.kafka.common.model.BaseEvent;
import com.jeffreyxu.kafka.common.model.UserEvent;
import com.jeffreyxu.kafka.common.model.BusinessEvent;
import com.jeffreyxu.kafka.common.model.SystemEvent;
import com.jeffreyxu.kafka.producer.entity.MessageLog;
import com.jeffreyxu.kafka.producer.repository.MessageLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * Service for producing messages to Kafka topics.
 * Handles message sending, logging, and statistics tracking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final MessageLogRepository messageLogRepository;
    private final StatsService statsService;

    // Topic names
    private static final String USER_EVENTS_TOPIC = "user-events";
    private static final String BUSINESS_EVENTS_TOPIC = "business-events";
    private static final String SYSTEM_EVENTS_TOPIC = "system-events";

    /**
     * Send a user event to Kafka
     */
    @Transactional
    public String sendUserEvent(UserEvent event) {
        return sendEvent(USER_EVENTS_TOPIC, event.getUserId(), event);
    }

    /**
     * Send a business event to Kafka
     */
    @Transactional
    public String sendBusinessEvent(BusinessEvent event) {
        return sendEvent(BUSINESS_EVENTS_TOPIC, event.getOrderId(), event);
    }

    /**
     * Send a system event to Kafka
     */
    @Transactional
    public String sendSystemEvent(SystemEvent event) {
        return sendEvent(SYSTEM_EVENTS_TOPIC, event.getServiceId(), event);
    }

    /**
     * Send multiple events in batch
     */
    @Transactional
    public List<String> sendBatch(List<BaseEvent> events) {
        List<String> messageIds = new ArrayList<>();
        
        for (BaseEvent event : events) {
            try {
                String messageId = null;
                
                if (event instanceof UserEvent) {
                    messageId = sendUserEvent((UserEvent) event);
                } else if (event instanceof BusinessEvent) {
                    messageId = sendBusinessEvent((BusinessEvent) event);
                } else if (event instanceof SystemEvent) {
                    messageId = sendSystemEvent((SystemEvent) event);
                } else {
                    log.warn("Unknown event type: {}", event.getClass().getSimpleName());
                    continue;
                }
                
                messageIds.add(messageId);
            } catch (Exception e) {
                log.error("Failed to send event in batch: {}", event.getId(), e);
                statsService.incrementErrorCount();
            }
        }
        
        return messageIds;
    }

    /**
     * Generic method to send any event to a topic
     */
    private String sendEvent(String topic, String key, BaseEvent event) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate event
            if (!event.isValid()) {
                throw new IllegalArgumentException("Invalid event: " + event.getDescription());
            }

            // Send to Kafka
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(topic, key, event);
            
            // Handle success/failure
            future.whenComplete((result, ex) -> {
                long processingTime = System.currentTimeMillis() - startTime;
                
                if (ex == null) {
                    // Success
                    logMessageSent(event, topic, key, result, processingTime, "SUCCESS", null);
                    statsService.incrementMessagesSent(topic);
                    statsService.updateAverageLatency(processingTime);
                    
                    log.info("Message sent successfully: {} to topic {} (partition: {}, offset: {})",
                        event.getId(), topic, result.getRecordMetadata().partition(), 
                        result.getRecordMetadata().offset());
                } else {
                    // Failure
                    logMessageSent(event, topic, key, null, processingTime, "FAILED", ex.getMessage());
                    statsService.incrementErrorCount();
                    
                    log.error("Failed to send message: {} to topic {}", event.getId(), topic, ex);
                }
            });

            return event.getId();
            
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            logMessageSent(event, topic, key, null, processingTime, "FAILED", e.getMessage());
            statsService.incrementErrorCount();
            
            log.error("Exception sending message: {} to topic {}", event.getId(), topic, e);
            throw new RuntimeException("Failed to send message", e);
        }
    }

    /**
     * Log message sending attempt to database
     */
    private void logMessageSent(BaseEvent event, String topic, String key, 
                               SendResult<String, Object> result, long processingTime,
                               String status, String errorMessage) {
        try {
            MessageLog messageLog = new MessageLog();
            messageLog.setMessageId(event.getId());
            messageLog.setTopic(topic);
            messageLog.setMessageKey(key);
            messageLog.setSentAt(LocalDateTime.now());
            messageLog.setStatus(status);
            messageLog.setErrorMessage(errorMessage);
            messageLog.setProcessingTimeMs(processingTime);
            
            if (result != null) {
                messageLog.setPartitionId(result.getRecordMetadata().partition());
                messageLog.setOffsetValue(result.getRecordMetadata().offset());
                messageLog.setMessageSize((int) result.getRecordMetadata().serializedValueSize());
            }
            
            messageLogRepository.save(messageLog);
            
        } catch (Exception e) {
            log.error("Failed to log message sending attempt", e);
        }
    }
}
