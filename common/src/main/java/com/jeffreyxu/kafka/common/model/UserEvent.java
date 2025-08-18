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
 * Represents user activity events in the system.
 * Examples: login, logout, page view, search, etc.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEvent extends BaseEvent {
    
    /**
     * Unique identifier for the user
     */
    @NotBlank(message = "User ID cannot be blank")
    private String userId;
    
    /**
     * Type of user event (LOGIN, LOGOUT, PAGE_VIEW, SEARCH, etc.)
     */
    @NotBlank(message = "Action cannot be blank")
    @Pattern(regexp = "^(LOGIN|LOGOUT|PAGE_VIEW|SEARCH|CLICK|PURCHASE|BROWSE|REGISTER|UPDATE_PROFILE)$",
             message = "Invalid user action")
    private String action;
    
    /**
     * User session identifier
     */
    private String sessionId;
    
    /**
     * User's IP address
     */
    @Pattern(regexp = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$|^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$",
             message = "Invalid IP address format")
    private String ipAddress;
    
    /**
     * Browser user agent string
     */
    private String userAgent;
    
    /**
     * Geographic location information
     */
    private String location;
    
    /**
     * Device information
     */
    private String deviceType;
    
    /**
     * Additional metadata specific to the user event
     */
    private Map<String, Object> metadata;
    
    @Override
    public String getEventType() {
        return "USER_EVENT";
    }
    
    @Override
    public boolean isValid() {
        return userId != null && !userId.trim().isEmpty() &&
               action != null && !action.trim().isEmpty();
    }
    
    @Override
    public String getDescription() {
        return String.format("User %s performed action %s", userId, action);
    }
    
    /**
     * Builder pattern with fluent API for easy event creation
     */
    public static UserEventBuilder builder() {
        return new UserEventBuilder();
    }
    
    public static class UserEventBuilder {
        private UserEvent event = new UserEvent();
        
        public UserEventBuilder userId(String userId) {
            event.setUserId(userId);
            return this;
        }
        
        public UserEventBuilder action(String action) {
            event.setAction(action);
            return this;
        }
        
        public UserEventBuilder sessionId(String sessionId) {
            event.setSessionId(sessionId);
            return this;
        }
        
        public UserEventBuilder ipAddress(String ipAddress) {
            event.setIpAddress(ipAddress);
            return this;
        }
        
        public UserEventBuilder userAgent(String userAgent) {
            event.setUserAgent(userAgent);
            return this;
        }
        
        public UserEventBuilder location(String location) {
            event.setLocation(location);
            return this;
        }
        
        public UserEventBuilder deviceType(String deviceType) {
            event.setDeviceType(deviceType);
            return this;
        }
        
        public UserEventBuilder metadata(Map<String, Object> metadata) {
            event.setMetadata(metadata);
            return this;
        }
        
        public UserEventBuilder source(String source) {
            event.setSource(source);
            return this;
        }
        
        public UserEventBuilder correlationId(String correlationId) {
            event.setCorrelationId(correlationId);
            return this;
        }
        
        public UserEvent build() {
            return event;
        }
    }
}
