# Task 0.5.2: Backend Test Summary (RED Phase)

## Completion Status: COMPLETE

### Overview

Task 0.5.2에서 작성된 모든 백엔드 테스트(RED)가 완료되었습니다.
총 18개의 테스트가 작성되었으며, 모든 테스트는 의도적으로 실패(RED) 상태입니다.

## Test Inventory

### 1. Kafka Producer Service Tests

**File**: `backend/src/test/java/com/example/chat/kafka/KafkaProducerServiceTest.java`

| Test Method | Description | Verification |
|-------------|-------------|--------------|
| `testSendMessage_Success` | ChatMessage 전송 시 Kafka 토픽에 발행 검증 | KafkaTestUtils로 토픽 메시지 확인 |
| `testSendEvent_Success` | ChatEvent 발행 검증 | KafkaTestUtils로 이벤트 확인 |
| `testSendMessage_Null` | Null 메시지 처리 | IllegalArgumentException 발생 |
| `testSendEvent_Null` | Null 이벤트 처리 | IllegalArgumentException 발생 |

**Dependencies**:
- `@EmbeddedKafka` (spring-kafka-test)
- Topics: `chat.message.v1`, `chat.event.v1`
- Serialization: JSON (ObjectMapper)

**Expected Failure**:
```
NoSuchBeanDefinitionException: No qualifying bean of type 'KafkaProducerService'
```

---

### 2. Kafka Consumer Service Tests

**File**: `backend/src/test/java/com/example/chat/kafka/KafkaConsumerServiceTest.java`

| Test Method | Description | Verification |
|-------------|-------------|--------------|
| `testConsumeMessage_Published` | Kafka 메시지 수신 및 Redis 캐싱 검증 | RedisCacheService.cacheRecentMessage() 호출 확인 |
| `testConsumeEvent_Published` | 이벤트 수신 및 처리 검증 | handleChatEvent() 호출 확인 |
| `testConsumeMessage_Concurrent` | 동시성 처리 검증 | 10개 메시지 동시 처리 확인 |

**Dependencies**:
- `@MockBean` RedisCacheService
- `@SpyBean` KafkaConsumerService
- CountDownLatch for async verification

**Expected Failure**:
```
NoSuchBeanDefinitionException: No qualifying bean of type 'KafkaConsumerService'
```

---

### 3. Redis Cache Service Tests

**File**: `backend/src/test/java/com/example/chat/redis/RedisCacheServiceTest.java`

| Test Method | Description | Verification |
|-------------|-------------|--------------|
| `testCacheRecentMessage_Success` | 최근 메시지 캐싱 | Redis List 저장 및 TTL 확인 |
| `testGetRecentMessages_Success` | 캐시 조회 | 3개 메시지 조회 검증 |
| `testCacheRecentMessage_FIFOLimit` | FIFO 및 최대 50개 제한 | 51개 중 최신 50개만 유지 |
| `testAddUserToRoom_Success` | 사용자 추가 | Redis Set에 userId 추가 |
| `testGetRoomUsers_Success` | 사용자 목록 조회 | 3명의 사용자 조회 |
| `testRemoveUserFromRoom_Success` | 사용자 제거 | Redis Set에서 userId 제거 |
| `testTTL_Expires` | TTL 검증 | 600초 TTL 설정 확인 |

**Dependencies**:
- `@Testcontainers` RedisContainer (redis:7-alpine)
- RedisTemplate<String, Object>
- Dynamic port binding

**Redis Key Schema**:
- `room:{roomId}:recent` - Recent messages (List, TTL 600s, max 50)
- `room:{roomId}:users` - Active users (Set)

**Expected Failure**:
```
NoSuchBeanDefinitionException: No qualifying bean of type 'RedisCacheService'
```

---

### 4. WebSocket Handler Tests

**File**: `backend/src/test/java/com/example/chat/websocket/ChatWebSocketHandlerTest.java`

| Test Method | Description | Verification |
|-------------|-------------|--------------|
| `testJoinRoom_Success` | 방 입장 처리 | Redis 사용자 추가 및 브로드캐스트 |
| `testSendMessage_Success` | 메시지 전송 | Kafka 발행 및 WebSocket 브로드캐스트 |
| `testLeaveRoom_Success` | 방 퇴장 | Redis 사용자 제거 및 브로드캐스트 |
| `testBroadcast_AllSubscribers` | 브로드캐스트 검증 | 2개 클라이언트 모두 메시지 수신 |

**Dependencies**:
- `@SpringBootTest(webEnvironment = RANDOM_PORT)`
- WebSocketStompClient + SockJsClient
- `@MockBean` KafkaProducerService, RedisCacheService
- BlockingQueue for async message verification

**WebSocket Configuration**:
- Endpoint: `/ws` (SockJS)
- Subscribe: `/topic/room/{roomId}`
- Send: `/app/chat.join`, `/app/chat.send`, `/app/chat.leave`

**Expected Failure**:
```
No message mapping found for destination '/app/chat.join'
```

---

## Supporting Files Created

### Domain Models

1. **ChatMessage.java**
   ```java
   - id: String (UUID)
   - roomId: String (Kafka partition key)
   - userId: String
   - username: String
   - content: String
   - timestamp: Instant
   - type: String (default: "text")
   ```

2. **ChatEvent.java**
   ```java
   - id: String (UUID)
   - roomId: String
   - userId: String
   - username: String
   - eventType: String (user_joined, user_left, user_typing)
   - timestamp: Instant
   ```

### Service Interfaces

1. **KafkaProducerService.java**
   - `void sendMessage(ChatMessage message)`
   - `void sendEvent(ChatEvent event)`

2. **KafkaConsumerService.java**
   - `void handleChatMessage(ChatMessage message)`
   - `void handleChatEvent(ChatEvent event)`

3. **RedisCacheService.java**
   - `void cacheRecentMessage(String roomId, ChatMessage message)`
   - `List<ChatMessage> getRecentMessages(String roomId)`
   - `void addUserToRoom(String roomId, String userId)`
   - `void removeUserFromRoom(String roomId, String userId)`
   - `Set<String> getRoomUsers(String roomId)`

---

## Dependencies Added

### build.gradle.kts

```kotlin
testImplementation("com.redis:testcontainers-redis:2.0.1")
```

**Existing Dependencies** (confirmed):
- `testImplementation("org.springframework.boot:spring-boot-starter-test")`
- `testImplementation("org.springframework.kafka:spring-kafka-test")`
- `testImplementation("org.testcontainers:testcontainers:1.19.3")`
- `testImplementation("org.testcontainers:kafka:1.19.3")`
- `testImplementation("org.testcontainers:junit-jupiter:1.19.3")`

---

## Test Execution

### Prerequisites

1. **Java 17+** installed
2. **Docker** running (for Testcontainers)
3. **Gradle Wrapper** (included in project)

### Run All Tests

```bash
cd backend
./gradlew test
```

**Current Status**: All tests will fail (RED) - This is expected!

### Run Individual Test Suites

```bash
# Kafka Producer Tests
./gradlew test --tests KafkaProducerServiceTest

# Kafka Consumer Tests
./gradlew test --tests KafkaConsumerServiceTest

# Redis Cache Tests
./gradlew test --tests RedisCacheServiceTest

# WebSocket Handler Tests
./gradlew test --tests ChatWebSocketHandlerTest
```

### Generate Test Report

```bash
./gradlew test
# Report: backend/build/reports/tests/test/index.html
```

---

## RED Phase Verification

### Why Tests Fail (Intentional)

All tests are designed to fail because:

1. **Service implementations do not exist**
   - Only interfaces are defined
   - No `@Service` or `@Component` annotations

2. **WebSocket message mappings not configured**
   - No `@MessageMapping` methods exist
   - No controller to handle WebSocket messages

3. **This is TDD RED phase**
   - Tests define the contract
   - Implementation happens in Phase 1

### Expected Error Messages

```
org.springframework.beans.factory.NoSuchBeanDefinitionException:
No qualifying bean of type 'com.example.chat.service.KafkaProducerService'
```

```
org.springframework.beans.factory.NoSuchBeanDefinitionException:
No qualifying bean of type 'com.example.chat.service.RedisCacheService'
```

```
No message mapping found for destination '/app/chat.join'
```

---

## Phase 1 Implementation Checklist

To make tests GREEN, implement:

### 1. Kafka Producer Service
- [ ] Create `KafkaProducerServiceImpl`
- [ ] Configure `KafkaTemplate<String, String>` with JSON serializer
- [ ] Implement `sendMessage()` - publish to `chat.message.v1`
- [ ] Implement `sendEvent()` - publish to `chat.event.v1`
- [ ] Add null validation

### 2. Kafka Consumer Service
- [ ] Create `KafkaConsumerServiceImpl`
- [ ] Add `@KafkaListener` for `chat.message.v1`
- [ ] Add `@KafkaListener` for `chat.event.v1`
- [ ] Integrate with `RedisCacheService.cacheRecentMessage()`
- [ ] Handle deserialization errors

### 3. Redis Cache Service
- [ ] Create `RedisCacheServiceImpl`
- [ ] Configure `RedisTemplate<String, Object>` with JSON serializer
- [ ] Implement `cacheRecentMessage()` with FIFO (max 50) and TTL (600s)
- [ ] Implement `getRecentMessages()`
- [ ] Implement `addUserToRoom()` using Redis Set
- [ ] Implement `removeUserFromRoom()`
- [ ] Implement `getRoomUsers()`

### 4. WebSocket Handler
- [ ] Create `ChatWebSocketController`
- [ ] Add `@MessageMapping("/chat.join")`
- [ ] Add `@MessageMapping("/chat.send")`
- [ ] Add `@MessageMapping("/chat.leave")`
- [ ] Configure `SimpMessagingTemplate` for broadcasting
- [ ] Integrate with `KafkaProducerService` and `RedisCacheService`

---

## Test Coverage Goals

| Component | Target Coverage | Current Status |
|-----------|----------------|----------------|
| KafkaProducerService | 90% | Tests ready (RED) |
| KafkaConsumerService | 85% | Tests ready (RED) |
| RedisCacheService | 95% | Tests ready (RED) |
| WebSocketHandler | 80% | Tests ready (RED) |

---

## Success Criteria

### Task 0.5.2 (Complete)
- [x] All tests are RED (failing)
- [x] Tests compile successfully
- [x] Clear comments and documentation
- [x] Tests designed to pass in Phase 1

### Phase 1 (Next)
- [ ] Implement services
- [ ] All tests GREEN (passing)
- [ ] 80%+ code coverage

---

## References

- Test Execution Guide: `backend/TEST_EXECUTION_GUIDE.md`
- TRD Document: `docs/02-trd.md`
- Testcontainers: https://testcontainers.com/
- Spring Kafka Test: https://docs.spring.io/spring-kafka/reference/testing.html

---

## Notes

- Docker must be running for Testcontainers
- Tests use dynamic port binding to avoid conflicts
- EmbeddedKafka runs on ports 9092, 9093
- Redis Testcontainer uses random port
- WebSocket tests use random server port (`@LocalServerPort`)

**Task 0.5.2 Status**: COMPLETE - All RED tests ready for Phase 1 implementation
