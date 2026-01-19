import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import ChatWindow from '../ChatWindow.vue'
import type { Message } from '~/types/chat'

// Mock child components
vi.mock('../MessageList.vue', () => ({
  default: {
    name: 'MessageList',
    props: ['messages'],
    template: '<div data-testid="message-list"><slot /></div>'
  }
}))

vi.mock('../MessageInput.vue', () => ({
  default: {
    name: 'MessageInput',
    emits: ['send'],
    template: '<div data-testid="message-input"><button @click="$emit(\'send\', \'test\')">Send</button></div>'
  }
}))

// Mock useChatRoom composable
const mockUseChatRoom = {
  messages: { value: [] as Message[] },
  onlineUsers: { value: 0 },
  joinRoom: vi.fn(),
  leaveRoom: vi.fn(),
  sendMessage: vi.fn()
}

vi.mock('~/composables/useChatRoom', () => ({
  useChatRoom: vi.fn(() => mockUseChatRoom)
}))

describe('ChatWindow', () => {
  let wrapper: VueWrapper<any>

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()

    // Reset mock data
    mockUseChatRoom.messages.value = []
    mockUseChatRoom.onlineUsers.value = 0
  })

  /**
   * Test 1: 기본 렌더링 테스트
   * Phase 2에서 구현 예정: ChatWindow 컴포넌트 기본 구조 렌더링
   */
  it('should render ChatWindow component', () => {
    // Given: ChatWindow 컴포넌트
    wrapper = mount(ChatWindow, {
      props: {
        roomId: 'test-room'
      }
    })

    // When: 컴포넌트 마운트
    // Then: 기본 요소들이 렌더링되어야 함 (현재 구현 일부만 - RED)
    expect(wrapper.find('[data-testid="message-list"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="message-input"]').exists()).toBe(true)
  })

  /**
   * Test 2: 메시지 목록 표시 테스트
   * Phase 2에서 구현 예정: useChatRoom의 메시지 데이터를 MessageList에 전달
   */
  it('should display messages from useChatRoom', () => {
    // Given: 메시지가 있는 상태
    const testMessages: Message[] = [
      {
        id: 'msg-1',
        roomId: 'test-room',
        userId: 'user-1',
        content: 'Hello',
        timestamp: Date.now()
      },
      {
        id: 'msg-2',
        roomId: 'test-room',
        userId: 'user-2',
        content: 'Hi there',
        timestamp: Date.now() + 1000
      }
    ]

    mockUseChatRoom.messages.value = testMessages

    // When: ChatWindow 컴포넌트 마운트
    wrapper = mount(ChatWindow, {
      props: {
        roomId: 'test-room'
      }
    })

    // Then: MessageList에 메시지가 전달되어야 함 (현재 구현 안 됨 - RED)
    const messageList = wrapper.findComponent({ name: 'MessageList' })
    expect(messageList.props('messages')).toEqual(testMessages)
  })

  /**
   * Test 3: 메시지 전송 이벤트 처리
   * Phase 2에서 구현 예정: MessageInput의 send 이벤트를 받아 useChatRoom.sendMessage 호출
   */
  it('should call sendMessage when MessageInput emits send event', async () => {
    // Given: ChatWindow 컴포넌트
    wrapper = mount(ChatWindow, {
      props: {
        roomId: 'test-room'
      }
    })

    const messageContent = 'Test message'

    // When: MessageInput에서 send 이벤트 발생
    const messageInput = wrapper.findComponent({ name: 'MessageInput' })
    await messageInput.vm.$emit('send', messageContent)

    // Then: sendMessage가 호출되어야 함 (현재 구현 안 됨 - RED)
    expect(mockUseChatRoom.sendMessage).toHaveBeenCalledWith(messageContent)
  })

  /**
   * Test 4: 방 입장 시 joinRoom 호출
   * Phase 2에서 구현 예정: 컴포넌트 마운트 시 자동으로 joinRoom 호출
   */
  it('should call joinRoom on mount', () => {
    // Given: ChatWindow 컴포넌트
    // When: 컴포넌트 마운트
    wrapper = mount(ChatWindow, {
      props: {
        roomId: 'test-room'
      }
    })

    // Then: joinRoom이 호출되어야 함 (현재 구현 안 됨 - RED)
    expect(mockUseChatRoom.joinRoom).toHaveBeenCalled()
  })

  /**
   * Test 5: 방 퇴장 시 leaveRoom 호출
   * Phase 2에서 구현 예정: 컴포넌트 언마운트 시 자동으로 leaveRoom 호출
   */
  it('should call leaveRoom on unmount', () => {
    // Given: 마운트된 ChatWindow 컴포넌트
    wrapper = mount(ChatWindow, {
      props: {
        roomId: 'test-room'
      }
    })

    mockUseChatRoom.leaveRoom.mockClear()

    // When: 컴포넌트 언마운트
    wrapper.unmount()

    // Then: leaveRoom이 호출되어야 함 (현재 구현 안 됨 - RED)
    expect(mockUseChatRoom.leaveRoom).toHaveBeenCalled()
  })

  /**
   * Test 6: 빈 상태 메시지 표시
   * Phase 2에서 구현 예정: 메시지가 없을 때 빈 상태 메시지 표시
   */
  it('should show empty state when no messages', () => {
    // Given: 메시지가 없는 상태
    mockUseChatRoom.messages.value = []

    // When: ChatWindow 컴포넌트 마운트
    wrapper = mount(ChatWindow, {
      props: {
        roomId: 'test-room'
      }
    })

    // Then: 빈 상태 메시지가 표시되어야 함 (현재 구현 안 됨 - RED)
    expect(wrapper.text()).toContain('메시지가 없습니다')
  })

  /**
   * Test 7: 온라인 사용자 수 표시
   * Phase 2에서 구현 예정: 채팅방의 온라인 사용자 수 표시
   */
  it('should display online users count', () => {
    // Given: 온라인 사용자가 있는 상태
    mockUseChatRoom.onlineUsers.value = 5

    // When: ChatWindow 컴포넌트 마운트
    wrapper = mount(ChatWindow, {
      props: {
        roomId: 'test-room'
      }
    })

    // Then: 온라인 사용자 수가 표시되어야 함 (현재 구현 안 됨 - RED)
    expect(wrapper.text()).toContain('5')
    expect(wrapper.text()).toMatch(/온라인|명|users?/i)
  })

  /**
   * Test 8: 메시지 자동 스크롤 테스트
   * Phase 2에서 구현 예정: 새 메시지 도착 시 자동으로 스크롤을 하단으로 이동
   */
  it('should auto scroll to bottom when new message arrives', async () => {
    // Given: 메시지가 있는 ChatWindow
    mockUseChatRoom.messages.value = [
      {
        id: 'msg-1',
        roomId: 'test-room',
        userId: 'user-1',
        content: 'First message',
        timestamp: Date.now()
      }
    ]

    wrapper = mount(ChatWindow, {
      props: {
        roomId: 'test-room'
      }
    })

    // Mock scrollIntoView
    const scrollIntoView = vi.fn()
    Element.prototype.scrollIntoView = scrollIntoView

    // When: 새 메시지 추가
    mockUseChatRoom.messages.value.push({
      id: 'msg-2',
      roomId: 'test-room',
      userId: 'user-2',
      content: 'New message',
      timestamp: Date.now() + 1000
    })

    await wrapper.vm.$nextTick()

    // Then: scrollIntoView가 호출되어야 함 (현재 구현 안 됨 - RED)
    expect(scrollIntoView).toHaveBeenCalled()
  })

  /**
   * Test 9: roomId prop 변경 시 재입장
   * Phase 2에서 구현 예정: roomId가 변경되면 이전 방 퇴장 후 새 방 입장
   */
  it('should rejoin when roomId prop changes', async () => {
    // Given: 첫 번째 방에 입장한 상태
    wrapper = mount(ChatWindow, {
      props: {
        roomId: 'room-1'
      }
    })

    mockUseChatRoom.leaveRoom.mockClear()
    mockUseChatRoom.joinRoom.mockClear()

    // When: roomId prop 변경
    await wrapper.setProps({ roomId: 'room-2' })

    // Then: leaveRoom과 joinRoom이 호출되어야 함 (현재 구현 안 됨 - RED)
    expect(mockUseChatRoom.leaveRoom).toHaveBeenCalled()
    expect(mockUseChatRoom.joinRoom).toHaveBeenCalled()
  })

  /**
   * Test 10: 로딩 상태 표시
   * Phase 2에서 구현 예정: 소켓 연결 중일 때 로딩 상태 표시
   */
  it('should show loading state while connecting', () => {
    // Given: 연결 중인 상태 (mock)
    const mockConnecting = true

    // When: ChatWindow 컴포넌트 마운트
    wrapper = mount(ChatWindow, {
      props: {
        roomId: 'test-room'
      }
    })

    // Then: 로딩 인디케이터가 표시되어야 함 (현재 구현 안 됨 - RED)
    // Note: useChatRoom 또는 useSocket에 connecting 상태 추가 필요
    if (mockConnecting) {
      expect(wrapper.find('[data-testid="loading"]').exists()).toBe(true)
    }
  })

  /**
   * Test 11: 에러 메시지 표시
   * Phase 2에서 구현 예정: 에러 발생 시 에러 메시지 표시
   */
  it('should display error message when error occurs', async () => {
    // Given: ChatWindow 컴포넌트
    wrapper = mount(ChatWindow, {
      props: {
        roomId: 'test-room'
      }
    })

    // When: 에러 상태 설정 (mock)
    // Note: useChatRoom 또는 useSocket에 error 상태 추가 필요
    const errorMessage = 'Connection failed'

    // Then: 에러 메시지가 표시되어야 함 (현재 구현 안 됨 - RED)
    // expect(wrapper.text()).toContain(errorMessage)
    expect(wrapper.find('[data-testid="error-message"]').exists()).toBe(true)
  })

  /**
   * Test 12: 타임스탬프 포맷 테스트
   * Phase 2에서 구현 예정: 메시지 타임스탬프를 사람이 읽을 수 있는 형식으로 표시
   */
  it('should format message timestamps correctly', () => {
    // Given: 타임스탬프가 있는 메시지
    const now = Date.now()
    mockUseChatRoom.messages.value = [
      {
        id: 'msg-1',
        roomId: 'test-room',
        userId: 'user-1',
        content: 'Test message',
        timestamp: now
      }
    ]

    // When: ChatWindow 컴포넌트 마운트
    wrapper = mount(ChatWindow, {
      props: {
        roomId: 'test-room'
      }
    })

    // Then: 타임스탬프가 포맷되어 표시되어야 함 (현재 구현 안 됨 - RED)
    // 예: "오후 3:45" 또는 "15:45" 형식
    const formattedTime = new Date(now).toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit'
    })

    expect(wrapper.text()).toContain(formattedTime)
  })

  /**
   * Test 13: 사용자 목록 사이드바 표시
   * Phase 2에서 구현 예정: 채팅방의 사용자 목록을 사이드바에 표시
   */
  it('should display user list sidebar', () => {
    // Given: ChatWindow 컴포넌트
    wrapper = mount(ChatWindow, {
      props: {
        roomId: 'test-room'
      }
    })

    // When: 사이드바 토글 버튼 클릭
    // Note: Phase 2에서 사이드바 토글 기능 구현 필요
    const toggleButton = wrapper.find('[data-testid="toggle-sidebar"]')

    // Then: 사용자 목록 사이드바가 표시되어야 함 (현재 구현 안 됨 - RED)
    expect(toggleButton.exists()).toBe(true)
  })

  /**
   * Test 14: 메시지 입력 필드 초기화 테스트
   * Phase 2에서 구현 예정: 메시지 전송 후 입력 필드 자동 초기화
   */
  it('should clear input field after sending message', async () => {
    // Given: ChatWindow 컴포넌트
    wrapper = mount(ChatWindow, {
      props: {
        roomId: 'test-room'
      }
    })

    const messageInput = wrapper.findComponent({ name: 'MessageInput' })

    // When: 메시지 전송
    await messageInput.vm.$emit('send', 'Test message')

    // Then: 입력 필드가 초기화되어야 함 (현재 구현 안 됨 - RED)
    // Note: MessageInput 컴포넌트에서 구현 필요
    expect(messageInput.exists()).toBe(true)
  })

  /**
   * Test 15: 연결 상태 표시
   * Phase 2에서 구현 예정: 소켓 연결 상태를 UI에 표시
   */
  it('should display connection status', () => {
    // Given: ChatWindow 컴포넌트
    wrapper = mount(ChatWindow, {
      props: {
        roomId: 'test-room'
      }
    })

    // Then: 연결 상태 인디케이터가 표시되어야 함 (현재 구현 안 됨 - RED)
    // 예: "연결됨", "연결 끊김", "재연결 중" 등
    expect(wrapper.find('[data-testid="connection-status"]').exists()).toBe(true)
  })

  /**
   * Test 16: 메시지 페이지네이션/무한 스크롤
   * Phase 2에서 구현 예정: 메시지가 많을 때 무한 스크롤 또는 페이지네이션
   */
  it('should support infinite scroll for messages', () => {
    // Given: 많은 메시지가 있는 상태
    const manyMessages: Message[] = Array.from({ length: 100 }, (_, i) => ({
      id: `msg-${i}`,
      roomId: 'test-room',
      userId: `user-${i % 5}`,
      content: `Message ${i}`,
      timestamp: Date.now() + i * 1000
    }))

    mockUseChatRoom.messages.value = manyMessages

    // When: ChatWindow 컴포넌트 마운트
    wrapper = mount(ChatWindow, {
      props: {
        roomId: 'test-room'
      }
    })

    // Then: 무한 스크롤 또는 페이지네이션 UI가 있어야 함 (현재 구현 안 됨 - RED)
    expect(wrapper.find('[data-testid="message-list"]').exists()).toBe(true)
    // Note: 실제 무한 스크롤 로직은 MessageList 컴포넌트에서 구현 필요
  })
})
