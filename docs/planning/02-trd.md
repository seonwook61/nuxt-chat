# TRD: realtime-chat-1000 기술 요구사항

## 기술 스택

### 백엔드
- **언어/프레임워크**: Java 17+, Spring Boot 3.x
- **메시징**: Apache Kafka (메시지 브로커, 영속성)
- **캐시/Pub-Sub**: Redis (최근 메시지 캐시, 실시간 Pub/Sub)
- **WebSocket**: Spring WebSocket (socket.io 호환 구현)
- **빌드 도구**: Gradle (Kotlin DSL)
- **테스트**: JUnit 5, Testcontainers (Kafka, Redis)

### 프론트엔드
- **프레임워크**: Nuxt 3
- **언어**: TypeScript
- **UI**: TailwindCSS
- **상태 관리**: Pinia
- **WebSocket 클라이언트**: socket.io-client
- **테스트**: Vitest, Playwright

### 인프라
- **컨테이너**: Docker, Docker Compose
- **메시지 브로커**: Kafka + Zookeeper
- **캐시**: Redis
- **배포**: (TBD - Render, Railway 등)

## 아키텍처 설계

### 시스템 구조
```
[Nuxt Frontend] ←→ [Spring Boot Backend] ←→ [Kafka] ←→ [Redis]
                         ↓ WebSocket
                    [socket.io Client]
```

### 메시지 흐름

1. **메시지 전송**:
   - 클라이언트 → WebSocket → Spring Boot
   - Spring Boot → Kafka (영속 저장)
   - Spring Boot → Redis Pub/Sub (실시간 전파)
   - Redis Pub/Sub → 모든 연결된 클라이언트

2. **메시지 수신**:
   - 새 사용자 입장 시 Redis에서 최근 메시지 로드
   - 실시간 메시지는 Redis Pub/Sub으로 수신

### Kafka 토픽 설계
- `chat.message.v1`: 채팅 메시지 (key: roomId, value: ChatMessage)
- `chat.event.v1`: 시스템 이벤트 (입장/퇴장 등)

### Redis 키 설계
- `room:{roomId}:recent`: 최근 메시지 목록 (List, TTL 10분)
- `room:{roomId}:users`: 현재 사용자 목록 (Set, TTL 5분)

## 성능 목표

- 동시접속: 1000명
- 메시지 처리량: 10,000 msg/sec
- 메시지 지연: p95 < 100ms
- CPU 사용률: < 70%
- 메모리 사용률: < 80%

## 보안 고려사항

- CORS 설정 (프론트엔드 도메인만 허용)
- Rate Limiting (1초당 10개 메시지)
- XSS 방지 (메시지 sanitization)
- WebSocket 연결 제한 (IP당 최대 10개 연결)
