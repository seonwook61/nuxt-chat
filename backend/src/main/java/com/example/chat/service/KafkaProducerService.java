package com.example.chat.service;

import com.example.chat.dto.ChatEvent;
import com.example.chat.dto.ChatMessage;
import com.example.chat.dto.MessageReaction;

/**
 * Kafka Producer Service Interface
 *
 * Sends messages and events to Kafka topics
 * - chat.message.v1: Chat messages
 * - chat.event.v1: User presence events
 * - chat.reaction.v1: Message reactions
 *
 * Implementation in Phase 1 & Phase 3.2
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

    /**
     * Send a message reaction to Kafka
     *
     * @param reaction MessageReaction to send
     * @throws IllegalArgumentException if reaction is null
     */
    void sendReaction(MessageReaction reaction);
}
