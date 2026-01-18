# Frontend Project Structure (Phase 0 Complete)

## Overview
Nuxt 3 프론트엔드 초기화 완료 - Phase 0, T0.3

## Directory Structure

```
frontend/
├── app/
│   ├── pages/
│   │   ├── index.vue                    # 채팅방 입장 페이지
│   │   └── rooms/
│   │       └── [roomId].vue             # 채팅방 페이지 (기본 레이아웃)
│   ├── components/
│   │   └── chat/
│   │       ├── ChatWindow.vue           # 채팅 윈도우 컨테이너
│   │       ├── MessageList.vue          # 메시지 리스트
│   │       └── MessageInput.vue         # 메시지 입력
│   ├── composables/
│   │   ├── useSocket.ts                 # Socket.io 훅 (기본 구조만)
│   │   └── useChatRoom.ts               # 채팅방 훅 (기본 구조만)
│   ├── stores/
│   │   └── chat.ts                      # Pinia 채팅 스토어 (기본 구조)
│   ├── types/
│   │   └── chat.ts                      # TypeScript 타입 정의
│   ├── plugins/
│   │   └── socket.client.ts             # Socket.io 플러그인 (기본 구조만)
│   └── app.vue                          # 루트 컴포넌트
├── test/
│   └── setup.ts                         # Vitest 설정
├── .dockerignore
├── .env.example
├── .gitignore
├── Dockerfile
├── nuxt.config.ts
├── package.json
├── playwright.config.ts
├── README.md
├── tailwind.config.js
├── tsconfig.json
└── vitest.config.ts
```

## Key Features Implemented

### 1. Pages
- **index.vue**: 채팅방 입장 UI
  - Room ID 입력
  - Quick Join 버튼

- **rooms/[roomId].vue**: 채팅방 UI
  - 헤더 (뒤로가기, 방 이름, 온라인 유저 수)
  - ChatWindow 컴포넌트 통합

### 2. Components
- **ChatWindow.vue**: 메시지 리스트와 입력 컨테이너
- **MessageList.vue**: 메시지 표시 (빈 상태 UI 포함)
- **MessageInput.vue**:
  - 자동 높이 조절 textarea
  - Enter 전송, Shift+Enter 줄바꿈
  - 글자 수 제한 (1000자)

### 3. Type Definitions
- Message
- ServerToClientEvents
- ClientToServerEvents
- SendMessagePayload
- SystemMessage
- ErrorMessage
- UserEvent
- ChatRoom

### 4. Store (Pinia)
- rooms: Map<string, ChatRoom>
- currentRoomId
- connected
- Getters: currentRoom, currentMessages
- Actions: setCurrentRoom, addMessage, clearRoom, setConnected

### 5. Composables (Placeholder)
- useSocket(): Socket.io 연결 관리 (Phase 2에서 구현)
- useChatRoom(): 채팅방 로직 (Phase 2에서 구현)

## Tech Stack

- **Framework**: Nuxt 3.17.5
- **Vue**: 3.5.26
- **Build Tool**: Vite 6.4.1
- **Runtime**: Nitro 2.13.1
- **Styling**: TailwindCSS 3.4.1
- **State**: Pinia 2.1.7
- **WebSocket**: socket.io-client 4.7.2
- **Testing**: Vitest 1.2.1, Playwright 1.41.1
- **Language**: TypeScript 5.3.3

## Build Status

✅ Development server: localhost:3000
✅ Production build: Success
✅ TailwindCSS: Configured
✅ TypeScript: Strict mode
✅ Docker: Ready

## Next Steps (Phase 0.5+)

1. **Phase 0.5**: 테스트 계약 작성
2. **Phase 1**: Socket.io 연결 구현
3. **Phase 2**: 실시간 메시징
4. **Phase 3**: 최적화 (가상 스크롤, 성능)

## Notes

- Socket.io 연결 로직은 Phase 2에서 구현
- 현재는 기본 UI 구조만 제공
- 모든 composable/plugin은 placeholder
- TypeScript strict mode 활성화
- TailwindCSS v3.4.1 사용 (v4 호환성 문제로 다운그레이드)
