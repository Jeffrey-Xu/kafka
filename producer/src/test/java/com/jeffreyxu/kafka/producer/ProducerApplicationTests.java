package com.jeffreyxu.kafka.producer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic integration test for the Producer application.
 * Verifies that the application context loads successfully.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class ProducerApplicationTests {

    /**
     * Test that the application context loads successfully
     */
    @Test
    void contextLoads() {
        // This test will pass if the Spring context loads without errors
        // We're not testing individual components to avoid external dependencies
    }
}
