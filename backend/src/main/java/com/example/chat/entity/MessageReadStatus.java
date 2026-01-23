package com.example.chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Message Read Status Entity
 *
 * Tracks which users have read which messages.
 * Used for implementing read receipts feature (✓✓ indicators).
 *
 * Schema:
 * - Primary Key: Auto-increment ID
 * - Unique Constraint: (message_id, user_id) - prevents duplicate reads
 * - Indexes: (room_id, user_id), (message_id), (read_at)
 *
 * @see com.example.chat.service.ReadReceiptService
 */
@Entity
@Table(
    name = "message_read_status",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_message_user",
            columnNames = {"message_id", "user_id"}
        )
    },
    indexes = {
        @Index(name = "idx_message_read_room_user", columnList = "room_id, user_id"),
        @Index(name = "idx_message_read_message", columnList = "message_id"),
        @Index(name = "idx_message_read_timestamp", columnList = "read_at")
    }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageReadStatus {

    /**
     * Auto-increment primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Message ID that was read
     * Foreign key to chat_message.message_id (logical relationship)
     */
    @Column(name = "message_id", nullable = false)
    private UUID messageId;

    /**
     * Room where the message was sent
     */
    @Column(name = "room_id", nullable = false, length = 100)
    private String roomId;

    /**
     * User who read the message
     */
    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    /**
     * Timestamp when the user read the message
     */
    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt;

    /**
     * Record creation timestamp (audit trail)
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
