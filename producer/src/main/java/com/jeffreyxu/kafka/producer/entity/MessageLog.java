package com.jeffreyxu.kafka.producer.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a log entry for messages sent to Kafka.
 * Tracks message sending attempts, success/failure, and metadata.
 */
@Entity
@Table(name = "message_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false, unique = true)
    private String messageId;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "partition_id")
    private Integer partitionId;

    @Column(name = "offset_value")
    private Long offsetValue;

    @Column(name = "message_key")
    private String messageKey;

    @Column(name = "message_size")
    private Integer messageSize;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "status", length = 20)
    private String status = "SUCCESS";

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @PrePersist
    protected void onCreate() {
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
    }
}
