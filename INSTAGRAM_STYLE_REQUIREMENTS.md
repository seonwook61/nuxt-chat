# Instagram Style Chat UI Requirements

## 프로젝트 개요
현재 기본 채팅 UI를 Instagram 스타일의 현대적인 채팅 인터페이스로 업그레이드

## 현재 상태 (Phase 2 완료)
- ✅ STOMP WebSocket 연결
- ✅ 실시간 메시지 송수신
- ✅ 온라인 사용자 수 표시
- ✅ 기본 채팅 UI

## 새로운 요구사항

### 1. 사용자 프로필 정보
- **사용자 사진 (Avatar)**: 각 메시지에 프로필 이미지 표시
- **사용자 이름**: 메시지 작성자 이름 표시
- **온라인 상태**: 사용자 아바타에 온라인 표시 배지

### 2. 메시지 기능 확장

#### 2.1 답글 기능 (Reply/Thread)
- 특정 메시지에 답글 달기
- 답글 메시지에 원본 메시지 인용 표시
- 답글 클릭 시 원본 메시지로 스크롤

#### 2.2 반응 기능 (Reactions)
- 좋아요 버튼 (❤️)
- 여러 이모지 반응 선택 가능 (👍, 😂, 😮, 😢, 🙏)
- 반응 카운트 표시
- 반응한 사용자 목록 표시

#### 2.3 메시지 액션
- **복사 (Copy)**: 메시지 텍스트 클립보드 복사
- **답글 (Reply)**: 해당 메시지에 답글
- **삭제 (Delete)**: 본인 메시지만 삭제 가능
- **신고 (Report)**: 타인 메시지 신고 (추후 구현)

### 3. 미디어 첨부

#### 3.1 이미지 첨부
- 이미지 파일 선택 및 업로드
- 이미지 미리보기 (썸네일)
- 이미지 클릭 시 전체 화면 모달
- 지원 형식: JPG, PNG, GIF, WEBP
- 최대 크기: 5MB

#### 3.2 추후 확장 (Phase 4)
- 동영상 첨부
- 음성 메시지
- 파일 첨부

### 4. UI/UX 개선

#### 4.1 메시지 버블 디자인
- Instagram 스타일 말풍선
- 본인 메시지: 오른쪽 정렬, 파란색 배경
- 타인 메시지: 왼쪽 정렬, 회색 배경
- 연속된 메시지 그룹핑 (타임스탬프 생략)

#### 4.2 입력창 개선
- 멀티라인 텍스트 입력 (자동 높이 조절)
- 이미지 첨부 버튼
- 이모지 피커
- 전송 버튼 활성화 상태 표시

#### 4.3 상호작용 피드백
- 타이핑 인디케이터 (누가 입력 중인지 표시)
- 메시지 전송 상태 (전송 중, 전송 완료, 실패)
- 읽음 표시 (추후 구현)

## 스키마 변경사항

### Backend DTO 변경

#### ChatMessage 확장
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {
    private UUID messageId;
    private String roomId;
    private String userId;
    private String username;
    private String userAvatar;          // NEW: 사용자 프로필 이미지 URL
    private String content;
    private LocalDateTime timestamp;
    private String type;                // TEXT, IMAGE, VIDEO, FILE

    // NEW: Reply 관련
    private UUID replyToMessageId;      // 답글 대상 메시지 ID
    private String replyToUsername;     // 답글 대상 사용자 이름
    private String replyToContent;      // 답글 대상 메시지 내용 (미리보기)

    // NEW: Media 관련
    private MediaAttachment media;      // 첨부 미디어 정보

    // NEW: Reactions 관련
    private Map<String, List<String>> reactions; // emoji -> [userId1, userId2, ...]
}

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MediaAttachment {
    private String type;                // IMAGE, VIDEO, FILE
    private String url;                 // 미디어 URL
    private String thumbnailUrl;        // 썸네일 URL
    private String filename;
    private Long fileSize;
    private Integer width;              // 이미지/비디오 너비
    private Integer height;             // 이미지/비디오 높이
}
```

#### MessageReaction DTO (새로운 이벤트)
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageReaction {
    private UUID reactionId;
    private UUID messageId;
    private String roomId;
    private String userId;
    private String username;
    private String emoji;               // ❤️, 👍, 😂, 😮, 😢, 🙏
    private String action;              // ADD, REMOVE
    private LocalDateTime timestamp;
}
```

#### TypingIndicator DTO (새로운 이벤트)
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TypingIndicator {
    private String roomId;
    private String userId;
    private String username;
    private boolean isTyping;           // true: 타이핑 시작, false: 타이핑 종료
    private LocalDateTime timestamp;
}
```

### Frontend Types 변경

#### types/chat.ts 확장
```typescript
export interface Message {
  messageId: string
  roomId: string
  userId: string
  username: string
  userAvatar?: string                 // NEW
  content: string
  timestamp: string
  type: 'TEXT' | 'IMAGE' | 'VIDEO' | 'FILE'

  // NEW: Reply
  replyToMessageId?: string
  replyToUsername?: string
  replyToContent?: string

  // NEW: Media
  media?: MediaAttachment

  // NEW: Reactions
  reactions?: Record<string, string[]> // emoji -> userId[]

  // NEW: UI State
  isSending?: boolean
  sendError?: string
}

export interface MediaAttachment {
  type: 'IMAGE' | 'VIDEO' | 'FILE'
  url: string
  thumbnailUrl?: string
  filename?: string
  fileSize?: number
  width?: number
  height?: number
}

export interface MessageReaction {
  reactionId: string
  messageId: string
  roomId: string
  userId: string
  username: string
  emoji: string
  action: 'ADD' | 'REMOVE'
  timestamp: string
}

export interface TypingIndicator {
  roomId: string
  userId: string
  username: string
  isTyping: boolean
  timestamp: string
}

export interface User {
  userId: string
  username: string
  avatar?: string
  isOnline?: boolean
}
```

### Redis 스키마 추가

```
# 메시지 반응
chat:message:{messageId}:reactions -> Hash
  {emoji}: {userId1,userId2,...}

# 타이핑 인디케이터
chat:room:{roomId}:typing -> Set (TTL 3초)
  {userId}

# 사용자 프로필
chat:user:{userId}:profile -> Hash
  username: string
  avatar: string
  status: online|offline
  lastSeen: timestamp

# 미디어 파일 메타데이터
chat:media:{mediaId} -> Hash
  url: string
  type: IMAGE|VIDEO|FILE
  filename: string
  fileSize: number
  uploadedBy: userId
  uploadedAt: timestamp
```

### Kafka Topics 확장

```
# 기존
chat.message.v1    -> ChatMessage

# 새로운 토픽
chat.reaction.v1   -> MessageReaction
chat.typing.v1     -> TypingIndicator
```

### STOMP Destinations 추가

```
# 메시지 반응
/app/chat.reaction           -> MessageReaction 전송
/topic/room/{roomId}/reaction -> MessageReaction 구독

# 타이핑 인디케이터
/app/chat.typing             -> TypingIndicator 전송
/topic/room/{roomId}/typing  -> TypingIndicator 구독

# 미디어 업로드 (REST API)
POST /api/media/upload       -> multipart/form-data
```

## 구현 우선순위

### Phase 3.1: UI 개선 (2-3일)
1. ✅ 사용자 아바타 표시
2. ✅ Instagram 스타일 메시지 버블
3. ✅ 메시지 그룹핑 및 타임스탬프
4. ✅ 개선된 입력창 (멀티라인)

### Phase 3.2: 반응 기능 (2일)
1. ✅ 좋아요 버튼
2. ✅ 다중 이모지 반응
3. ✅ 반응 카운트 및 사용자 목록
4. ✅ Backend 반응 이벤트 처리

### Phase 3.3: 답글 기능 (2일)
1. ✅ 답글 UI (인용 메시지 표시)
2. ✅ 답글 데이터 전송
3. ✅ 원본 메시지로 스크롤

### Phase 3.4: 이미지 첨부 (3-4일)
1. ✅ 이미지 선택 및 미리보기
2. ✅ 이미지 업로드 (S3 또는 로컬 저장소)
3. ✅ 이미지 메시지 표시
4. ✅ 이미지 모달 (전체 화면)

### Phase 3.5: 추가 기능 (1-2일)
1. ✅ 타이핑 인디케이터
2. ✅ 메시지 복사
3. ✅ 메시지 삭제 (본인만)
4. ✅ 이모지 피커

## 기술 스택

### Frontend 추가 라이브러리
- **Headless UI**: 모달, 드롭다운 등 접근성 좋은 컴포넌트
- **emoji-picker-element**: 이모지 피커
- **image-compressor.js**: 이미지 압축
- **photoswipe**: 이미지 갤러리 모달

### Backend 추가 라이브러리
- **Spring Boot Multipart**: 파일 업로드
- **Thumbnailator**: 이미지 썸네일 생성
- **AWS S3 SDK** (옵션): 이미지 저장소
- **ImgProxy** (옵션): 이미지 리사이징 서비스

## 파일 구조 변경

### Frontend 추가 파일
```
frontend/app/
├── components/
│   ├── chat/
│   │   ├── ChatWindow.vue              (기존)
│   │   ├── MessageList.vue             (기존)
│   │   ├── MessageInput.vue            (수정)
│   │   ├── MessageBubble.vue           (NEW) Instagram 스타일 버블
│   │   ├── MessageActions.vue          (NEW) 복사/답글/삭제 메뉴
│   │   ├── MessageReactions.vue        (NEW) 반응 표시 및 추가
│   │   ├── ReplyPreview.vue            (NEW) 답글 대상 미리보기
│   │   ├── ImageAttachment.vue         (NEW) 이미지 메시지
│   │   ├── ImageUpload.vue             (NEW) 이미지 선택 및 업로드
│   │   ├── ImageModal.vue              (NEW) 전체 화면 이미지
│   │   ├── EmojiPicker.vue             (NEW) 이모지 선택기
│   │   ├── TypingIndicator.vue         (NEW) 타이핑 중 표시
│   │   └── UserAvatar.vue              (NEW) 사용자 아바타
├── composables/
│   ├── useSocket.ts                    (기존)
│   ├── useChatRoom.ts                  (수정 - 반응/답글/타이핑)
│   ├── useMessageActions.ts            (NEW) 메시지 액션
│   ├── useImageUpload.ts               (NEW) 이미지 업로드
│   └── useTypingIndicator.ts           (NEW) 타이핑 인디케이터
└── types/
    └── chat.ts                         (수정 - 위 스키마 참고)
```

### Backend 추가 파일
```
backend/src/main/java/com/example/chat/
├── dto/
│   ├── ChatMessage.java                (수정)
│   ├── MessageReaction.java            (NEW)
│   ├── TypingIndicator.java            (NEW)
│   ├── MediaAttachment.java            (NEW)
│   └── MediaUploadResponse.java        (NEW)
├── controller/
│   ├── ChatWebSocketController.java    (수정 - 반응/타이핑)
│   └── MediaController.java            (NEW) 이미지 업로드 REST API
├── service/
│   ├── MediaService.java               (NEW) 이미지 저장 및 처리
│   └── ReactionService.java            (NEW) 반응 처리
└── config/
    └── FileUploadConfig.java           (NEW) 파일 업로드 설정
```

## 성능 고려사항

### 이미지 최적화
- 클라이언트에서 이미지 압축 (최대 1920x1080)
- 썸네일 자동 생성 (200x200)
- WebP 형식 변환 (지원 브라우저)
- Lazy loading (스크롤 시 로드)

### 실시간 이벤트 최적화
- 타이핑 인디케이터 debounce (500ms)
- 반응 이벤트 batching (100ms)
- Redis TTL 적극 활용 (타이핑 3초, 반응 캐시 1시간)

## 보안 고려사항

### 파일 업로드
- MIME type 검증
- 파일 크기 제한 (5MB)
- 파일 확장자 화이트리스트
- 안티바이러스 스캔 (옵션)
- Content-Security-Policy 설정

### 사용자 입력
- XSS 방지 (텍스트 이스케이프)
- 메시지 길이 제한 (1000자)
- Rate limiting (메시지 전송 5/초, 반응 10/초)

## 테스트 계획

### Unit Tests
- MessageBubble 컴포넌트 렌더링
- 반응 추가/제거 로직
- 이미지 업로드 검증
- 답글 데이터 구조

### Integration Tests
- WebSocket 반응 이벤트 전송/수신
- 이미지 업로드 및 메시지 전송
- 타이핑 인디케이터 동작

### E2E Tests
- 전체 채팅 플로우 (메시지 → 반응 → 답글)
- 이미지 첨부 및 표시
- 다중 사용자 동시 사용

## 예상 일정
- **Phase 3.1**: 2-3일 (UI 개선)
- **Phase 3.2**: 2일 (반응 기능)
- **Phase 3.3**: 2일 (답글 기능)
- **Phase 3.4**: 3-4일 (이미지 첨부)
- **Phase 3.5**: 1-2일 (추가 기능)
- **테스트 및 버그 수정**: 2-3일

**총 예상 기간**: 12-16일
