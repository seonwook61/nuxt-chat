# Phase 3.2 Bug Fixes

## 수정된 문제

### 1. 반응 기능이 작동하지 않는 문제

**문제**:
- 메시지를 길게 눌러도 반응이 추가되지 않음
- 이모지 선택이 안됨

**원인**:
1. `useReactions` composable에서 timestamp 형식 불일치
   - 프론트엔드: `toISOString()` → `2024-01-21T14:30:00.123Z`
   - 백엔드 기대 형식: `yyyy-MM-dd'T'HH:mm:ss` (밀리초와 'Z' 없음)

2. `MessageBubble` 컴포넌트가 currentUserId와 currentUsername을 localStorage에서 직접 읽음
   - `onMounted`에서 읽기 때문에 초기 렌더링 시 빈 값
   - Props로 전달받아야 함

**수정 사항**:

1. **useReactions.ts** - timestamp 형식 수정
```typescript
// Before
timestamp: new Date().toISOString()

// After
const now = new Date()
const timestamp = now.toISOString().substring(0, 19) // yyyy-MM-dd'T'HH:mm:ss
```

2. **MessageBubble.vue** - Props로 사용자 정보 받기
```typescript
// Before
const currentUserId = ref('')
const currentUsername = ref('')

onMounted(() => {
  if (process.client) {
    currentUserId.value = localStorage.getItem('userId') || ...
  }
})

// After
interface Props {
  message: Message
  currentUserId: string
  currentUsername: string
}
```

3. **MessageList.vue** - 사용자 정보 전달
```typescript
interface Props {
  messages: readonly Message[]
  currentUserId: string
  currentUsername?: string
}
```

4. **ChatWindow.vue** - currentUsername 전달
```vue
<MessageList
  :messages="messages"
  :current-user-id="currentUserId"
  :current-username="currentUser.username"
/>
```

### 2. 채팅창 왼쪽 버튼 UI 개선

**문제**:
- 뒤로가기 버튼이 눈에 잘 안 띔
- 클릭 영역이 작음
- 호버 효과 없음

**수정 사항**:

**[roomId].vue** - 버튼 UI 개선
```vue
<!-- Before -->
<button
  @click="router.push('/')"
  class="text-gray-600 hover:text-gray-900"
>
  <svg class="w-6 h-6">...</svg>
</button>
<h1 class="text-2xl font-bold text-gray-900">
  Room: {{ roomId }}
</h1>

<!-- After -->
<button
  @click="router.push('/')"
  class="p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors"
  title="홈으로 돌아가기"
>
  <svg class="w-6 h-6">...</svg>
</button>
<div>
  <h1 class="text-xl font-bold text-gray-900">
    {{ roomId }}
  </h1>
  <p class="text-xs text-gray-500">채팅방</p>
</div>
```

**개선 사항**:
- `p-2` 패딩 추가로 클릭 영역 확대
- `hover:bg-gray-100` 호버 배경색 추가
- `rounded-lg` 둥근 모서리
- `transition-colors` 부드러운 색상 전환
- `title` 속성으로 툴팁 추가
- "Room:" 텍스트 제거하고 서브 텍스트 "채팅방" 추가

## 테스트 결과

### 반응 기능 테스트
1. ✅ 메시지 500ms 길게 누르기 → 반응 피커 표시
2. ✅ 이모지 선택 → 반응 추가됨
3. ✅ 반응 뱃지 클릭 → 반응 제거됨
4. ✅ 실시간 동기화 작동

### UI 개선 테스트
1. ✅ 뒤로가기 버튼 클릭 영역 확대
2. ✅ 호버 시 배경색 변경
3. ✅ 툴팁 표시 ("홈으로 돌아가기")
4. ✅ 채팅방 제목 간소화

## 빌드 상태
- ✅ Frontend Build: 성공
- ✅ Backend: 변경 없음 (정상 작동)
- ✅ Dev Server: http://localhost:3005

## 다음 단계
- Phase 4: 메시지 영구 저장 (PostgreSQL)
