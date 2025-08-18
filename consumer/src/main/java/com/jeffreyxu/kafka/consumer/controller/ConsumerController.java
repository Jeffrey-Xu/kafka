package com.jeffreyxu.kafka.consumer.controller;

import com.jeffreyxu.kafka.consumer.service.StatsService;
import com.jeffreyxu.kafka.consumer.repository.ProcessedMessageRepository;
import com.jeffreyxu.kafka.consumer.repository.UserEventRepository;
import com.jeffreyxu.kafka.consumer.repository.BusinessEventRepository;
import com.jeffreyxu.kafka.consumer.repository.SystemEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for consumer service monitoring and statistics.
 * Provides endpoints for health checks, statistics, and operational data.
 */
@RestController
@RequestMapping("/api/consumer")
@RequiredArgsConstructor
@Slf4j
public class ConsumerController {

    private final StatsService statsService;
    private final ProcessedMessageRepository processedMessageRepository;
    private final UserEventRepository userEventRepository;
    private final BusinessEventRepository businessEventRepository;
    private final SystemEventRepository systemEventRepository;

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Check database connectivity by counting records
            long processedMessages = processedMessageRepository.count();
            long userEvents = userEventRepository.count();
            long businessEvents = businessEventRepository.count();
            long systemEvents = systemEventRepository.count();
            
            health.put("status", "UP");
            health.put("timestamp", LocalDateTime.now());
            health.put("database", "CONNECTED");
            health.put("processedMessages", processedMessages);
            health.put("userEvents", userEvents);
            health.put("businessEvents", businessEvents);
            health.put("systemEvents", systemEvents);
            health.put("uptime", statsService.getUptimeSeconds());
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            log.error("Health check failed", e);
            
            health.put("status", "DOWN");
            health.put("timestamp", LocalDateTime.now());
            health.put("database", "DISCONNECTED");
            health.put("error", e.getMessage());
            
            return ResponseEntity.status(503).body(health);
        }
    }

    /**
     * Get processing statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<StatsService.StatsSnapshot> getStats() {
        try {
            StatsService.StatsSnapshot stats = statsService.getStatsSnapshot();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Failed to get statistics", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get processing statistics summary
     */
    @GetMapping("/stats/summary")
    public ResponseEntity<Map<String, Object>> getStatsSummary() {
        try {
            Map<String, Object> summary = new HashMap<>();
            
            summary.put("totalProcessedMessages", statsService.getTotalProcessedMessages());
            summary.put("totalProcessingErrors", statsService.getTotalProcessingErrors());
            summary.put("successRate", String.format("%.2f%%", statsService.getSuccessRate()));
            summary.put("errorRate", String.format("%.2f%%", statsService.getErrorRate()));
            summary.put("averageProcessingTime", String.format("%.2fms", statsService.getAverageProcessingTime()));
            summary.put("messagesPerSecond", String.format("%.2f", statsService.getMessagesPerSecond()));
            summary.put("uptimeSeconds", statsService.getUptimeSeconds());
            
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Failed to get statistics summary", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get topic processing counts
     */
    @GetMapping("/stats/topics")
    public ResponseEntity<Map<String, Long>> getTopicStats() {
        try {
            Map<String, Long> topicCounts = statsService.getAllTopicCounts();
            return ResponseEntity.ok(topicCounts);
        } catch (Exception e) {
            log.error("Failed to get topic statistics", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get error statistics
     */
    @GetMapping("/stats/errors")
    public ResponseEntity<Map<String, Long>> getErrorStats() {
        try {
            Map<String, Long> errorCounts = statsService.getAllErrorCounts();
            return ResponseEntity.ok(errorCounts);
        } catch (Exception e) {
            log.error("Failed to get error statistics", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get database record counts
     */
    @GetMapping("/stats/database")
    public ResponseEntity<Map<String, Object>> getDatabaseStats() {
        try {
            Map<String, Object> dbStats = new HashMap<>();
            
            dbStats.put("processedMessages", processedMessageRepository.count());
            dbStats.put("userEvents", userEventRepository.count());
            dbStats.put("businessEvents", businessEventRepository.count());
            dbStats.put("systemEvents", systemEventRepository.count());
            
            // Get recent activity (last 24 hours)
            LocalDateTime since = LocalDateTime.now().minusHours(24);
            dbStats.put("recentProcessedMessages", processedMessageRepository.countByProcessedAtAfter(since));
            
            return ResponseEntity.ok(dbStats);
        } catch (Exception e) {
            log.error("Failed to get database statistics", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get user event analytics
     */
    @GetMapping("/analytics/users")
    public ResponseEntity<Map<String, Object>> getUserAnalytics(
            @RequestParam(defaultValue = "24") int hours,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            LocalDateTime since = LocalDateTime.now().minusHours(hours);
            
            Map<String, Object> analytics = new HashMap<>();
            analytics.put("activitySummary", userEventRepository.getUserActivitySummary(since, limit));
            analytics.put("eventTypeDistribution", userEventRepository.getEventTypeDistribution(since));
            analytics.put("totalUserEvents", userEventRepository.count());
            
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Failed to get user analytics", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get business event analytics
     */
    @GetMapping("/analytics/business")
    public ResponseEntity<Map<String, Object>> getBusinessAnalytics(
            @RequestParam(defaultValue = "24") int hours,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            LocalDateTime since = LocalDateTime.now().minusHours(hours);
            
            Map<String, Object> analytics = new HashMap<>();
            analytics.put("customerSummary", businessEventRepository.getCustomerTransactionSummary(since, limit));
            analytics.put("orderStatusDistribution", businessEventRepository.getOrderStatusDistribution(since));
            analytics.put("paymentMethodAnalytics", businessEventRepository.getPaymentMethodAnalytics(since));
            analytics.put("dailyRevenue", businessEventRepository.getDailyRevenueSummary(since));
            analytics.put("topCustomers", businessEventRepository.getTopCustomersByRevenue(since, limit));
            analytics.put("totalBusinessEvents", businessEventRepository.count());
            
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Failed to get business analytics", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get system event analytics
     */
    @GetMapping("/analytics/system")
    public ResponseEntity<Map<String, Object>> getSystemAnalytics(
            @RequestParam(defaultValue = "24") int hours) {
        try {
            LocalDateTime since = LocalDateTime.now().minusHours(hours);
            
            Map<String, Object> analytics = new HashMap<>();
            analytics.put("serviceHealthSummary", systemEventRepository.getServiceHealthSummary(since));
            analytics.put("severityDistribution", systemEventRepository.getSeverityDistribution(since));
            analytics.put("componentErrorAnalysis", systemEventRepository.getComponentErrorAnalysis(since));
            analytics.put("hostPerformanceSummary", systemEventRepository.getHostPerformanceSummary(since));
            analytics.put("environmentStabilityReport", systemEventRepository.getEnvironmentStabilityReport(since));
            analytics.put("alertSummary", systemEventRepository.getAlertSummary(since));
            analytics.put("totalSystemEvents", systemEventRepository.count());
            analytics.put("criticalEventsCount", systemEventRepository.countCriticalEvents());
            
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Failed to get system analytics", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Reset statistics (for testing purposes)
     */
    @PostMapping("/stats/reset")
    public ResponseEntity<Map<String, String>> resetStats() {
        try {
            statsService.resetStats();
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Statistics have been reset successfully");
            response.put("timestamp", LocalDateTime.now().toString());
            
            log.info("Statistics reset via API call");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to reset statistics", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Log current statistics to console
     */
    @PostMapping("/stats/log")
    public ResponseEntity<Map<String, String>> logStats() {
        try {
            statsService.logStatsSummary();
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Statistics logged to console successfully");
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to log statistics", e);
            return ResponseEntity.status(500).build();
        }
    }
}
