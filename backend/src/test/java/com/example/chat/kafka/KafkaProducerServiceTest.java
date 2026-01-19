package com.example.chat.kafka;

import com.example.chat.config.KafkaConfig;
import com.example.chat.model.ChatEvent;
import com.example.chat.model.ChatMessage;
import com.example.chat.service.KafkaProducerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Kafka Producer Service Test (RED Phase)
 *
 * Tests the KafkaProducerService for:
 * - Publishing ChatMessage to chat.message.v1 topic
 * - Publishing ChatEvent to chat.event.v1 topic
 * - Error handling for null messages
 *
 * Phase 1에서 KafkaProducerService 구현 시 테스트 통과 예정
 *
 * Dependencies:
 * - EmbeddedKafka (spring-kafka-test)
 * - KafkaTestUtils for consuming test messages
 */
@SpringBootTest
@EmbeddedKafka(
    partitions = 1,
    topics = {KafkaConfig.TOPIC_CHAT_MESSAGE, KafkaConfig.TOPIC_CHAT_EVENT},
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092",
        "port=9092"
    }
)
class KafkaProducerServiceTest {

    @Autowired(required = false)
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
    }

    /**
     * Test: ChatMessage 전송 시 Kafka 토픽에 발행되는지 검증
     *
     * Given: ChatMessage 객체
     * When: kafkaProducerService.sendMessage(message) 호출
     * Then: chat.message.v1 토픽에 메시지가 발행됨
     */
    @Test
    void testSendMessage_Success() throws Exception {
        // GIVEN
        ChatMessage message = ChatMessage.builder()
            .id(UUID.randomUUID().toString())
            .roomId("room-123")
            .userId("user-456")
            .username("testuser")
            .content("Hello, Kafka!")
            .timestamp(Instant.now())
            .type("text")
            .build();

        // WHEN
        // Phase 1에서 구현될 서비스 호출
        kafkaProducerService.sendMessage(message);

        // THEN
        // Kafka 토픽에서 메시지 검증
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
            "test-group",
            "true",
            embeddedKafkaBroker
        );
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        var consumerFactory = new DefaultKafkaConsumerFactory<String, String>(consumerProps);
        var consumer = consumerFactory.createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, KafkaConfig.TOPIC_CHAT_MESSAGE);

        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(
            consumer,
            KafkaConfig.TOPIC_CHAT_MESSAGE,
            Duration.ofSeconds(10)
        );

        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo("room-123");

        ChatMessage receivedMessage = objectMapper.readValue(record.value(), ChatMessage.class);
        assertThat(receivedMessage.getId()).isEqualTo(message.getId());
        assertThat(receivedMessage.getContent()).isEqualTo("Hello, Kafka!");

        consumer.close();
    }

    /**
     * Test: ChatEvent 발행 검증
     *
     * Given: ChatEvent 객체 (user_joined)
     * When: kafkaProducerService.sendEvent(event) 호출
     * Then: chat.event.v1 토픽에 이벤트 발행됨
     */
    @Test
    void testSendEvent_Success() throws Exception {
        // GIVEN
        ChatEvent event = ChatEvent.builder()
            .id(UUID.randomUUID().toString())
            .roomId("room-789")
            .userId("user-101")
            .username("newuser")
            .eventType("user_joined")
            .timestamp(Instant.now())
            .build();

        // WHEN
        kafkaProducerService.sendEvent(event);

        // THEN
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
            "test-event-group",
            "true",
            embeddedKafkaBroker
        );
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        var consumerFactory = new DefaultKafkaConsumerFactory<String, String>(consumerProps);
        var consumer = consumerFactory.createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, KafkaConfig.TOPIC_CHAT_EVENT);

        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(
            consumer,
            KafkaConfig.TOPIC_CHAT_EVENT,
            Duration.ofSeconds(10)
        );

        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo("room-789");

        ChatEvent receivedEvent = objectMapper.readValue(record.value(), ChatEvent.class);
        assertThat(receivedEvent.getEventType()).isEqualTo("user_joined");
        assertThat(receivedEvent.getUsername()).isEqualTo("newuser");

        consumer.close();
    }

    /**
     * Test: Null 메시지 처리
     *
     * Given: Null ChatMessage
     * When: kafkaProducerService.sendMessage(null) 호출
     * Then: IllegalArgumentException 발생
     */
    @Test
    void testSendMessage_Null() {
        // WHEN/THEN
        assertThatThrownBy(() -> kafkaProducerService.sendMessage(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("message");
    }

    /**
     * Test: Null 이벤트 처리
     *
     * Given: Null ChatEvent
     * When: kafkaProducerService.sendEvent(null) 호출
     * Then: IllegalArgumentException 발생
     */
    @Test
    void testSendEvent_Null() {
        // WHEN/THEN
        assertThatThrownBy(() -> kafkaProducerService.sendEvent(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("event");
    }
}
