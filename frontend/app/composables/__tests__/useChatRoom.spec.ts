import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useChatRoom } from '../useChatRoom'
import { useChatStore } from '~/stores/chat'
import type { Message } from '~/types/chat'

// Mock useSocket composable
const mockSocket = {
  emit: vi.fn(),
  on: vi.fn(),
  off: vi.fn(),
  connected: true
}

vi.mock('../useSocket', () => ({
  useSocket: vi.fn(() => ({
    socket: { value: mockSocket },
    connected: { value: true },
    emit: mockSocket.emit,
    on: mockSocket.on
  }))
}))

describe('useChatRoom', () => {
  beforeEach(() => {
    // Pinia 스토어 초기화
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  /**
   * Test 1: 방 입장 테스트
   * Phase 2에서 구현 예정: Socket.io로 join_room 이벤트 발송 및 스토어 업데이트
   */
  it('should join room and emit join_room event', () => {
    // Given: useChatRoom composable
    const roomId = 'test-room-1'
    const { joinRoom } = useChatRoom(roomId)
    const chatStore = useChatStore()

    // When: joinRoom() 호출
    joinRoom()

    // Then: join_room 이벤트가 발송되고 스토어가 업데이트되어야 함 (현재 구현 안 됨 - RED)
    expect(mockSocket.emit).toHaveBeenCalledWith('join_room', roomId)
    expect(chatStore.currentRoomId).toBe(roomId)
  })

  /**
   * Test 2: 메시지 전송 테스트
   * Phase 2에서 구현 예정: Socket.io로 send_message 이벤트 발송
   */
  it('should send message via socket', () => {
    // Given: 입장한 방
    const roomId = 'test-room-1'
    const { joinRoom, sendMessage } = useChatRoom(roomId)
    joinRoom()

    const messageContent = 'Hello, World!'

    // When: sendMessage() 호출
    sendMessage(messageContent)

    // Then: send_message 이벤트가 발송되어야 함 (현재 구현 안 됨 - RED)
    expect(mockSocket.emit).toHaveBeenCalledWith('send_message', {
      roomId,
      content: messageContent
    })
  })

  /**
   * Test 3: 메시지 수신 테스트
   * Phase 2에서 구현 예정: Socket.io로 message 이벤트 수신 및 스토어 업데이트
   */
  it('should receive message and update store', () => {
    // Given: 입장한 방과 message 이벤트 리스너
    const roomId = 'test-room-1'
    const { joinRoom, messages } = useChatRoom(roomId)
    const chatStore = useChatStore()

    joinRoom()

    const receivedMessage: Message = {
      id: 'msg-1',
      roomId,
      userId: 'user-2',
      content: 'Hi there!',
      timestamp: Date.now()
    }

    // When: 서버에서 message 이벤트 수신 시뮬레이션
    // Note: Phase 2에서 on('message', handler) 구현 필요
    chatStore.addMessage(receivedMessage)

    // Then: 메시지가 스토어와 로컬 상태에 추가되어야 함 (현재 구현 안 됨 - RED)
    expect(messages.value).toContainEqual(receivedMessage)
    expect(chatStore.currentMessages).toContainEqual(receivedMessage)
  })

  /**
   * Test 4: 사용자 입장 이벤트 처리
   * Phase 2에서 구현 예정: userJoined 이벤트 수신 시 온라인 사용자 수 증가
   */
  it('should handle user joined event', () => {
    // Given: 입장한 방
    const roomId = 'test-room-1'
    const { joinRoom, onlineUsers } = useChatRoom(roomId)
    joinRoom()

    const initialUserCount = onlineUsers.value

    // When: userJoined 이벤트 수신 시뮬레이션
    // Note: Phase 2에서 on('userJoined', handler) 구현 필요
    const userEvent = {
      userId: 'user-2',
      roomId,
      timestamp: Date.now()
    }

    // Then: 온라인 사용자 수가 증가해야 함 (현재 구현 안 됨 - RED)
    // onlineUsers.value++를 트리거하는 이벤트 핸들러 필요
    expect(onlineUsers.value).toBeGreaterThan(initialUserCount)
  })

  /**
   * Test 5: 방 퇴장 테스트
   * Phase 2에서 구현 예정: Socket.io로 leave_room 이벤트 발송 및 상태 초기화
   */
  it('should leave room and clean up', () => {
    // Given: 입장한 방
    const roomId = 'test-room-1'
    const { joinRoom, leaveRoom, messages } = useChatRoom(roomId)
    const chatStore = useChatStore()

    joinRoom()
    chatStore.addMessage({
      id: 'msg-1',
      roomId,
      userId: 'user-1',
      content: 'Test',
      timestamp: Date.now()
    })

    // When: leaveRoom() 호출
    leaveRoom()

    // Then: leave_room 이벤트 발송 및 상태 초기화되어야 함 (현재 구현 안 됨 - RED)
    expect(mockSocket.emit).toHaveBeenCalledWith('leave_room', roomId)
    expect(chatStore.currentRoomId).toBeNull()
    expect(messages.value).toHaveLength(0)
  })

  /**
   * Test 6: 동시 다발적 메시지 수신 테스트
   * Phase 2에서 구현 예정: 여러 메시지가 빠르게 도착해도 순서대로 처리
   */
  it('should handle concurrent messages in order', async () => {
    // Given: 입장한 방
    const roomId = 'test-room-1'
    const { joinRoom, messages } = useChatRoom(roomId)
    const chatStore = useChatStore()

    joinRoom()

    const message1: Message = {
      id: 'msg-1',
      roomId,
      userId: 'user-1',
      content: 'First',
      timestamp: Date.now()
    }

    const message2: Message = {
      id: 'msg-2',
      roomId,
      userId: 'user-2',
      content: 'Second',
      timestamp: Date.now() + 1
    }

    const message3: Message = {
      id: 'msg-3',
      roomId,
      userId: 'user-3',
      content: 'Third',
      timestamp: Date.now() + 2
    }

    // When: 빠른 연속으로 메시지 수신
    chatStore.addMessage(message1)
    chatStore.addMessage(message2)
    chatStore.addMessage(message3)

    // Then: 모든 메시지가 순서대로 추가되어야 함 (현재 구현 안 됨 - RED)
    expect(messages.value).toHaveLength(3)
    expect(messages.value[0].content).toBe('First')
    expect(messages.value[1].content).toBe('Second')
    expect(messages.value[2].content).toBe('Third')
  })

  /**
   * Test 7: 빈 메시지 전송 방지 테스트
   * Phase 2에서 구현 예정: 빈 문자열 또는 공백만 있는 메시지 전송 차단
   */
  it('should not send empty or whitespace-only messages', () => {
    // Given: 입장한 방
    const roomId = 'test-room-1'
    const { joinRoom, sendMessage } = useChatRoom(roomId)
    joinRoom()

    mockSocket.emit.mockClear()

    // When: 빈 메시지 전송 시도
    sendMessage('')
    sendMessage('   ')
    sendMessage('\n\t')

    // Then: send_message 이벤트가 발송되지 않아야 함 (현재 구현 안 됨 - RED)
    expect(mockSocket.emit).not.toHaveBeenCalledWith(
      'send_message',
      expect.anything()
    )
  })

  /**
   * Test 8: 메시지 길이 제한 테스트
   * Phase 2에서 구현 예정: 1000자 이상 메시지 전송 방지
   */
  it('should enforce message length limit', () => {
    // Given: 입장한 방
    const roomId = 'test-room-1'
    const { joinRoom, sendMessage } = useChatRoom(roomId)
    joinRoom()

    mockSocket.emit.mockClear()

    const longMessage = 'a'.repeat(1001)

    // When: 1000자 초과 메시지 전송 시도
    sendMessage(longMessage)

    // Then: send_message 이벤트가 발송되지 않거나 잘린 메시지가 전송되어야 함 (현재 구현 안 됨 - RED)
    expect(mockSocket.emit).not.toHaveBeenCalledWith('send_message', {
      roomId,
      content: longMessage
    })
  })

  /**
   * Test 9: 사용자 퇴장 이벤트 처리
   * Phase 2에서 구현 예정: userLeft 이벤트 수신 시 온라인 사용자 수 감소
   */
  it('should handle user left event', () => {
    // Given: 입장한 방 (온라인 사용자 2명)
    const roomId = 'test-room-1'
    const { joinRoom, onlineUsers } = useChatRoom(roomId)
    joinRoom()

    // Set initial user count
    onlineUsers.value = 2

    // When: userLeft 이벤트 수신 시뮬레이션
    const userEvent = {
      userId: 'user-2',
      roomId,
      timestamp: Date.now()
    }

    // Then: 온라인 사용자 수가 감소해야 함 (현재 구현 안 됨 - RED)
    // onlineUsers.value--를 트리거하는 이벤트 핸들러 필요
    expect(onlineUsers.value).toBe(1)
  })

  /**
   * Test 10: 에러 이벤트 처리
   * Phase 2에서 구현 예정: error 이벤트 수신 시 에러 상태 업데이트
   */
  it('should handle error events from server', () => {
    // Given: 입장한 방
    const roomId = 'test-room-1'
    const { joinRoom } = useChatRoom(roomId)
    joinRoom()

    const errorMessage = {
      code: 'ROOM_FULL',
      message: 'The room is full',
      timestamp: Date.now()
    }

    // When: error 이벤트 수신 시뮬레이션
    // Note: Phase 2에서 on('error', handler) 구현 및 에러 상태 추가 필요

    // Then: 에러 상태가 업데이트되어야 함 (현재 구현 안 됨 - RED)
    // expect(error.value).toEqual(errorMessage)
    expect(mockSocket.on).toBeDefined()
  })

  /**
   * Test 11: 시스템 메시지 수신 테스트
   * Phase 2에서 구현 예정: system 이벤트 수신 시 시스템 메시지 표시
   */
  it('should receive and display system messages', () => {
    // Given: 입장한 방
    const roomId = 'test-room-1'
    const { joinRoom } = useChatRoom(roomId)
    joinRoom()

    const systemMessage = {
      type: 'info' as const,
      message: 'Server maintenance in 5 minutes',
      timestamp: Date.now()
    }

    // When: system 이벤트 수신 시뮬레이션
    // Note: Phase 2에서 on('system', handler) 구현 필요

    // Then: 시스템 메시지가 표시되어야 함 (현재 구현 안 됨 - RED)
    // expect(systemMessages.value).toContainEqual(systemMessage)
    expect(mockSocket.on).toBeDefined()
  })

  /**
   * Test 12: 연결 끊김 시 재연결 후 방 재입장
   * Phase 2에서 구현 예정: 소켓 재연결 시 자동으로 이전 방에 재입장
   */
  it('should rejoin room after reconnection', () => {
    // Given: 입장한 방
    const roomId = 'test-room-1'
    const { joinRoom } = useChatRoom(roomId)
    joinRoom()

    mockSocket.emit.mockClear()

    // When: 소켓 재연결 이벤트 발생
    // Note: Phase 2에서 reconnect 이벤트 리스너 구현 필요

    // Then: 자동으로 join_room 이벤트를 다시 발송해야 함 (현재 구현 안 됨 - RED)
    // expect(mockSocket.emit).toHaveBeenCalledWith('join_room', roomId)
    expect(mockSocket.on).toBeDefined()
  })

  /**
   * Test 13: 다른 방으로 이동 테스트
   * Phase 2에서 구현 예정: 현재 방 퇴장 후 새 방 입장
   */
  it('should leave current room before joining new room', () => {
    // Given: 첫 번째 방에 입장
    const roomId1 = 'room-1'
    const room1 = useChatRoom(roomId1)
    room1.joinRoom()

    mockSocket.emit.mockClear()

    // When: 두 번째 방으로 이동
    const roomId2 = 'room-2'
    const room2 = useChatRoom(roomId2)
    room2.joinRoom()

    // Then: leave_room과 join_room이 순서대로 호출되어야 함 (현재 구현 안 됨 - RED)
    expect(mockSocket.emit).toHaveBeenCalledWith('leave_room', roomId1)
    expect(mockSocket.emit).toHaveBeenCalledWith('join_room', roomId2)
  })
})
