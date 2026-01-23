package com.example.chat.service;

import com.example.chat.dto.ReadReceiptDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Read Receipt Service
 *
 * Manages message read status tracking and read receipts.
 *
 * Features:
 * - Mark messages as read
 * - Get read status for messages
 * - Cache last read position in Redis
 * - Prevent duplicate read receipts
 *
 * Storage:
 * - PostgreSQL: Permanent read status records
 * - Redis: Cache for last read message per user (TTL 1 hour)
 *
 * @see com.example.chat.service.impl.ReadReceiptServiceImpl
 */
public interface ReadReceiptService {

    /**
     * Mark a message as read by a user
     *
     * Process:
     * 1. Check Redis cache for duplicate
     * 2. Save to PostgreSQL
     * 3. Update Redis cache
     * 4. Return read receipt DTO for broadcasting
     *
     * @param roomId Room ID
     * @param userId User ID who read the message
     * @param messageId Message ID that was read
     * @return ReadReceiptDTO for broadcasting, or null if duplicate
     */
    ReadReceiptDTO markAsRead(String roomId, String userId, UUID messageId);

    /**
     * Get list of users who read a specific message
     *
     * @param messageId Message ID
     * @return Set of user IDs who read the message
     */
    Set<String> getUsersWhoRead(UUID messageId);

    /**
     * Get read status for multiple messages (batch operation)
     * Used when loading message history
     *
     * @param messageIds List of message IDs
     * @return Map of messageId -> Set of userIds who read it
     */
    Map<UUID, Set<String>> getReadStatusForMessages(List<UUID> messageIds);

    /**
     * Get the last message ID read by a user in a room
     * Used to determine read/unread status
     *
     * @param roomId Room ID
     * @param userId User ID
     * @return Last read message ID, or null if none
     */
    UUID getLastReadMessageId(String roomId, String userId);

    /**
     * Count how many users read a specific message
     *
     * @param messageId Message ID
     * @return Number of users who read the message
     */
    long getReadCount(UUID messageId);

    /**
     * Check if a user has read a specific message
     *
     * @param messageId Message ID
     * @param userId User ID
     * @return true if the user has read the message
     */
    boolean hasUserRead(UUID messageId, String userId);
}
