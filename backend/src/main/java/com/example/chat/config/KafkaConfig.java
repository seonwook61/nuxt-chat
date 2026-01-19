package com.example.chat.config;

import org.springframework.context.annotation.Configuration;

/**
 * Kafka configuration for chat message streaming
 * - Producer: Chat messages from REST API
 * - Consumer: WebSocket fanout, persistence, events
 * - Topics: chat.message.v1, chat.event.v1
 * - Key: roomId (for partition affinity)
 *
 * Implementation in Phase 1
 */
@Configuration
public class KafkaConfig {

    // Topic definitions (TRD: 02-trd.md)
    public static final String TOPIC_CHAT_MESSAGE = "chat.message.v1";
    public static final String TOPIC_CHAT_EVENT = "chat.event.v1";

    // Consumer groups
    public static final String GROUP_WEBSOCKET_FANOUT = "websocket-fanout";
    public static final String GROUP_PERSIST_STORE = "persist-store";
    public static final String GROUP_EVENT_HANDLER = "event-handler";

}
