package com.example.chat.controller;

import com.example.chat.config.WebSocketConfig;
import com.example.chat.dto.ChatEvent;
import com.example.chat.dto.ChatMessage;
import com.example.chat.dto.MessageReaction;
import com.example.chat.service.KafkaProducerService;
import com.example.chat.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * WebSocket Controller for Real-Time Chat
 *
 * Handles WebSocket STOMP messages:
 * - /app/chat.join -> handleJoin -> /topic/room/{roomId}
 * - /app/chat.send -> handleMessage -> /topic/room/{roomId}
 * - /app/chat.leave -> handleLeave -> /topic/room/{roomId}
 * - /app/chat.reaction -> handleReaction -> /topic/room/{roomId}
 *
 * TDD Phase 1 & Phase 3.2: Implementation to make tests GREEN
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final KafkaProducerService kafkaProducerService;
    private final RedisCacheService redisCacheService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle user joining a room
     * - Add user to Redis presence
     * - Send join event via Kafka
     * - Broadcast to all room subscribers
     *
     * @param event ChatEvent with userId and roomId
     */
    @MessageMapping("/chat.join")
    public void handleJoin(ChatEvent event) {
        log.info("User {} joining room {}", event.getUserId(), event.getRoomId());

        try {
            // Add user to Redis room presence
            redisCacheService.addUserToRoom(event.getRoomId(), event.getUserId());

            // Get current online user count
            long onlineCount = redisCacheService.getRoomUserCount(event.getRoomId());

            // Add online count to metadata
            if (event.getMetadata() == null) {
                event.setMetadata(new java.util.HashMap<>());
            }
            event.getMetadata().put("onlineCount", onlineCount);

            // Send join event to Kafka
            kafkaProducerService.sendEvent(event);

            // Broadcast to all subscribers of the room topic
            String topic = WebSocketConfig.WS_TOPIC_PREFIX + "/room/" + event.getRoomId();
            messagingTemplate.convertAndSend(topic, event);

            log.debug("User join event broadcast: roomId={}, onlineCount={}", event.getRoomId(), onlineCount);
        } catch (Exception e) {
            log.error("Error handling user join: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle incoming chat message
     * - Send to Kafka for persistence
     * - Broadcast to all room subscribers via WebSocket
     *
     * @param message ChatMessage from client
     */
    @MessageMapping("/chat.send")
    public void handleMessage(ChatMessage message) {
        log.info("Message from {} in room {}", message.getUserId(), message.getRoomId());

        try {
            // Send message to Kafka for persistence
            kafkaProducerService.sendMessage(message);

            // Broadcast to all subscribers of the room topic
            String topic = WebSocketConfig.WS_TOPIC_PREFIX + "/room/" + message.getRoomId();
            messagingTemplate.convertAndSend(topic, message);

            log.debug("Message broadcast: messageId={}, roomId={}", message.getMessageId(), message.getRoomId());
        } catch (Exception e) {
            log.error("Error handling message: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle user leaving a room
     * - Remove user from Redis presence
     * - Send leave event via Kafka
     * - Broadcast to all room subscribers
     *
     * @param event ChatEvent with userId and roomId
     */
    @MessageMapping("/chat.leave")
    public void handleLeave(ChatEvent event) {
        log.info("User {} leaving room {}", event.getUserId(), event.getRoomId());

        try {
            // Remove user from Redis room presence
            redisCacheService.removeUserFromRoom(event.getRoomId(), event.getUserId());

            // Get current online user count (after removal)
            long onlineCount = redisCacheService.getRoomUserCount(event.getRoomId());

            // Add online count to metadata
            if (event.getMetadata() == null) {
                event.setMetadata(new java.util.HashMap<>());
            }
            event.getMetadata().put("onlineCount", onlineCount);

            // Send leave event to Kafka
            kafkaProducerService.sendEvent(event);

            // Broadcast to all subscribers of the room topic
            String topic = WebSocketConfig.WS_TOPIC_PREFIX + "/room/" + event.getRoomId();
            messagingTemplate.convertAndSend(topic, event);

            log.debug("User leave event broadcast: roomId={}, onlineCount={}", event.getRoomId(), onlineCount);
        } catch (Exception e) {
            log.error("Error handling user leave: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle message reaction (add/remove)
     * - Update Redis reaction cache
     * - Send reaction event via Kafka
     * - Broadcast to all room subscribers
     *
     * @param reaction MessageReaction from client
     */
    @MessageMapping("/chat.reaction")
    public void handleReaction(MessageReaction reaction) {
        log.info("Reaction {} from user {} on message {} in room {}",
                reaction.getEmoji(), reaction.getUserId(), reaction.getMessageId(), reaction.getRoomId());

        try {
            // Generate reaction ID and timestamp if not provided
            if (reaction.getReactionId() == null) {
                reaction.setReactionId(UUID.randomUUID());
            }
            if (reaction.getTimestamp() == null) {
                reaction.setTimestamp(LocalDateTime.now());
            }

            // Update Redis based on action
            if ("ADD".equals(reaction.getAction())) {
                redisCacheService.addReaction(
                        reaction.getMessageId(),
                        reaction.getEmoji(),
                        reaction.getUserId()
                );
            } else if ("REMOVE".equals(reaction.getAction())) {
                redisCacheService.removeReaction(
                        reaction.getMessageId(),
                        reaction.getEmoji(),
                        reaction.getUserId()
                );
            }

            // Send reaction event to Kafka for persistence/analytics
            kafkaProducerService.sendReaction(reaction);

            // Broadcast to all subscribers of the room topic
            String topic = WebSocketConfig.WS_TOPIC_PREFIX + "/room/" + reaction.getRoomId();
            messagingTemplate.convertAndSend(topic, reaction);

            log.debug("Reaction broadcast: messageId={}, emoji={}, action={}, roomId={}",
                    reaction.getMessageId(), reaction.getEmoji(), reaction.getAction(), reaction.getRoomId());
        } catch (Exception e) {
            log.error("Error handling reaction: {}", e.getMessage(), e);
        }
    }

    /**
     * Exception handler for WebSocket message processing errors
     * Logs errors without throwing (graceful error handling)
     *
     * @param exception Exception thrown during message processing
     */
    @MessageMapping("/error")
    public void handleException(Exception exception) {
        log.error("WebSocket error: {}", exception.getMessage(), exception);
    }
}
