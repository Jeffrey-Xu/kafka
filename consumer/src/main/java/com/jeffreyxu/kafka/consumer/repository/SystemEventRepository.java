package com.jeffreyxu.kafka.consumer.repository;

import com.jeffreyxu.kafka.consumer.entity.SystemEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for SystemEventEntity.
 * Provides data access methods for system monitoring, alerting, and operational analytics.
 */
@Repository
public interface SystemEventRepository extends JpaRepository<SystemEventEntity, Long> {

    /**
     * Find events by service ID
     */
    List<SystemEventEntity> findByServiceIdOrderByCreatedAtDesc(String serviceId);

    /**
     * Find events by event type
     */
    List<SystemEventEntity> findByEventTypeOrderByCreatedAtDesc(String eventType);

    /**
     * Find events by severity
     */
    List<SystemEventEntity> findBySeverityOrderByCreatedAtDesc(String severity);

    /**
     * Find events by component
     */
    List<SystemEventEntity> findByComponentOrderByCreatedAtDesc(String component);

    /**
     * Find events by environment
     */
    List<SystemEventEntity> findByEnvironmentOrderByCreatedAtDesc(String environment);

    /**
     * Find events by host ID
     */
    List<SystemEventEntity> findByHostIdOrderByCreatedAtDesc(String hostId);

    /**
     * Find events by process ID
     */
    List<SystemEventEntity> findByProcessIdOrderByCreatedAtDesc(String processId);

    /**
     * Find events within date range
     */
    List<SystemEventEntity> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find critical events (HIGH and CRITICAL severity)
     */
    @Query("SELECT s FROM SystemEventEntity s WHERE s.severity IN ('CRITICAL', 'HIGH') ORDER BY s.createdAt DESC")
    List<SystemEventEntity> findCriticalEvents();

    /**
     * Find critical events within date range
     */
    @Query("SELECT s FROM SystemEventEntity s WHERE s.severity IN ('CRITICAL', 'HIGH') AND s.createdAt >= :since ORDER BY s.createdAt DESC")
    List<SystemEventEntity> findCriticalEventsSince(@Param("since") LocalDateTime since);

    /**
     * Find events with stack traces (indicating errors)
     */
    @Query("SELECT s FROM SystemEventEntity s WHERE s.stackTrace IS NOT NULL ORDER BY s.createdAt DESC")
    List<SystemEventEntity> findEventsWithStackTrace();

    /**
     * Count events by service
     */
    @Query("SELECT COUNT(s) FROM SystemEventEntity s WHERE s.serviceId = :serviceId")
    long countByServiceId(@Param("serviceId") String serviceId);

    /**
     * Count events by severity
     */
    @Query("SELECT COUNT(s) FROM SystemEventEntity s WHERE s.severity = :severity")
    long countBySeverity(@Param("severity") String severity);

    /**
     * Count events by component
     */
    @Query("SELECT COUNT(s) FROM SystemEventEntity s WHERE s.component = :component")
    long countByComponent(@Param("component") String component);

    /**
     * Count critical events
     */
    @Query("SELECT COUNT(s) FROM SystemEventEntity s WHERE s.severity IN ('CRITICAL', 'HIGH')")
    long countCriticalEvents();

    /**
     * Get service health summary
     */
    @Query(value = """
        SELECT service_id,
               COUNT(*) as total_events,
               COUNT(CASE WHEN severity IN ('CRITICAL', 'HIGH') THEN 1 END) as critical_events,
               COUNT(CASE WHEN severity = 'MEDIUM' THEN 1 END) as medium_events,
               COUNT(CASE WHEN severity = 'LOW' THEN 1 END) as low_events,
               COUNT(CASE WHEN severity = 'INFO' THEN 1 END) as info_events,
               COUNT(DISTINCT component) as affected_components,
               COUNT(DISTINCT host_id) as affected_hosts,
               MIN(created_at) as first_event,
               MAX(created_at) as last_event
        FROM system_events
        WHERE created_at >= :since
        GROUP BY service_id
        ORDER BY critical_events DESC, total_events DESC
        """, nativeQuery = true)
    List<Object[]> getServiceHealthSummary(@Param("since") LocalDateTime since);

    /**
     * Get severity distribution
     */
    @Query(value = """
        SELECT severity,
               COUNT(*) as count,
               COUNT(DISTINCT service_id) as affected_services,
               COUNT(DISTINCT component) as affected_components
        FROM system_events
        WHERE created_at >= :since
        GROUP BY severity
        ORDER BY 
            CASE severity
                WHEN 'CRITICAL' THEN 1
                WHEN 'HIGH' THEN 2
                WHEN 'MEDIUM' THEN 3
                WHEN 'LOW' THEN 4
                WHEN 'INFO' THEN 5
                ELSE 6
            END
        """, nativeQuery = true)
    List<Object[]> getSeverityDistribution(@Param("since") LocalDateTime since);

    /**
     * Get component error analysis
     */
    @Query(value = """
        SELECT component,
               COUNT(*) as total_events,
               COUNT(CASE WHEN severity IN ('CRITICAL', 'HIGH') THEN 1 END) as critical_events,
               COUNT(CASE WHEN stack_trace IS NOT NULL THEN 1 END) as error_events,
               COUNT(DISTINCT service_id) as affected_services,
               COUNT(DISTINCT host_id) as affected_hosts,
               MAX(created_at) as last_event
        FROM system_events
        WHERE created_at >= :since
        AND component IS NOT NULL
        GROUP BY component
        ORDER BY critical_events DESC, total_events DESC
        """, nativeQuery = true)
    List<Object[]> getComponentErrorAnalysis(@Param("since") LocalDateTime since);

    /**
     * Get host performance summary
     */
    @Query(value = """
        SELECT host_id,
               COUNT(*) as total_events,
               COUNT(CASE WHEN severity IN ('CRITICAL', 'HIGH') THEN 1 END) as critical_events,
               COUNT(DISTINCT service_id) as services_count,
               COUNT(DISTINCT component) as components_count,
               COUNT(DISTINCT process_id) as processes_count,
               MIN(created_at) as first_event,
               MAX(created_at) as last_event
        FROM system_events
        WHERE created_at >= :since
        AND host_id IS NOT NULL
        GROUP BY host_id
        ORDER BY critical_events DESC, total_events DESC
        """, nativeQuery = true)
    List<Object[]> getHostPerformanceSummary(@Param("since") LocalDateTime since);

    /**
     * Get hourly event trends
     */
    @Query(value = """
        SELECT DATE_FORMAT(created_at, '%Y-%m-%d %H:00:00') as hour_bucket,
               COUNT(*) as total_events,
               COUNT(CASE WHEN severity IN ('CRITICAL', 'HIGH') THEN 1 END) as critical_events,
               COUNT(DISTINCT service_id) as unique_services,
               COUNT(DISTINCT host_id) as unique_hosts
        FROM system_events
        WHERE created_at >= :since
        GROUP BY DATE_FORMAT(created_at, '%Y-%m-%d %H:00:00')
        ORDER BY hour_bucket DESC
        """, nativeQuery = true)
    List<Object[]> getHourlyEventTrends(@Param("since") LocalDateTime since);

    /**
     * Get environment stability report
     */
    @Query(value = """
        SELECT environment,
               COUNT(*) as total_events,
               COUNT(CASE WHEN severity IN ('CRITICAL', 'HIGH') THEN 1 END) as critical_events,
               COUNT(CASE WHEN stack_trace IS NOT NULL THEN 1 END) as error_events,
               COUNT(DISTINCT service_id) as services_count,
               COUNT(DISTINCT host_id) as hosts_count,
               ROUND(
                   (COUNT(CASE WHEN severity IN ('CRITICAL', 'HIGH') THEN 1 END) * 100.0 / COUNT(*)), 2
               ) as critical_event_percentage
        FROM system_events
        WHERE created_at >= :since
        AND environment IS NOT NULL
        GROUP BY environment
        ORDER BY critical_event_percentage DESC, total_events DESC
        """, nativeQuery = true)
    List<Object[]> getEnvironmentStabilityReport(@Param("since") LocalDateTime since);

    /**
     * Find recent errors with stack traces
     */
    @Query(value = """
        SELECT * FROM system_events
        WHERE stack_trace IS NOT NULL
        AND created_at >= :since
        ORDER BY created_at DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<SystemEventEntity> getRecentErrorsWithStackTrace(@Param("since") LocalDateTime since, @Param("limit") int limit);

    /**
     * Find events by message pattern (for log analysis)
     */
    @Query("SELECT s FROM SystemEventEntity s WHERE s.message LIKE %:pattern% ORDER BY s.createdAt DESC")
    List<SystemEventEntity> findByMessageContaining(@Param("pattern") String pattern);

    /**
     * Get alert summary for monitoring dashboards
     */
    @Query(value = """
        SELECT 
            COUNT(*) as total_events,
            COUNT(CASE WHEN severity = 'CRITICAL' THEN 1 END) as critical_count,
            COUNT(CASE WHEN severity = 'HIGH' THEN 1 END) as high_count,
            COUNT(CASE WHEN severity = 'MEDIUM' THEN 1 END) as medium_count,
            COUNT(CASE WHEN severity = 'LOW' THEN 1 END) as low_count,
            COUNT(CASE WHEN severity = 'INFO' THEN 1 END) as info_count,
            COUNT(DISTINCT service_id) as affected_services,
            COUNT(DISTINCT host_id) as affected_hosts,
            COUNT(CASE WHEN stack_trace IS NOT NULL THEN 1 END) as error_count
        FROM system_events
        WHERE created_at >= :since
        """, nativeQuery = true)
    Object[] getAlertSummary(@Param("since") LocalDateTime since);
}
