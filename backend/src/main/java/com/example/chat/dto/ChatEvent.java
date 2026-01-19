package com.example.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Chat Event DTO
 *
 * Represents system events for presence tracking and room management.
 * - Kafka Topic: chat.event.v1
 * - Partition Key: roomId (ensures event ordering per room)
 * - Serialization: JSON
 *
 * Event Types:
 * - USER_JOINED: User entered a room
 * - USER_LEFT: User left a room
 * - MESSAGE_SENT: Message delivery confirmation (optional)
 *
 * @see com.example.chat.service.KafkaProducerService#sendEvent(ChatEvent)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatEvent {

    /**
     * Unique event identifier
     */
    @NotNull(message = "eventId cannot be null")
    private UUID eventId;

    /**
     * Room/channel identifier where event occurred
     */
    @NotBlank(message = "roomId cannot be blank")
    private String roomId;

    /**
     * Event type: USER_JOINED, USER_LEFT, MESSAGE_SENT
     */
    @NotBlank(message = "eventType cannot be blank")
    private String eventType;

    /**
     * User identifier (optional for some event types)
     */
    private String userId;

    /**
     * Display name of the user (optional)
     */
    private String username;

    /**
     * Timestamp when event occurred (server time)
     */
    @NotNull(message = "timestamp cannot be null")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * Additional metadata for the event
     * - Can store custom properties per event type
     * - Example: {"previousUserCount": 5, "newUserCount": 6}
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}
