package com.jeffreyxu.kafka.consumer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.Map;

/**
 * Service for tracking processing statistics and metrics.
 * Provides real-time metrics for monitoring and alerting.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {

    // Message processing counters
    private final Map<String, LongAdder> processedMessageCounters = new ConcurrentHashMap<>();
    private final LongAdder totalProcessedMessages = new LongAdder();
    private final LongAdder totalProcessingErrors = new LongAdder();
    
    // Processing time tracking
    private final LongAdder totalProcessingTime = new LongAdder();
    private final AtomicLong maxProcessingTime = new AtomicLong(0);
    private final AtomicLong minProcessingTime = new AtomicLong(Long.MAX_VALUE);
    
    // Service start time
    private final LocalDateTime serviceStartTime = LocalDateTime.now();
    
    // Error tracking
    private final Map<String, LongAdder> errorCounters = new ConcurrentHashMap<>();
    
    /**
     * Increment processed message count for a specific topic
     */
    public void incrementProcessedMessages(String topic) {
        processedMessageCounters.computeIfAbsent(topic, k -> new LongAdder()).increment();
        totalProcessedMessages.increment();
        
        log.debug("Incremented processed message count for topic: {}", topic);
    }
    
    /**
     * Increment processing error count
     */
    public void incrementProcessingErrors() {
        totalProcessingErrors.increment();
        log.debug("Incremented processing error count");
    }
    
    /**
     * Increment error count for a specific error type
     */
    public void incrementErrorCount(String errorType) {
        errorCounters.computeIfAbsent(errorType, k -> new LongAdder()).increment();
        log.debug("Incremented error count for type: {}", errorType);
    }
    
    /**
     * Update processing time statistics
     */
    public void updateAverageProcessingTime(long processingTimeMs) {
        totalProcessingTime.add(processingTimeMs);
        
        // Update max processing time
        maxProcessingTime.updateAndGet(current -> Math.max(current, processingTimeMs));
        
        // Update min processing time
        minProcessingTime.updateAndGet(current -> Math.min(current, processingTimeMs));
        
        log.debug("Updated processing time: {}ms", processingTimeMs);
    }
    
    /**
     * Get total processed messages count
     */
    public long getTotalProcessedMessages() {
        return totalProcessedMessages.sum();
    }
    
    /**
     * Get processed messages count for a specific topic
     */
    public long getProcessedMessagesForTopic(String topic) {
        LongAdder counter = processedMessageCounters.get(topic);
        return counter != null ? counter.sum() : 0;
    }
    
    /**
     * Get all topic processing counts
     */
    public Map<String, Long> getAllTopicCounts() {
        Map<String, Long> result = new ConcurrentHashMap<>();
        processedMessageCounters.forEach((topic, counter) -> 
            result.put(topic, counter.sum()));
        return result;
    }
    
    /**
     * Get total processing errors count
     */
    public long getTotalProcessingErrors() {
        return totalProcessingErrors.sum();
    }
    
    /**
     * Get error count for a specific error type
     */
    public long getErrorCountForType(String errorType) {
        LongAdder counter = errorCounters.get(errorType);
        return counter != null ? counter.sum() : 0;
    }
    
    /**
     * Get all error type counts
     */
    public Map<String, Long> getAllErrorCounts() {
        Map<String, Long> result = new ConcurrentHashMap<>();
        errorCounters.forEach((errorType, counter) -> 
            result.put(errorType, counter.sum()));
        return result;
    }
    
    /**
     * Get average processing time in milliseconds
     */
    public double getAverageProcessingTime() {
        long totalMessages = getTotalProcessedMessages();
        if (totalMessages == 0) {
            return 0.0;
        }
        return (double) totalProcessingTime.sum() / totalMessages;
    }
    
    /**
     * Get maximum processing time in milliseconds
     */
    public long getMaxProcessingTime() {
        long max = maxProcessingTime.get();
        return max == 0 ? 0 : max;
    }
    
    /**
     * Get minimum processing time in milliseconds
     */
    public long getMinProcessingTime() {
        long min = minProcessingTime.get();
        return min == Long.MAX_VALUE ? 0 : min;
    }
    
    /**
     * Get processing success rate as percentage
     */
    public double getSuccessRate() {
        long totalMessages = getTotalProcessedMessages();
        long totalErrors = getTotalProcessingErrors();
        
        if (totalMessages == 0) {
            return 100.0;
        }
        
        long successfulMessages = totalMessages - totalErrors;
        return (double) successfulMessages / totalMessages * 100.0;
    }
    
    /**
     * Get error rate as percentage
     */
    public double getErrorRate() {
        return 100.0 - getSuccessRate();
    }
    
    /**
     * Get service uptime in seconds
     */
    public long getUptimeSeconds() {
        return java.time.Duration.between(serviceStartTime, LocalDateTime.now()).getSeconds();
    }
    
    /**
     * Get messages per second rate
     */
    public double getMessagesPerSecond() {
        long uptimeSeconds = getUptimeSeconds();
        if (uptimeSeconds == 0) {
            return 0.0;
        }
        return (double) getTotalProcessedMessages() / uptimeSeconds;
    }
    
    /**
     * Get comprehensive statistics summary
     */
    public StatsSnapshot getStatsSnapshot() {
        return StatsSnapshot.builder()
            .totalProcessedMessages(getTotalProcessedMessages())
            .totalProcessingErrors(getTotalProcessingErrors())
            .successRate(getSuccessRate())
            .errorRate(getErrorRate())
            .averageProcessingTime(getAverageProcessingTime())
            .maxProcessingTime(getMaxProcessingTime())
            .minProcessingTime(getMinProcessingTime())
            .messagesPerSecond(getMessagesPerSecond())
            .uptimeSeconds(getUptimeSeconds())
            .serviceStartTime(serviceStartTime)
            .snapshotTime(LocalDateTime.now())
            .topicCounts(getAllTopicCounts())
            .errorCounts(getAllErrorCounts())
            .build();
    }
    
    /**
     * Reset all statistics (useful for testing or periodic resets)
     */
    public void resetStats() {
        processedMessageCounters.clear();
        errorCounters.clear();
        totalProcessedMessages.reset();
        totalProcessingErrors.reset();
        totalProcessingTime.reset();
        maxProcessingTime.set(0);
        minProcessingTime.set(Long.MAX_VALUE);
        
        log.info("All statistics have been reset");
    }
    
    /**
     * Log current statistics summary
     */
    public void logStatsSummary() {
        StatsSnapshot snapshot = getStatsSnapshot();
        
        log.info("=== Processing Statistics Summary ===");
        log.info("Total Messages Processed: {}", snapshot.getTotalProcessedMessages());
        log.info("Total Processing Errors: {}", snapshot.getTotalProcessingErrors());
        log.info("Success Rate: {:.2f}%", snapshot.getSuccessRate());
        log.info("Error Rate: {:.2f}%", snapshot.getErrorRate());
        log.info("Average Processing Time: {:.2f}ms", snapshot.getAverageProcessingTime());
        log.info("Max Processing Time: {}ms", snapshot.getMaxProcessingTime());
        log.info("Min Processing Time: {}ms", snapshot.getMinProcessingTime());
        log.info("Messages Per Second: {:.2f}", snapshot.getMessagesPerSecond());
        log.info("Service Uptime: {}s", snapshot.getUptimeSeconds());
        log.info("Topic Counts: {}", snapshot.getTopicCounts());
        log.info("Error Counts: {}", snapshot.getErrorCounts());
        log.info("=====================================");
    }
    
    /**
     * Statistics snapshot data class
     */
    @lombok.Data
    @lombok.Builder
    public static class StatsSnapshot {
        private long totalProcessedMessages;
        private long totalProcessingErrors;
        private double successRate;
        private double errorRate;
        private double averageProcessingTime;
        private long maxProcessingTime;
        private long minProcessingTime;
        private double messagesPerSecond;
        private long uptimeSeconds;
        private LocalDateTime serviceStartTime;
        private LocalDateTime snapshotTime;
        private Map<String, Long> topicCounts;
        private Map<String, Long> errorCounts;
    }
}
