package com.example.chat.service.impl;

import com.example.chat.config.RedisConfig;
import com.example.chat.dto.ChatMessage;
import com.example.chat.dto.ReactionSummary;
import com.example.chat.service.RedisCacheService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis Cache Service Implementation
 *
 * Manages:
 * - Recent messages cache (FIFO, max 50, TTL 600s)
 * - Room user presence (Set)
 *
 * Redis Key Schema:
 * - room:{roomId}:recent - Recent messages (List)
 * - room:{roomId}:users - Active users (Set)
 *
 * TDD Phase 1: Implementation to make tests GREEN
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheServiceImpl implements RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Cache a recent message for a room
     * - Stores in Redis List
     * - FIFO: Max 50 messages
     * - TTL: 600 seconds
     *
     * @param roomId Room ID
     * @param message ChatMessage to cache
     */
    @Override
    public void cacheRecentMessage(String roomId, ChatMessage message) {
        try {
            String key = buildRecentMessagesKey(roomId);

            // Add message to list (right push - append to end)
            redisTemplate.opsForList().rightPush(key, message);

            // Keep only last 50 messages (FIFO - remove oldest)
            Long listSize = redisTemplate.opsForList().size(key);
            if (listSize != null && listSize > 50) {
                redisTemplate.opsForList().trim(key, listSize - 50, -1);
            }

            // Set TTL for the key
            redisTemplate.expire(key, RedisConfig.TTL_RECENT_MESSAGES, TimeUnit.SECONDS);

            log.debug("Cached message for room {}: {}", roomId, message.getMessageId());
        } catch (Exception e) {
            log.error("Error caching message for room {}: {}", roomId, e.getMessage(), e);
        }
    }

    /**
     * Get recent messages for a room
     *
     * @param roomId Room ID
     * @return List of recent messages (max 50)
     */
    @Override
    public List<ChatMessage> getRecentMessages(String roomId) {
        try {
            String key = buildRecentMessagesKey(roomId);

            // Get all messages from the list
            List<Object> cachedMessages = redisTemplate.opsForList().range(key, 0, -1);

            if (cachedMessages == null || cachedMessages.isEmpty()) {
                return Collections.emptyList();
            }

            // Convert Objects to ChatMessage
            return cachedMessages.stream()
                .filter(msg -> msg instanceof ChatMessage)
                .map(msg -> (ChatMessage) msg)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving recent messages for room {}: {}", roomId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Add a user to a room
     *
     * @param roomId Room ID
     * @param userId User ID
     */
    @Override
    public void addUserToRoom(String roomId, String userId) {
        try {
            String key = buildRoomUsersKey(roomId);

            // Add user to set
            redisTemplate.opsForSet().add(key, userId);

            // Set TTL for the key
            redisTemplate.expire(key, RedisConfig.TTL_ROOM_USERS, TimeUnit.SECONDS);

            log.debug("Added user {} to room {}", userId, roomId);
        } catch (Exception e) {
            log.error("Error adding user to room {}: {}", roomId, e.getMessage(), e);
        }
    }

    /**
     * Remove a user from a room
     *
     * @param roomId Room ID
     * @param userId User ID
     */
    @Override
    public void removeUserFromRoom(String roomId, String userId) {
        try {
            String key = buildRoomUsersKey(roomId);

            // Remove user from set
            redisTemplate.opsForSet().remove(key, userId);

            log.debug("Removed user {} from room {}", userId, roomId);
        } catch (Exception e) {
            log.error("Error removing user from room {}: {}", roomId, e.getMessage(), e);
        }
    }

    /**
     * Get all users in a room
     *
     * @param roomId Room ID
     * @return Set of user IDs
     */
    @Override
    public Set<String> getRoomUsers(String roomId) {
        try {
            String key = buildRoomUsersKey(roomId);

            // Get all members from set
            Set<Object> members = redisTemplate.opsForSet().members(key);

            if (members == null || members.isEmpty()) {
                return Collections.emptySet();
            }

            // Convert Objects to Strings
            return members.stream()
                .filter(member -> member instanceof String)
                .map(member -> (String) member)
                .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Error retrieving room users for room {}: {}", roomId, e.getMessage(), e);
            return Collections.emptySet();
        }
    }

    /**
     * Build Redis key for recent messages
     */
    private String buildRecentMessagesKey(String roomId) {
        return RedisConfig.CACHE_RECENT_MESSAGES + roomId + RedisConfig.RECENT_MESSAGES_SUFFIX;
    }

    /**
     * Build Redis key for room users
     */
    private String buildRoomUsersKey(String roomId) {
        return RedisConfig.CACHE_ROOM_USERS + roomId + RedisConfig.ROOM_USERS_SUFFIX;
    }
}
