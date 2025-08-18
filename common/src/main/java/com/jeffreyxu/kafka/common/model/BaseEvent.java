package com.jeffreyxu.kafka.common.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base class for all Kafka events in the system.
 * Provides common fields and JSON polymorphic type handling.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "eventType"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = UserEvent.class, name = "USER_EVENT"),
    @JsonSubTypes.Type(value = BusinessEvent.class, name = "BUSINESS_EVENT"),
    @JsonSubTypes.Type(value = SystemEvent.class, name = "SYSTEM_EVENT")
})
public abstract class BaseEvent {
    
    /**
     * Unique identifier for this event
     */
    @NotBlank(message = "Event ID cannot be blank")
    private String id = UUID.randomUUID().toString();
    
    /**
     * Timestamp when the event was created
     */
    @NotNull(message = "Timestamp cannot be null")
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * Source system or service that generated this event
     */
    @NotBlank(message = "Source cannot be blank")
    private String source;
    
    /**
     * Version of the event schema
     */
    @NotBlank(message = "Version cannot be blank")
    private String version = "1.0";
    
    /**
     * Correlation ID for tracing related events
     */
    private String correlationId;
    
    /**
     * Get the event type for polymorphic JSON handling
     */
    public abstract String getEventType();
    
    /**
     * Validate the event data
     */
    public abstract boolean isValid();
    
    /**
     * Get a human-readable description of the event
     */
    public abstract String getDescription();
}
