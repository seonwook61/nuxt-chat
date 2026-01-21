package com.example.chat.repository;

import com.example.chat.entity.ChatMessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, UUID> {

    /**
     * Find messages by room ID ordered by timestamp descending
     */
    List<ChatMessageEntity> findByRoomIdOrderByTimestampDesc(String roomId, Pageable pageable);

    /**
     * Find messages by room ID before a specific timestamp
     */
    List<ChatMessageEntity> findByRoomIdAndTimestampBeforeOrderByTimestampDesc(
        String roomId,
        LocalDateTime timestamp,
        Pageable pageable
    );

    /**
     * Count total messages in a room
     */
    long countByRoomId(String roomId);

    /**
     * Delete old messages before a specific timestamp
     */
    void deleteByTimestampBefore(LocalDateTime timestamp);

    /**
     * Find messages by user ID
     */
    List<ChatMessageEntity> findByUserId(String userId);
}
