package com.example.chat.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

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

    /**
     * Configure ObjectMapper with Java 8+ date/time support
     * - Register JavaTimeModule for LocalDateTime, Instant, etc.
     * - Used by Kafka, Redis, and REST serialization
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    /**
     * Configure RedisTemplate with JSON serialization
     * - Key: String
     * - Value: JSON-serialized Object
     * - Handles ChatMessage and other DTOs
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Configure JSON serializer for values
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer =
            new Jackson2JsonRedisSerializer<>(Object.class);

        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(
            BasicPolymorphicTypeValidator.getInstance(),
            ObjectMapper.DefaultTyping.NON_FINAL
        );
        jackson2JsonRedisSerializer.setObjectMapper(om);

        // String serializer for keys
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // Set key-value serialization
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(jackson2JsonRedisSerializer);

        // Set hash key-value serialization
        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

}
