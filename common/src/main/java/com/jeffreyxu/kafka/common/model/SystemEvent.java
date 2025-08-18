package com.jeffreyxu.kafka.common.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.Map;

/**
 * Represents system operational events.
 * Examples: health checks, alerts, performance metrics, etc.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemEvent extends BaseEvent {
    
    /**
     * Identifier of the service that generated this event
     */
    @NotBlank(message = "Service ID cannot be blank")
    private String serviceId;
    
    /**
     * Type of system event
     */
    @NotBlank(message = "Event type cannot be blank")
    @Pattern(regexp = "^(HEALTH_CHECK|ALERT|METRIC_UPDATE|SERVICE_START|SERVICE_STOP|ERROR|WARNING|INFO|DEBUG)$",
             message = "Invalid system event type")
    private String eventType;
    
    /**
     * Severity level of the event
     */
    @NotBlank(message = "Severity cannot be blank")
    @Pattern(regexp = "^(CRITICAL|HIGH|MEDIUM|LOW|INFO)$",
             message = "Invalid severity level")
    private String severity;
    
    /**
     * Event message or description
     */
    @NotBlank(message = "Message cannot be blank")
    private String message;
    
    /**
     * Component or module that generated the event
     */
    private String component;
    
    /**
     * Environment where the event occurred
     */
    @Pattern(regexp = "^(DEVELOPMENT|STAGING|PRODUCTION|TEST)$",
             message = "Invalid environment")
    private String environment;
    
    /**
     * Host or instance identifier
     */
    private String hostId;
    
    /**
     * Process or thread identifier
     */
    private String processId;
    
    /**
     * Additional system context and metadata
     */
    private Map<String, Object> metadata;
    
    /**
     * Stack trace if this is an error event
     */
    private String stackTrace;
    
    @Override
    public String getEventType() {
        return "SYSTEM_EVENT";
    }
    
    @Override
    public boolean isValid() {
        return serviceId != null && !serviceId.trim().isEmpty() &&
               eventType != null && !eventType.trim().isEmpty() &&
               severity != null && !severity.trim().isEmpty() &&
               message != null && !message.trim().isEmpty();
    }
    
    @Override
    public String getDescription() {
        return String.format("System event from %s: %s [%s] - %s",
                serviceId, eventType, severity, message);
    }
    
    /**
     * Check if this is a critical event that requires immediate attention
     */
    public boolean isCritical() {
        return "CRITICAL".equals(severity) || "HIGH".equals(severity);
    }
    
    /**
     * Check if this is an error event
     */
    public boolean isError() {
        return "ERROR".equals(eventType) || "CRITICAL".equals(severity);
    }
    
    /**
     * Builder pattern with fluent API for easy event creation
     */
    public static SystemEventBuilder builder() {
        return new SystemEventBuilder();
    }
    
    public static class SystemEventBuilder {
        private SystemEvent event = new SystemEvent();
        
        public SystemEventBuilder serviceId(String serviceId) {
            event.setServiceId(serviceId);
            return this;
        }
        
        public SystemEventBuilder eventType(String eventType) {
            event.setEventType(eventType);
            return this;
        }
        
        public SystemEventBuilder severity(String severity) {
            event.setSeverity(severity);
            return this;
        }
        
        public SystemEventBuilder message(String message) {
            event.setMessage(message);
            return this;
        }
        
        public SystemEventBuilder component(String component) {
            event.setComponent(component);
            return this;
        }
        
        public SystemEventBuilder environment(String environment) {
            event.setEnvironment(environment);
            return this;
        }
        
        public SystemEventBuilder hostId(String hostId) {
            event.setHostId(hostId);
            return this;
        }
        
        public SystemEventBuilder processId(String processId) {
            event.setProcessId(processId);
            return this;
        }
        
        public SystemEventBuilder metadata(Map<String, Object> metadata) {
            event.setMetadata(metadata);
            return this;
        }
        
        public SystemEventBuilder stackTrace(String stackTrace) {
            event.setStackTrace(stackTrace);
            return this;
        }
        
        public SystemEventBuilder source(String source) {
            event.setSource(source);
            return this;
        }
        
        public SystemEventBuilder correlationId(String correlationId) {
            event.setCorrelationId(correlationId);
            return this;
        }
        
        public SystemEvent build() {
            return event;
        }
    }
}
