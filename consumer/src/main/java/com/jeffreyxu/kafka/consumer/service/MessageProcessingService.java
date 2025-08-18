package com.jeffreyxu.kafka.consumer.service;

import com.jeffreyxu.kafka.common.model.UserEvent;
import com.jeffreyxu.kafka.common.model.BusinessEvent;
import com.jeffreyxu.kafka.common.model.SystemEvent;
import com.jeffreyxu.kafka.consumer.entity.ProcessedMessage;
import com.jeffreyxu.kafka.consumer.entity.UserEventEntity;
import com.jeffreyxu.kafka.consumer.entity.BusinessEventEntity;
import com.jeffreyxu.kafka.consumer.entity.SystemEventEntity;
import com.jeffreyxu.kafka.consumer.repository.ProcessedMessageRepository;
import com.jeffreyxu.kafka.consumer.repository.UserEventRepository;
import com.jeffreyxu.kafka.consumer.repository.BusinessEventRepository;
import com.jeffreyxu.kafka.consumer.repository.SystemEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

/**
 * Service for processing Kafka messages and storing them in the database.
 * Handles different event types and maintains processing audit trail.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageProcessingService {

    private final ProcessedMessageRepository processedMessageRepository;
    private final UserEventRepository userEventRepository;
    private final BusinessEventRepository businessEventRepository;
    private final SystemEventRepository systemEventRepository;
    private final ObjectMapper objectMapper;
    private final StatsService statsService;

    /**
     * Process a user event
     */
    @Transactional
    public void processUserEvent(UserEvent event, String topic, int partition, long offset, String key) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Create processed message record
            ProcessedMessage processedMessage = createProcessedMessage(
                event.getId(), topic, partition, offset, key, "USER_EVENT", event);
            
            // Create user event entity
            UserEventEntity userEventEntity = new UserEventEntity();
            userEventEntity.setUserId(event.getUserId());
            userEventEntity.setEventType(event.getAction());
            userEventEntity.setSessionId(event.getSessionId());
            userEventEntity.setIpAddress(event.getIpAddress());
            userEventEntity.setUserAgent(event.getUserAgent());
            userEventEntity.setLocation(event.getLocation());
            userEventEntity.setDeviceType(event.getDeviceType());
            userEventEntity.setCreatedAt(event.getTimestamp());
            userEventEntity.setProcessedAt(LocalDateTime.now());
            
            if (event.getMetadata() != null) {
                userEventEntity.setMetadata(objectMapper.writeValueAsString(event.getMetadata()));
            }
            
            // Save both records
            processedMessageRepository.save(processedMessage);
            userEventRepository.save(userEventEntity);
            
            // Update processing time
            long processingTime = System.currentTimeMillis() - startTime;
            processedMessage.setProcessingTimeMs(processingTime);
            processedMessageRepository.save(processedMessage);
            
            // Update statistics
            statsService.incrementProcessedMessages("user-events");
            statsService.updateAverageProcessingTime(processingTime);
            
            log.debug("User event processed and stored: {}", event.getId());
            
        } catch (Exception e) {
            log.error("Failed to process user event: {}", event.getId(), e);
            
            // Create failed processing record
            createFailedProcessingRecord(event.getId(), topic, partition, offset, key, 
                "USER_EVENT", event, e.getMessage());
            
            statsService.incrementProcessingErrors();
            throw new RuntimeException("Failed to process user event", e);
        }
    }

    /**
     * Process a business event
     */
    @Transactional
    public void processBusinessEvent(BusinessEvent event, String topic, int partition, long offset, String key) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Create processed message record
            ProcessedMessage processedMessage = createProcessedMessage(
                event.getId(), topic, partition, offset, key, "BUSINESS_EVENT", event);
            
            // Create business event entity
            BusinessEventEntity businessEventEntity = new BusinessEventEntity();
            businessEventEntity.setOrderId(event.getOrderId());
            businessEventEntity.setCustomerId(event.getCustomerId());
            businessEventEntity.setEventType(event.getEventType());
            businessEventEntity.setAmount(event.getAmount());
            businessEventEntity.setCurrency(event.getCurrency());
            businessEventEntity.setPaymentMethod(event.getPaymentMethod());
            businessEventEntity.setShippingAddress(event.getShippingAddress());
            businessEventEntity.setBillingAddress(event.getBillingAddress());
            businessEventEntity.setOrderStatus(event.getOrderStatus());
            businessEventEntity.setCreatedAt(event.getTimestamp());
            businessEventEntity.setProcessedAt(LocalDateTime.now());
            
            if (event.getOrderDetails() != null) {
                businessEventEntity.setOrderDetails(objectMapper.writeValueAsString(event.getOrderDetails()));
            }
            
            // Save both records
            processedMessageRepository.save(processedMessage);
            businessEventRepository.save(businessEventEntity);
            
            // Update processing time
            long processingTime = System.currentTimeMillis() - startTime;
            processedMessage.setProcessingTimeMs(processingTime);
            processedMessageRepository.save(processedMessage);
            
            // Update statistics
            statsService.incrementProcessedMessages("business-events");
            statsService.updateAverageProcessingTime(processingTime);
            
            log.debug("Business event processed and stored: {}", event.getId());
            
        } catch (Exception e) {
            log.error("Failed to process business event: {}", event.getId(), e);
            
            // Create failed processing record
            createFailedProcessingRecord(event.getId(), topic, partition, offset, key, 
                "BUSINESS_EVENT", event, e.getMessage());
            
            statsService.incrementProcessingErrors();
            throw new RuntimeException("Failed to process business event", e);
        }
    }

    /**
     * Process a system event
     */
    @Transactional
    public void processSystemEvent(SystemEvent event, String topic, int partition, long offset, String key) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Create processed message record
            ProcessedMessage processedMessage = createProcessedMessage(
                event.getId(), topic, partition, offset, key, "SYSTEM_EVENT", event);
            
            // Create system event entity
            SystemEventEntity systemEventEntity = new SystemEventEntity();
            systemEventEntity.setServiceId(event.getServiceId());
            systemEventEntity.setEventType(event.getEventType());
            systemEventEntity.setSeverity(event.getSeverity());
            systemEventEntity.setMessage(event.getMessage());
            systemEventEntity.setComponent(event.getComponent());
            systemEventEntity.setEnvironment(event.getEnvironment());
            systemEventEntity.setHostId(event.getHostId());
            systemEventEntity.setProcessId(event.getProcessId());
            systemEventEntity.setStackTrace(event.getStackTrace());
            systemEventEntity.setCreatedAt(event.getTimestamp());
            systemEventEntity.setProcessedAt(LocalDateTime.now());
            
            if (event.getMetadata() != null) {
                systemEventEntity.setMetadata(objectMapper.writeValueAsString(event.getMetadata()));
            }
            
            // Save both records
            processedMessageRepository.save(processedMessage);
            systemEventRepository.save(systemEventEntity);
            
            // Update processing time
            long processingTime = System.currentTimeMillis() - startTime;
            processedMessage.setProcessingTimeMs(processingTime);
            processedMessageRepository.save(processedMessage);
            
            // Update statistics
            statsService.incrementProcessedMessages("system-events");
            statsService.updateAverageProcessingTime(processingTime);
            
            log.debug("System event processed and stored: {}", event.getId());
            
        } catch (Exception e) {
            log.error("Failed to process system event: {}", event.getId(), e);
            
            // Create failed processing record
            createFailedProcessingRecord(event.getId(), topic, partition, offset, key, 
                "SYSTEM_EVENT", event, e.getMessage());
            
            statsService.incrementProcessingErrors();
            throw new RuntimeException("Failed to process system event", e);
        }
    }

    /**
     * Create a processed message record
     */
    private ProcessedMessage createProcessedMessage(String messageId, String topic, int partition, 
                                                  long offset, String key, String eventType, Object event) {
        try {
            ProcessedMessage processedMessage = new ProcessedMessage();
            processedMessage.setMessageId(messageId);
            processedMessage.setTopic(topic);
            processedMessage.setPartitionId(partition);
            processedMessage.setOffsetValue(offset);
            processedMessage.setMessageKey(key);
            processedMessage.setEventType(eventType);
            processedMessage.setPayload(objectMapper.writeValueAsString(event));
            processedMessage.setProcessedAt(LocalDateTime.now());
            processedMessage.setStatus("SUCCESS");
            
            return processedMessage;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create processed message record", e);
        }
    }

    /**
     * Create a failed processing record
     */
    private void createFailedProcessingRecord(String messageId, String topic, int partition, 
                                            long offset, String key, String eventType, 
                                            Object event, String errorMessage) {
        try {
            ProcessedMessage processedMessage = new ProcessedMessage();
            processedMessage.setMessageId(messageId);
            processedMessage.setTopic(topic);
            processedMessage.setPartitionId(partition);
            processedMessage.setOffsetValue(offset);
            processedMessage.setMessageKey(key);
            processedMessage.setEventType(eventType);
            processedMessage.setPayload(objectMapper.writeValueAsString(event));
            processedMessage.setProcessedAt(LocalDateTime.now());
            processedMessage.setStatus("FAILED");
            processedMessage.setErrorMessage(errorMessage);
            
            processedMessageRepository.save(processedMessage);
        } catch (Exception e) {
            log.error("Failed to create failed processing record", e);
        }
    }
}
