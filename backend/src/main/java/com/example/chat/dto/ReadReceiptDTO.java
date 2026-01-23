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
import java.util.UUID;

/**
 * Read Receipt DTO
 *
 * Represents a message read event sent via WebSocket.
 * Used to track and broadcast when a user reads a message.
 *
 * Data Flow:
 * 1. Client sends read event via WebSocket (/app/chat.read)
 * 2. Backend stores in PostgreSQL (message_read_status table)
 * 3. Backend caches in Redis (last read position)
 * 4. Backend broadcasts to all room subscribers
 * 5. Frontend updates UI (✓ → ✓✓)
 *
 * Performance:
 * - Client debounces read events (1 second)
 * - Redis caching reduces DB queries
 * - Duplicate reads are ignored (unique constraint)
 *
 * @see com.example.chat.controller.ChatWebSocketController#handleReadReceipt(ReadReceiptDTO)
 * @see com.example.chat.service.ReadReceiptService
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadReceiptDTO {

    /**
     * Room identifier where the message was sent
     */
    @NotBlank(message = "roomId cannot be blank")
    private String roomId;

    /**
     * User who read the message
     */
    @NotBlank(message = "userId cannot be blank")
    private String userId;

    /**
     * Message ID that was read
     */
    @NotNull(message = "messageId cannot be null")
    @JsonProperty("messageId")
    private UUID messageId;

    /**
     * Timestamp when the message was read (client time)
     * Server will override with server time for accuracy
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}
