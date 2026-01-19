package com.example.chat.redis;

import com.example.chat.model.ChatMessage;
import com.example.chat.service.RedisCacheService;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Redis Cache Service Test (RED Phase)
 *
 * Tests the RedisCacheService for:
 * - Caching recent messages (FIFO, max 50)
 * - Getting recent messages from cache
 * - Adding/removing users from rooms
 * - Getting room user list
 * - TTL expiration (600 seconds)
 *
 * Phase 1에서 RedisCacheService 구현 시 테스트 통과 예정
 *
 * Dependencies:
 * - Testcontainers Redis
 * - Spring Data Redis
 *
 * Redis Key Schema:
 * - room:{roomId}:recent - Recent messages (List)
 * - room:{roomId}:users - Active users (Set)
 */
@SpringBootTest
@Testcontainers
class RedisCacheServiceTest {

    @Container
    static RedisContainer redis = new RedisContainer(
        DockerImageName.parse("redis:7-alpine")
    ).withExposedPorts(6379);

    @Autowired(required = false)
    private RedisCacheService redisCacheService;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    /**
     * Test: 최근 메시지 캐싱
     *
     * Given: ChatMessage 및 roomId
     * When: redisCacheService.cacheRecentMessage(roomId, message) 호출
     * Then: Redis key room:{roomId}:recent에 저장됨
     */
    @Test
    void testCacheRecentMessage_Success() {
        // GIVEN
        String roomId = "room-cache-1";
        ChatMessage message = ChatMessage.builder()
            .id(UUID.randomUUID().toString())
            .roomId(roomId)
            .userId("user-123")
            .username("testuser")
            .content("Test message for caching")
            .timestamp(Instant.now())
            .type("text")
            .build();

        // WHEN
        // Phase 1에서 구현될 서비스 호출
        redisCacheService.cacheRecentMessage(roomId, message);

        // THEN
        // Redis에서 직접 검증
        String redisKey = "room:" + roomId + ":recent";
        List<Object> cachedMessages = redisTemplate.opsForList().range(redisKey, 0, -1);

        assertThat(cachedMessages).isNotNull();
        assertThat(cachedMessages).hasSize(1);

        // TTL 검증
        Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
        assertThat(ttl).isGreaterThan(0).isLessThanOrEqualTo(600);
    }

    /**
     * Test: 캐시 조회
     *
     * Given: 미리 캐시된 메시지들
     * When: getRecentMessages(roomId) 호출
     * Then: 최대 50개 메시지 반환 (FIFO)
     */
    @Test
    void testGetRecentMessages_Success() {
        // GIVEN
        String roomId = "room-cache-2";

        // 3개의 메시지 캐싱
        for (int i = 1; i <= 3; i++) {
            ChatMessage message = ChatMessage.builder()
                .id(UUID.randomUUID().toString())
                .roomId(roomId)
                .userId("user-" + i)
                .username("user" + i)
                .content("Message " + i)
                .timestamp(Instant.now())
                .type("text")
                .build();

            redisCacheService.cacheRecentMessage(roomId, message);
        }

        // WHEN
        List<ChatMessage> recentMessages = redisCacheService.getRecentMessages(roomId);

        // THEN
        assertThat(recentMessages).isNotNull();
        assertThat(recentMessages).hasSize(3);
        assertThat(recentMessages.get(0).getContent()).isEqualTo("Message 1");
        assertThat(recentMessages.get(2).getContent()).isEqualTo("Message 3");
    }

    /**
     * Test: FIFO 및 최대 50개 제한
     *
     * Given: 51개의 메시지 캐싱
     * When: getRecentMessages 호출
     * Then: 최신 50개만 반환 (오래된 1개는 삭제됨)
     */
    @Test
    void testCacheRecentMessage_FIFOLimit() {
        // GIVEN
        String roomId = "room-cache-3";

        // 51개의 메시지 캐싱
        for (int i = 1; i <= 51; i++) {
            ChatMessage message = ChatMessage.builder()
                .id(UUID.randomUUID().toString())
                .roomId(roomId)
                .userId("user-bulk")
                .username("bulkuser")
                .content("Message " + i)
                .timestamp(Instant.now())
                .type("text")
                .build();

            redisCacheService.cacheRecentMessage(roomId, message);
        }

        // WHEN
        List<ChatMessage> recentMessages = redisCacheService.getRecentMessages(roomId);

        // THEN
        assertThat(recentMessages).hasSize(50);
        // 첫 번째 메시지는 삭제되고 2번부터 시작
        assertThat(recentMessages.get(0).getContent()).isEqualTo("Message 2");
        assertThat(recentMessages.get(49).getContent()).isEqualTo("Message 51");
    }

    /**
     * Test: 사용자 추가
     *
     * Given: roomId, userId
     * When: addUserToRoom(roomId, userId) 호출
     * Then: Redis Set room:{roomId}:users에 추가됨
     */
    @Test
    void testAddUserToRoom_Success() {
        // GIVEN
        String roomId = "room-user-1";
        String userId = "user-add-1";

        // WHEN
        redisCacheService.addUserToRoom(roomId, userId);

        // THEN
        String redisKey = "room:" + roomId + ":users";
        Set<Object> members = redisTemplate.opsForSet().members(redisKey);

        assertThat(members).isNotNull();
        assertThat(members).contains(userId);
    }

    /**
     * Test: 사용자 목록 조회
     *
     * Given: roomId에 여러 사용자 추가
     * When: getRoomUsers(roomId) 호출
     * Then: 현재 방의 모든 사용자 반환
     */
    @Test
    void testGetRoomUsers_Success() {
        // GIVEN
        String roomId = "room-user-2";
        redisCacheService.addUserToRoom(roomId, "user-1");
        redisCacheService.addUserToRoom(roomId, "user-2");
        redisCacheService.addUserToRoom(roomId, "user-3");

        // WHEN
        Set<String> users = redisCacheService.getRoomUsers(roomId);

        // THEN
        assertThat(users).isNotNull();
        assertThat(users).hasSize(3);
        assertThat(users).containsExactlyInAnyOrder("user-1", "user-2", "user-3");
    }

    /**
     * Test: 사용자 제거
     *
     * Given: 방에 추가된 사용자
     * When: removeUserFromRoom(roomId, userId) 호출
     * Then: Redis Set에서 제거됨
     */
    @Test
    void testRemoveUserFromRoom_Success() {
        // GIVEN
        String roomId = "room-user-3";
        String userId = "user-remove-1";
        redisCacheService.addUserToRoom(roomId, userId);

        // WHEN
        redisCacheService.removeUserFromRoom(roomId, userId);

        // THEN
        Set<String> users = redisCacheService.getRoomUsers(roomId);
        assertThat(users).doesNotContain(userId);
    }

    /**
     * Test: TTL 검증
     *
     * Given: 메시지 캐시 (TTL 600초)
     * When: TTL 확인
     * Then: Redis에서 600초 이내 TTL 설정됨
     */
    @Test
    void testTTL_Expires() {
        // GIVEN
        String roomId = "room-ttl-1";
        ChatMessage message = ChatMessage.builder()
            .id(UUID.randomUUID().toString())
            .roomId(roomId)
            .userId("user-ttl")
            .username("ttluser")
            .content("TTL test message")
            .timestamp(Instant.now())
            .type("text")
            .build();

        // WHEN
        redisCacheService.cacheRecentMessage(roomId, message);

        // THEN
        String redisKey = "room:" + roomId + ":recent";
        Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);

        assertThat(ttl).isNotNull();
        assertThat(ttl).isGreaterThan(0);
        assertThat(ttl).isLessThanOrEqualTo(600);
    }
}
