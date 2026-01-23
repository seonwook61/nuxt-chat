import type { ReadReceipt, Message } from '~/types/chat'

/**
 * Read Receipts Composable (Phase 6)
 *
 * Manages message read status and events:
 * - Send read receipt via WebSocket (/app/chat.read)
 * - Listen to read receipt events from other users
 * - Debounce read events (1 second) to reduce network traffic
 * - Track read status for messages (readBy, readCount)
 * - Auto-mark visible messages as read (Intersection Observer)
 *
 * Usage:
 * ```ts
 * const { markAsRead, handleIncomingReadReceipt, getReadStatus } = useReadReceipts(roomId, currentUserId)
 *
 * // When message becomes visible
 * onIntersection(() => markAsRead(messageId))
 *
 * // Display read status
 * const status = getReadStatus(messageId)
 * ```
 */
export function useReadReceipts(roomId: string, currentUserId: string) {
  const { connected, send } = useSocket()

  // Store read status for messages: messageId -> Set of userIds who read it
  const readStatusMap = ref<Map<string, Set<string>>>(new Map())

  // Track last sent read receipt to avoid duplicates
  const lastSentMessageId = ref<string | null>(null)

  // Debounce timeout
  let debounceTimeout: NodeJS.Timeout | null = null

  /**
   * Send read receipt to server
   * - Sends messageId to /app/chat.read
   * - Debounced to reduce network traffic
   */
  const sendReadReceipt = (messageId: string) => {
    if (!connected.value) {
      console.warn('[useReadReceipts] Socket not connected, cannot send read receipt')
      return
    }

    // Skip if same as last sent (duplicate prevention)
    if (messageId === lastSentMessageId.value) {
      console.log('[useReadReceipts] Skipping duplicate read receipt:', messageId)
      return
    }

    // Format timestamp as LocalDateTime (yyyy-MM-dd'T'HH:mm:ss) without 'Z'
    const now = new Date()
    const timestamp = now.toISOString().substring(0, 19) // Remove milliseconds and 'Z'

    const payload: ReadReceipt = {
      roomId,
      userId: currentUserId,
      messageId,
      timestamp
    }

    console.log('[useReadReceipts] Sending read receipt:', payload)

    try {
      send('/app/chat.read', payload)
      lastSentMessageId.value = messageId
    } catch (error) {
      console.error('[useReadReceipts] Failed to send read receipt:', error)
    }
  }

  /**
   * Mark a message as read (debounced)
   * - Sends read receipt to server after 1 second
   * - Only sends the latest messageId if multiple are marked in quick succession
   *
   * @param messageId Message ID to mark as read
   */
  const markAsRead = (messageId: string) => {
    // Clear existing debounce timeout
    if (debounceTimeout) {
      clearTimeout(debounceTimeout)
    }

    // Debounce: Only send after 1 second of inactivity
    debounceTimeout = setTimeout(() => {
      sendReadReceipt(messageId)
    }, 1000)
  }

  /**
   * Handle incoming read receipt event from WebSocket
   * - Updates readStatusMap with new read status
   * - Ignores own read receipts
   *
   * @param receipt ReadReceipt event from server
   */
  const handleIncomingReadReceipt = (receipt: ReadReceipt) => {
    console.log('[useReadReceipts] Received read receipt:', receipt)

    // Ignore own read receipts
    if (receipt.userId === currentUserId) {
      console.log('[useReadReceipts] Ignoring own read receipt')
      return
    }

    // Get or create Set for this messageId
    let readByUsers = readStatusMap.value.get(receipt.messageId)
    if (!readByUsers) {
      readByUsers = new Set<string>()
      readStatusMap.value.set(receipt.messageId, readByUsers)
    }

    // Add userId to the Set
    readByUsers.add(receipt.userId)

    console.log('[useReadReceipts] Updated read status for message:', receipt.messageId, 'readBy:', Array.from(readByUsers))
  }

  /**
   * Get read status for a specific message
   * - Returns list of userIds who read the message
   * - Returns empty array if no one has read it
   *
   * @param messageId Message ID
   * @returns Array of userIds who read this message
   */
  const getReadStatus = (messageId: string): string[] => {
    const readByUsers = readStatusMap.value.get(messageId)
    return readByUsers ? Array.from(readByUsers) : []
  }

  /**
   * Get read count for a specific message
   * - Returns number of users who read the message
   *
   * @param messageId Message ID
   * @returns Number of users who read this message
   */
  const getReadCount = (messageId: string): number => {
    const readByUsers = readStatusMap.value.get(messageId)
    return readByUsers ? readByUsers.size : 0
  }

  /**
   * Check if a message has been read by anyone
   *
   * @param messageId Message ID
   * @returns true if at least one user has read the message
   */
  const isRead = (messageId: string): boolean => {
    return getReadCount(messageId) > 0
  }

  /**
   * Initialize read status from message history
   * - Used when loading message history with pre-populated read status
   *
   * @param messages Array of messages with readBy field
   */
  const initializeReadStatus = (messages: Message[]) => {
    messages.forEach((msg) => {
      if (msg.readBy && msg.readBy.length > 0) {
        readStatusMap.value.set(msg.messageId, new Set(msg.readBy))
      }
    })
    console.log('[useReadReceipts] Initialized read status for', messages.length, 'messages')
  }

  /**
   * Cleanup on unmount
   */
  onUnmounted(() => {
    if (debounceTimeout) {
      clearTimeout(debounceTimeout)
    }
    readStatusMap.value.clear()
    lastSentMessageId.value = null
  })

  return {
    markAsRead,
    handleIncomingReadReceipt,
    getReadStatus,
    getReadCount,
    isRead,
    initializeReadStatus,
    readStatusMap: readonly(readStatusMap)
  }
}
