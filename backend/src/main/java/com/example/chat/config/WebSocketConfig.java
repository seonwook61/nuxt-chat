package com.example.chat.config;

import org.springframework.context.annotation.Configuration;

/**
 * WebSocket configuration for real-time chat
 * - STOMP over WebSocket (or raw WebSocket + socket.io adapter)
 * - Redis pub/sub for horizontal scaling
 * - Session management
 *
 * Implementation in Phase 1
 */
@Configuration
public class WebSocketConfig {

    // WebSocket endpoints
    public static final String WS_ENDPOINT = "/ws";
    public static final String WS_TOPIC_PREFIX = "/topic";
    public static final String WS_APP_PREFIX = "/app";

}
