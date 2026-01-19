package com.example.chat.service;

import com.example.chat.dto.ChatEvent;
import com.example.chat.dto.ChatMessage;

/**
 * Kafka Producer Service Interface
 *
 * Sends messages and events to Kafka topics
 * - chat.message.v1: Chat messages
 * - chat.event.v1: User presence events
 *
 * Implementation in Phase 1
 */
public interface KafkaProducerService {

    /**
     * Send a chat message to Kafka
     *
     * @param message ChatMessage to send
     * @throws IllegalArgumentException if message is null
     */
    void sendMessage(ChatMessage message);

    /**
     * Send a chat event to Kafka
     *
     * @param event ChatEvent to send
     * @throws IllegalArgumentException if event is null
     */
    void sendEvent(ChatEvent event);
}
