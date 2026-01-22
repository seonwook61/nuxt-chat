package com.example.chat.service;

import com.example.chat.dto.ChatMessage;
import com.example.chat.dto.ReactionSummary;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Redis Cache Service Interface
 *
 * Manages:
 * - Recent messages cache (FIFO, max 50, TTL 600s)
 * - Room user presence (Set)
 * - Message reactions (Hash per message)
 *
 * Redis Key Schema:
 * - room:{roomId}:recent - Recent messages (List)
 * - room:{roomId}:users - Active users (Set)
 * - room:{roomId}:typing - Typing users (Set, TTL 5s)
 * - message:{messageId}:reactions - Message reactions (Hash: emoji -> Set<userId>)
 *
 * Implementation in Phase 1, Phase 3.2, Phase 5
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

    /**
     * Add a reaction to a message
     * - Stores in Redis Hash: message:{messageId}:reactions
     * - Field: emoji, Value: Set of user IDs (JSON)
     * - TTL: 86400 seconds (24 hours)
     *
     * @param messageId Message ID
     * @param emoji Emoji type (HEART, LAUGH, WOW, SAD, THUMBS_UP, FIRE)
     * @param userId User ID who reacted
     */
    void addReaction(UUID messageId, String emoji, String userId);

    /**
     * Remove a reaction from a message
     *
     * @param messageId Message ID
     * @param emoji Emoji type
     * @param userId User ID who reacted
     */
    void removeReaction(UUID messageId, String emoji, String userId);

    /**
     * Get all reactions for a message
     *
     * @param messageId Message ID
     * @return ReactionSummary containing all reactions
     */
    ReactionSummary getReactions(UUID messageId);

    /**
     * Get reactions for multiple messages
     * Useful for batch loading reactions when fetching recent messages
     *
     * @param messageIds List of message IDs
     * @return Map of messageId -> ReactionSummary
     */
    Map<UUID, ReactionSummary> getReactionsForMessages(List<UUID> messageIds);

    /**
     * Add a user to typing set
     * - Stores in Redis Set: room:{roomId}:typing
     * - TTL: 5 seconds (auto-cleanup for abandoned typing states)
     *
     * @param roomId Room ID
     * @param userId User ID who is typing
     */
    void addTypingUser(String roomId, String userId);

    /**
     * Remove a user from typing set
     *
     * @param roomId Room ID
     * @param userId User ID who stopped typing
     */
    void removeTypingUser(String roomId, String userId);

    /**
     * Get all typing users in a room
     *
     * @param roomId Room ID
     * @return Set of user IDs who are currently typing
     */
    Set<String> getTypingUsers(String roomId);

    /**
     * Get count of users in a room
     *
     * @param roomId Room ID
     * @return Number of users in the room
     */
    long getRoomUserCount(String roomId);
}
