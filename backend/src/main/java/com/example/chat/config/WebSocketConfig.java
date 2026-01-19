package com.example.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time chat
 * - STOMP over WebSocket (or raw WebSocket + socket.io adapter)
 * - Redis pub/sub for horizontal scaling
 * - Session management
 *
 * Implementation in Phase 1
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // WebSocket endpoints
    public static final String WS_ENDPOINT = "/ws";
    public static final String WS_TOPIC_PREFIX = "/topic";
    public static final String WS_APP_PREFIX = "/app";

    /**
     * Configure message broker
     * - Enable simple broker for /topic destinations
     * - Set application destination prefix to /app
     * - Messages are sent to subscribers of topics
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory message broker for /topic destinations
        config.enableSimpleBroker(WS_TOPIC_PREFIX);

        // Application destination prefix for incoming messages
        config.setApplicationDestinationPrefixes(WS_APP_PREFIX);

        // Set user destination prefix for one-to-one messaging (if needed)
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Register STOMP endpoints
     * - Endpoint: /ws for WebSocket connections
     * - SockJS fallback for browsers without WebSocket support
     * - Allow all origins (for development; restrict in production)
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(WS_ENDPOINT)
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }
}
