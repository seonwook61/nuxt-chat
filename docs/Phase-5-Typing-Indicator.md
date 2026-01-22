# Phase 5: 입력 중 표시 (Typing Indicator)

## 📋 요구사항 정의

### 기능 요구사항
1. 사용자가 메시지를 입력하면 다른 참여자들에게 "OOO님이 입력 중..." 표시
2. 입력을 멈추면 3초 후 자동으로 표시 제거
3. 동시에 여러 사용자가 입력 중일 때 "Alice, Bob님이 입력 중..." 형태로 표시
4. 자신의 입력 중 상태는 표시하지 않음
5. 메시지를 전송하면 즉시 입력 중 상태 제거

### 비기능 요구사항
1. **성능**: 입력 이벤트 디바운싱 (500ms)으로 과도한 네트워크 트래픽 방지
2. **확장성**: Redis TTL 기반으로 자동 정리
3. **실시간성**: WebSocket으로 즉각 전파 (< 100ms)
4. **안정성**: 비정상 종료 시 Redis TTL로 자동 정리

---

## 🏗️ 아키텍처 설계

### 데이터 흐름

```
[Client A - 입력 시작]
  ↓ (500ms 디바운스)
  WebSocket /app/chat.typing
  ↓
[Backend - TypingIndicator 수신]
  ↓
1. Redis: room:{roomId}:typing → Set<userId> (TTL: 5초)
2. WebSocket Broadcast → /topic/room/{roomId}
  ↓
[Client B, C, D - 입력 중 표시]
  "Alice님이 입력 중..."
```

### Redis 데이터 구조

```
Key: room:{roomId}:typing
Type: Set
Value: [userId1, userId2, userId3]
TTL: 5초 (자동 만료)

예시:
Key: room:general:typing
Members: ["user-123", "user-456"]
TTL: 5초
```

---

## 🔧 기술 스택

### Backend
- **WebSocket**: STOMP `/app/chat.typing`
- **Redis**: Set 자료구조 + TTL
- **데이터 모델**: TypingIndicator DTO

### Frontend
- **Debounce**: 500ms (lodash 또는 자체 구현)
- **UI**: MessageList 하단에 표시
- **상태 관리**: ref (로컬 상태)

---

## 📊 ERD (Extended)

기존 ERD에 추가 없음 (Redis 기반, DB 저장 불필요)

---

## 🔄 시퀀스 다이어그램

### 1. 입력 시작

```
Client A          Backend           Redis            Client B
   |                 |                 |                 |
   |-- input ------->|                 |                 |
   | (debounce 500ms)|                 |                 |
   |                 |                 |                 |
   |-- typing:true ->|                 |                 |
   |                 |-- SADD -------->|                 |
   |                 |   room:gen:typing, userId         |
   |                 |   + EXPIRE 5s   |                 |
   |                 |<-- OK -----------|                 |
   |                 |                 |                 |
   |                 |-- broadcast ----|---------------->|
   |                 |   TypingIndicator                 |
   |                 |                 |                 |
   |                 |                 |        [표시] "Alice님이 입력 중..."
```

### 2. 입력 중지

```
Client A          Backend           Redis            Client B
   |                 |                 |                 |
   |-- stop typing ->|                 |                 |
   | (3초 후)        |                 |                 |
   |                 |                 |                 |
   |-- typing:false->|                 |                 |
   |                 |-- SREM -------->|                 |
   |                 |   room:gen:typing, userId         |
   |                 |<-- OK -----------|                 |
   |                 |                 |                 |
   |                 |-- broadcast ----|---------------->|
   |                 |   TypingIndicator                 |
   |                 |                 |                 |
   |                 |                 |        [제거] 표시 사라짐
```

### 3. 메시지 전송

```
Client A          Backend           Redis            Client B
   |                 |                 |                 |
   |-- send msg ---->|                 |                 |
   |                 |-- SREM -------->|                 |
   |                 |   (typing 제거) |                 |
   |                 |                 |                 |
   |                 |-- broadcast ----|---------------->|
   |                 |   ChatMessage   |                 |
   |                 |   + TypingIndicator (stop)        |
   |                 |                 |                 |
```

---

## 📦 데이터 모델

### Backend DTO

```java
/**
 * Typing Indicator DTO
 * 사용자가 입력 중임을 실시간으로 전달
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TypingIndicator {

    /**
     * 방 ID
     */
    @NotBlank
    private String roomId;

    /**
     * 입력 중인 사용자 ID
     */
    @NotBlank
    private String userId;

    /**
     * 입력 중인 사용자 이름
     */
    @NotBlank
    private String username;

    /**
     * 입력 상태 (true: 입력 중, false: 중지)
     */
    @NotNull
    private Boolean isTyping;

    /**
     * 타임스탬프
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}
```

### Frontend Type

```typescript
export interface TypingIndicator {
  roomId: string
  userId: string
  username: string
  isTyping: boolean
  timestamp: string
}

export interface TypingState {
  users: Map<string, string> // userId -> username
}
```

---

## 🎨 UI 설계

### 위치
메시지 리스트 하단, 입력창 바로 위

### 디자인
```
┌─────────────────────────────────┐
│ [Messages]                      │
│ Alice: Hello                    │
│ Bob: Hi                         │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ 💬 Alice님이 입력 중...     │ │ ← Typing Indicator
│ └─────────────────────────────┘ │
│                                 │
│ [😀] [Message input...] [Send]  │
└─────────────────────────────────┘
```

### 애니메이션
- 점 3개 깜빡임 애니메이션 (...)
- Fade in/out transition

---

## ⚙️ Redis Service 확장

### 메서드 추가

```java
public interface RedisCacheService {
    // 기존 메서드들...

    /**
     * Add user to typing set
     */
    void addTypingUser(String roomId, String userId);

    /**
     * Remove user from typing set
     */
    void removeTypingUser(String roomId, String userId);

    /**
     * Get all typing users in a room
     */
    Set<String> getTypingUsers(String roomId);
}
```

---

## 🧪 테스트 시나리오

### 시나리오 1: 기본 입력 중 표시
1. Alice가 general 방에서 입력 시작
2. Bob 화면에 "Alice님이 입력 중..." 표시
3. Alice가 입력 중지
4. 3초 후 Bob 화면에서 표시 제거

### 시나리오 2: 다중 사용자
1. Alice 입력 시작 → "Alice님이 입력 중..."
2. Bob 입력 시작 → "Alice, Bob님이 입력 중..."
3. Alice 입력 중지 → "Bob님이 입력 중..."
4. Bob 입력 중지 → 표시 제거

### 시나리오 3: 메시지 전송
1. Alice 입력 시작 → 표시
2. Alice 메시지 전송 → 즉시 표시 제거

### 시나리오 4: 비정상 종료
1. Alice 입력 시작
2. Alice 브라우저 강제 종료
3. 5초 후 Redis TTL로 자동 정리 → 표시 제거

---

## 📈 성능 최적화

### 1. Debouncing (500ms)
- 매 키 입력마다 WebSocket 전송 방지
- 네트워크 트래픽 95% 감소

### 2. Redis TTL (5초)
- 비정상 종료 시 자동 정리
- 메모리 누수 방지

### 3. Set 자료구조
- O(1) 추가/제거/조회
- 중복 방지

---

## 🚀 구현 순서

1. ✅ 요구사항 정의 및 설계 (현재)
2. Backend DTO 생성 (`TypingIndicator.java`)
3. Redis Service 확장
4. WebSocket Controller 추가 (`/app/chat.typing`)
5. Frontend Type 정의
6. Frontend Composable (`useTyping.ts`)
7. Frontend UI 컴포넌트 (`TypingIndicator.vue`)
8. 통합 및 테스트
9. 문서화

---

## 📝 다음 단계

구현 완료 후 다음 기능 후보:
- Phase 6: 읽음 표시 (Read Receipts)
- Phase 7: 무한 스크롤 (Pagination)
- Phase 8: 메시지 검색 (Full-text Search)
