package com.example.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Typing Indicator DTO
 *
 * Represents real-time typing status of users in a chat room.
 * Used to show "Alice is typing..." indicator to other participants.
 *
 * Data Flow:
 * 1. Client sends typing event via WebSocket (/app/chat.typing)
 * 2. Backend stores in Redis Set with TTL (5 seconds)
 * 3. Backend broadcasts to all room subscribers
 * 4. Frontend displays typing indicator
 *
 * Performance Optimization:
 * - Client debounces input events (500ms)
 * - Redis TTL auto-cleanup (5 seconds)
 * - No database persistence needed
 *
 * @see com.example.chat.controller.ChatWebSocketController#handleTyping(TypingIndicator)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TypingIndicator {

    /**
     * Room identifier where typing occurs
     */
    @NotBlank(message = "roomId cannot be blank")
    private String roomId;

    /**
     * User ID of the person typing
     */
    @NotBlank(message = "userId cannot be blank")
    private String userId;

    /**
     * Username for display ("Alice is typing...")
     */
    @NotBlank(message = "username cannot be blank")
    private String username;

    /**
     * Typing status
     * - true: User started typing
     * - false: User stopped typing
     */
    @NotNull(message = "isTyping cannot be null")
    @JsonProperty("isTyping")
    private Boolean isTyping;

    /**
     * Timestamp when typing status changed (server time)
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}
