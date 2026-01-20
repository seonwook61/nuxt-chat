import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useChatRoom } from '../useChatRoom'
import { useChatStore } from '~/stores/chat'
import type { Message, ChatEvent } from '~/types/chat'

// Mock useSocket composable
const mockSubscribe = vi.fn()
const mockUnsubscribe = vi.fn()
const mockSend = vi.fn()
const mockConnect = vi.fn()
const mockDisconnect = vi.fn()

let subscribedCallback: ((payload: any) => void) | null = null

// Mock before importing useChatRoom
const mockUseSocket = vi.fn(() => ({
  connected: { value: true },
  connecting: { value: false },
  subscribe: (destination: string, callback: (payload: any) => void) => {
    subscribedCallback = callback
    return mockSubscribe(destination, callback)
  },
  unsubscribe: mockUnsubscribe,
  send: mockSend,
  connect: mockConnect,
  disconnect: mockDisconnect
}))

vi.mock('~/composables/useSocket', () => ({
  useSocket: mockUseSocket
}))

describe('useChatRoom', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    subscribedCallback = null
    mockSubscribe.mockReturnValue('sub-123')
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  /**
   * Test 1: 방 입장 테스트
   * STOMP subscribe + send /app/chat.join
   */
  it('should join room and subscribe to topic', async () => {
    // Given: useChatRoom composable
    const roomId = 'test-room-1'
    const { joinRoom } = useChatRoom(roomId)
    const chatStore = useChatStore()

    // When: joinRoom() 호출
    await joinRoom()

    // Then: topic 구독 및 join 메시지 전송
    expect(mockSubscribe).toHaveBeenCalledWith(
      `/topic/room/${roomId}`,
      expect.any(Function)
    )
    expect(mockSend).toHaveBeenCalledWith('/app/chat.join', {
      roomId,
      userId: expect.any(String),
      username: expect.any(String)
    })
    expect(chatStore.currentRoomId).toBe(roomId)
  })

  /**
   * Test 2: 메시지 전송 테스트
   * STOMP send /app/chat.send
   */
  it('should send message via STOMP', async () => {
    // Given: 입장한 방
    const roomId = 'test-room-1'
    const { joinRoom, sendMessage } = useChatRoom(roomId)
    await joinRoom()

    const messageContent = 'Hello, World!'

    // When: sendMessage() 호출
    sendMessage(messageContent)

    // Then: /app/chat.send로 메시지 전송
    expect(mockSend).toHaveBeenCalledWith('/app/chat.send', {
      roomId,
      userId: expect.any(String),
      username: expect.any(String),
      content: messageContent
    })
  })

  /**
   * Test 3: 메시지 수신 테스트
   * STOMP topic에서 ChatMessage 수신
   */
  it('should receive ChatMessage and update messages', async () => {
    // Given: 입장한 방
    const roomId = 'test-room-1'
    const { joinRoom, messages } = useChatRoom(roomId)
    await joinRoom()

    const receivedMessage: Message = {
      messageId: 'msg-1',
      roomId,
      userId: 'user-2',
      username: 'TestUser',
      content: 'Hi there!',
      timestamp: new Date().toISOString(),
      type: 'CHAT'
    }

    // When: 구독 콜백을 통해 메시지 수신
    subscribedCallback?.(receivedMessage)

    // Then: 메시지가 로컬 상태에 추가되어야 함
    expect(messages.value).toHaveLength(1)
    expect(messages.value[0]).toEqual(receivedMessage)
  })

  /**
   * Test 4: 사용자 입장 이벤트 처리
   * ChatEvent USER_JOINED 수신 시 온라인 사용자 수 증가
   */
  it('should handle USER_JOINED event', async () => {
    // Given: 입장한 방
    const roomId = 'test-room-1'
    const { joinRoom, onlineUsers } = useChatRoom(roomId)
    await joinRoom()

    const initialUserCount = onlineUsers.value

    const userJoinedEvent: ChatEvent = {
      eventId: 'evt-1',
      roomId,
      userId: 'user-2',
      username: 'NewUser',
      eventType: 'USER_JOINED',
      timestamp: new Date().toISOString()
    }

    // When: USER_JOINED 이벤트 수신
    subscribedCallback?.(userJoinedEvent)

    // Then: 온라인 사용자 수가 증가해야 함
    expect(onlineUsers.value).toBe(initialUserCount + 1)
  })

  /**
   * Test 5: 방 퇴장 테스트
   * STOMP unsubscribe + send /app/chat.leave
   */
  it('should leave room and clean up', async () => {
    // Given: 입장한 방
    const roomId = 'test-room-1'
    const { joinRoom, leaveRoom, messages } = useChatRoom(roomId)

    await joinRoom()

    // When: leaveRoom() 호출
    leaveRoom()

    // Then: 구독 해제 및 leave 메시지 전송
    expect(mockUnsubscribe).toHaveBeenCalledWith('sub-123')
    expect(mockSend).toHaveBeenCalledWith('/app/chat.leave', {
      roomId,
      userId: expect.any(String)
    })
    expect(messages.value).toHaveLength(0)
  })

  /**
   * Test 6: 중복 메시지 방지 테스트
   * 동일한 messageId의 메시지는 중복 추가되지 않음
   */
  it('should prevent duplicate messages', async () => {
    // Given: 입장한 방
    const roomId = 'test-room-1'
    const { joinRoom, messages } = useChatRoom(roomId)
    await joinRoom()

    const message: Message = {
      messageId: 'msg-1',
      roomId,
      userId: 'user-1',
      username: 'TestUser',
      content: 'Test',
      timestamp: new Date().toISOString(),
      type: 'CHAT'
    }

    // When: 동일한 메시지를 두 번 수신
    subscribedCallback?.(message)
    subscribedCallback?.(message)

    // Then: 메시지가 한 번만 추가되어야 함
    expect(messages.value).toHaveLength(1)
  })

  /**
   * Test 7: 빈 메시지 전송 방지 테스트
   * 빈 문자열 또는 공백만 있는 메시지 전송 차단
   */
  it('should not send empty or whitespace-only messages', async () => {
    // Given: 입장한 방
    const roomId = 'test-room-1'
    const { joinRoom, sendMessage } = useChatRoom(roomId)
    await joinRoom()

    mockSend.mockClear()

    // When: 빈 메시지 전송 시도
    sendMessage('')
    sendMessage('   ')
    sendMessage('\n\t')

    // Then: send 메서드가 호출되지 않아야 함
    expect(mockSend).not.toHaveBeenCalledWith(
      '/app/chat.send',
      expect.anything()
    )
  })

  /**
   * Test 8: 메시지 길이 제한 테스트
   * 1000자 이상 메시지 전송 방지
   */
  it('should enforce message length limit', async () => {
    // Given: 입장한 방
    const roomId = 'test-room-1'
    const { joinRoom, sendMessage } = useChatRoom(roomId)
    await joinRoom()

    mockSend.mockClear()

    const longMessage = 'a'.repeat(1001)

    // When: 1000자 초과 메시지 전송 시도
    sendMessage(longMessage)

    // Then: send 메서드가 호출되지 않아야 함
    expect(mockSend).not.toHaveBeenCalledWith('/app/chat.send', {
      roomId,
      userId: expect.any(String),
      username: expect.any(String),
      content: longMessage
    })
  })

  /**
   * Test 9: 사용자 퇴장 이벤트 처리
   * ChatEvent USER_LEFT 수신 시 온라인 사용자 수 감소
   */
  it('should handle USER_LEFT event', async () => {
    // Given: 입장한 방 (온라인 사용자 2명)
    const roomId = 'test-room-1'
    const { joinRoom, onlineUsers } = useChatRoom(roomId)
    await joinRoom()

    // Simulate initial users
    subscribedCallback?.({
      eventId: 'evt-1',
      roomId,
      userId: 'user-2',
      username: 'User2',
      eventType: 'USER_JOINED',
      timestamp: new Date().toISOString()
    })

    const userCount = onlineUsers.value

    const userLeftEvent: ChatEvent = {
      eventId: 'evt-2',
      roomId,
      userId: 'user-2',
      username: 'User2',
      eventType: 'USER_LEFT',
      timestamp: new Date().toISOString()
    }

    // When: USER_LEFT 이벤트 수신
    subscribedCallback?.(userLeftEvent)

    // Then: 온라인 사용자 수가 감소해야 함
    expect(onlineUsers.value).toBe(userCount - 1)
  })

  /**
   * Test 10: 연결되지 않은 상태에서 메시지 전송 방지
   */
  it('should not send message when not connected', async () => {
    // Given: 연결되지 않은 상태로 mock 재정의
    mockUseSocket.mockReturnValueOnce({
      connected: { value: false },
      connecting: { value: false },
      subscribe: mockSubscribe,
      unsubscribe: mockUnsubscribe,
      send: mockSend,
      connect: mockConnect,
      disconnect: mockDisconnect
    })

    const roomId = 'test-room-1'
    const { sendMessage } = useChatRoom(roomId)

    mockSend.mockClear()

    // When: 메시지 전송 시도
    sendMessage('Hello')

    // Then: send 메서드가 호출되지 않아야 함
    expect(mockSend).not.toHaveBeenCalled()
  })

  /**
   * Test 11: 방 입장하지 않은 상태에서 메시지 전송 방지
   */
  it('should not send message when not joined', () => {
    // Given: 방에 입장하지 않은 상태
    const roomId = 'test-room-1'
    const { sendMessage } = useChatRoom(roomId)

    mockSend.mockClear()

    // When: 메시지 전송 시도
    sendMessage('Hello')

    // Then: send 메서드가 호출되지 않아야 함
    expect(mockSend).not.toHaveBeenCalled()
  })

  /**
   * Test 12: Ref 형태의 roomId 처리
   */
  it('should handle reactive roomId', async () => {
    // Given: ref로 전달된 roomId
    const roomIdRef = ref('room-1')
    const { joinRoom } = useChatRoom(roomIdRef)

    // When: joinRoom() 호출
    await joinRoom()

    // Then: 현재 roomId 값으로 구독 및 전송
    expect(mockSubscribe).toHaveBeenCalledWith(
      '/topic/room/room-1',
      expect.any(Function)
    )
  })

  /**
   * Test 13: 메시지 trim 처리
   */
  it('should trim message content before sending', async () => {
    // Given: 입장한 방
    const roomId = 'test-room-1'
    const { joinRoom, sendMessage } = useChatRoom(roomId)
    await joinRoom()

    mockSend.mockClear()

    // When: 앞뒤 공백이 있는 메시지 전송
    sendMessage('  Hello World  ')

    // Then: trim된 메시지가 전송되어야 함
    expect(mockSend).toHaveBeenCalledWith('/app/chat.send', {
      roomId,
      userId: expect.any(String),
      username: expect.any(String),
      content: 'Hello World'
    })
  })
})
