package com.example.chat.service.impl;

import com.example.chat.config.KafkaConfig;
import com.example.chat.dto.ChatEvent;
import com.example.chat.dto.ChatMessage;
import com.example.chat.dto.MessageReaction;
import com.example.chat.service.KafkaProducerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

/**
 * Kafka Producer Service Implementation
 *
 * Publishes chat messages and events to Kafka topics:
 * - chat.message.v1: Chat messages (partition key: roomId)
 * - chat.event.v1: System events (partition key: roomId)
 * - chat.reaction.v1: Message reactions (partition key: roomId)
 *
 * TDD Phase 1 & Phase 3.2: Implementation to make tests GREEN
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Send a chat message to Kafka
     *
     * @param message ChatMessage to publish
     * @throws IllegalArgumentException if message is null
     */
    @Override
    public void sendMessage(ChatMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("message cannot be null");
        }

        try {
            String messageJson = objectMapper.writeValueAsString(message);
            String partitionKey = message.getRoomId();

            // Create message with partition key (roomId)
            Message<String> kafkaMessage = MessageBuilder
                .withPayload(messageJson)
                .setHeader(KafkaHeaders.TOPIC, KafkaConfig.TOPIC_CHAT_MESSAGE)
                .setHeader(KafkaHeaders.KEY, partitionKey)
                .build();

            kafkaTemplate.send(kafkaMessage);

            log.debug("Published message to Kafka topic {}: messageId={}, roomId={}",
                KafkaConfig.TOPIC_CHAT_MESSAGE, message.getMessageId(), message.getRoomId());
        } catch (JsonProcessingException e) {
            log.error("Error serializing message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to serialize chat message", e);
        } catch (Exception e) {
            log.error("Error sending message to Kafka: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send chat message to Kafka", e);
        }
    }

    /**
     * Send a chat event to Kafka
     *
     * @param event ChatEvent to publish
     * @throws IllegalArgumentException if event is null
     */
    @Override
    public void sendEvent(ChatEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event cannot be null");
        }

        try {
            String eventJson = objectMapper.writeValueAsString(event);
            String partitionKey = event.getRoomId();

            // Create message with partition key (roomId)
            Message<String> kafkaMessage = MessageBuilder
                .withPayload(eventJson)
                .setHeader(KafkaHeaders.TOPIC, KafkaConfig.TOPIC_CHAT_EVENT)
                .setHeader(KafkaHeaders.KEY, partitionKey)
                .build();

            kafkaTemplate.send(kafkaMessage);

            log.debug("Published event to Kafka topic {}: eventId={}, eventType={}, roomId={}",
                KafkaConfig.TOPIC_CHAT_EVENT, event.getEventId(), event.getEventType(), event.getRoomId());
        } catch (JsonProcessingException e) {
            log.error("Error serializing event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to serialize chat event", e);
        } catch (Exception e) {
            log.error("Error sending event to Kafka: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send chat event to Kafka", e);
        }
    }

    /**
     * Send a message reaction to Kafka
     *
     * @param reaction MessageReaction to publish
     * @throws IllegalArgumentException if reaction is null
     */
    @Override
    public void sendReaction(MessageReaction reaction) {
        if (reaction == null) {
            throw new IllegalArgumentException("reaction cannot be null");
        }

        try {
            String reactionJson = objectMapper.writeValueAsString(reaction);
            String partitionKey = reaction.getRoomId();

            // Create message with partition key (roomId)
            Message<String> kafkaMessage = MessageBuilder
                .withPayload(reactionJson)
                .setHeader(KafkaHeaders.TOPIC, KafkaConfig.TOPIC_CHAT_REACTION)
                .setHeader(KafkaHeaders.KEY, partitionKey)
                .build();

            kafkaTemplate.send(kafkaMessage);

            log.debug("Published reaction to Kafka topic {}: reactionId={}, messageId={}, emoji={}, action={}, roomId={}",
                KafkaConfig.TOPIC_CHAT_REACTION, reaction.getReactionId(), reaction.getMessageId(),
                reaction.getEmoji(), reaction.getAction(), reaction.getRoomId());
        } catch (JsonProcessingException e) {
            log.error("Error serializing reaction: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to serialize message reaction", e);
        } catch (Exception e) {
            log.error("Error sending reaction to Kafka: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send message reaction to Kafka", e);
        }
    }
}
