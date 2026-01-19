package com.example.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Message Response DTO
 *
 * Outgoing HTTP response body for chat message operations.
 * - Returned from POST /api/rooms/{roomId}/messages
 * - Returned from GET /api/rooms/{roomId}/messages (list)
 * - Simplified view of ChatMessage for REST API clients
 *
 * @see com.example.chat.dto.ChatMessage
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageResponse {

    /**
     * Unique message identifier
     */
    private UUID messageId;

    /**
     * Room/channel identifier
     */
    private String roomId;

    /**
     * User identifier who sent the message
     */
    private String userId;

    /**
     * Display name of the user
     */
    private String username;

    /**
     * Message content
     */
    private String content;

    /**
     * Timestamp when message was created (server time)
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}
