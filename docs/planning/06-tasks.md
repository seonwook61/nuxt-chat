# TASKS: realtime-chat-1000 마일스톤 및 태스크

## 마일스톤 개요

| 마일스톤 | 기간 | 설명 | 상태 |
|---------|------|------|------|
| M0 | 1일 | 프로젝트 셋업 | ⏳ 진행중 |
| M0.5 | 1일 | 계약 우선 정의 (Contract-First) | ⏸️ 대기 |
| M1 | 3일 | 백엔드 핵심 기능 구현 | ⏸️ 대기 |
| M2 | 3일 | 프론트엔드 UI 구현 | ⏸️ 대기 |
| M3 | 2일 | 통합 및 부하 테스트 | ⏸️ 대기 |
| M4 | 1-2일 | 배포 및 문서화 | ⏸️ 대기 |

**전체 기간**: 11-12일

---

## M0: 프로젝트 셋업 (Phase 0)

**목표**: 개발 가능한 환경 구축

### T0.1 백엔드 초기화
- [x] Git 저장소 초기화
- [ ] backend/ 폴더 생성
- [ ] Gradle 프로젝트 생성
- [ ] Spring Boot 애플리케이션 생성
- [ ] Kafka 설정 (기본)
- [ ] Redis 설정 (기본)
- [ ] Health Check 엔드포인트

**담당**: backend-specialist
**예상 소요**: 2-3시간

### T0.2 Docker Compose 설정
- [ ] docker-compose.yml 작성
- [ ] Kafka + Zookeeper 설정
- [ ] Redis 설정
- [ ] 로컬 환경 실행 검증

**담당**: orchestrator (직접)
**예상 소요**: 1시간

### T0.3 프론트엔드 초기화
- [ ] frontend/ 폴더 생성
- [ ] Nuxt 3 프로젝트 생성
- [ ] TailwindCSS 설정
- [ ] Pinia 설정
- [ ] socket.io-client 설정
- [ ] 기본 라우팅 설정

**담당**: frontend-specialist
**예상 소요**: 2-3시간

### T0.4 문서화
- [x] README.md
- [x] PRD 작성
- [x] TRD 작성
- [x] .gitignore

**담당**: orchestrator (직접)
**예상 소요**: 1시간

---

## M0.5: 계약 우선 정의 (Phase 0.5)

**목표**: TDD RED 상태 - 테스트만 작성, 구현 없음

### T0.5.1 메시지 계약 정의
- [ ] Kafka 메시지 스키마 정의 (ChatMessage DTO)
- [ ] socket.io 이벤트 정의 (join_room, send_message, receive_message)
- [ ] API 엔드포인트 정의 (REST)

**담당**: integration-validator
**예상 소요**: 2시간

### T0.5.2 백엔드 테스트 작성 (RED)
- [ ] Kafka Producer 테스트 (실패하는 테스트)
- [ ] Kafka Consumer 테스트 (실패하는 테스트)
- [ ] Redis 캐시 테스트 (실패하는 테스트)
- [ ] WebSocket 핸들러 테스트 (실패하는 테스트)

**담당**: test-specialist
**예상 소요**: 3시간

### T0.5.3 프론트엔드 테스트 작성 (RED)
- [ ] socket.io 연결 테스트 (실패하는 테스트)
- [ ] 메시지 전송 테스트 (실패하는 테스트)
- [ ] 채팅방 컴포넌트 테스트 (실패하는 테스트)

**담당**: test-specialist
**예상 소요**: 2시간

---

## M1: 백엔드 핵심 기능 구현 (Phase 1)

**목표**: TDD GREEN - 테스트 통과시키기

### T1.1 Kafka Producer/Consumer 구현
- [ ] Kafka Producer 구현
- [ ] Kafka Consumer 구현
- [ ] 메시지 직렬화/역직렬화
- [ ] 테스트 통과 확인 (RED → GREEN)

**담당**: backend-specialist
**예상 소요**: 4시간

### T1.2 Redis 캐싱 구현
- [ ] 최근 메시지 캐시 로직
- [ ] 사용자 목록 캐시
- [ ] TTL 설정
- [ ] 테스트 통과 확인 (RED → GREEN)

**담당**: backend-specialist
**예상 소요**: 3시간

### T1.3 WebSocket 핸들러 구현
- [ ] WebSocket 연결 핸들러
- [ ] 메시지 수신 핸들러
- [ ] 메시지 브로드캐스트
- [ ] 테스트 통과 확인 (RED → GREEN)

**담당**: backend-specialist
**예상 소요**: 4시간

---

## M2: 프론트엔드 UI 구현 (Phase 2)

**목표**: 사용자 인터페이스 완성

### T2.1 채팅방 페이지
- [ ] 메시지 목록 컴포넌트
- [ ] 메시지 입력 컴포넌트
- [ ] 사용자 목록 사이드바
- [ ] 테스트 통과 확인

**담당**: frontend-specialist
**예상 소요**: 5시간

### T2.2 socket.io 클라이언트 연동
- [ ] WebSocket 연결 로직
- [ ] 메시지 송수신 로직
- [ ] 재연결 로직
- [ ] 테스트 통과 확인

**담당**: frontend-specialist
**예상 소요**: 4시간

### T2.3 Pinia 스토어 구현
- [ ] 채팅 스토어
- [ ] 메시지 상태 관리
- [ ] 사용자 상태 관리
- [ ] 테스트 통과 확인

**담당**: frontend-specialist
**예상 소요**: 3시간

---

## M3: 통합 및 부하 테스트 (Phase 3)

**목표**: 성능 목표 달성 확인

### T3.1 E2E 테스트
- [ ] Playwright E2E 테스트 작성
- [ ] 메시지 송수신 시나리오
- [ ] 다중 사용자 시나리오

**담당**: test-specialist
**예상 소요**: 4시간

### T3.2 부하 테스트
- [ ] 부하 테스트 스크립트 작성 (k6 or JMeter)
- [ ] 동시접속 1000명 테스트
- [ ] p95 지연시간 측정
- [ ] 병목 지점 파악 및 최적화

**담당**: backend-specialist + test-specialist
**예상 소요**: 6시간

---

## M4: 배포 및 문서화 (Phase 4)

### T4.1 배포 준비
- [ ] Dockerfile 최적화
- [ ] 환경 변수 설정
- [ ] CI/CD 파이프라인 (선택)

**담당**: backend-specialist + frontend-specialist
**예상 소요**: 3시간

### T4.2 문서화
- [ ] API 문서 (Swagger)
- [ ] 배포 가이드
- [ ] 아키텍처 다이어그램

**담당**: orchestrator
**예상 소요**: 2시간

---

## 다음 태스크

**현재 Phase**: Phase 0 (M0 - 프로젝트 셋업)
**다음 태스크**: T0.1 백엔드 초기화 (backend-specialist)
