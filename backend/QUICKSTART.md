# Quick Start Guide

## Phase 0, T0.1 완료 확인

### 1. 구조 검증

```bash
./verify.sh
```

**예상 결과:**
- 통과: 19
- 실패: 0-1 (Java 미설치 시 1)

### 2. Java 설치 (미설치 시)

**Windows:**
```bash
choco install openjdk17
```

**macOS:**
```bash
brew install openjdk@17
```

**Linux:**
```bash
sudo apt-get install openjdk-17-jdk
```

**설치 확인:**
```bash
java -version
# openjdk version "17.x.x" 확인
```

### 3. Kafka & Redis 실행

```bash
cd ..  # 프로젝트 루트로
docker-compose up -d kafka redis

# 상태 확인
docker-compose ps
```

**예상 출력:**
```
NAME      SERVICE   STATUS    PORTS
kafka     kafka     running   0.0.0.0:9092->9092/tcp
redis     redis     running   0.0.0.0:6379->6379/tcp
```

### 4. 백엔드 빌드 및 실행

```bash
cd backend

# 빌드 (테스트 제외)
.\gradlew clean build -x test

# 실행
.\gradlew bootRun
```

**예상 로그:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.1)

...
Started ChatApplication in X.XXX seconds
```

### 5. Health Check 검증

**새 터미널에서:**
```bash
curl http://localhost:8080/health
```

**예상 응답:**
```json
{"status":"ok"}
```

**브라우저:**
http://localhost:8080/health

### 6. Actuator Endpoints 확인

```bash
# Prometheus metrics
curl http://localhost:8080/actuator/prometheus

# Health 상세
curl http://localhost:8080/actuator/health

# 전체 endpoints
curl http://localhost:8080/actuator
```

## 문제 해결

### Java 버전 오류

```bash
# 현재 Java 버전 확인
java -version

# JAVA_HOME 설정 (Linux/macOS)
export JAVA_HOME=/path/to/java17
export PATH=$JAVA_HOME/bin:$PATH

# JAVA_HOME 설정 (Windows PowerShell)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

### Gradle 빌드 실패

```bash
# Gradle Wrapper 재생성
./gradlew wrapper --gradle-version 8.5

# Gradle 캐시 클리어
./gradlew clean --refresh-dependencies
```

### 포트 충돌 (8080)

**Windows:**
```bash
# 8080 포트 사용 중인 프로세스 찾기
netstat -ano | findstr :8080

# 프로세스 종료
taskkill /PID <PID> /F
```

**Linux/macOS:**
```bash
# 8080 포트 사용 중인 프로세스 찾기
lsof -i :8080

# 프로세스 종료
kill -9 <PID>
```

**또는 포트 변경:**
```bash
# .env 파일 생성
cp .env.example .env

# .env 파일 수정
SERVER_PORT=8081
```

### Kafka/Redis 연결 실패

```bash
# 컨테이너 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs kafka
docker-compose logs redis

# 재시작
docker-compose restart kafka redis

# 완전 재시작
docker-compose down
docker-compose up -d
```

## 다음 단계

Phase 0, T0.1 완료 후:

1. **Phase 0.5**: 테스트 계약 작성 (TDD)
   - HealthController 테스트
   - 통합 테스트 기본 구조

2. **Phase 1**: 채팅 메시지 API
   - POST /api/rooms/{roomId}/messages
   - Kafka Producer 구현
   - 메시지 검증 및 에러 처리

3. **Phase 2**: WebSocket 연동
   - Kafka Consumer
   - Redis Pub/Sub
   - WebSocket 메시지 fanout

## 참고 문서

- [README.md](README.md) - 전체 프로젝트 가이드
- [../docs/planning/01-prd.md](../docs/planning/01-prd.md) - 제품 요구사항
- [../docs/planning/02-trd.md](../docs/planning/02-trd.md) - 기술 설계
- [../docs/planning/06-tasks.md](../docs/planning/06-tasks.md) - 태스크 목록
