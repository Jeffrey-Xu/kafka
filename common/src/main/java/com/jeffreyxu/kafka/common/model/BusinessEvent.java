package com.jeffreyxu.kafka.common.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Represents business transaction events in the system.
 * Examples: order created, payment processed, shipment, etc.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessEvent extends BaseEvent {
    
    /**
     * Unique identifier for the order/transaction
     */
    @NotBlank(message = "Order ID cannot be blank")
    private String orderId;
    
    /**
     * Customer identifier
     */
    @NotBlank(message = "Customer ID cannot be blank")
    private String customerId;
    
    /**
     * Type of business event
     */
    @NotBlank(message = "Event type cannot be blank")
    @Pattern(regexp = "^(ORDER_CREATED|ORDER_UPDATED|ORDER_CANCELLED|PAYMENT_INITIATED|PAYMENT_COMPLETED|PAYMENT_FAILED|SHIPMENT_CREATED|SHIPMENT_DISPATCHED|SHIPMENT_DELIVERED|REFUND_INITIATED|REFUND_COMPLETED)$",
             message = "Invalid business event type")
    private String eventType;
    
    /**
     * Transaction amount
     */
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal amount;
    
    /**
     * Currency code (ISO 4217)
     */
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code")
    private String currency = "USD";
    
    /**
     * Payment method used
     */
    private String paymentMethod;
    
    /**
     * Shipping address
     */
    private String shippingAddress;
    
    /**
     * Billing address
     */
    private String billingAddress;
    
    /**
     * Order status
     */
    @Pattern(regexp = "^(PENDING|CONFIRMED|PROCESSING|SHIPPED|DELIVERED|CANCELLED|REFUNDED)$",
             message = "Invalid order status")
    private String orderStatus;
    
    /**
     * Detailed order information
     */
    private Map<String, Object> orderDetails;
    
    /**
     * Additional business metadata
     */
    private Map<String, Object> metadata;
    
    @Override
    public String getEventType() {
        return "BUSINESS_EVENT";
    }
    
    @Override
    public boolean isValid() {
        return orderId != null && !orderId.trim().isEmpty() &&
               customerId != null && !customerId.trim().isEmpty() &&
               eventType != null && !eventType.trim().isEmpty();
    }
    
    @Override
    public String getDescription() {
        return String.format("Business event %s for order %s (customer: %s, amount: %s %s)",
                eventType, orderId, customerId, amount, currency);
    }
    
    /**
     * Builder pattern with fluent API for easy event creation
     */
    public static BusinessEventBuilder builder() {
        return new BusinessEventBuilder();
    }
    
    public static class BusinessEventBuilder {
        private BusinessEvent event = new BusinessEvent();
        
        public BusinessEventBuilder orderId(String orderId) {
            event.setOrderId(orderId);
            return this;
        }
        
        public BusinessEventBuilder customerId(String customerId) {
            event.setCustomerId(customerId);
            return this;
        }
        
        public BusinessEventBuilder eventType(String eventType) {
            event.setEventType(eventType);
            return this;
        }
        
        public BusinessEventBuilder amount(BigDecimal amount) {
            event.setAmount(amount);
            return this;
        }
        
        public BusinessEventBuilder currency(String currency) {
            event.setCurrency(currency);
            return this;
        }
        
        public BusinessEventBuilder paymentMethod(String paymentMethod) {
            event.setPaymentMethod(paymentMethod);
            return this;
        }
        
        public BusinessEventBuilder shippingAddress(String shippingAddress) {
            event.setShippingAddress(shippingAddress);
            return this;
        }
        
        public BusinessEventBuilder billingAddress(String billingAddress) {
            event.setBillingAddress(billingAddress);
            return this;
        }
        
        public BusinessEventBuilder orderStatus(String orderStatus) {
            event.setOrderStatus(orderStatus);
            return this;
        }
        
        public BusinessEventBuilder orderDetails(Map<String, Object> orderDetails) {
            event.setOrderDetails(orderDetails);
            return this;
        }
        
        public BusinessEventBuilder metadata(Map<String, Object> metadata) {
            event.setMetadata(metadata);
            return this;
        }
        
        public BusinessEventBuilder source(String source) {
            event.setSource(source);
            return this;
        }
        
        public BusinessEventBuilder correlationId(String correlationId) {
            event.setCorrelationId(correlationId);
            return this;
        }
        
        public BusinessEvent build() {
            return event;
        }
    }
}
