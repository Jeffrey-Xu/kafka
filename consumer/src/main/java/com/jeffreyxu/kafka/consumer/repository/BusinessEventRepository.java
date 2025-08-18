package com.jeffreyxu.kafka.consumer.repository;

import com.jeffreyxu.kafka.consumer.entity.BusinessEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for BusinessEventEntity.
 * Provides data access methods for business transaction analytics and reporting.
 */
@Repository
public interface BusinessEventRepository extends JpaRepository<BusinessEventEntity, Long> {

    /**
     * Find events by customer ID
     */
    List<BusinessEventEntity> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    /**
     * Find events by order ID
     */
    List<BusinessEventEntity> findByOrderIdOrderByCreatedAtDesc(String orderId);

    /**
     * Find events by event type
     */
    List<BusinessEventEntity> findByEventTypeOrderByCreatedAtDesc(String eventType);

    /**
     * Find events by order status
     */
    List<BusinessEventEntity> findByOrderStatusOrderByCreatedAtDesc(String orderStatus);

    /**
     * Find events by payment method
     */
    List<BusinessEventEntity> findByPaymentMethodOrderByCreatedAtDesc(String paymentMethod);

    /**
     * Find events by currency
     */
    List<BusinessEventEntity> findByCurrencyOrderByCreatedAtDesc(String currency);

    /**
     * Find events within date range
     */
    List<BusinessEventEntity> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find events with amount greater than specified value
     */
    List<BusinessEventEntity> findByAmountGreaterThanOrderByAmountDesc(BigDecimal amount);

    /**
     * Find events with amount between specified values
     */
    List<BusinessEventEntity> findByAmountBetweenOrderByAmountDesc(BigDecimal minAmount, BigDecimal maxAmount);

    /**
     * Count events by customer
     */
    @Query("SELECT COUNT(b) FROM BusinessEventEntity b WHERE b.customerId = :customerId")
    long countByCustomerId(@Param("customerId") String customerId);

    /**
     * Count events by event type
     */
    @Query("SELECT COUNT(b) FROM BusinessEventEntity b WHERE b.eventType = :eventType")
    long countByEventType(@Param("eventType") String eventType);

    /**
     * Count events by order status
     */
    @Query("SELECT COUNT(b) FROM BusinessEventEntity b WHERE b.orderStatus = :orderStatus")
    long countByOrderStatus(@Param("orderStatus") String orderStatus);

    /**
     * Calculate total revenue by currency
     */
    @Query("SELECT b.currency, SUM(b.amount) FROM BusinessEventEntity b WHERE b.currency = :currency GROUP BY b.currency")
    BigDecimal getTotalRevenueByCurrency(@Param("currency") String currency);

    /**
     * Calculate total revenue for all currencies
     */
    @Query("SELECT b.currency, SUM(b.amount) FROM BusinessEventEntity b GROUP BY b.currency ORDER BY SUM(b.amount) DESC")
    List<Object[]> getTotalRevenueByAllCurrencies();

    /**
     * Get customer transaction summary
     */
    @Query(value = """
        SELECT customer_id,
               COUNT(*) as total_transactions,
               COUNT(DISTINCT order_id) as unique_orders,
               SUM(amount) as total_amount,
               AVG(amount) as avg_amount,
               MIN(amount) as min_amount,
               MAX(amount) as max_amount,
               COUNT(DISTINCT payment_method) as payment_methods_used,
               MIN(created_at) as first_transaction,
               MAX(created_at) as last_transaction
        FROM business_events
        WHERE created_at >= :since
        GROUP BY customer_id
        ORDER BY total_amount DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> getCustomerTransactionSummary(@Param("since") LocalDateTime since, @Param("limit") int limit);

    /**
     * Get order status distribution
     */
    @Query(value = """
        SELECT order_status, COUNT(*) as count, SUM(amount) as total_amount
        FROM business_events
        WHERE created_at >= :since
        GROUP BY order_status
        ORDER BY count DESC
        """, nativeQuery = true)
    List<Object[]> getOrderStatusDistribution(@Param("since") LocalDateTime since);

    /**
     * Get payment method analytics
     */
    @Query(value = """
        SELECT payment_method,
               COUNT(*) as transaction_count,
               SUM(amount) as total_amount,
               AVG(amount) as avg_amount
        FROM business_events
        WHERE created_at >= :since
        GROUP BY payment_method
        ORDER BY transaction_count DESC
        """, nativeQuery = true)
    List<Object[]> getPaymentMethodAnalytics(@Param("since") LocalDateTime since);

    /**
     * Get daily revenue summary
     */
    @Query(value = """
        SELECT DATE(created_at) as transaction_date,
               COUNT(*) as transaction_count,
               SUM(amount) as daily_revenue,
               COUNT(DISTINCT customer_id) as unique_customers,
               COUNT(DISTINCT order_id) as unique_orders
        FROM business_events
        WHERE created_at >= :since
        GROUP BY DATE(created_at)
        ORDER BY transaction_date DESC
        """, nativeQuery = true)
    List<Object[]> getDailyRevenueSummary(@Param("since") LocalDateTime since);

    /**
     * Get top customers by revenue
     */
    @Query(value = """
        SELECT customer_id,
               SUM(amount) as total_revenue,
               COUNT(*) as transaction_count,
               COUNT(DISTINCT order_id) as order_count,
               MAX(created_at) as last_transaction
        FROM business_events
        WHERE created_at >= :since
        GROUP BY customer_id
        ORDER BY total_revenue DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> getTopCustomersByRevenue(@Param("since") LocalDateTime since, @Param("limit") int limit);

    /**
     * Find high-value transactions
     */
    @Query(value = """
        SELECT * FROM business_events
        WHERE amount >= :threshold
        AND created_at >= :since
        ORDER BY amount DESC, created_at DESC
        """, nativeQuery = true)
    List<BusinessEventEntity> getHighValueTransactions(@Param("threshold") BigDecimal threshold, @Param("since") LocalDateTime since);
}
