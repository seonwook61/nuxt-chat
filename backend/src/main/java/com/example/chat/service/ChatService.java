package com.example.chat.service;

import com.example.chat.dto.MessageRequest;
import com.example.chat.dto.MessageResponse;
import com.example.chat.dto.RoomInfo;

import java.util.List;

/**
 * Chat Service Interface
 *
 * High-level service for chat operations exposed via REST API.
 * - Orchestrates Kafka producers and Redis cache
 * - Provides business logic for chat features
 * - Validates and transforms DTOs
 *
 * Implementation in Phase 1
 */
public interface ChatService {

    /**
     * Send a chat message to a room
     * - Validates request
     * - Publishes to Kafka (chat.message.v1)
     * - Returns message confirmation
     *
     * @param roomId Room identifier
     * @param request Message request containing content and user info
     * @return MessageResponse with generated messageId and timestamp
     * @throws IllegalArgumentException if roomId or request is invalid
     */
    MessageResponse sendMessage(String roomId, MessageRequest request);

    /**
     * Get recent messages for a room
     * - Retrieves from Redis cache (max 50 messages)
     * - Falls back to empty list if cache miss
     *
     * @param roomId Room identifier
     * @return List of recent messages (newest first)
     */
    List<MessageResponse> getRecentMessages(String roomId);

    /**
     * Get room information and statistics
     * - Room metadata
     * - Current user count
     * - Creation time
     *
     * @param roomId Room identifier
     * @return RoomInfo with current statistics
     * @throws IllegalArgumentException if room does not exist
     */
    RoomInfo getRoomInfo(String roomId);
}
