# Backend Test Execution Guide (RED Phase)

## Overview

이 가이드는 Task 0.5.2에서 작성된 RED 테스트들의 실행 방법을 설명합니다.
모든 테스트는 현재 **실패(RED)** 상태이며, Phase 1에서 서비스 구현 시 통과(GREEN)로 전환됩니다.

## Prerequisites

### Required Software

1. **Java 17+**
   ```bash
   java -version
   # java version "17.0.x" or higher
   ```

2. **Docker** (Testcontainers 실행을 위해 필수)
   ```bash
   docker --version
   # Docker version 20.10.x or higher
   ```

3. **Gradle 8.x** (프로젝트에 포함된 Gradle Wrapper 사용)

### Environment Setup

Docker Desktop이 실행 중이어야 합니다:
```bash
# Windows
docker ps

# 정상 실행되면 컨테이너 목록이 표시됨
```

## Test Structure

### Created Test Files

```
backend/src/test/java/com/example/chat/
├── kafka/
│   ├── KafkaProducerServiceTest.java     (4 tests)
│   └── KafkaConsumerServiceTest.java     (3 tests)
├── redis/
│   └── RedisCacheServiceTest.java        (7 tests)
└── websocket/
    └── ChatWebSocketHandlerTest.java     (4 tests)

Total: 18 RED tests
```

### Supporting Files Created

```
backend/src/main/java/com/example/chat/
├── model/
│   ├── ChatMessage.java
│   └── ChatEvent.java
└── service/
    ├── KafkaProducerService.java (interface)
    ├── KafkaConsumerService.java (interface)
    └── RedisCacheService.java (interface)
```

## Running Tests

### 1. All Tests (Expected to FAIL - RED Phase)

```bash
cd backend
./gradlew test
```

**Expected Result**: All tests will fail with errors like:
- `NoSuchBeanDefinitionException` - Service implementations not found
- `required = false` will prevent complete test failure in some cases

### 2. Individual Test Suites

#### Kafka Producer Tests
```bash
./gradlew test --tests KafkaProducerServiceTest
```

**Test Cases**:
- `testSendMessage_Success` - Verify ChatMessage published to Kafka
- `testSendEvent_Success` - Verify ChatEvent published to Kafka
- `testSendMessage_Null` - Verify null validation
- `testSendEvent_Null` - Verify null validation

#### Kafka Consumer Tests
```bash
./gradlew test --tests KafkaConsumerServiceTest
```

**Test Cases**:
- `testConsumeMessage_Published` - Verify message consumption and Redis caching
- `testConsumeEvent_Published` - Verify event consumption
- `testConsumeMessage_Concurrent` - Verify concurrent message processing

#### Redis Cache Tests
```bash
./gradlew test --tests RedisCacheServiceTest
```

**Test Cases**:
- `testCacheRecentMessage_Success` - Verify message caching
- `testGetRecentMessages_Success` - Verify cache retrieval
- `testCacheRecentMessage_FIFOLimit` - Verify FIFO with 50 message limit
- `testAddUserToRoom_Success` - Verify user addition
- `testGetRoomUsers_Success` - Verify user list retrieval
- `testRemoveUserFromRoom_Success` - Verify user removal
- `testTTL_Expires` - Verify 600s TTL

#### WebSocket Handler Tests
```bash
./gradlew test --tests ChatWebSocketHandlerTest
```

**Test Cases**:
- `testJoinRoom_Success` - Verify room join handling
- `testSendMessage_Success` - Verify message sending
- `testLeaveRoom_Success` - Verify room leave handling
- `testBroadcast_AllSubscribers` - Verify broadcast to all subscribers

### 3. Test with Verbose Output

```bash
./gradlew test --info
```

### 4. Generate Test Report

```bash
./gradlew test
# Report generated at: backend/build/reports/tests/test/index.html
```

## Test Configuration

### Dependencies (build.gradle.kts)

```kotlin
testImplementation("org.springframework.boot:spring-boot-starter-test")
testImplementation("org.springframework.kafka:spring-kafka-test")
testImplementation("org.testcontainers:testcontainers:1.19.3")
testImplementation("org.testcontainers:kafka:1.19.3")
testImplementation("org.testcontainers:junit-jupiter:1.19.3")
testImplementation("com.redis:testcontainers-redis:2.0.1")
```

### Testcontainers

Tests use:
- **EmbeddedKafka** (spring-kafka-test) for Kafka tests
- **RedisContainer** (Testcontainers) for Redis tests
- **SockJS Client** for WebSocket tests

Docker containers will be started automatically during test execution.

## Expected Failures (RED Phase)

### Current Status

All tests are expected to fail with:

1. **Kafka Tests**:
   ```
   org.springframework.beans.factory.NoSuchBeanDefinitionException:
   No qualifying bean of type 'com.example.chat.service.KafkaProducerService'
   ```

2. **Redis Tests**:
   ```
   org.springframework.beans.factory.NoSuchBeanDefinitionException:
   No qualifying bean of type 'com.example.chat.service.RedisCacheService'
   ```

3. **WebSocket Tests**:
   ```
   No message mapping found for destination '/app/chat.join'
   ```

### Why Tests Fail (RED Phase Design)

These tests are designed to fail because:
- Service **interfaces** exist, but **implementations** do not
- WebSocket **message mappings** are not configured
- This is **intentional** - TDD RED phase

## Next Steps (Phase 1)

In Phase 1, implement:

1. **KafkaProducerServiceImpl**
   - Implement `sendMessage()` and `sendEvent()`
   - Configure KafkaTemplate with JSON serialization

2. **KafkaConsumerServiceImpl**
   - Implement `@KafkaListener` for message and event topics
   - Integrate with RedisCacheService

3. **RedisCacheServiceImpl**
   - Implement Redis List operations for recent messages
   - Implement Redis Set operations for room users
   - Configure TTL (600 seconds)

4. **ChatWebSocketHandler**
   - Implement `@MessageMapping("/chat.join")`
   - Implement `@MessageMapping("/chat.send")`
   - Implement `@MessageMapping("/chat.leave")`
   - Configure SimpMessagingTemplate for broadcasting

After implementation, re-run tests to verify GREEN status:
```bash
./gradlew test
# Expected: All tests pass (GREEN)
```

## Troubleshooting

### Docker Not Running

```
Could not start container: docker not running
```

**Solution**: Start Docker Desktop

### Port Conflicts

```
Address already in use: bind
```

**Solution**: Stop other services using ports 9092, 9093, 6379

### Out of Memory

```
Java heap space
```

**Solution**: Increase Gradle memory
```bash
export GRADLE_OPTS="-Xmx2048m"
```

## Test Coverage Goals

- **Unit Tests**: 80%+ coverage
- **Integration Tests**: Critical flows (Kafka, Redis, WebSocket)
- **E2E Tests**: End-to-end scenarios (Phase 2)

## References

- Testcontainers: https://testcontainers.com/
- Spring Boot Test: https://spring.io/guides/gs/testing-web/
- Spring Kafka Test: https://docs.spring.io/spring-kafka/reference/testing.html
