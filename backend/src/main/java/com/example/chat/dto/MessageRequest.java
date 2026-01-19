package com.example.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Message Request DTO
 *
 * Incoming HTTP request body for sending a chat message.
 * - Endpoint: POST /api/rooms/{roomId}/messages
 * - Validated on controller layer
 * - Converted to ChatMessage for Kafka publishing
 *
 * @see com.example.chat.dto.ChatMessage
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageRequest {

    /**
     * Message content (max 1000 characters)
     */
    @NotBlank(message = "content cannot be blank")
    @Size(max = 1000, message = "content must not exceed 1000 characters")
    private String content;

    /**
     * User ID sending the message
     * TODO Phase 2: Extract from JWT token instead of request body
     */
    @NotBlank(message = "userId cannot be blank")
    private String userId;

    /**
     * Display name of the user
     * TODO Phase 2: Fetch from user service or JWT claims
     */
    @NotBlank(message = "username cannot be blank")
    private String username;
}
