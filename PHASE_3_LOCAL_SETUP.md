# Phase 3: 통합 테스트 - 로컬 환경 실행 가이드

## 현재 상태
- **Phase 1 완료**: Backend STOMP WebSocket 구현 (18 tests GREEN)
- **Phase 2 완료**: Frontend Nuxt 3 UI 구현 (39 tests GREEN)
- **Java 11**: 설치 완료
- **Docker**: 설치 완료

---

## 🚀 Phase 3 실행 순서

### 1단계: Docker 서비스 시작 (5분)

Kafka, Redis, Zookeeper를 시작합니다.

```bash
# 프로젝트 루트에서
docker-compose up -d

# 상태 확인
docker-compose ps
```

**확인 항목:**
- kafka 서비스: UP
- redis 서비스: UP
- zookeeper 서비스: UP

---

### 2단계: Backend 서버 시작 (3분)

새로운 터미널 창에서:

```bash
cd backend

# Windows PowerShell
.\gradlew bootRun

# Mac/Linux
./gradlew bootRun
```

**확인:**
- 콘솔에 "Started ChatApplication" 메시지 출력
- 포트 8080에서 실행 중
- WebSocket 엔드포인트: ws://localhost:8080/ws

### ✅ Backend 시작 확인

```bash
curl http://localhost:8080/actuator/health

# 결과:
# {"status":"UP"}
```

---

### 3단계: Frontend 개발 서버 시작 (3분)

새로운 터미널 창에서:

```bash
cd frontend

# 의존성 설치 (처음 1회만)
npm install

# 개발 서버 실행
npm run dev
```

**확인:**
- 콘솔에 "Nuxt DevServer" 메시지 출력
- 포트 3000에서 실행 중
- http://localhost:3000 에서 접속 가능

---

### 4단계: 브라우저에서 테스트 (5-10분)

#### 4.1 첫 번째 창에서 테스트
1. http://localhost:3000 접속
2. "test-room-1" 입력 후 입장
3. 연결 상태 확인
   - 상단에 "Connected" 표시
   - WebSocket 연결 확인 (DevTools > Network > WS)

#### 4.2 두 번째 창에서 테스트 (멀티 유저 시뮬레이션)
1. 새 브라우저 탭 또는 프라이빗 창 열기
2. http://localhost:3000 접속
3. 동일한 "test-room-1" 입력 후 입장
4. 두 창이 모두 "Connected" 표시 확인

#### 4.3 메시지 송수신 테스트
1. **첫 번째 창에서:**
   - "Hello from User 1" 입력 후 전송
   - 메시지 즉시 화면에 표시

2. **두 번째 창에서:**
   - "Hello from User 1" 메시지 실시간 수신 확인
   - 메시지 타임스탐프 확인

3. **두 번째 창에서 메시지 전송:**
   - "Hello from User 2" 입력 후 전송
   - 첫 번째 창에서 실시간 수신 확인

#### 4.4 고급 테스트

**재연결 테스트:**
1. 첫 번째 브라우저 DevTools 열기 (F12)
2. Network 탭에서 "Disable cache" 체크
3. WebSocket 연결 우클릭 → "Disconnect"
4. "Reconnecting..." 표시 확인
5. 5초 후 자동 재연결 확인
6. 이전 메시지 로드 확인

**다중 방 테스트:**
1. 첫 번째 창: "test-room-1"
2. 두 번째 창: "test-room-2"
3. 메시지 전송 시 다른 방에는 표시 안됨 확인

**Backend 중단 테스트:**
1. Backend 터미널에서 Ctrl+C
2. 프론트엔드에서 "Reconnecting..." 표시 확인
3. Backend 다시 시작 후 자동 재연결 확인

---

## 📊 DevTools에서 확인할 항목

### Network 탭
1. WS 연결 확인
   - URL: `ws://localhost:8080/ws/...`
   - Protocol: websocket
   - Status: 101 Switching Protocols

2. STOMP 프레임 확인
   - STOMP CONNECT, SUBSCRIBE, SEND 프레임 확인

### Console 탭
1. 에러 메시지 없음 확인
2. 경고 메시지 최소화

### Application 탭
1. LocalStorage에 연결 상태 확인
2. WebSocket 메시지 추적

---

## 🔍 Backend API 확인

터미널에서 다음 명령어 실행:

```bash
# Health Check
curl http://localhost:8080/actuator/health

# Redis 캐시 확인 (샘플)
curl http://localhost:8080/api/cache/room/test-room-1/messages

# Kafka 메시지 확인 (docker exec 필요)
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic chat.message.v1 --from-beginning
```

---

## 📈 성능 측정 체크리스트

### E2E 통합 테스트 검증 항목

- [ ] **단일 사용자 테스트 (3초)**
  - [ ] 방 입장 성공
  - [ ] WebSocket 연결 확인
  - [ ] 메시지 전송 성공
  - [ ] 메시지 수신 성공
  - [ ] 타임스탐프 형식 확인 (ISO 8601)

- [ ] **다중 사용자 테스트 (5초)**
  - [ ] 5명 동시 입장 성공
  - [ ] 각 사용자 메시지 송수신 성공
  - [ ] 메시지 순서 보장
  - [ ] 온라인 사용자 수 동기화

- [ ] **재연결 테스트 (8초)**
  - [ ] 자동 재연결 작동
  - [ ] 이전 메시지 로드
  - [ ] 재연결 중 메시지 큐잉

- [ ] **에러 핸들링 테스트**
  - [ ] Backend 중단 시 에러 표시
  - [ ] 빈 메시지 송신 차단
  - [ ] 1000자 초과 메시지 차단

---

## 🛠️ 문제 해결

### Backend 시작 실패

```
ERROR: JAVA_HOME is not set
```

**해결:**
```bash
# PowerShell에서
$env:JAVA_HOME = "C:\Program Files\Java\jdk-11"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
java -version  # 확인

cd backend
.\gradlew bootRun
```

### 포트 충돌

```
ERROR: Address already in use :::8080
```

**해결:**
```bash
# 포트 종료
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# 또는 다른 포트 사용
# backend/application.yml: server.port=8081
```

### Docker 서비스 연결 실패

```
ERROR: Connection refused to localhost:9092 (Kafka)
```

**해결:**
```bash
# Docker 컨테이너 재시작
docker-compose restart

# 또는 전체 재시작
docker-compose down
docker-compose up -d
```

### Frontend npm 의존성 에러

```
npm ERR! ERESOLVE unable to resolve dependency tree
```

**해결:**
```bash
cd frontend
npm install --legacy-peer-deps
npm run dev
```

---

## 📝 실행 명령어 요약

### 터미널 1: Docker 서비스
```bash
docker-compose up -d
```

### 터미널 2: Backend
```bash
cd backend
$env:JAVA_HOME = "C:\Program Files\Java\jdk-11"
.\gradlew bootRun
```

### 터미널 3: Frontend
```bash
cd frontend
npm install
npm run dev
```

### 브라우저: http://localhost:3000

---

## 📊 예상 성능 지표 (로컬 환경)

| 메트릭 | 예상값 | 측정 방법 |
|--------|--------|---------|
| 연결 시간 | < 500ms | DevTools Network 탭 |
| 메시지 송신 지연 | < 50ms | 송신→수신 간격 |
| 메시지 수신 지연 | < 100ms | 수신 타임스탐프 |
| 메모리 사용 | 100-150MB | Task Manager |
| CPU 사용 | 2-5% | Task Manager |

---

## 🎯 다음 단계 (Phase 3 완료 후)

1. **E2E 테스트 자동화** (Playwright)
   ```bash
   npm run test:e2e
   ```

2. **부하 테스트** (k6)
   ```bash
   npm run test:load:smoke
   npm run test:load
   npm run test:load:stress
   ```

3. **성능 리포트 생성**
   - Playwright HTML 리포트
   - k6 성능 리포트

4. **Phase 4: 배포 준비**
   - Docker 이미지 빌드
   - Kubernetes 배포 (선택사항)

---

## 📞 지원

문제가 발생하면:
1. Backend 콘솔 로그 확인
2. Frontend 콘솔 에러 확인
3. Docker 컨테이너 로그 확인
   ```bash
   docker-compose logs kafka
   docker-compose logs redis
   ```
