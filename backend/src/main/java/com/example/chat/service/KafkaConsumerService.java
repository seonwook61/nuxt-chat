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
     * - Deserialize JSON to ChatMessage
     * - Cache in Redis
     * - Broadcast via WebSocket
     *
     * @param messageJson JSON string of ChatMessage from Kafka
     */
    void handleChatMessage(String messageJson);

    /**
     * Handle incoming chat event from Kafka
     * - Deserialize JSON to ChatEvent
     * - Update presence in Redis
     * - Broadcast via WebSocket
     *
     * @param eventJson JSON string of ChatEvent from Kafka
     */
    void handleChatEvent(String eventJson);
}
