package com.example.chat.service;

import com.example.chat.dto.ChatMessage;
import com.example.chat.dto.MessageReaction;
import com.example.chat.entity.ChatMessageEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface MessagePersistenceService {

    /**
     * Save a chat message to the database
     */
    void saveMessage(ChatMessage message);

    /**
     * Save a message reaction to the database
     */
    void saveReaction(MessageReaction reaction);

    /**
     * Remove a message reaction from the database
     */
    void removeReaction(MessageReaction reaction);

    /**
     * Get message history for a room (most recent first)
     */
    List<ChatMessageEntity> getMessageHistory(String roomId, int limit);

    /**
     * Get message history before a specific timestamp (pagination)
     */
    List<ChatMessageEntity> getMessageHistoryBefore(String roomId, LocalDateTime before, int limit);

    /**
     * Ensure a chat room exists in the database
     */
    void ensureRoomExists(String roomId);

    /**
     * Delete old messages (cleanup task)
     */
    void deleteOldMessages(LocalDateTime before);
}
