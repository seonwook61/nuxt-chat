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

// Mock useSocket composable
const mockUseSocket = {
  connected: { value: true },
  connecting: { value: false },
  subscribe: vi.fn(),
  unsubscribe: vi.fn(),
  send: vi.fn(),
  connect: vi.fn(),
  disconnect: vi.fn()
}

vi.mock('~/composables/useSocket', () => ({
  useSocket: vi.fn(() => mockUseSocket)
}))

// Mock useChatRoom composable
const mockUseChatRoom = {
  messages: { value: [] as Message[] },
  onlineUsers: { value: 0 },
  isJoined: { value: false },
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
    mockUseChatRoom.isJoined.value = false
    mockUseSocket.connected.value = true
  })

  /**
   * Test 1: 기본 렌더링 테스트
   */
  it('should render ChatWindow component', () => {
    // Given: ChatWindow 컴포넌트
    wrapper = mount(ChatWindow, {
      props: {
        roomId: 'test-room'
      }
    })

    // When: 컴포넌트 마운트
    // Then: 기본 요소들이 렌더링되어야 함
    expect(wrapper.find('[data-testid="message-list"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="message-input"]').exists()).toBe(true)
  })

  /**
   * Test 2: 메시지 목록 표시 테스트
   */
  it('should display messages from useChatRoom', () => {
    // Given: 메시지가 있는 상태
    const testMessages: Message[] = [
      {
        messageId: 'msg-1',
        roomId: 'test-room',
        userId: 'user-1',
        username: 'User1',
        content: 'Hello',
        timestamp: new Date().toISOString(),
        type: 'CHAT'
      },
      {
        messageId: 'msg-2',
        roomId: 'test-room',
        userId: 'user-2',
        username: 'User2',
        content: 'Hi there',
        timestamp: new Date().toISOString(),
        type: 'CHAT'
      }
    ]

    mockUseChatRoom.messages.value = testMessages

    // When: ChatWindow 컴포넌트 마운트
    wrapper = mount(ChatWindow, {
      props: {
        roomId: 'test-room'
      }
    })

    // Then: MessageList에 메시지가 전달되어야 함
    const messageList = wrapper.findComponent({ name: 'MessageList' })
    expect(messageList.props('messages')).toEqual(testMessages)
  })

  /**
   * Test 3: 메시지 전송 이벤트 처리
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

    // Then: sendMessage가 호출되어야 함
    expect(mockUseChatRoom.sendMessage).toHaveBeenCalledWith(messageContent)
  })

  /**
   * Test 4: 방 입장 시 joinRoom 호출
   */
  it('should call joinRoom on mount when connected', () => {
    // Given: 연결된 상태
    mockUseSocket.connected.value = true

    // When: 컴포넌트 마운트
    wrapper = mount(ChatWindow, {
      props: {
        roomId: 'test-room'
      }
    })

    // Then: joinRoom이 호출되어야 함
    expect(mockUseChatRoom.joinRoom).toHaveBeenCalled()
  })

  /**
   * Test 5: 방 퇴장 시 leaveRoom 호출
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

    // Then: leaveRoom이 호출되어야 함
    expect(mockUseChatRoom.leaveRoom).toHaveBeenCalled()
  })

  /**
   * Test 6: 온라인 사용자 수 표시
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

    // Then: 온라인 사용자 수가 표시되어야 함
    expect(wrapper.text()).toContain('5')
    expect(wrapper.text()).toMatch(/online/i)
  })

  /**
   * Test 7: roomId 표시
   */
  it('should display room ID', () => {
    // Given: ChatWindow 컴포넌트
    const roomId = 'test-room-123'

    // When: 컴포넌트 마운트
    wrapper = mount(ChatWindow, {
      props: {
        roomId
      }
    })

    // Then: roomId가 표시되어야 함
    expect(wrapper.text()).toContain(roomId)
  })

  /**
   * Test 8: 연결 상태 표시
   */
  it('should show connecting message when not connected', () => {
    // Given: 연결되지 않은 상태
    mockUseSocket.connected.value = false

    // When: ChatWindow 컴포넌트 마운트
    wrapper = mount(ChatWindow, {
      props: {
        roomId: 'test-room'
      }
    })

    // Then: 연결 중 메시지가 표시되어야 함
    expect(wrapper.text()).toMatch(/connect/i)
  })

  /**
   * Test 9: 연결된 상태에서는 연결 메시지 미표시
   */
  it('should not show connecting message when connected', () => {
    // Given: 연결된 상태
    mockUseSocket.connected.value = true

    // When: ChatWindow 컴포넌트 마운트
    wrapper = mount(ChatWindow, {
      props: {
        roomId: 'test-room'
      }
    })

    // Then: 연결 중 메시지가 표시되지 않아야 함
    const connectingElement = wrapper.find('.bg-yellow-100')
    expect(connectingElement.exists()).toBe(false)
  })

  /**
   * Test 10: roomId prop 변경 시 재입장
   */
  it('should handle roomId change', async () => {
    // Given: 첫 번째 방에 입장한 상태
    mockUseSocket.connected.value = true
    wrapper = mount(ChatWindow, {
      props: {
        roomId: 'room-1'
      }
    })

    mockUseChatRoom.leaveRoom.mockClear()
    mockUseChatRoom.joinRoom.mockClear()

    // When: roomId prop 변경
    await wrapper.setProps({ roomId: 'room-2' })
    await wrapper.vm.$nextTick()

    // Then: leaveRoom과 joinRoom이 호출되어야 함
    expect(mockUseChatRoom.leaveRoom).toHaveBeenCalled()
    expect(mockUseChatRoom.joinRoom).toHaveBeenCalled()
  })

  /**
   * Test 11: Room info 헤더 렌더링
   */
  it('should render room info header', () => {
    // Given: ChatWindow 컴포넌트
    mockUseChatRoom.onlineUsers.value = 3

    // When: 컴포넌트 마운트
    wrapper = mount(ChatWindow, {
      props: {
        roomId: 'my-room'
      }
    })

    // Then: 방 정보 헤더가 렌더링되어야 함
    expect(wrapper.find('.bg-gray-50').exists()).toBe(true)
    expect(wrapper.text()).toContain('Room')
    expect(wrapper.text()).toContain('my-room')
  })
})
