package com.example.chat.controller;

import com.example.chat.dto.ChatMessage;
import com.example.chat.dto.MessageResponse;
import com.example.chat.entity.ChatMessageEntity;
import com.example.chat.service.MessagePersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Message History
 * Provides endpoints to retrieve historical messages from PostgreSQL
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MessageHistoryController {

    private final MessagePersistenceService persistenceService;

    /**
     * Get message history for a room
     * GET /api/messages/history/{roomId}?limit=50
     */
    @GetMapping("/history/{roomId}")
    public ResponseEntity<List<MessageResponse>> getMessageHistory(
        @PathVariable String roomId,
        @RequestParam(defaultValue = "50") int limit
    ) {
        try {
            log.debug("Fetching message history for room: {}, limit: {}", roomId, limit);

            List<ChatMessageEntity> entities = persistenceService.getMessageHistory(roomId, limit);

            List<MessageResponse> messages = entities.stream()
                .map(this::entityToResponse)
                .collect(Collectors.toList());

            log.debug("Retrieved {} messages for room: {}", messages.size(), roomId);

            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error fetching message history for room: {}", roomId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Convert ChatMessageEntity to MessageResponse DTO
     */
    private MessageResponse entityToResponse(ChatMessageEntity entity) {
        return MessageResponse.builder()
            .messageId(entity.getMessageId())
            .roomId(entity.getRoomId())
            .userId(entity.getUserId())
            .username(entity.getUsername())
            .content(entity.getContent())
            .timestamp(entity.getTimestamp())
            .build();
    }
}
