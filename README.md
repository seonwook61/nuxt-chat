# realtime-chat-1000

동시접속 1000명을 지원하는 실시간 채팅 시스템

## 기술 스택

### 백엔드
- Java 17+
- Spring Boot 3.x
- Spring Kafka (메시지 브로커)
- Spring Data Redis (캐시 + Pub/Sub)
- Gradle (Kotlin DSL)

### 프론트엔드
- Nuxt 3
- Vue 3 + Composition API
- TypeScript
- TailwindCSS
- Pinia (상태 관리)
- socket.io-client

### 인프라
- Apache Kafka (메시징)
- Redis (캐시 + Pub/Sub)
- Docker + Docker Compose

## 프로젝트 구조

```
realtime-chat-1000/
├── backend/          # Spring Boot 백엔드
├── frontend/         # Nuxt 3 프론트엔드
├── docs/             # 기획 문서
│   └── planning/
│       ├── 01-prd.md
│       ├── 02-trd.md
│       └── 06-tasks.md
├── docker-compose.yml
└── README.md
```

## 사전 요구사항

- Java 17+ (백엔드)
- Node.js 18+ (프론트엔드)
- Docker & Docker Compose (인프라)

**Java 설치 확인:**
```bash
java -version  # Java 17 이상 필요
```

Java 미설치 시:
- Windows: `choco install openjdk17`
- macOS: `brew install openjdk@17`
- Linux: `sudo apt-get install openjdk-17-jdk`

자세한 설정은 [backend/README.md](backend/README.md) 참고

## 개발 환경 실행

### 1. 로컬 인프라 실행 (Kafka + Redis)

```bash
docker-compose up -d
docker-compose ps  # 서비스 상태 확인
```

### 2. 백엔드 실행

```bash
cd backend
./gradlew bootRun
```

브라우저에서 http://localhost:8080/health 접속하여 상태 확인
- 예상 응답: `{"status":"ok"}`

자세한 내용: [backend/README.md](backend/README.md)

### 3. 프론트엔드 실행

```bash
cd frontend
npm install
npm run dev
```

브라우저에서 http://localhost:3000 접속

## 성능 목표

- 동시접속자: 1000명 이상
- 메시지 지연: p95 < 100ms
- 메시지 유실: 0%

## 마일스톤

- [x] M0: 프로젝트 셋업 (Phase 0) - 진행중
- [ ] M0.5: 계약 우선 정의 (Contract-First TDD)
- [ ] M1: 백엔드 핵심 기능 구현
- [ ] M2: 프론트엔드 UI 구현
- [ ] M3: 통합 및 부하 테스트
- [ ] M4: 배포 및 문서화

## 라이선스

MIT
