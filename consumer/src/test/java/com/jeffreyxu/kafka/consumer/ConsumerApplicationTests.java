package com.jeffreyxu.kafka.consumer;

import com.jeffreyxu.kafka.consumer.service.StatsService;
import com.jeffreyxu.kafka.consumer.service.MessageProcessingService;
import com.jeffreyxu.kafka.consumer.repository.ProcessedMessageRepository;
import com.jeffreyxu.kafka.consumer.repository.UserEventRepository;
import com.jeffreyxu.kafka.consumer.repository.BusinessEventRepository;
import com.jeffreyxu.kafka.consumer.repository.SystemEventRepository;
import com.jeffreyxu.kafka.consumer.controller.ConsumerController;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the Consumer application.
 * Verifies that all components are properly wired and functional.
 */
@SpringBootTest
@ActiveProfiles("test")
class ConsumerApplicationTests {

    @Autowired
    private StatsService statsService;

    @Autowired
    private MessageProcessingService messageProcessingService;

    @Autowired
    private ProcessedMessageRepository processedMessageRepository;

    @Autowired
    private UserEventRepository userEventRepository;

    @Autowired
    private BusinessEventRepository businessEventRepository;

    @Autowired
    private SystemEventRepository systemEventRepository;

    @Autowired
    private ConsumerController consumerController;

    /**
     * Test that the application context loads successfully
     */
    @Test
    void contextLoads() {
        // Verify all main components are loaded
        assertThat(statsService).isNotNull();
        assertThat(messageProcessingService).isNotNull();
        assertThat(processedMessageRepository).isNotNull();
        assertThat(userEventRepository).isNotNull();
        assertThat(businessEventRepository).isNotNull();
        assertThat(systemEventRepository).isNotNull();
        assertThat(consumerController).isNotNull();
    }

    /**
     * Test StatsService basic functionality
     */
    @Test
    void testStatsService() {
        // Test initial state
        assertThat(statsService.getTotalProcessedMessages()).isEqualTo(0);
        assertThat(statsService.getTotalProcessingErrors()).isEqualTo(0);
        assertThat(statsService.getSuccessRate()).isEqualTo(100.0);

        // Test incrementing counters
        statsService.incrementProcessedMessages("test-topic");
        assertThat(statsService.getTotalProcessedMessages()).isEqualTo(1);
        assertThat(statsService.getProcessedMessagesForTopic("test-topic")).isEqualTo(1);

        // Test processing time tracking
        statsService.updateAverageProcessingTime(100L);
        assertThat(statsService.getAverageProcessingTime()).isEqualTo(100.0);
        assertThat(statsService.getMaxProcessingTime()).isEqualTo(100L);
        assertThat(statsService.getMinProcessingTime()).isEqualTo(100L);

        // Test error tracking
        statsService.incrementProcessingErrors();
        assertThat(statsService.getTotalProcessingErrors()).isEqualTo(1);
        assertThat(statsService.getSuccessRate()).isEqualTo(50.0);
        assertThat(statsService.getErrorRate()).isEqualTo(50.0);

        // Test stats snapshot
        StatsService.StatsSnapshot snapshot = statsService.getStatsSnapshot();
        assertThat(snapshot).isNotNull();
        assertThat(snapshot.getTotalProcessedMessages()).isEqualTo(1);
        assertThat(snapshot.getTotalProcessingErrors()).isEqualTo(1);

        // Reset for clean state
        statsService.resetStats();
        assertThat(statsService.getTotalProcessedMessages()).isEqualTo(0);
        assertThat(statsService.getTotalProcessingErrors()).isEqualTo(0);
    }

    /**
     * Test repository beans are properly configured
     */
    @Test
    void testRepositories() {
        // Test that repositories are JPA repositories with expected methods
        assertThat(processedMessageRepository.count()).isGreaterThanOrEqualTo(0);
        assertThat(userEventRepository.count()).isGreaterThanOrEqualTo(0);
        assertThat(businessEventRepository.count()).isGreaterThanOrEqualTo(0);
        assertThat(systemEventRepository.count()).isGreaterThanOrEqualTo(0);
    }

    /**
     * Test controller health endpoint
     */
    @Test
    void testControllerHealth() {
        var healthResponse = consumerController.health();
        assertThat(healthResponse).isNotNull();
        assertThat(healthResponse.getStatusCode().is2xxSuccessful()).isTrue();
        
        var healthBody = healthResponse.getBody();
        assertThat(healthBody).isNotNull();
        assertThat(healthBody.get("status")).isEqualTo("UP");
        assertThat(healthBody.get("database")).isEqualTo("CONNECTED");
    }

    /**
     * Test controller stats endpoint
     */
    @Test
    void testControllerStats() {
        var statsResponse = consumerController.getStats();
        assertThat(statsResponse).isNotNull();
        assertThat(statsResponse.getStatusCode().is2xxSuccessful()).isTrue();
        
        var statsBody = statsResponse.getBody();
        assertThat(statsBody).isNotNull();
        assertThat(statsBody.getTotalProcessedMessages()).isGreaterThanOrEqualTo(0);
        assertThat(statsBody.getUptimeSeconds()).isGreaterThan(0);
    }
}
