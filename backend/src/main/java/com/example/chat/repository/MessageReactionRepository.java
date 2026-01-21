package com.example.chat.repository;

import com.example.chat.entity.MessageReactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageReactionRepository extends JpaRepository<MessageReactionEntity, UUID> {

    /**
     * Find all reactions for a specific message
     */
    List<MessageReactionEntity> findByMessageId(UUID messageId);

    /**
     * Find a specific reaction by message, user, and emoji
     */
    Optional<MessageReactionEntity> findByMessageIdAndUserIdAndEmoji(
        UUID messageId,
        String userId,
        String emoji
    );

    /**
     * Delete a specific reaction
     */
    @Modifying
    void deleteByMessageIdAndUserIdAndEmoji(UUID messageId, String userId, String emoji);

    /**
     * Delete all reactions for a message
     */
    @Modifying
    void deleteByMessageId(UUID messageId);

    /**
     * Count reactions for a message
     */
    long countByMessageId(UUID messageId);

    /**
     * Find all reactions by a user
     */
    List<MessageReactionEntity> findByUserId(String userId);
}
