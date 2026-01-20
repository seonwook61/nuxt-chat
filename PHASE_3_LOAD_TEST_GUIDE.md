# Phase 3: 부하 테스트 가이드 (k6)

## 📋 목차

1. [환경 준비](#환경-준비)
2. [k6 설치](#k6-설치)
3. [테스트 실행](#테스트-실행)
4. [성능 지표 해석](#성능-지표-해석)
5. [결과 분석](#결과-분석)
6. [최적화 권장사항](#최적화-권장사항)

---

## 환경 준비

### 필수 사항

1. **Backend 실행 중**
   ```bash
   cd backend
   .\gradlew bootRun
   ```

2. **Frontend 실행 중**
   ```bash
   cd frontend
   npm run dev
   ```

3. **Docker 서비스 실행 중**
   ```bash
   docker-compose up -d
   docker-compose ps
   ```

4. **모니터링 도구 준비**
   - Task Manager (또는 htop) - CPU/Memory 모니터링
   - DevTools - WebSocket 연결 확인

---

## k6 설치

### Windows

#### 방법 1: Chocolatey (추천)
```powershell
choco install k6
k6 --version  # 확인
```

#### 방법 2: 직접 다운로드
https://github.com/grafana/k6/releases 에서 다운로드 후 PATH에 추가

#### 방법 3: Docker
```bash
docker run -i grafana/k6 run - < tests/load/smoke-test.js
```

### Mac
```bash
brew install k6
k6 --version
```

### Linux (Ubuntu/Debian)
```bash
sudo apt-get install -y gnupg2 curl
curl https://dl.k6.io/apt/apt.grafana.com/DEB-GPG-KEY.gpg | sudo apt-key add -
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/apt stable main" | sudo tee /etc/apt/sources.list.d/k6-stable.list
sudo apt-get update
sudo apt-get install k6
```

---

## 테스트 실행

### 1️⃣ Smoke Test (기본 동작 확인)

**목표**: 10 VU × 1분 - 기본 기능 작동 확인

```bash
cd <프로젝트 루트>
k6 run tests/load/smoke-test.js
```

**예상 결과**:
```
✓ connected
✓ message sent

checks.............................: 98.56% 200 out of 203
data_received......................: 45 kB
data_sent...........................: 52 kB
http_req_duration..................: avg=123ms   min=45ms    med=120ms   max=456ms p(90)=235ms p(95)=312ms p(99)=412ms
ws_connection_time.................: avg=234ms   min=123ms   med=240ms   max=567ms p(90)=450ms p(95)=520ms p(99)=560ms
ws_error_rate.......................: 0.00%
messages_sent.......................: 98
```

**실행 시간**: ~1분

**다음 단계**: 에러가 없으면 Load Test 진행

---

### 2️⃣ Load Test (정상 부하)

**목표**: 500 VU × 8분 - 정상 부하 성능 측정

```bash
k6 run tests/load/load-test.js
```

**구성**:
- Ramp-up: 2분 (0→500 VU)
- Sustain: 5분 (500 VU 유지)
- Ramp-down: 1분 (500→0 VU)

**예상 결과**:
```
checks.............................: 97.23% 2456 out of 2528
data_received......................: 1.2 MB
data_sent...........................: 1.8 MB
ws_connection_time.................: avg=245ms   min=98ms    med=235ms   max=890ms p(90)=520ms p(95)=680ms p(99)=950ms
message_delivery_time..............: avg=45ms    min=12ms    med=42ms    max=234ms p(90)=89ms   p(95)=120ms p(99)=180ms
message_receive_time...............: avg=78ms    min=22ms    med=75ms    max=456ms p(90)=145ms  p(95)=190ms p(99)=280ms
ws_error_rate.......................: 0.12%
messages_sent.......................: 2456
messages_received...................: 2398
active_connections..................: 0 / 500
```

**성능 목표 달성 여부**:
- ✅ p95 메시지 지연 < 100ms: 120ms (⚠️ 약간 초과)
- ✅ p99 메시지 지연 < 500ms: 180ms ✓
- ✅ 에러율 < 1%: 0.12% ✓

**실행 시간**: ~8분

**평가**: 성능 목표 거의 달성. 약간의 최적화 여지 있음.

---

### 3️⃣ Stress Test (극한 부하)

**주의**: 이 테스트는 시스템에 상당한 부하를 줍니다.

```bash
# Task Manager 또는 모니터링 도구 먼저 실행
# 그 후 아래 명령어 실행
k6 run tests/load/stress-test.js
```

**구성**:
- Ramp-up: 2분 (0→1000 VU)
- Sustain: 10분 (1000 VU 유지)
- Ramp-down: 1분 (1000→0 VU)

**예상 결과**:
```
checks.............................: 94.56% 9234 out of 9768
data_received......................: 4.5 MB
data_sent...........................: 6.2 MB
ws_connection_time.................: avg=456ms   min=145ms   med=420ms   max=2456ms p(90)=890ms  p(95)=1200ms p(99)=1890ms
message_delivery_time..............: avg=145ms   min=34ms    med=120ms   max=1234ms p(90)=280ms  p(95)=380ms  p(99)=890ms
message_receive_time...............: avg=234ms   min=56ms    med=210ms   max=2100ms p(90)=450ms  p(95)=680ms  p(99)=1200ms
ws_error_rate.......................: 2.34%
messages_sent.......................: 9234
messages_received...................: 8901
connection_errors...................: 156
active_connections..................: 0 / 1000
```

**성능 평가**:
- p95 메시지 지연 380ms (목표: < 250ms) - ⚠️ 초과
- 에러율 2.34% (목표: < 5%) - ✓ 양호
- 메모리: 약 3.5GB
- CPU: 약 75-85%

**실행 시간**: ~13분

**평가**: 시스템이 1000명 동시접속을 처리하지만, 지연 시간이 증가함. 최적화 필요.

---

## 성능 지표 해석

### 주요 메트릭

| 메트릭 | 설명 | 정상 범위 |
|--------|------|---------|
| `ws_connection_time` | WebSocket 연결 시간 | p95 < 500ms |
| `message_delivery_time` | 메시지 송신 시간 | p95 < 100ms |
| `message_receive_time` | 메시지 수신 지연 | p95 < 150ms |
| `ws_error_rate` | 에러 발생률 | < 1% |
| `messages_sent` | 총 송신 메시지 수 | |
| `messages_received` | 총 수신 메시지 수 | |
| `active_connections` | 동시 연결 수 | 목표 VU 수 |

### 성능 등급

```
p95 메시지 지연:
- 50ms 이하: ⭐⭐⭐⭐⭐ (우수)
- 50-100ms: ⭐⭐⭐⭐ (양호)
- 100-200ms: ⭐⭐⭐ (보통)
- 200-500ms: ⭐⭐ (미흡)
- 500ms 이상: ⭐ (부족)

에러율:
- 0-0.5%: ⭐⭐⭐⭐⭐
- 0.5-1%: ⭐⭐⭐⭐
- 1-2%: ⭐⭐⭐
- 2-5%: ⭐⭐
- 5% 이상: ⭐
```

---

## 결과 분석

### 결과 저장 및 분석

```bash
# JSON 형식으로 결과 저장
k6 run tests/load/load-test.js -o json=results/load-test-$(date +%Y%m%d_%H%M%S).json

# HTML 리포트 생성 (k6 확장 필요)
k6 run tests/load/load-test.js --out json=results/load-test.json
```

### 결과 비교

```bash
# 두 번 실행하여 성능 비교
k6 run tests/load/load-test.js -o json=results/load-test-run1.json
# (최적화 작업)
k6 run tests/load/load-test.js -o json=results/load-test-run2.json

# 비교 분석
# run1 vs run2의 메트릭 비교
```

### 병목 지점 식별

**높은 연결 시간**: Backend 연결 이슈
```bash
# Smoke test 실행하여 단일 연결 확인
k6 run tests/load/smoke-test.js
```

**높은 메시지 지연**: Kafka/Redis 병목
```bash
# Kafka 내부 지연 확인
docker logs kafka | grep latency

# Redis 내부 지연 확인
docker exec redis redis-cli latency latest
```

**높은 에러율**: 시스템 과부하
```bash
# Backend 로그 확인
# tail -f backend/logs/application.log

# Docker 리소스 확인
docker stats
```

---

## 최적화 권장사항

### 1. Kafka 최적화

**현재 설정**:
```yaml
broker:
  num.partitions: 1
  replication.factor: 1
```

**최적화**:
```yaml
# docker-compose.yml 수정
environment:
  KAFKA_NUM_PARTITIONS: 5  # 파티션 수 증가
  KAFKA_DEFAULT_REPLICATION_FACTOR: 1
  KAFKA_LOG_RETENTION_MS: 3600000  # 1시간 보존
```

### 2. Redis 최적화

**현재 설정**:
```
maxclients: 10000
maxmemory: unlimited
```

**최적화**:
```bash
# docker-compose.yml
command: redis-server --maxmemory 2gb --maxmemory-policy allkeys-lru
```

### 3. Backend 최적화

**메모리 설정**:
```bash
# backend/application.yml
server:
  tomcat:
    threads:
      max: 200  # WebSocket 스레드 풀 증가
      min-spare: 50
```

**WebSocket 최적화**:
```java
// WebSocketConfig.java
registry.setMessageSizeLimit(32768);  // 메시지 크기 제한
registry.setSendBufferSizeLimit(32768);
```

### 4. Frontend 최적화

**STOMP 메시지 배치**:
```typescript
// useSocket.ts
const batchSize = 10;  // 메시지 배치 처리
const batchInterval = 100;  // ms
```

---

## 모니터링 체크리스트

### 테스트 전

- [ ] Docker 서비스 모두 실행 중 (`docker-compose ps`)
- [ ] Backend 실행 중 (`http://localhost:8080/actuator/health` = UP)
- [ ] Frontend 실행 중 (`http://localhost:3000` 접속 가능)
- [ ] Task Manager 또는 모니터링 도구 준비
- [ ] 디스크 공간 충분 (최소 1GB)

### 테스트 중

- [ ] Task Manager에서 CPU/Memory 모니터링
  - CPU: 정상 (< 90%)
  - Memory: 증가 추이 모니터링
- [ ] Backend 콘솔에서 에러 로그 모니터링
- [ ] k6 콘솔에서 메트릭 실시간 확인

### 테스트 후

- [ ] 메트릭 결과 기록
- [ ] Docker 컨테이너 정리
  ```bash
  docker-compose down
  ```
- [ ] 성능 보고서 작성

---

## 성능 보고서 템플릿

```markdown
# Phase 3 부하 테스트 보고서

## 테스트 환경
- 날짜: [테스트 날짜]
- 시스템: [Windows/Mac/Linux]
- CPU: [CPU 정보]
- Memory: [메모리 정보]
- Backend: Java [버전]
- Frontend: Node.js [버전]

## 테스트 결과

### Smoke Test (10 VU, 1분)
- 평균 연결 시간: [ms]
- p95 메시지 지연: [ms]
- 에러율: [%]
- 평가: PASS ✓

### Load Test (500 VU, 8분)
- 평균 연결 시간: [ms]
- p95 메시지 지연: [ms]
- p99 메시지 지연: [ms]
- 에러율: [%]
- 평가: PASS/FAIL

### Stress Test (1000 VU, 13분)
- 평균 연결 시간: [ms]
- p95 메시지 지연: [ms]
- p99 메시지 지연: [ms]
- 에러율: [%]
- 메모리 사용: [GB]
- CPU 사용: [%]
- 평가: PASS/FAIL

## 병목 지점
1. [문제]
2. [문제]

## 권장사항
1. [권장]
2. [권장]

## 성능 목표 달성 여부
- [ ] p95 메시지 지연 < 100ms (Load Test)
- [ ] 에러율 < 1% (Load Test)
- [ ] 1000 VU 동시접속 지원
```

---

## 추가 명령어

```bash
# 특정 메트릭만 출력
k6 run tests/load/load-test.js --summary-trend-stats "avg,min,med,max,p(95),p(99)"

# 결과를 InfluxDB로 전송 (Cloud)
k6 run tests/load/load-test.js -o cloud

# 상세 디버그 모드
k6 run tests/load/load-test.js -v

# VU 수 조정
k6 run tests/load/load-test.js --vus 100 --duration 1m
```

---

## 문제 해결

### k6 실행 오류

```
WebSocket: connection failed
```

**해결**: Backend가 실행 중인지 확인
```bash
curl http://localhost:8080/actuator/health
```

### 메모리 부족

```
ERROR: Out of memory
```

**해결**: VU 수 감소 또는 메모리 증설
```bash
k6 run tests/load/load-test.js --vus 250  # VU 수 감소
```

### CPU 과부하

```
System unresponsive
```

**해결**: 테스트 일시 중지 또는 VU 감소
- 키보드: Ctrl+C로 테스트 중단
- 다시 시작: VU 수 50% 감소하여 재시도

---

## 참고 자료

- k6 공식 문서: https://k6.io/docs/
- WebSocket 성능 최적화: https://developer.mozilla.org/en-US/docs/Web/API/WebSocket
- Apache Kafka 성능 튜닝: https://kafka.apache.org/documentation/#brokerconfigs
- Redis 성능 최적화: https://redis.io/commands/latency/
