package com.example.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Chat Message DTO
 *
 * Represents a chat message for Kafka streaming and Redis caching.
 * - Kafka Topic: chat.message.v1
 * - Partition Key: roomId (ensures same room messages go to same partition)
 * - Serialization: JSON (Spring Kafka default)
 *
 * Message Types:
 * - TEXT: Regular chat message
 * - JOIN: User joined notification
 * - LEAVE: User left notification
 *
 * @see com.example.chat.service.KafkaProducerService#sendMessage(ChatMessage)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {

    /**
     * Unique message identifier (generated on server)
     */
    @NotNull(message = "messageId cannot be null")
    private UUID messageId;

    /**
     * Room/channel identifier
     */
    @NotBlank(message = "roomId cannot be blank")
    private String roomId;

    /**
     * User identifier who sent the message
     */
    @NotBlank(message = "userId cannot be blank")
    private String userId;

    /**
     * Display name of the user
     */
    @NotBlank(message = "username cannot be blank")
    private String username;

    /**
     * Message content (max 1000 characters)
     */
    @NotBlank(message = "content cannot be blank")
    @Size(max = 1000, message = "content must not exceed 1000 characters")
    private String content;

    /**
     * Timestamp when message was created (server time)
     */
    @NotNull(message = "timestamp cannot be null")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * Message type: TEXT, JOIN, or LEAVE
     */
    @NotNull(message = "type cannot be null")
    @Pattern(regexp = "TEXT|JOIN|LEAVE", message = "type must be TEXT, JOIN, or LEAVE")
    private String type;
}
