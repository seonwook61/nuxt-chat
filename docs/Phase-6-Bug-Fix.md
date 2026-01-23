# Phase 6: Read Receipts Bug Fix

## 문제 설명

읽음 표시(Read Receipts) 기능이 작동하지 않는 문제가 발생했습니다.

## 원인 분석

### 1. 메시지 히스토리 로드 시 읽음 상태 누락

**문제**: `MessageHistoryController`에서 메시지 히스토리를 반환할 때 읽음 상태(`readBy`, `readCount`)를 포함하지 않았습니다.

**영향**:
- 페이지 로드 시 이전 메시지들의 읽음 상태가 표시되지 않음
- 새 메시지만 읽음 표시가 작동함

### 2. Frontend 초기화 누락

**문제**: ChatWindow에서 메시지 히스토리 로드 후 `initializeReadStatus()`를 호출하지 않았습니다.

**영향**:
- Backend에서 읽음 상태를 받아와도 Frontend의 `readStatusMap`에 초기화되지 않음

## 수정 사항

### Backend 수정

#### 1. MessageResponse DTO 확장

**파일**: `backend/src/main/java/com/example/chat/dto/MessageResponse.java`

```java
/**
 * List of user IDs who read this message
 * Phase 6: Read receipts
 */
private Set<String> readBy;

/**
 * Number of users who read this message
 * Phase 6: Read receipts
 */
private Integer readCount;

/**
 * Reactions on this message (emoji -> list of userIds)
 * Phase 3.2: Reaction feature
 */
private Map<String, List<String>> reactions;
```

#### 2. MessageHistoryController 수정

**파일**: `backend/src/main/java/com/example/chat/controller/MessageHistoryController.java`

**변경 사항**:
1. `ReadReceiptService` 의존성 주입 추가
2. 메시지 히스토리 조회 시 읽음 상태 배치 쿼리 추가
3. `entityToResponse()` 메소드에 읽음 상태 파라미터 추가

```java
// Batch fetch read status for all messages (Phase 6)
Map<UUID, Set<String>> readStatusMap = readReceiptService.getReadStatusForMessages(messageIds);

// Convert entities to responses with read status
List<MessageResponse> messages = entities.stream()
    .map(entity -> entityToResponse(entity, readStatusMap.get(entity.getMessageId())))
    .collect(Collectors.toList());
```

**성능 최적화**:
- N+1 쿼리 문제 방지를 위해 배치 쿼리 사용
- `ReadReceiptService.getReadStatusForMessages()` 메소드 활용

### Frontend 수정

#### 1. useMessageHistory 수정

**파일**: `frontend/app/composables/useMessageHistory.ts`

```typescript
return {
  messageId: msg.messageId,
  roomId: msg.roomId,
  userId: msg.userId,
  username: msg.username,
  content: msg.content,
  timestamp: timestamp.toISOString(),
  type: msg.type || 'TEXT',
  reactions: msg.reactions || {},
  // Phase 6: Read receipt status from backend
  readBy: msg.readBy || [],
  readCount: msg.readCount || 0,
  // UI-specific fields
  isOwn: false,
  showAvatar: false,
  showUsername: false,
  showTimestamp: false
}
```

#### 2. ChatWindow 수정

**파일**: `frontend/app/components/chat/ChatWindow.vue`

```typescript
// Initialize read status when messages are loaded (from history)
watch(messages, (newMessages) => {
  if (newMessages.length > 0) {
    initializeReadStatus(newMessages)
  }
}, { immediate: true })
```

**추가된 기능**:
- 메시지 로드 시 `initializeReadStatus()` 자동 호출
- `immediate: true`로 초기 렌더링 시에도 실행

## 데이터 흐름

### 1. 메시지 히스토리 로드 (초기 로딩)

```
Frontend (useChatRoom.joinRoom)
  ↓
Backend (GET /api/messages/history/{roomId})
  ↓
MessageHistoryController
  ├─ persistenceService.getMessageHistory() → 메시지 조회
  └─ readReceiptService.getReadStatusForMessages() → 읽음 상태 조회 (배치)
  ↓
MessageResponse (readBy, readCount 포함)
  ↓
Frontend (useMessageHistory.fetchMessageHistory)
  ↓
Message[] (readBy, readCount 포함)
  ↓
ChatWindow.initializeReadStatus()
  ↓
readStatusMap 초기화
  ↓
UI 업데이트 (✓ or ✓✓ 표시)
```

### 2. 실시간 읽음 처리 (새 메시지)

```
Frontend (ChatWindow - message becomes visible)
  ↓
readReceipts.markAsRead(messageId)
  ↓
WebSocket: /app/chat.read
  ↓
Backend (ChatWebSocketController.handleReadReceipt)
  ↓
readReceiptService.markAsRead()
  ├─ Redis 캐시 확인 (중복 방지)
  ├─ PostgreSQL 저장
  └─ Redis 캐시 업데이트
  ↓
WebSocket Broadcast: /topic/room/{roomId}
  ↓
Frontend (handleIncomingReadReceipt)
  ↓
readStatusMap 업데이트
  ↓
UI 업데이트 (✓ → ✓✓)
```

## 테스트 시나리오

### 1. 초기 로딩 테스트

1. 사용자 A가 메시지 전송
2. 사용자 B가 메시지 읽음 (✓✓ 표시됨)
3. 사용자 A가 페이지 새로고침
4. **예상 결과**: 사용자 A는 기존 메시지의 ✓✓ 표시를 볼 수 있어야 함

### 2. 실시간 업데이트 테스트

1. 사용자 A가 메시지 전송 (✓ 표시)
2. 사용자 B가 채팅방 입장 및 메시지 읽음
3. **예상 결과**: 사용자 A는 실시간으로 ✓ → ✓✓ 변경을 봐야 함

### 3. 다중 사용자 테스트

1. 사용자 A가 메시지 전송
2. 사용자 B가 읽음 (readCount: 1)
3. 사용자 C가 읽음 (readCount: 2)
4. **예상 결과**: 읽은 사람 수가 정확하게 표시되어야 함

## 성능 고려사항

### 1. 배치 쿼리 최적화

**Before (N+1 문제)**:
```java
messages.forEach(msg -> {
    Set<String> readBy = readReceiptService.getUsersWhoRead(msg.getMessageId());
    // 메시지 50개 → 50번의 DB 쿼리
})
```

**After (배치 쿼리)**:
```java
Map<UUID, Set<String>> readStatusMap = readReceiptService.getReadStatusForMessages(messageIds);
// 메시지 50개 → 1번의 DB 쿼리
```

### 2. Redis 캐싱

- 마지막 읽음 위치는 Redis에 1시간 TTL로 캐싱
- 중복 읽음 이벤트는 Redis에서 필터링
- DB 쿼리 부하 최소화

## 빌드 및 배포

### Backend

```bash
cd backend
./gradlew build -x test
```

### Frontend

```bash
cd frontend
npm run build
```

## 완료 체크리스트

- [x] MessageResponse DTO에 readBy, readCount 필드 추가
- [x] MessageHistoryController에 ReadReceiptService 통합
- [x] 배치 쿼리로 읽음 상태 조회 최적화
- [x] useMessageHistory에서 readBy, readCount 매핑
- [x] ChatWindow에서 initializeReadStatus 호출
- [x] Backend 빌드 성공
- [x] Frontend 빌드 성공
- [ ] 수동 테스트 (3개 시나리오)
- [ ] 성능 테스트 (50개 메시지 로드 시간)

## 다음 단계

1. **통합 테스트**: 실제 사용자 시나리오로 테스트
2. **성능 모니터링**: 메시지 히스토리 로드 시간 측정
3. **문서화**: API 스펙에 readBy, readCount 필드 추가
