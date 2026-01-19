package com.example.chat.kafka;

import com.example.chat.config.KafkaConfig;
import com.example.chat.model.ChatEvent;
import com.example.chat.model.ChatMessage;
import com.example.chat.service.KafkaConsumerService;
import com.example.chat.service.RedisCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Kafka Consumer Service Test (RED Phase)
 *
 * Tests the KafkaConsumerService for:
 * - Consuming ChatMessage from chat.message.v1
 * - Consuming ChatEvent from chat.event.v1
 * - Integration with RedisCacheService
 * - Concurrent message processing
 *
 * Phase 1에서 KafkaConsumerService 구현 시 테스트 통과 예정
 *
 * Dependencies:
 * - EmbeddedKafka
 * - MockBean RedisCacheService
 * - SpyBean KafkaConsumerService (when implemented)
 */
@SpringBootTest
@EmbeddedKafka(
    partitions = 1,
    topics = {KafkaConfig.TOPIC_CHAT_MESSAGE, KafkaConfig.TOPIC_CHAT_EVENT},
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:9093",
        "port=9093"
    }
)
class KafkaConsumerServiceTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean(name = "kafkaConsumerService")
    private KafkaConsumerService kafkaConsumerService;

    @MockBean
    private RedisCacheService redisCacheService;

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9093");
        registry.add("spring.kafka.consumer.group-id", () -> KafkaConfig.GROUP_WEBSOCKET_FANOUT);
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
    }

    /**
     * Test: Kafka에서 메시지 수신 및 처리 검증
     *
     * Given: Kafka 토픽에 ChatMessage 발행
     * When: Consumer가 메시지 수신
     * Then: RedisCacheService.cacheRecentMessage() 호출됨
     */
    @Test
    void testConsumeMessage_Published() throws Exception {
        // GIVEN
        ChatMessage message = ChatMessage.builder()
            .id(UUID.randomUUID().toString())
            .roomId("room-consume-1")
            .userId("user-999")
            .username("consumer-test")
            .content("Test message for consumer")
            .timestamp(Instant.now())
            .type("text")
            .build();

        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(redisCacheService).cacheRecentMessage(anyString(), any(ChatMessage.class));

        // Kafka Producer 설정
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        var producerFactory = new DefaultKafkaProducerFactory<String, String>(producerProps);
        var kafkaTemplate = new KafkaTemplate<>(producerFactory);

        // WHEN
        String messageJson = objectMapper.writeValueAsString(message);
        kafkaTemplate.send(KafkaConfig.TOPIC_CHAT_MESSAGE, "room-consume-1", messageJson);

        // THEN
        boolean received = latch.await(10, TimeUnit.SECONDS);
        assertThat(received).isTrue();

        verify(redisCacheService, timeout(5000).atLeastOnce())
            .cacheRecentMessage(eq("room-consume-1"), argThat(msg ->
                msg.getContent().equals("Test message for consumer")
            ));
    }

    /**
     * Test: 이벤트 수신 검증
     *
     * Given: Kafka 토픽에 ChatEvent 발행
     * When: Consumer 수신
     * Then: 적절한 처리 (로깅, Redis Pub/Sub 발행 등)
     */
    @Test
    void testConsumeEvent_Published() throws Exception {
        // GIVEN
        ChatEvent event = ChatEvent.builder()
            .id(UUID.randomUUID().toString())
            .roomId("room-event-1")
            .userId("user-888")
            .username("event-user")
            .eventType("user_joined")
            .timestamp(Instant.now())
            .build();

        CountDownLatch latch = new CountDownLatch(1);

        // Phase 1에서 구현될 이벤트 핸들러 검증
        // 현재는 consumer가 수신만 확인
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(kafkaConsumerService).handleChatEvent(any(ChatEvent.class));

        // Kafka Producer 설정
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        var producerFactory = new DefaultKafkaProducerFactory<String, String>(producerProps);
        var kafkaTemplate = new KafkaTemplate<>(producerFactory);

        // WHEN
        String eventJson = objectMapper.writeValueAsString(event);
        kafkaTemplate.send(KafkaConfig.TOPIC_CHAT_EVENT, "room-event-1", eventJson);

        // THEN
        boolean received = latch.await(10, TimeUnit.SECONDS);
        assertThat(received).isTrue();

        verify(kafkaConsumerService, timeout(5000).atLeastOnce())
            .handleChatEvent(argThat(e -> e.getEventType().equals("user_joined")));
    }

    /**
     * Test: 동시성 처리
     *
     * Given: 여러 스레드에서 메시지 발행
     * When/Then: 모든 메시지 처리됨 (순서 보장 안 함)
     */
    @Test
    void testConsumeMessage_Concurrent() throws Exception {
        // GIVEN
        int messageCount = 10;
        CountDownLatch latch = new CountDownLatch(messageCount);

        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(redisCacheService).cacheRecentMessage(anyString(), any(ChatMessage.class));

        // Kafka Producer 설정
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        var producerFactory = new DefaultKafkaProducerFactory<String, String>(producerProps);
        var kafkaTemplate = new KafkaTemplate<>(producerFactory);

        // WHEN - 동시에 여러 메시지 발행
        for (int i = 0; i < messageCount; i++) {
            ChatMessage message = ChatMessage.builder()
                .id(UUID.randomUUID().toString())
                .roomId("room-concurrent")
                .userId("user-" + i)
                .username("concurrent-user-" + i)
                .content("Concurrent message " + i)
                .timestamp(Instant.now())
                .type("text")
                .build();

            String messageJson = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(KafkaConfig.TOPIC_CHAT_MESSAGE, "room-concurrent", messageJson);
        }

        // THEN
        boolean allReceived = latch.await(15, TimeUnit.SECONDS);
        assertThat(allReceived).isTrue();

        verify(redisCacheService, timeout(10000).times(messageCount))
            .cacheRecentMessage(eq("room-concurrent"), any(ChatMessage.class));
    }
}
