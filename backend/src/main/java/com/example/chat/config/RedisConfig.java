package com.example.chat.config;

import org.springframework.context.annotation.Configuration;

/**
 * Redis configuration for real-time features (TRD: 02-trd.md)
 * - Pub/Sub: WebSocket message fanout across server instances
 * - Cache: Recent messages (List or Stream), room users (Set)
 * - Rate limiting: IP/User based restrictions
 *
 * Key patterns (TRD 02-trd.md):
 * - room:{roomId}:recent -> Recent messages (List, TTL 10min)
 * - room:{roomId}:users -> Current users (Set, TTL 5min)
 *
 * Implementation in Phase 1
 */
@Configuration
public class RedisConfig {

    // Redis channels for Pub/Sub
    public static final String CHANNEL_CHAT_FANOUT = "chat:fanout";

    // Cache key prefixes (TRD 02-trd.md)
    public static final String CACHE_RECENT_MESSAGES = "room:";
    public static final String CACHE_ROOM_USERS = "room:";

    // Cache key suffixes
    public static final String RECENT_MESSAGES_SUFFIX = ":recent";
    public static final String ROOM_USERS_SUFFIX = ":users";

    // Rate limit keys
    public static final String RATE_LIMIT_IP = "rate:ip:";
    public static final String RATE_LIMIT_USER = "rate:user:";

    // TTL (seconds)
    public static final int TTL_RECENT_MESSAGES = 600;  // 10 minutes
    public static final int TTL_ROOM_USERS = 300;       // 5 minutes

}
