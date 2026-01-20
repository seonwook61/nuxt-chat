import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { useSocket, __resetStompClient } from '../useSocket'
import type { Client, StompSubscription } from '@stomp/stompjs'

// Mock @stomp/stompjs
const mockSubscribe = vi.fn()
const mockPublish = vi.fn()
const mockActivate = vi.fn()
const mockDeactivate = vi.fn()

let mockOnConnect: (() => void) | null = null
let mockOnDisconnect: (() => void) | null = null

vi.mock('@stomp/stompjs', () => ({
  Client: vi.fn().mockImplementation((config) => {
    mockOnConnect = config.onConnect
    mockOnDisconnect = config.onDisconnect
    return {
      activate: mockActivate,
      deactivate: mockDeactivate,
      subscribe: mockSubscribe,
      publish: mockPublish,
      connected: false
    }
  })
}))

vi.mock('sockjs-client', () => ({
  default: vi.fn()
}))

describe('useSocket', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockOnConnect = null
    mockOnDisconnect = null
    __resetStompClient()
  })

  afterEach(() => {
    vi.restoreAllMocks()
    __resetStompClient()
  })

  /**
   * Test 1: Socket 연결 테스트
   * STOMP client activate() 호출 및 연결 상태 관리
   */
  it('should connect to server', () => {
    // Given: useSocket composable
    const { connect, connected } = useSocket()

    // When: connect() 메서드 호출 및 onConnect 콜백 실행
    connect()
    mockOnConnect?.()

    // Then: connected 상태가 true로 변경되어야 함
    expect(mockActivate).toHaveBeenCalled()
    expect(connected.value).toBe(true)
  })

  /**
   * Test 2: Socket 연결 해제 테스트
   * STOMP client deactivate() 호출 및 상태 업데이트
   */
  it('should disconnect from server', () => {
    // Given: 연결된 소켓
    const { connect, disconnect, connected } = useSocket()
    connect()
    mockOnConnect?.()

    // When: disconnect() 호출
    disconnect()

    // Then: connected 상태가 false로 변경되어야 함
    expect(mockDeactivate).toHaveBeenCalled()
    expect(connected.value).toBe(false)
  })

  /**
   * Test 3: 구독(subscribe) 테스트
   * STOMP client subscribe() 메서드로 토픽 구독
   */
  it('should subscribe to topic', () => {
    // Given: 연결된 소켓
    const { connect, subscribe } = useSocket()
    connect()
    mockOnConnect?.()

    const mockCallback = vi.fn()
    const mockSubscription: StompSubscription = {
      id: 'sub-1',
      unsubscribe: vi.fn()
    }
    mockSubscribe.mockReturnValue(mockSubscription)

    // When: subscribe() 메서드로 토픽 구독
    const subId = subscribe('/topic/test', mockCallback)

    // Then: client.subscribe가 호출되어야 함
    expect(mockSubscribe).toHaveBeenCalledWith('/topic/test', expect.any(Function))
    expect(subId).toBeDefined()
  })

  /**
   * Test 4: 메시지 발송 테스트
   * STOMP client publish() 메서드로 서버에 메시지 전송
   */
  it('should send message to server', () => {
    // Given: 연결된 소켓
    const { connect, send } = useSocket()
    connect()
    mockOnConnect?.()

    const testData = { roomId: 'room1', content: 'Hello' }

    // When: send() 메서드로 메시지 발송
    send('/app/chat.send', testData)

    // Then: client.publish가 올바른 인자로 호출되어야 함
    expect(mockPublish).toHaveBeenCalledWith({
      destination: '/app/chat.send',
      body: JSON.stringify(testData)
    })
  })

  /**
   * Test 5: 구독 해제 테스트
   * STOMP subscription unsubscribe() 호출
   */
  it('should unsubscribe from topic', () => {
    // Given: 구독된 토픽
    const { connect, subscribe, unsubscribe } = useSocket()
    connect()
    mockOnConnect?.()

    const mockUnsubscribe = vi.fn()
    const mockSubscription: StompSubscription = {
      id: 'sub-1',
      unsubscribe: mockUnsubscribe
    }
    mockSubscribe.mockReturnValue(mockSubscription)

    const subId = subscribe('/topic/test', vi.fn())

    // When: unsubscribe() 호출
    unsubscribe(subId)

    // Then: subscription.unsubscribe가 호출되어야 함
    expect(mockUnsubscribe).toHaveBeenCalled()
  })

})
