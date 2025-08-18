package com.jeffreyxu.kafka.consumer.repository;

import com.jeffreyxu.kafka.consumer.entity.UserEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for UserEventEntity.
 * Provides data access methods for user activity analytics.
 */
@Repository
public interface UserEventRepository extends JpaRepository<UserEventEntity, Long> {

    /**
     * Find events by user ID
     */
    List<UserEventEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Find events by event type
     */
    List<UserEventEntity> findByEventTypeOrderByCreatedAtDesc(String eventType);

    /**
     * Find events by session ID
     */
    List<UserEventEntity> findBySessionIdOrderByCreatedAtDesc(String sessionId);

    /**
     * Count events by user
     */
    @Query("SELECT COUNT(u) FROM UserEventEntity u WHERE u.userId = :userId")
    long countByUserId(@Param("userId") String userId);

    /**
     * Count events by type
     */
    @Query("SELECT COUNT(u) FROM UserEventEntity u WHERE u.eventType = :eventType")
    long countByEventType(@Param("eventType") String eventType);

    /**
     * Get user activity summary
     */
    @Query(value = """
        SELECT user_id,
               COUNT(*) as total_events,
               COUNT(DISTINCT event_type) as unique_event_types,
               MIN(created_at) as first_activity,
               MAX(created_at) as last_activity,
               COUNT(DISTINCT session_id) as session_count
        FROM user_events
        WHERE created_at >= :since
        GROUP BY user_id
        ORDER BY total_events DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> getUserActivitySummary(@Param("since") LocalDateTime since, @Param("limit") int limit);

    /**
     * Get event type distribution
     */
    @Query(value = """
        SELECT event_type, COUNT(*) as count
        FROM user_events
        WHERE created_at >= :since
        GROUP BY event_type
        ORDER BY count DESC
        """, nativeQuery = true)
    List<Object[]> getEventTypeDistribution(@Param("since") LocalDateTime since);
}
