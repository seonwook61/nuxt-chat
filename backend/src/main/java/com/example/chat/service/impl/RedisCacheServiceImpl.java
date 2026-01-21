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
 * - Message reactions (Hash per message)
 *
 * Redis Key Schema:
 * - room:{roomId}:recent - Recent messages (List)
 * - room:{roomId}:users - Active users (Set)
 * - message:{messageId}:reactions - Message reactions (Hash: emoji -> Set<userId>)
 *
 * TDD Phase 1 & Phase 3.2: Implementation to make tests GREEN
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheServiceImpl implements RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

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
    @Override
    public void addReaction(UUID messageId, String emoji, String userId) {
        try {
            String key = buildReactionKey(messageId);

            // Get existing reactions for this emoji
            Set<String> users = getReactionUsersForEmoji(key, emoji);

            // Add user to the set
            users.add(userId);

            // Store back to Redis as JSON
            String usersJson = objectMapper.writeValueAsString(users);
            redisTemplate.opsForHash().put(key, emoji, usersJson);

            // Set TTL for the key (24 hours)
            redisTemplate.expire(key, 86400, TimeUnit.SECONDS);

            log.debug("Added reaction {} from user {} to message {}", emoji, userId, messageId);
        } catch (Exception e) {
            log.error("Error adding reaction to message {}: {}", messageId, e.getMessage(), e);
        }
    }

    /**
     * Remove a reaction from a message
     *
     * @param messageId Message ID
     * @param emoji Emoji type
     * @param userId User ID who reacted
     */
    @Override
    public void removeReaction(UUID messageId, String emoji, String userId) {
        try {
            String key = buildReactionKey(messageId);

            // Get existing reactions for this emoji
            Set<String> users = getReactionUsersForEmoji(key, emoji);

            // Remove user from the set
            users.remove(userId);

            if (users.isEmpty()) {
                // If no more users for this emoji, remove the field
                redisTemplate.opsForHash().delete(key, emoji);
            } else {
                // Store back to Redis as JSON
                String usersJson = objectMapper.writeValueAsString(users);
                redisTemplate.opsForHash().put(key, emoji, usersJson);
            }

            log.debug("Removed reaction {} from user {} on message {}", emoji, userId, messageId);
        } catch (Exception e) {
            log.error("Error removing reaction from message {}: {}", messageId, e.getMessage(), e);
        }
    }

    /**
     * Get all reactions for a message
     *
     * @param messageId Message ID
     * @return ReactionSummary containing all reactions
     */
    @Override
    public ReactionSummary getReactions(UUID messageId) {
        try {
            String key = buildReactionKey(messageId);

            Map<Object, Object> reactionsMap = redisTemplate.opsForHash().entries(key);

            if (reactionsMap == null || reactionsMap.isEmpty()) {
                return new ReactionSummary();
            }

            ReactionSummary summary = new ReactionSummary();

            for (Map.Entry<Object, Object> entry : reactionsMap.entrySet()) {
                String emoji = (String) entry.getKey();
                String usersJson = (String) entry.getValue();

                try {
                    Set<String> users = objectMapper.readValue(usersJson, new TypeReference<Set<String>>() {});
                    summary.getReactions().put(emoji, users);
                } catch (Exception e) {
                    log.error("Error parsing reaction users for emoji {}: {}", emoji, e.getMessage());
                }
            }

            return summary;
        } catch (Exception e) {
            log.error("Error retrieving reactions for message {}: {}", messageId, e.getMessage(), e);
            return new ReactionSummary();
        }
    }

    /**
     * Get reactions for multiple messages
     * Useful for batch loading reactions when fetching recent messages
     *
     * @param messageIds List of message IDs
     * @return Map of messageId -> ReactionSummary
     */
    @Override
    public Map<UUID, ReactionSummary> getReactionsForMessages(List<UUID> messageIds) {
        Map<UUID, ReactionSummary> result = new HashMap<>();

        if (messageIds == null || messageIds.isEmpty()) {
            return result;
        }

        for (UUID messageId : messageIds) {
            ReactionSummary summary = getReactions(messageId);
            if (summary.getTotalReactionCount() > 0) {
                result.put(messageId, summary);
            }
        }

        return result;
    }

    /**
     * Build Redis key for message reactions
     */
    private String buildReactionKey(UUID messageId) {
        return "message:" + messageId + ":reactions";
    }

    /**
     * Get the set of users who reacted with a specific emoji
     */
    private Set<String> getReactionUsersForEmoji(String key, String emoji) {
        try {
            Object usersObj = redisTemplate.opsForHash().get(key, emoji);

            if (usersObj == null) {
                return new HashSet<>();
            }

            String usersJson = (String) usersObj;
            return objectMapper.readValue(usersJson, new TypeReference<Set<String>>() {});
        } catch (Exception e) {
            log.error("Error retrieving users for emoji {}: {}", emoji, e.getMessage());
            return new HashSet<>();
        }
    }
}
