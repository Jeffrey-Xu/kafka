package com.jeffreyxu.kafka.producer.controller;

import com.jeffreyxu.kafka.common.model.BaseEvent;
import com.jeffreyxu.kafka.common.model.UserEvent;
import com.jeffreyxu.kafka.common.model.BusinessEvent;
import com.jeffreyxu.kafka.common.model.SystemEvent;
import com.jeffreyxu.kafka.producer.service.MessageProducerService;
import com.jeffreyxu.kafka.producer.service.StatsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * REST controller for sending messages to Kafka topics.
 * Provides endpoints for different types of events and batch operations.
 */
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Slf4j
@Validated
public class MessageController {

    private final MessageProducerService messageProducerService;
    private final StatsService statsService;

    /**
     * Send a single user event to Kafka
     */
    @PostMapping("/user")
    public ResponseEntity<Map<String, Object>> sendUserEvent(@Valid @RequestBody UserEvent event) {
        log.info("Received user event: {}", event.getDescription());
        
        try {
            String messageId = messageProducerService.sendUserEvent(event);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("messageId", messageId);
            response.put("eventType", "USER_EVENT");
            response.put("description", event.getDescription());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to send user event", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Send a single business event to Kafka
     */
    @PostMapping("/business")
    public ResponseEntity<Map<String, Object>> sendBusinessEvent(@Valid @RequestBody BusinessEvent event) {
        log.info("Received business event: {}", event.getDescription());
        
        try {
            String messageId = messageProducerService.sendBusinessEvent(event);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("messageId", messageId);
            response.put("eventType", "BUSINESS_EVENT");
            response.put("description", event.getDescription());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to send business event", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Send a single system event to Kafka
     */
    @PostMapping("/system")
    public ResponseEntity<Map<String, Object>> sendSystemEvent(@Valid @RequestBody SystemEvent event) {
        log.info("Received system event: {}", event.getDescription());
        
        try {
            String messageId = messageProducerService.sendSystemEvent(event);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("messageId", messageId);
            response.put("eventType", "SYSTEM_EVENT");
            response.put("description", event.getDescription());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to send system event", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Send multiple events in batch
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> sendBatch(@Valid @RequestBody List<BaseEvent> events) {
        log.info("Received batch of {} events", events.size());
        
        try {
            List<String> messageIds = messageProducerService.sendBatch(events);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("messageIds", messageIds);
            response.put("totalSent", messageIds.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to send batch events", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get producer statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            Map<String, Object> stats = statsService.getProducerStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Failed to get producer stats", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "kafka-producer");
        health.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return ResponseEntity.ok(health);
    }
}
