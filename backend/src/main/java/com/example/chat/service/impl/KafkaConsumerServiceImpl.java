package com.example.chat.service.impl;

import com.example.chat.config.KafkaConfig;
import com.example.chat.dto.ChatEvent;
import com.example.chat.dto.ChatMessage;
import com.example.chat.dto.MessageReaction;
import com.example.chat.service.KafkaConsumerService;
import com.example.chat.service.MessagePersistenceService;
import com.example.chat.service.RedisCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Kafka Consumer Service Implementation
 *
 * Consumes messages and events from Kafka topics:
 * - chat.message.v1: Process incoming chat messages (cache in Redis, persist to PostgreSQL)
 * - chat.event.v1: Process user presence events (update presence)
 * - chat.reaction.v1: Process message reactions (cache in Redis, persist to PostgreSQL)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerServiceImpl implements KafkaConsumerService {

    private final RedisCacheService redisCacheService;
    private final MessagePersistenceService persistenceService;
    private final ObjectMapper objectMapper;

    /**
     * Handle incoming chat message from Kafka
     * - Deserialize JSON to ChatMessage
     * - Cache in Redis
     * - Log message receipt
     *
     * @param messageJson JSON string of ChatMessage from Kafka
     */
    @KafkaListener(
        topics = KafkaConfig.TOPIC_CHAT_MESSAGE,
        groupId = KafkaConfig.GROUP_WEBSOCKET_FANOUT,
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleChatMessage(@Payload String messageJson) {
        try {
            ChatMessage message = objectMapper.readValue(messageJson, ChatMessage.class);

            log.debug("Received chat message from Kafka: messageId={}, roomId={}, userId={}",
                message.getMessageId(), message.getRoomId(), message.getUserId());

            // Cache the message in Redis for recent messages
            redisCacheService.cacheRecentMessage(message.getRoomId(), message);

            // Persist the message to PostgreSQL
            persistenceService.saveMessage(message);

            log.debug("Cached and persisted message: roomId={}", message.getRoomId());
        } catch (Exception e) {
            log.error("Error processing chat message: {}", e.getMessage(), e);
            // Don't throw exception - continue processing next message
        }
    }

    /**
     * Handle incoming chat event from Kafka
     * - Deserialize JSON to ChatEvent
     * - Process event (e.g., update presence)
     * - Log event receipt
     *
     * @param eventJson JSON string of ChatEvent from Kafka
     */
    @KafkaListener(
        topics = KafkaConfig.TOPIC_CHAT_EVENT,
        groupId = KafkaConfig.GROUP_EVENT_HANDLER,
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleChatEvent(@Payload String eventJson) {
        try {
            ChatEvent event = objectMapper.readValue(eventJson, ChatEvent.class);

            log.debug("Received chat event from Kafka: eventId={}, eventType={}, roomId={}",
                event.getEventId(), event.getEventType(), event.getRoomId());

            // Process event based on type
            handleEventByType(event);

            log.debug("Processed event: eventType={}", event.getEventType());
        } catch (Exception e) {
            log.error("Error processing chat event: {}", e.getMessage(), e);
            // Don't throw exception - continue processing next message
        }
    }

    /**
     * Handle incoming message reaction from Kafka
     * - Deserialize JSON to MessageReaction
     * - Persist to PostgreSQL
     * - Update Redis cache
     *
     * @param reactionJson JSON string of MessageReaction from Kafka
     */
    @KafkaListener(
        topics = KafkaConfig.TOPIC_CHAT_REACTION,
        groupId = KafkaConfig.GROUP_PERSIST_STORE,
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleMessageReaction(@Payload String reactionJson) {
        try {
            MessageReaction reaction = objectMapper.readValue(reactionJson, MessageReaction.class);

            log.debug("Received reaction from Kafka: messageId={}, userId={}, emoji={}, action={}",
                reaction.getMessageId(), reaction.getUserId(), reaction.getEmoji(), reaction.getAction());

            // Persist based on action
            if ("ADD".equals(reaction.getAction())) {
                persistenceService.saveReaction(reaction);
            } else if ("REMOVE".equals(reaction.getAction())) {
                persistenceService.removeReaction(reaction);
            }

            log.debug("Persisted reaction: action={}", reaction.getAction());
        } catch (Exception e) {
            log.error("Error processing message reaction: {}", e.getMessage(), e);
            // Don't throw exception - continue processing next message
        }
    }

    /**
     * Handle event based on event type
     * - USER_JOINED: Add user to room presence
     * - USER_LEFT: Remove user from room presence
     * - MESSAGE_SENT: Log event
     *
     * @param event ChatEvent to process
     */
    private void handleEventByType(ChatEvent event) {
        String eventType = event.getEventType();

        switch (eventType) {
            case "USER_JOINED":
                if (event.getUserId() != null) {
                    redisCacheService.addUserToRoom(event.getRoomId(), event.getUserId());
                    log.debug("User joined room: userId={}, roomId={}",
                        event.getUserId(), event.getRoomId());
                }
                break;

            case "USER_LEFT":
                if (event.getUserId() != null) {
                    redisCacheService.removeUserFromRoom(event.getRoomId(), event.getUserId());
                    log.debug("User left room: userId={}, roomId={}",
                        event.getUserId(), event.getRoomId());
                }
                break;

            case "MESSAGE_SENT":
                log.debug("Message event: roomId={}", event.getRoomId());
                break;

            default:
                log.warn("Unknown event type: {}", eventType);
        }
    }
}
