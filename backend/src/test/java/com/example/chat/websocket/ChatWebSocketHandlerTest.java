package com.example.chat.websocket;

import com.example.chat.dto.ChatEvent;
import com.example.chat.dto.ChatMessage;
import com.example.chat.service.KafkaProducerService;
import com.example.chat.service.RedisCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * WebSocket Handler Test (RED Phase)
 *
 * Tests the WebSocket handlers for:
 * - Join room: @MessageMapping("/chat.join")
 * - Send message: @MessageMapping("/chat.send")
 * - Leave room: @MessageMapping("/chat.leave")
 * - Broadcast to all subscribers
 *
 * Phase 1에서 ChatWebSocketHandler 구현 시 테스트 통과 예정
 *
 * Dependencies:
 * - Spring WebSocket Test
 * - SockJS client
 * - MockBean for KafkaProducerService, RedisCacheService
 *
 * WebSocket Endpoints:
 * - /ws (SockJS endpoint)
 * - /topic/room/{roomId} (subscribe destination)
 * - /app/chat.join, /app/chat.send, /app/chat.leave (message destinations)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatWebSocketHandlerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KafkaProducerService kafkaProducerService;

    @MockBean
    private RedisCacheService redisCacheService;

    private WebSocketStompClient stompClient;
    private String wsUrl;

    @BeforeEach
    void setUp() {
        wsUrl = "ws://localhost:" + port + "/ws";

        // SockJS + STOMP 클라이언트 설정
        var sockJsClient = new SockJsClient(
            List.of(new WebSocketTransport(new StandardWebSocketClient()))
        );

        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    /**
     * Test: 방 입장 처리
     *
     * Given: WebSocket 연결, joinRoom 메시지
     * When: /app/chat.join 메시지 전송
     * Then:
     * - User를 Redis Set에 추가 (verify)
     * - user_joined 이벤트 브로드캐스트 (verify)
     */
    @Test
    void testJoinRoom_Success() throws Exception {
        // GIVEN
        String roomId = "room-join-1";
        String userId = "user-join-1";
        BlockingQueue<ChatEvent> receivedEvents = new LinkedBlockingQueue<>();

        StompSession session = stompClient
            .connectAsync(wsUrl, new StompSessionHandlerAdapter() {})
            .get(5, TimeUnit.SECONDS);

        // Subscribe to room topic
        session.subscribe("/topic/room/" + roomId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatEvent.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                receivedEvents.add((ChatEvent) payload);
            }
        });

        ChatEvent joinEvent = ChatEvent.builder()
            .messageId(UUID.randomUUID())
            .roomId(roomId)
            .userId(userId)
            .username("joinuser")
            .eventType("user_joined")
            .timestamp(Instant.now())
            .build();

        // WHEN
        session.send("/app/chat.join", joinEvent);

        // THEN
        ChatEvent received = receivedEvents.poll(5, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.getEventType()).isEqualTo("user_joined");
        assertThat(received.getUserId()).isEqualTo(userId);

        verify(redisCacheService, timeout(3000).atLeastOnce())
            .addUserToRoom(eq(roomId), eq(userId));

        session.disconnect();
    }

    /**
     * Test: 메시지 전송
     *
     * Given: 연결된 WebSocket, send_message 이벤트
     * When: /app/chat.send 메시지 전송
     * Then:
     * - Kafka에 발행 (verify)
     * - /topic/room/{roomId}로 브로드캐스트 (verify)
     */
    @Test
    void testSendMessage_Success() throws Exception {
        // GIVEN
        String roomId = "room-send-1";
        BlockingQueue<ChatMessage> receivedMessages = new LinkedBlockingQueue<>();

        StompSession session = stompClient
            .connectAsync(wsUrl, new StompSessionHandlerAdapter() {})
            .get(5, TimeUnit.SECONDS);

        // Subscribe to room topic
        session.subscribe("/topic/room/" + roomId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                receivedMessages.add((ChatMessage) payload);
            }
        });

        ChatMessage message = ChatMessage.builder()
            .messageId(UUID.randomUUID())
            .roomId(roomId)
            .userId("user-send-1")
            .username("sender")
            .content("Hello WebSocket!")
            .timestamp(Instant.now())
            .type("text")
            .build();

        // WHEN
        session.send("/app/chat.send", message);

        // THEN
        ChatMessage received = receivedMessages.poll(5, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.getContent()).isEqualTo("Hello WebSocket!");

        verify(kafkaProducerService, timeout(3000).atLeastOnce())
            .sendMessage(argThat(msg -> msg.getContent().equals("Hello WebSocket!")));

        session.disconnect();
    }

    /**
     * Test: 방 퇴장
     *
     * Given: 연결된 사용자
     * When: /app/chat.leave 메시지 전송
     * Then:
     * - Redis에서 사용자 제거 (verify)
     * - user_left 이벤트 브로드캐스트 (verify)
     */
    @Test
    void testLeaveRoom_Success() throws Exception {
        // GIVEN
        String roomId = "room-leave-1";
        String userId = "user-leave-1";
        BlockingQueue<ChatEvent> receivedEvents = new LinkedBlockingQueue<>();

        StompSession session = stompClient
            .connectAsync(wsUrl, new StompSessionHandlerAdapter() {})
            .get(5, TimeUnit.SECONDS);

        session.subscribe("/topic/room/" + roomId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatEvent.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                receivedEvents.add((ChatEvent) payload);
            }
        });

        ChatEvent leaveEvent = ChatEvent.builder()
            .messageId(UUID.randomUUID())
            .roomId(roomId)
            .userId(userId)
            .username("leaveuser")
            .eventType("user_left")
            .timestamp(Instant.now())
            .build();

        // WHEN
        session.send("/app/chat.leave", leaveEvent);

        // THEN
        ChatEvent received = receivedEvents.poll(5, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.getEventType()).isEqualTo("user_left");

        verify(redisCacheService, timeout(3000).atLeastOnce())
            .removeUserFromRoom(eq(roomId), eq(userId));

        session.disconnect();
    }

    /**
     * Test: 브로드캐스트 검증
     *
     * Given: 같은 방의 여러 클라이언트
     * When: 한 클라이언트가 메시지 전송
     * Then: 모든 구독자가 메시지 수신
     */
    @Test
    void testBroadcast_AllSubscribers() throws Exception {
        // GIVEN
        String roomId = "room-broadcast-1";

        BlockingQueue<ChatMessage> client1Messages = new LinkedBlockingQueue<>();
        BlockingQueue<ChatMessage> client2Messages = new LinkedBlockingQueue<>();

        // Client 1 연결
        StompSession session1 = stompClient
            .connectAsync(wsUrl, new StompSessionHandlerAdapter() {})
            .get(5, TimeUnit.SECONDS);

        session1.subscribe("/topic/room/" + roomId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                client1Messages.add((ChatMessage) payload);
            }
        });

        // Client 2 연결
        StompSession session2 = stompClient
            .connectAsync(wsUrl, new StompSessionHandlerAdapter() {})
            .get(5, TimeUnit.SECONDS);

        session2.subscribe("/topic/room/" + roomId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                client2Messages.add((ChatMessage) payload);
            }
        });

        ChatMessage message = ChatMessage.builder()
            .messageId(UUID.randomUUID())
            .roomId(roomId)
            .userId("user-broadcast")
            .username("broadcaster")
            .content("Broadcast message")
            .timestamp(Instant.now())
            .type("text")
            .build();

        // WHEN - Client 1에서 메시지 전송
        session1.send("/app/chat.send", message);

        // THEN - 두 클라이언트 모두 수신
        ChatMessage client1Received = client1Messages.poll(5, TimeUnit.SECONDS);
        ChatMessage client2Received = client2Messages.poll(5, TimeUnit.SECONDS);

        assertThat(client1Received).isNotNull();
        assertThat(client1Received.getContent()).isEqualTo("Broadcast message");

        assertThat(client2Received).isNotNull();
        assertThat(client2Received.getContent()).isEqualTo("Broadcast message");

        session1.disconnect();
        session2.disconnect();
    }
}
