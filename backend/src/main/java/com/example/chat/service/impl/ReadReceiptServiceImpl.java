package com.example.chat.service.impl;

import com.example.chat.dto.ReadReceiptDTO;
import com.example.chat.entity.MessageReadStatus;
import com.example.chat.repository.MessageReadStatusRepository;
import com.example.chat.service.ReadReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Read Receipt Service Implementation
 *
 * Manages message read status with PostgreSQL persistence and Redis caching.
 *
 * Performance Optimization:
 * - Redis cache for last read position (TTL 1 hour)
 * - Duplicate prevention via unique constraint
 * - Batch query support for message history
 *
 * @see ReadReceiptService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReadReceiptServiceImpl implements ReadReceiptService {

    private final MessageReadStatusRepository readStatusRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String LAST_READ_KEY_PREFIX = "room:%s:lastRead:%s"; // room:{roomId}:lastRead:{userId}
    private static final long CACHE_TTL_SECONDS = 3600; // 1 hour

    /**
     * Mark a message as read by a user
     *
     * Process:
     * 1. Check Redis cache for duplicate (same messageId)
     * 2. Save to PostgreSQL (unique constraint prevents duplicates)
     * 3. Update Redis cache with new messageId
     * 4. Return DTO for broadcasting
     *
     * @param roomId Room ID
     * @param userId User ID who read the message
     * @param messageId Message ID that was read
     * @return ReadReceiptDTO for broadcasting, or null if duplicate
     */
    @Override
    @Transactional
    public ReadReceiptDTO markAsRead(String roomId, String userId, UUID messageId) {
        log.debug("[ReadReceipt] markAsRead - roomId: {}, userId: {}, messageId: {}", roomId, userId, messageId);

        try {
            // 1. Check Redis cache for duplicate
            String cacheKey = String.format(LAST_READ_KEY_PREFIX, roomId, userId);
            String lastReadMessageId = redisTemplate.opsForValue().get(cacheKey);

            if (messageId.toString().equals(lastReadMessageId)) {
                log.debug("[ReadReceipt] Duplicate read event (cached), skipping - messageId: {}", messageId);
                return null;
            }

            // 2. Check if already read in database (additional safety check)
            if (readStatusRepository.existsByMessageIdAndUserId(messageId, userId)) {
                log.debug("[ReadReceipt] Message already marked as read in DB - messageId: {}, userId: {}", messageId, userId);
                // Update cache anyway
                redisTemplate.opsForValue().set(cacheKey, messageId.toString(), CACHE_TTL_SECONDS, TimeUnit.SECONDS);
                return null;
            }

            // 3. Save to PostgreSQL
            LocalDateTime now = LocalDateTime.now();
            MessageReadStatus readStatus = MessageReadStatus.builder()
                    .messageId(messageId)
                    .roomId(roomId)
                    .userId(userId)
                    .readAt(now)
                    .build();

            readStatusRepository.save(readStatus);
            log.info("[ReadReceipt] Saved read status - messageId: {}, userId: {}", messageId, userId);

            // 4. Update Redis cache
            redisTemplate.opsForValue().set(cacheKey, messageId.toString(), CACHE_TTL_SECONDS, TimeUnit.SECONDS);
            log.debug("[ReadReceipt] Updated Redis cache - key: {}, messageId: {}", cacheKey, messageId);

            // 5. Return DTO for broadcasting
            return ReadReceiptDTO.builder()
                    .roomId(roomId)
                    .userId(userId)
                    .messageId(messageId)
                    .timestamp(now)
                    .build();

        } catch (Exception e) {
            log.error("[ReadReceipt] Error marking message as read - messageId: {}, userId: {}", messageId, userId, e);
            throw new RuntimeException("Failed to mark message as read", e);
        }
    }

    /**
     * Get list of users who read a specific message
     *
     * @param messageId Message ID
     * @return Set of user IDs who read the message
     */
    @Override
    @Transactional(readOnly = true)
    public Set<String> getUsersWhoRead(UUID messageId) {
        log.debug("[ReadReceipt] getUsersWhoRead - messageId: {}", messageId);

        try {
            List<MessageReadStatus> readStatuses = readStatusRepository.findByMessageId(messageId);
            Set<String> userIds = readStatuses.stream()
                    .map(MessageReadStatus::getUserId)
                    .collect(Collectors.toSet());

            log.debug("[ReadReceipt] Found {} users who read message {}", userIds.size(), messageId);
            return userIds;

        } catch (Exception e) {
            log.error("[ReadReceipt] Error getting users who read message: {}", messageId, e);
            return Collections.emptySet();
        }
    }

    /**
     * Get read status for multiple messages (batch operation)
     * Used when loading message history
     *
     * @param messageIds List of message IDs
     * @return Map of messageId -> Set of userIds who read it
     */
    @Override
    @Transactional(readOnly = true)
    public Map<UUID, Set<String>> getReadStatusForMessages(List<UUID> messageIds) {
        log.debug("[ReadReceipt] getReadStatusForMessages - count: {}", messageIds.size());

        if (messageIds == null || messageIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            List<MessageReadStatus> readStatuses = readStatusRepository.findByMessageIdIn(messageIds);

            // Group by messageId -> Set of userIds
            Map<UUID, Set<String>> result = readStatuses.stream()
                    .collect(Collectors.groupingBy(
                            MessageReadStatus::getMessageId,
                            Collectors.mapping(MessageReadStatus::getUserId, Collectors.toSet())
                    ));

            log.debug("[ReadReceipt] Retrieved read status for {} messages", result.size());
            return result;

        } catch (Exception e) {
            log.error("[ReadReceipt] Error getting read status for messages", e);
            return Collections.emptyMap();
        }
    }

    /**
     * Get the last message ID read by a user in a room
     * Used to determine read/unread status
     *
     * @param roomId Room ID
     * @param userId User ID
     * @return Last read message ID, or null if none
     */
    @Override
    @Transactional(readOnly = true)
    public UUID getLastReadMessageId(String roomId, String userId) {
        log.debug("[ReadReceipt] getLastReadMessageId - roomId: {}, userId: {}", roomId, userId);

        try {
            // 1. Try Redis cache first
            String cacheKey = String.format(LAST_READ_KEY_PREFIX, roomId, userId);
            String cachedMessageId = redisTemplate.opsForValue().get(cacheKey);

            if (cachedMessageId != null) {
                log.debug("[ReadReceipt] Cache hit - lastReadMessageId: {}", cachedMessageId);
                return UUID.fromString(cachedMessageId);
            }

            // 2. Cache miss - query database
            Optional<MessageReadStatus> lastRead = readStatusRepository.findLastReadByRoomAndUser(roomId, userId);

            if (lastRead.isPresent()) {
                UUID messageId = lastRead.get().getMessageId();
                log.debug("[ReadReceipt] DB hit - lastReadMessageId: {}", messageId);

                // Update cache
                redisTemplate.opsForValue().set(cacheKey, messageId.toString(), CACHE_TTL_SECONDS, TimeUnit.SECONDS);

                return messageId;
            }

            log.debug("[ReadReceipt] No read status found for roomId: {}, userId: {}", roomId, userId);
            return null;

        } catch (Exception e) {
            log.error("[ReadReceipt] Error getting last read message ID - roomId: {}, userId: {}", roomId, userId, e);
            return null;
        }
    }

    /**
     * Count how many users read a specific message
     *
     * @param messageId Message ID
     * @return Number of users who read the message
     */
    @Override
    @Transactional(readOnly = true)
    public long getReadCount(UUID messageId) {
        log.debug("[ReadReceipt] getReadCount - messageId: {}", messageId);

        try {
            long count = readStatusRepository.countByMessageId(messageId);
            log.debug("[ReadReceipt] Read count: {} for messageId: {}", count, messageId);
            return count;

        } catch (Exception e) {
            log.error("[ReadReceipt] Error getting read count for messageId: {}", messageId, e);
            return 0;
        }
    }

    /**
     * Check if a user has read a specific message
     *
     * @param messageId Message ID
     * @param userId User ID
     * @return true if the user has read the message
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasUserRead(UUID messageId, String userId) {
        log.debug("[ReadReceipt] hasUserRead - messageId: {}, userId: {}", messageId, userId);

        try {
            boolean hasRead = readStatusRepository.existsByMessageIdAndUserId(messageId, userId);
            log.debug("[ReadReceipt] User {} has read message {}: {}", userId, messageId, hasRead);
            return hasRead;

        } catch (Exception e) {
            log.error("[ReadReceipt] Error checking if user read message - messageId: {}, userId: {}", messageId, userId, e);
            return false;
        }
    }
}
