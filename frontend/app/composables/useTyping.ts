import type { TypingIndicator } from '~/types/chat'

/**
 * Typing Indicator Composable (Phase 5)
 *
 * Manages typing state and events:
 * - Send typing status via WebSocket (/app/chat.typing)
 * - Listen to typing events from other users
 * - Debounce typing events (500ms) to reduce network traffic
 * - Auto-stop typing after 3 seconds of inactivity
 *
 * Usage:
 * ```ts
 * const { startTyping, stopTyping, typingUsers } = useTyping(roomId, currentUserId)
 *
 * // When user starts typing
 * onInput(() => startTyping())
 *
 * // Display typing users
 * <TypingIndicator :users="typingUsers" />
 * ```
 */
export function useTyping(roomId: string, currentUserId: string, currentUsername: string) {
  const { connected, send } = useSocket()
  const typingUsers = ref<Set<string>>(new Set())
  const typingUsernames = ref<Map<string, string>>(new Map())

  let typingTimeout: NodeJS.Timeout | null = null
  let debounceTimeout: NodeJS.Timeout | null = null
  let isCurrentlyTyping = false

  /**
   * Send typing indicator to server
   */
  const sendTypingIndicator = (isTyping: boolean) => {
    if (!connected.value) {
      console.warn('[useTyping] Socket not connected, cannot send typing indicator')
      return
    }

    // Format timestamp as LocalDateTime (yyyy-MM-dd'T'HH:mm:ss) without 'Z'
    const now = new Date()
    const timestamp = now.toISOString().substring(0, 19) // Remove milliseconds and 'Z'

    const payload: TypingIndicator = {
      roomId,
      userId: currentUserId,
      username: currentUsername,
      isTyping,
      timestamp
    }

    console.log('[useTyping] Sending typing indicator:', payload)

    try {
      send('/app/chat.typing', payload)
    } catch (error) {
      console.error('[useTyping] Failed to send typing indicator:', error)
    }
  }

  /**
   * Start typing (debounced)
   * - Sends typing=true to server
   * - Auto-stops after 3 seconds of inactivity
   */
  const startTyping = () => {
    // Clear existing debounce timeout
    if (debounceTimeout) {
      clearTimeout(debounceTimeout)
    }

    // Debounce: Only send after 500ms of continuous typing
    debounceTimeout = setTimeout(() => {
      if (!isCurrentlyTyping) {
        sendTypingIndicator(true)
        isCurrentlyTyping = true
      }

      // Reset auto-stop timeout
      if (typingTimeout) {
        clearTimeout(typingTimeout)
      }

      // Auto-stop after 3 seconds
      typingTimeout = setTimeout(() => {
        stopTyping()
      }, 3000)
    }, 500)
  }

  /**
   * Stop typing
   * - Sends typing=false to server
   * - Clears all timeouts
   */
  const stopTyping = () => {
    if (debounceTimeout) {
      clearTimeout(debounceTimeout)
      debounceTimeout = null
    }

    if (typingTimeout) {
      clearTimeout(typingTimeout)
      typingTimeout = null
    }

    if (isCurrentlyTyping) {
      sendTypingIndicator(false)
      isCurrentlyTyping = false
    }
  }

  /**
   * Handle incoming typing indicator events
   */
  const handleTypingEvent = (indicator: TypingIndicator) => {
    console.log('[useTyping] handleTypingEvent:', indicator, 'currentUserId:', currentUserId)

    // Ignore own typing events
    if (indicator.userId === currentUserId) {
      console.log('[useTyping] Ignoring own typing event')
      return
    }

    if (indicator.isTyping) {
      console.log('[useTyping] Adding typing user:', indicator.username)
      typingUsers.value.add(indicator.userId)
      typingUsernames.value.set(indicator.userId, indicator.username)
    } else {
      console.log('[useTyping] Removing typing user:', indicator.username)
      typingUsers.value.delete(indicator.userId)
      typingUsernames.value.delete(indicator.userId)
    }

    console.log('[useTyping] Current typing users:', Array.from(typingUsernames.value.values()))
  }

  /**
   * Handle incoming typing indicator from subscription
   * This should be called by the parent component's subscription handler
   */
  const handleIncomingTyping = (data: any) => {
    try {
      console.log('[useTyping] Received typing event:', data)
      // Check if this is a typing indicator message
      if (data && data.isTyping !== undefined) {
        console.log('[useTyping] Processing typing indicator:', data)
        handleTypingEvent(data as TypingIndicator)
      }
    } catch (error) {
      console.error('[useTyping] Error handling typing event:', error)
    }
  }

  /**
   * Get display text for typing users
   * - "Alice님이 입력 중..."
   * - "Alice, Bob님이 입력 중..."
   * - "Alice 외 2명이 입력 중..."
   */
  const typingText = computed(() => {
    const count = typingUsers.value.size
    if (count === 0) return ''

    const names = Array.from(typingUsernames.value.values())

    if (count === 1) {
      return `${names[0]}님이 입력 중...`
    } else if (count === 2) {
      return `${names[0]}, ${names[1]}님이 입력 중...`
    } else {
      return `${names[0]} 외 ${count - 1}명이 입력 중...`
    }
  })

  /**
   * Cleanup on unmount
   */
  onUnmounted(() => {
    stopTyping()
    typingUsers.value.clear()
    typingUsernames.value.clear()
  })

  return {
    startTyping,
    stopTyping,
    handleIncomingTyping,
    typingUsers: readonly(typingUsers),
    typingUsernames: readonly(typingUsernames),
    typingText,
    isTyping: computed(() => typingUsers.value.size > 0)
  }
}
