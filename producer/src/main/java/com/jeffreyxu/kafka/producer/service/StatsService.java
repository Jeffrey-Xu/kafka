package com.jeffreyxu.kafka.producer.service;

import com.jeffreyxu.kafka.producer.repository.MessageLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service for tracking and providing producer statistics.
 * Integrates with Micrometer for metrics collection.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {

    private final MessageLogRepository messageLogRepository;
    private final MeterRegistry meterRegistry;

    // Metrics
    private Counter messagesSentCounter;
    private Counter errorsCounter;
    private Timer processingTimer;
    private final AtomicLong totalMessagesSent = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final AtomicReference<Double> averageLatency = new AtomicReference<>(0.0);

    @PostConstruct
    public void initMetrics() {
        // Initialize Micrometer metrics
        messagesSentCounter = Counter.builder("kafka.producer.messages.sent")
            .description("Total number of messages sent to Kafka")
            .register(meterRegistry);

        errorsCounter = Counter.builder("kafka.producer.errors")
            .description("Total number of errors while sending messages")
            .register(meterRegistry);

        processingTimer = Timer.builder("kafka.producer.processing.time")
            .description("Time taken to process and send messages")
            .register(meterRegistry);

        // Register gauges
        Gauge.builder("kafka.producer.messages.total")
            .description("Total messages sent")
            .register(meterRegistry, this, StatsService::getTotalMessagesSent);

        Gauge.builder("kafka.producer.success.rate")
            .description("Success rate percentage")
            .register(meterRegistry, this, StatsService::getSuccessRate);

        Gauge.builder("kafka.producer.average.latency")
            .description("Average processing latency in milliseconds")
            .register(meterRegistry, this, StatsService::getAverageLatencyValue);

        // Load initial stats from database
        loadInitialStats();
    }

    /**
     * Increment messages sent counter
     */
    public void incrementMessagesSent(String topic) {
        messagesSentCounter.increment();
        totalMessagesSent.incrementAndGet();
        
        // Topic-specific counter
        Counter.builder("kafka.producer.messages.sent.by.topic")
            .tag("topic", topic)
            .register(meterRegistry)
            .increment();
    }

    /**
     * Increment error counter
     */
    public void incrementErrorCount() {
        errorsCounter.increment();
        totalErrors.incrementAndGet();
    }

    /**
     * Update average latency
     */
    public void updateAverageLatency(long latencyMs) {
        processingTimer.record(latencyMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        
        // Update rolling average (simple implementation)
        double currentAvg = averageLatency.get();
        double newAvg = (currentAvg + latencyMs) / 2.0;
        averageLatency.set(newAvg);
    }

    /**
     * Get comprehensive producer statistics
     */
    public Map<String, Object> getProducerStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Basic counts
            long totalMessages = messageLogRepository.countTotalMessages();
            long successfulMessages = messageLogRepository.countByStatus("SUCCESS");
            long failedMessages = messageLogRepository.countByStatus("FAILED");
            
            // Calculate rates
            double successRate = totalMessages > 0 ? (successfulMessages * 100.0 / totalMessages) : 0.0;
            Double avgProcessingTime = messageLogRepository.getAverageProcessingTime();
            
            // Topic-specific stats
            Map<String, Long> topicStats = new HashMap<>();
            topicStats.put("user-events", messageLogRepository.countByTopic("user-events"));
            topicStats.put("business-events", messageLogRepository.countByTopic("business-events"));
            topicStats.put("system-events", messageLogRepository.countByTopic("system-events"));
            
            // Recent activity (last hour)
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            long recentMessages = messageLogRepository.findRecentMessages(oneHourAgo).size();
            
            // Build response
            stats.put("totalMessages", totalMessages);
            stats.put("successfulMessages", successfulMessages);
            stats.put("failedMessages", failedMessages);
            stats.put("successRate", Math.round(successRate * 100.0) / 100.0);
            stats.put("averageProcessingTimeMs", avgProcessingTime != null ? Math.round(avgProcessingTime * 100.0) / 100.0 : 0.0);
            stats.put("messagesLastHour", recentMessages);
            stats.put("topicBreakdown", topicStats);
            stats.put("timestamp", LocalDateTime.now());
            
            // Runtime metrics
            stats.put("runtimeStats", getRuntimeStats());
            
        } catch (Exception e) {
            log.error("Error getting producer stats", e);
            stats.put("error", "Failed to retrieve statistics: " + e.getMessage());
        }
        
        return stats;
    }

    /**
     * Get runtime statistics
     */
    private Map<String, Object> getRuntimeStats() {
        Map<String, Object> runtime = new HashMap<>();
        
        runtime.put("totalMessagesSent", totalMessagesSent.get());
        runtime.put("totalErrors", totalErrors.get());
        runtime.put("averageLatency", Math.round(averageLatency.get() * 100.0) / 100.0);
        runtime.put("uptime", getUptimeSeconds());
        
        return runtime;
    }

    /**
     * Load initial statistics from database
     */
    private void loadInitialStats() {
        try {
            long totalMessages = messageLogRepository.countTotalMessages();
            long errors = messageLogRepository.countByStatus("FAILED");
            Double avgLatency = messageLogRepository.getAverageProcessingTime();
            
            totalMessagesSent.set(totalMessages);
            totalErrors.set(errors);
            if (avgLatency != null) {
                averageLatency.set(avgLatency);
            }
            
            log.info("Loaded initial stats: {} messages, {} errors, {}ms avg latency", 
                totalMessages, errors, avgLatency);
                
        } catch (Exception e) {
            log.warn("Could not load initial stats from database", e);
        }
    }

    // Getter methods for Micrometer gauges
    private double getTotalMessagesSent() {
        return totalMessagesSent.get();
    }

    private double getSuccessRate() {
        long total = totalMessagesSent.get();
        long errors = totalErrors.get();
        return total > 0 ? ((total - errors) * 100.0 / total) : 100.0;
    }

    private double getAverageLatencyValue() {
        return averageLatency.get();
    }

    private long getUptimeSeconds() {
        return java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
    }
}
