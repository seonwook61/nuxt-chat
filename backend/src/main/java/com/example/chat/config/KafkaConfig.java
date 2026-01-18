package com.example.chat.config;

import org.springframework.context.annotation.Configuration;

/**
 * Kafka configuration for chat message streaming
 * - Producer: Chat messages from REST API
 * - Consumer: WebSocket fanout, persistence, moderation
 * - Topics: chat.message.v1, chat.moderation.v1
 * - Key: roomId (for partition affinity)
 *
 * Implementation in Phase 1
 */
@Configuration
public class KafkaConfig {

    // Topic definitions
    public static final String TOPIC_CHAT_MESSAGE = "chat.message.v1";
    public static final String TOPIC_CHAT_MODERATION = "chat.moderation.v1";

    // Consumer groups
    public static final String GROUP_WEBSOCKET_FANOUT = "websocket-fanout";
    public static final String GROUP_PERSIST_STORE = "persist-store";
    public static final String GROUP_MODERATION = "moderation";

}
