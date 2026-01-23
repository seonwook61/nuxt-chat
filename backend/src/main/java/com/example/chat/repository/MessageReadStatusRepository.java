package com.example.chat.repository;

import com.example.chat.entity.MessageReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for MessageReadStatus entity
 *
 * Provides data access methods for read receipt tracking.
 */
@Repository
public interface MessageReadStatusRepository extends JpaRepository<MessageReadStatus, Long> {

    /**
     * Find read receipt for a specific message and user
     *
     * @param messageId Message ID
     * @param userId User ID
     * @return Optional read receipt
     */
    Optional<MessageReadStatus> findByMessageIdAndUserId(UUID messageId, String userId);

    /**
     * Find all users who read a specific message
     *
     * @param messageId Message ID
     * @return List of read receipts
     */
    List<MessageReadStatus> findByMessageId(UUID messageId);

    /**
     * Find read receipts for multiple messages
     * Used for batch loading when displaying message history
     *
     * @param messageIds List of message IDs
     * @return List of read receipts
     */
    List<MessageReadStatus> findByMessageIdIn(List<UUID> messageIds);

    /**
     * Get the last read message ID for a user in a room
     *
     * @param roomId Room ID
     * @param userId User ID
     * @return Optional of the most recent read receipt
     */
    @Query("SELECT mrs FROM MessageReadStatus mrs " +
           "WHERE mrs.roomId = :roomId AND mrs.userId = :userId " +
           "ORDER BY mrs.readAt DESC")
    Optional<MessageReadStatus> findLastReadByRoomAndUser(
        @Param("roomId") String roomId,
        @Param("userId") String userId
    );

    /**
     * Count how many users read a specific message
     *
     * @param messageId Message ID
     * @return Number of users who read the message
     */
    long countByMessageId(UUID messageId);

    /**
     * Check if a user has read a specific message
     *
     * @param messageId Message ID
     * @param userId User ID
     * @return true if the user has read the message
     */
    boolean existsByMessageIdAndUserId(UUID messageId, String userId);

    /**
     * Delete read receipts older than a certain date
     * Used for data retention/cleanup
     *
     * @param cutoffDate Cutoff date
     */
    @Query("DELETE FROM MessageReadStatus mrs WHERE mrs.createdAt < :cutoffDate")
    void deleteOlderThan(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}
