# Chat Backend - Spring Boot

실시간 채팅 애플리케이션의 백엔드 서버 (동시접속 1000명 규모)

## 기술 스택

- Java 17+
- Spring Boot 3.2.1
- Spring Kafka (메시지 스트리밍)
- Spring Data Redis (실시간 pub/sub, 캐싱)
- Spring WebSocket (WebSocket 연동)
- Spring Security (JWT 인증 - Phase 1)
- Micrometer + Prometheus (모니터링)

## 사전 요구사항

### 1. Java 17+ 설치

**Windows:**
```bash
# Chocolatey 사용
choco install openjdk17

# 또는 수동 설치: https://adoptium.net/
```

**macOS:**
```bash
brew install openjdk@17
```

**Linux:**
```bash
sudo apt-get install openjdk-17-jdk
```

### 2. Docker & Docker Compose 설치

Kafka와 Redis를 로컬에서 실행하기 위해 필요합니다.

## 프로젝트 구조

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/example/chat/
│   │   │   ├── ChatApplication.java          # 메인 애플리케이션
│   │   │   ├── config/
│   │   │   │   ├── KafkaConfig.java          # Kafka 설정
│   │   │   │   ├── RedisConfig.java          # Redis 설정
│   │   │   │   ├── WebSocketConfig.java      # WebSocket 설정
│   │   │   │   └── SecurityConfig.java       # Security 설정 (임시)
│   │   │   └── controller/
│   │   │       └── HealthController.java     # Health check
│   │   └── resources/
│   │       ├── application.yml               # 기본 설정
│   │       └── application-dev.yml           # 개발 환경 설정
│   └── test/
│       └── java/com/example/chat/
├── build.gradle.kts                          # Gradle 빌드 설정
├── settings.gradle.kts
├── Dockerfile
└── .env.example                              # 환경 변수 템플릿
```

## 설정

### 환경 변수 설정

```bash
# backend/.env 파일 생성
cp .env.example .env

# 필요한 값 수정
# KAFKA_BOOTSTRAP_SERVERS=localhost:9092
# REDIS_HOST=localhost
# REDIS_PORT=6379
```

### Kafka & Redis 실행 (Docker Compose)

프로젝트 루트의 docker-compose.yml 사용:

```bash
cd ..  # 프로젝트 루트로 이동
docker-compose up -d kafka redis
```

## 빌드 및 실행

### 빌드

```bash
# Gradle Wrapper 사용 (권장)
./gradlew clean build

# 테스트 제외 빌드
./gradlew clean build -x test
```

### 로컬 실행

```bash
# 개발 모드로 실행
./gradlew bootRun

# 또는 jar 파일 직접 실행
java -jar build/libs/chat-backend-0.0.1-SNAPSHOT.jar
```

서버가 http://localhost:8080 에서 실행됩니다.

### Health Check 검증

```bash
# Health endpoint
curl http://localhost:8080/health

# 예상 응답: {"status":"ok"}
```

### Actuator Endpoints

```bash
# Prometheus metrics
curl http://localhost:8080/actuator/prometheus

# Health 상세 정보
curl http://localhost:8080/actuator/health

# 모든 endpoints
curl http://localhost:8080/actuator
```

## Docker 실행

### 이미지 빌드

```bash
docker build -t chat-backend .
```

### 컨테이너 실행

```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILE=dev \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  -e REDIS_HOST=redis \
  -e REDIS_PORT=6379 \
  chat-backend
```

## 테스트

```bash
# 전체 테스트 실행 (Phase 0.5+)
./gradlew test

# 특정 테스트 실행
./gradlew test --tests "HealthControllerTest"

# 테스트 커버리지 리포트
./gradlew test jacocoTestReport
```

## 개발 가이드

### 아키텍처 설계

**메시지 흐름:**
```
클라이언트 → REST API → Kafka Producer → Kafka Topic (chat.message.v1)
                                              ↓
                                         Kafka Consumer
                                              ↓
                                    ┌─────────┴─────────┐
                                    ↓                   ↓
                            Redis Pub/Sub        DB 저장
                                    ↓
                            WebSocket 서버들
                                    ↓
                                클라이언트
```

**Kafka 토픽:**
- `chat.message.v1`: 채팅 메시지
- `chat.moderation.v1`: 모더레이션 이벤트

**Kafka Consumer Groups:**
- `websocket-fanout`: WebSocket 서버용 (실시간 전달)
- `persist-store`: DB 저장용
- `moderation`: 모더레이션 처리용

**Redis 사용:**
- Pub/Sub: 다중 WebSocket 서버 간 메시지 fanout
- Cache: 최근 메시지 (List 또는 Stream)
- Rate limiting: IP/User 기반

### Phase별 구현 계획

- **Phase 0** (현재): 프로젝트 구조 및 기본 설정
- **Phase 0.5**: 테스트 계약 작성 (TDD)
- **Phase 1**: 채팅 메시지 API (Kafka Producer)
- **Phase 2**: WebSocket 연동 (Kafka Consumer + Redis Pub/Sub)
- **Phase 3**: 인증 (JWT)
- **Phase 4**: 모더레이션 및 추가 기능

## API 명세 (Phase 1+)

### 채팅 메시지

```
POST /api/rooms/{roomId}/messages
Content-Type: application/json

{
  "content": "Hello, World!",
  "userId": "user123"
}
```

### 최근 메시지 조회

```
GET /api/rooms/{roomId}/messages?cursor=...&limit=50
```

### 방 생성

```
POST /api/rooms
Content-Type: application/json

{
  "name": "General Chat",
  "maxUsers": 1000
}
```

## 모니터링

### Prometheus 메트릭

```bash
# Kafka 관련
kafka_producer_record_send_total
kafka_consumer_records_consumed_total

# Redis 관련
redis_commands_processed_total

# HTTP 관련
http_server_requests_seconds
```

### 로그 레벨 조정

```yaml
# application-dev.yml
logging:
  level:
    com.example.chat: DEBUG
    org.springframework.kafka: INFO
```

## 문제 해결

### Kafka 연결 실패

```bash
# Kafka 상태 확인
docker-compose ps kafka

# Kafka 로그 확인
docker-compose logs kafka

# 재시작
docker-compose restart kafka
```

### Redis 연결 실패

```bash
# Redis 상태 확인
docker-compose ps redis

# Redis CLI 접속
docker exec -it <redis-container> redis-cli
> PING
PONG
```

### 포트 충돌

```bash
# 8080 포트 사용 중인 프로세스 확인 (Windows)
netstat -ano | findstr :8080

# 프로세스 종료 (관리자 권한)
taskkill /PID <PID> /F
```

## 라이센스

MIT
