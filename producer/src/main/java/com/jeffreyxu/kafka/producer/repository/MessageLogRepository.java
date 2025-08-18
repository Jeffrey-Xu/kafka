package com.jeffreyxu.kafka.producer.repository;

import com.jeffreyxu.kafka.producer.entity.MessageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for MessageLog entities.
 * Provides data access methods for message logging and statistics.
 */
@Repository
public interface MessageLogRepository extends JpaRepository<MessageLog, Long> {

    /**
     * Find message log by message ID
     */
    Optional<MessageLog> findByMessageId(String messageId);

    /**
     * Find all messages for a specific topic
     */
    List<MessageLog> findByTopicOrderBySentAtDesc(String topic);

    /**
     * Find messages by status
     */
    List<MessageLog> findByStatusOrderBySentAtDesc(String status);

    /**
     * Count total messages sent
     */
    @Query("SELECT COUNT(m) FROM MessageLog m")
    long countTotalMessages();

    /**
     * Count messages by status
     */
    @Query("SELECT COUNT(m) FROM MessageLog m WHERE m.status = :status")
    long countByStatus(@Param("status") String status);

    /**
     * Count messages by topic
     */
    @Query("SELECT COUNT(m) FROM MessageLog m WHERE m.topic = :topic")
    long countByTopic(@Param("topic") String topic);

    /**
     * Get average processing time
     */
    @Query("SELECT AVG(m.processingTimeMs) FROM MessageLog m WHERE m.processingTimeMs IS NOT NULL")
    Double getAverageProcessingTime();

    /**
     * Get average processing time by topic
     */
    @Query("SELECT AVG(m.processingTimeMs) FROM MessageLog m WHERE m.topic = :topic AND m.processingTimeMs IS NOT NULL")
    Double getAverageProcessingTimeByTopic(@Param("topic") String topic);

    /**
     * Find recent messages (last N hours)
     */
    @Query("SELECT m FROM MessageLog m WHERE m.sentAt >= :since ORDER BY m.sentAt DESC")
    List<MessageLog> findRecentMessages(@Param("since") LocalDateTime since);

    /**
     * Get success rate percentage
     */
    @Query("SELECT (COUNT(CASE WHEN m.status = 'SUCCESS' THEN 1 END) * 100.0 / COUNT(m)) FROM MessageLog m")
    Double getSuccessRate();

    /**
     * Get hourly message counts for the last 24 hours
     */
    @Query(value = """
        SELECT DATE_FORMAT(sent_at, '%Y-%m-%d %H:00:00') as hour, 
               COUNT(*) as count,
               topic
        FROM message_log 
        WHERE sent_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
        GROUP BY DATE_FORMAT(sent_at, '%Y-%m-%d %H:00:00'), topic
        ORDER BY hour DESC
        """, nativeQuery = true)
    List<Object[]> getHourlyMessageCounts();

    /**
     * Delete old message logs (cleanup)
     */
    @Query("DELETE FROM MessageLog m WHERE m.sentAt < :cutoffDate")
    void deleteOldMessages(@Param("cutoffDate") LocalDateTime cutoffDate);
}
