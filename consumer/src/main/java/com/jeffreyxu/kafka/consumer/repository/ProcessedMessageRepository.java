package com.jeffreyxu.kafka.consumer.repository;

import com.jeffreyxu.kafka.consumer.entity.ProcessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ProcessedMessage entities.
 * Provides data access methods for processed message tracking and analytics.
 */
@Repository
public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, Long> {

    /**
     * Find processed message by message ID
     */
    Optional<ProcessedMessage> findByMessageId(String messageId);

    /**
     * Find all processed messages for a specific topic
     */
    List<ProcessedMessage> findByTopicOrderByProcessedAtDesc(String topic);

    /**
     * Find processed messages by event type
     */
    List<ProcessedMessage> findByEventTypeOrderByProcessedAtDesc(String eventType);

    /**
     * Find processed messages by status
     */
    List<ProcessedMessage> findByStatusOrderByProcessedAtDesc(String status);
    
    /**
     * Find processed messages by status with pagination
     */
    List<ProcessedMessage> findByStatusOrderByProcessedAtDesc(String status, org.springframework.data.domain.Pageable pageable);

    /**
     * Count total processed messages
     */
    @Query("SELECT COUNT(p) FROM ProcessedMessage p")
    long countTotalProcessed();

    /**
     * Count processed messages by status
     */
    @Query("SELECT COUNT(p) FROM ProcessedMessage p WHERE p.status = :status")
    long countByStatus(@Param("status") String status);

    /**
     * Count processed messages by event type
     */
    @Query("SELECT COUNT(p) FROM ProcessedMessage p WHERE p.eventType = :eventType")
    long countByEventType(@Param("eventType") String eventType);

    /**
     * Count processed messages by topic
     */
    @Query("SELECT COUNT(p) FROM ProcessedMessage p WHERE p.topic = :topic")
    long countByTopic(@Param("topic") String topic);

    /**
     * Get average processing time
     */
    @Query("SELECT AVG(p.processingTimeMs) FROM ProcessedMessage p WHERE p.processingTimeMs IS NOT NULL")
    Double getAverageProcessingTime();

    /**
     * Get average processing time by event type
     */
    @Query("SELECT AVG(p.processingTimeMs) FROM ProcessedMessage p WHERE p.eventType = :eventType AND p.processingTimeMs IS NOT NULL")
    Double getAverageProcessingTimeByEventType(@Param("eventType") String eventType);

    /**
     * Find recent processed messages (last N hours)
     */
    @Query("SELECT p FROM ProcessedMessage p WHERE p.processedAt >= :since ORDER BY p.processedAt DESC")
    List<ProcessedMessage> findRecentMessages(@Param("since") LocalDateTime since);

    /**
     * Get success rate percentage
     */
    @Query("SELECT (COUNT(CASE WHEN p.status = 'SUCCESS' THEN 1 END) * 100.0 / COUNT(p)) FROM ProcessedMessage p")
    Double getSuccessRate();

    /**
     * Get processing statistics by topic and time window
     */
    @Query(value = """
        SELECT topic,
               event_type,
               COUNT(*) as message_count,
               AVG(processing_time_ms) as avg_processing_time,
               MAX(processing_time_ms) as max_processing_time,
               SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) as success_count,
               SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) as error_count
        FROM processed_messages 
        WHERE processed_at >= :since
        GROUP BY topic, event_type
        ORDER BY message_count DESC
        """, nativeQuery = true)
    List<Object[]> getProcessingStatsByTopic(@Param("since") LocalDateTime since);

    /**
     * Count processed messages after a specific date
     */
    @Query("SELECT COUNT(p) FROM ProcessedMessage p WHERE p.processedAt >= :since")
    long countByProcessedAtAfter(@Param("since") LocalDateTime since);

    /**
     * Delete old processed messages (cleanup)
     */
    @Query("DELETE FROM ProcessedMessage p WHERE p.processedAt < :cutoffDate")
    void deleteOldMessages(@Param("cutoffDate") LocalDateTime cutoffDate);
}
