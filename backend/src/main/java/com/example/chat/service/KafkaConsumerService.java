package com.example.chat.service;

import com.example.chat.dto.ChatEvent;
import com.example.chat.dto.ChatMessage;

/**
 * Kafka Consumer Service Interface
 *
 * Consumes messages and events from Kafka topics
 * - chat.message.v1: Process incoming chat messages
 * - chat.event.v1: Process user presence events
 *
 * Implementation in Phase 1
 */
public interface KafkaConsumerService {

    /**
     * Handle incoming chat message from Kafka
     * - Cache in Redis
     * - Broadcast via WebSocket
     *
     * @param message ChatMessage from Kafka
     */
    void handleChatMessage(ChatMessage message);

    /**
     * Handle incoming chat event from Kafka
     * - Update presence in Redis
     * - Broadcast via WebSocket
     *
     * @param event ChatEvent from Kafka
     */
    void handleChatEvent(ChatEvent event);
}
