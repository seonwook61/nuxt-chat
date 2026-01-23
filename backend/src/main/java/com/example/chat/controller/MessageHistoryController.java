package com.example.chat.controller;

import com.example.chat.dto.MessageResponse;
import com.example.chat.entity.ChatMessageEntity;
import com.example.chat.service.MessagePersistenceService;
import com.example.chat.service.ReadReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
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
    private final ReadReceiptService readReceiptService;

    /**
     * Get message history for a room
     * GET /api/messages/history/{roomId}?limit=50
     *
     * Phase 6: Now includes read receipt status for all messages
     */
    @GetMapping("/history/{roomId}")
    public ResponseEntity<List<MessageResponse>> getMessageHistory(
        @PathVariable String roomId,
        @RequestParam(defaultValue = "50") int limit
    ) {
        try {
            log.debug("Fetching message history for room: {}, limit: {}", roomId, limit);

            List<ChatMessageEntity> entities = persistenceService.getMessageHistory(roomId, limit);

            // Get message IDs for batch read status query
            List<UUID> messageIds = entities.stream()
                .map(ChatMessageEntity::getMessageId)
                .collect(Collectors.toList());

            // Batch fetch read status for all messages (Phase 6)
            Map<UUID, Set<String>> readStatusMap = readReceiptService.getReadStatusForMessages(messageIds);

            // Convert entities to responses with read status
            List<MessageResponse> messages = entities.stream()
                .map(entity -> entityToResponse(entity, readStatusMap.get(entity.getMessageId())))
                .collect(Collectors.toList());

            log.debug("Retrieved {} messages for room: {} with read status", messages.size(), roomId);

            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error fetching message history for room: {}", roomId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Convert ChatMessageEntity to MessageResponse DTO
     *
     * @param entity ChatMessageEntity from database
     * @param readBy Set of user IDs who read this message (can be null)
     * @return MessageResponse DTO with read status
     */
    private MessageResponse entityToResponse(ChatMessageEntity entity, Set<String> readBy) {
        return MessageResponse.builder()
            .messageId(entity.getMessageId())
            .roomId(entity.getRoomId())
            .userId(entity.getUserId())
            .username(entity.getUsername())
            .content(entity.getContent())
            .timestamp(entity.getTimestamp())
            .readBy(readBy != null ? readBy : Collections.emptySet())
            .readCount(readBy != null ? readBy.size() : 0)
            .build();
    }
}
