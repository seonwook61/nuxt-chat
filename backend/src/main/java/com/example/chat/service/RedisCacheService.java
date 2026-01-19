package com.example.chat.service;

import com.example.chat.dto.ChatMessage;

import java.util.List;
import java.util.Set;

/**
 * Redis Cache Service Interface
 *
 * Manages:
 * - Recent messages cache (FIFO, max 50, TTL 600s)
 * - Room user presence (Set)
 *
 * Redis Key Schema:
 * - room:{roomId}:recent - Recent messages (List)
 * - room:{roomId}:users - Active users (Set)
 *
 * Implementation in Phase 1
 */
public interface RedisCacheService {

    /**
     * Cache a recent message for a room
     * - Stores in Redis List
     * - FIFO: Max 50 messages
     * - TTL: 600 seconds
     *
     * @param roomId Room ID
     * @param message ChatMessage to cache
     */
    void cacheRecentMessage(String roomId, ChatMessage message);

    /**
     * Get recent messages for a room
     *
     * @param roomId Room ID
     * @return List of recent messages (max 50)
     */
    List<ChatMessage> getRecentMessages(String roomId);

    /**
     * Add a user to a room
     *
     * @param roomId Room ID
     * @param userId User ID
     */
    void addUserToRoom(String roomId, String userId);

    /**
     * Remove a user from a room
     *
     * @param roomId Room ID
     * @param userId User ID
     */
    void removeUserFromRoom(String roomId, String userId);

    /**
     * Get all users in a room
     *
     * @param roomId Room ID
     * @return Set of user IDs
     */
    Set<String> getRoomUsers(String roomId);
}
