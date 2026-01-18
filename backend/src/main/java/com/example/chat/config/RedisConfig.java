package com.example.chat.config;

import org.springframework.context.annotation.Configuration;

/**
 * Redis configuration for real-time features
 * - Pub/Sub: WebSocket message fanout across server instances
 * - Cache: Recent messages (List or Stream)
 * - Rate limiting: IP/User based restrictions
 *
 * Implementation in Phase 1
 */
@Configuration
public class RedisConfig {

    // Redis channels
    public static final String CHANNEL_CHAT_FANOUT = "chat:fanout";

    // Cache keys
    public static final String CACHE_RECENT_MESSAGES = "chat:recent:";
    public static final String CACHE_ROOM_INFO = "chat:room:";

    // Rate limit keys
    public static final String RATE_LIMIT_IP = "rate:ip:";
    public static final String RATE_LIMIT_USER = "rate:user:";

}
