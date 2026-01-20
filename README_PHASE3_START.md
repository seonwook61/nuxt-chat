# 🚀 Phase 3: 통합 테스트 시작 가이드

## 👋 환영합니다!

Backend와 Frontend가 모두 완성되었습니다. 이제 **로컬 환경에서 실시간 채팅 시스템을 직접 실행**할 수 있습니다.

---

## ⚡ 5분 안에 시작하기

### 1단계: Docker 서비스 시작 (1분)

```bash
docker-compose up -d
docker-compose ps
```

### 2단계: Backend 시작 (1분)

**PowerShell 또는 CMD 열기:**

```powershell
# Java 경로 설정
$env:JAVA_HOME = "C:\Program Files\Java\jdk-11"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Backend 실행
cd backend
.\gradlew bootRun
```

**확인**: 콘솔에 `Started ChatApplication` 메시지 출력

### 3단계: Frontend 시작 (2분)

**새 터미널 창에서:**

```bash
cd frontend
npm install  # 처음 1회만
npm run dev
```

**확인**: 콘솔에 `Nuxt DevServer` 메시지 출력

### 4단계: 브라우저 테스트 (1분)

1. http://localhost:3000 접속
2. "test-room-1" 입력 후 입장
3. **연결 상태 표시** 확인 ("Connected")
4. 메시지 입력 후 전송
5. **메시지 즉시 표시** 확인

✅ **축하합니다! 시스템이 정상 작동합니다.**

---

## 📊 다음: 성능 테스트

### 부하 테스트 실행 (선택사항)

```bash
# k6 설치 (처음 1회)
choco install k6

# Smoke Test (1분)
k6 run tests/load/smoke-test.js

# Load Test (8분) - 성능 측정
k6 run tests/load/load-test.js

# Stress Test (13분) - 극한 부하
k6 run tests/load/stress-test.js
```

---

## 📚 상세 가이드

| 문서 | 용도 |
|------|------|
| [PHASE_3_LOCAL_SETUP.md](PHASE_3_LOCAL_SETUP.md) | **로컬 환경 실행 가이드** (필독) |
| [PHASE_3_LOAD_TEST_GUIDE.md](PHASE_3_LOAD_TEST_GUIDE.md) | **부하 테스트 상세 가이드** |
| [PHASE_3_SUMMARY.md](PHASE_3_SUMMARY.md) | **Phase 3 완료 보고서** |

---

## 🔍 검증 체크리스트

### 기본 동작 확인

- [ ] Docker 서비스: kafka, redis, zookeeper 모두 UP
- [ ] Backend: http://localhost:8080/actuator/health = UP
- [ ] Frontend: http://localhost:3000 접속 가능
- [ ] WebSocket: DevTools에서 ws:// 연결 확인

### 메시지 송수신 테스트

- [ ] 브라우저 1에서 메시지 전송 가능
- [ ] 메시지가 즉시 화면에 표시됨
- [ ] 브라우저 2에서 메시지 수신 확인
- [ ] 타임스탐프 표시 (ISO 8601 형식)

### 재연결 테스트

- [ ] DevTools에서 WebSocket 연결 끊김
- [ ] "Reconnecting..." 표시 확인
- [ ] 5초 후 자동 재연결
- [ ] 이전 메시지 로드

---

## 💡 주요 기능

### 실시간 메시지 송수신
- STOMP WebSocket 기반 실시간 통신
- 메시지 배송 시간: < 100ms (목표)

### 다중 사용자 지원
- 동시 500명 접속 지원 (성능 목표)
- 극한 1000명 접속 가능

### 메시지 캐싱
- Redis에서 최근 50개 메시지 보관
- 10분 TTL

### 자동 재연결
- 연결 끊김 시 5초 후 자동 재연결
- 4초 heartbeat 유지

---

## 🛠️ 환경 요구사항

| 항목 | 버전 | 확인 |
|------|------|------|
| Java | 11+ | `java -version` |
| Node.js | 18+ | `node --version` |
| Docker | 20+ | `docker --version` |
| k6 | 최신 | `k6 --version` |

---

## 📁 프로젝트 구조

```
nuxt-chat/
├── backend/                    # Spring Boot 백엔드
│   └── ✅ 18 테스트 (GREEN)
├── frontend/                   # Nuxt 3 프론트엔드
│   └── ✅ 39 테스트 (GREEN)
├── tests/load/                 # k6 부하 테스트
│   ├── smoke-test.js          # 10 VU × 1분
│   ├── load-test.js           # 500 VU × 8분
│   └── stress-test.js         # 1000 VU × 13분
├── docker-compose.yml         # Docker 서비스
├── PHASE_3_LOCAL_SETUP.md     # 로컬 실행 가이드
├── PHASE_3_LOAD_TEST_GUIDE.md # 부하 테스트 가이드
└── PHASE_3_SUMMARY.md         # 종합 보고서
```

---

## 🔴 문제 해결

### Backend 시작 오류
```
ERROR: JAVA_HOME is not set
```
→ PowerShell에서 JAVA_HOME 설정:
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-11"
```

### WebSocket 연결 실패
```
WebSocket connection failed
```
→ Backend가 실행 중인지 확인:
```bash
curl http://localhost:8080/actuator/health
```

### 포트 충돌
```
Address already in use :::8080
```
→ 포트 해제:
```bash
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

더 많은 문제 해결: [PHASE_3_LOCAL_SETUP.md](PHASE_3_LOCAL_SETUP.md) 참고

---

## 🎯 성능 목표

### Load Test (500 VU)
- ✅ p95 메시지 배송 < 100ms
- ✅ 에러율 < 1%
- ✅ 메모리 < 2GB

### Stress Test (1000 VU)
- ✓ 1000명 동시접속
- ✓ p95 메시지 지연 < 250ms
- ✓ 에러율 < 5%

---

## 📞 지원

질문이나 문제가 있으시면:

1. **PHASE_3_LOCAL_SETUP.md** - 로컬 실행 가이드 확인
2. **PHASE_3_LOAD_TEST_GUIDE.md** - 부하 테스트 가이드 확인
3. **PHASE_3_SUMMARY.md** - 종합 정보 확인

---

## ✨ 다음 단계

### Phase 3 완료 후
- ✅ 로컬 환경에서 실시간 채팅 시스템 동작 확인
- ✅ 부하 테스트로 성능 검증

### Phase 4 (배포 준비)
- Docker 이미지 빌드
- CI/CD 파이프라인 구축
- API 문서화

---

## 🎉 시작하기

**지금 시작하세요!**

```bash
# 1단계
docker-compose up -d

# 2단계 (새 터미널)
cd backend && .\gradlew bootRun

# 3단계 (새 터미널)
cd frontend && npm run dev

# 4단계
# 브라우저: http://localhost:3000
```

**5분 후, 실시간 채팅이 화면에 띄워집니다.** 🚀

---

**Happy Testing! 🎉**

