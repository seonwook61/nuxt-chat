# Backend Test Suite (RED Phase - Task 0.5.2)

## Overview

This directory contains all backend tests for the nuxt-chat application.
All tests are currently in **RED phase** (intentionally failing) and will turn **GREEN** in Phase 1 after service implementation.

## Test Structure

```
src/test/java/com/example/chat/
├── kafka/
│   ├── KafkaProducerServiceTest.java     # 4 tests - Kafka message/event publishing
│   └── KafkaConsumerServiceTest.java     # 3 tests - Kafka message/event consumption
├── redis/
│   └── RedisCacheServiceTest.java        # 7 tests - Redis caching & user presence
└── websocket/
    └── ChatWebSocketHandlerTest.java     # 4 tests - WebSocket STOMP messaging

Total: 18 RED tests
```

## Quick Start

### Prerequisites
- Java 17+
- Docker running (for Testcontainers)

### Run All Tests
```bash
cd backend
./gradlew test
```

**Expected**: All tests will fail (RED) - this is intentional!

### Run Individual Suites
```bash
# Kafka tests
./gradlew test --tests KafkaProducerServiceTest
./gradlew test --tests KafkaConsumerServiceTest

# Redis tests
./gradlew test --tests RedisCacheServiceTest

# WebSocket tests
./gradlew test --tests ChatWebSocketHandlerTest
```

## Test Details

### Kafka Producer Tests (4 tests)
Tests for publishing messages to Kafka topics:
- `chat.message.v1` - Chat messages
- `chat.event.v1` - User presence events

Uses: `@EmbeddedKafka`, `KafkaTestUtils`

### Kafka Consumer Tests (3 tests)
Tests for consuming messages from Kafka and processing:
- Message caching in Redis
- Event handling
- Concurrent message processing

Uses: `@MockBean`, `@SpyBean`, `CountDownLatch`

### Redis Cache Tests (7 tests)
Tests for Redis operations:
- Recent message caching (FIFO, max 50, TTL 600s)
- Room user presence (Set operations)
- TTL verification

Uses: `@Testcontainers`, `RedisContainer`, `RedisTemplate`

### WebSocket Handler Tests (4 tests)
Tests for WebSocket STOMP messaging:
- Join room (`/app/chat.join`)
- Send message (`/app/chat.send`)
- Leave room (`/app/chat.leave`)
- Broadcast to multiple subscribers

Uses: `WebSocketStompClient`, `SockJsClient`, `BlockingQueue`

## Why Tests Fail (RED Phase)

All tests fail because:
1. Service **implementations** do not exist (only interfaces)
2. WebSocket **message mappings** are not configured
3. This is **intentional** - TDD RED phase defines the contract

## Phase 1 Implementation

To turn tests GREEN, implement:
1. `KafkaProducerServiceImpl` - Kafka message publishing
2. `KafkaConsumerServiceImpl` - Kafka message consumption with `@KafkaListener`
3. `RedisCacheServiceImpl` - Redis operations with `RedisTemplate`
4. `ChatWebSocketController` - WebSocket message handling with `@MessageMapping`

See `TEST_SUMMARY.md` for detailed implementation checklist.

## Documentation

- **TEST_SUMMARY.md** - Complete test inventory and status
- **TEST_EXECUTION_GUIDE.md** - Detailed execution guide and troubleshooting

## Test Technologies

| Technology | Purpose |
|------------|---------|
| JUnit 5 | Test framework |
| Spring Boot Test | Spring context testing |
| Testcontainers | Docker-based integration tests |
| EmbeddedKafka | In-memory Kafka broker |
| RedisContainer | Dockerized Redis instance |
| MockBean/SpyBean | Mocking and spying |
| WebSocketStompClient | WebSocket client testing |

## Test Coverage Goals

- Unit Tests: 80%+
- Integration Tests: Critical flows
- All tests GREEN in Phase 1

---

**Status**: RED Phase Complete - Ready for Phase 1 Implementation
