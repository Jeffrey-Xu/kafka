package com.jeffreyxu.kafka.common.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for event model classes.
 * Tests basic functionality without external dependencies.
 */
class EventModelTests {

    @Test
    void testUserEventCreation() {
        UserEvent event = new UserEvent();
        event.setUserId("user123");
        event.setAction("login");
        event.setTimestamp(LocalDateTime.now());
        
        assertThat(event.getUserId()).isEqualTo("user123");
        assertThat(event.getAction()).isEqualTo("login");
        assertThat(event.getTimestamp()).isNotNull();
        assertThat(event.getDescription()).contains("login");
    }

    @Test
    void testBusinessEventCreation() {
        BusinessEvent event = new BusinessEvent();
        event.setOrderId("order123");
        event.setCustomerId("customer456");
        event.setAmount(new BigDecimal("99.99"));
        event.setCurrency("USD");
        event.setTimestamp(LocalDateTime.now());
        
        assertThat(event.getOrderId()).isEqualTo("order123");
        assertThat(event.getCustomerId()).isEqualTo("customer456");
        assertThat(event.getAmount()).isEqualTo(new BigDecimal("99.99"));
        assertThat(event.getCurrency()).isEqualTo("USD");
        assertThat(event.getDescription()).contains("order123");
    }

    @Test
    void testSystemEventCreation() {
        SystemEvent event = new SystemEvent();
        event.setServiceId("service123");
        event.setEventType("error");
        event.setSeverity("HIGH");
        event.setMessage("Test error message");
        event.setTimestamp(LocalDateTime.now());
        
        assertThat(event.getServiceId()).isEqualTo("service123");
        assertThat(event.getEventType()).isEqualTo("error");
        assertThat(event.getSeverity()).isEqualTo("HIGH");
        assertThat(event.getMessage()).isEqualTo("Test error message");
        assertThat(event.isCritical()).isTrue();
        assertThat(event.getDescription()).contains("error");
    }
}
