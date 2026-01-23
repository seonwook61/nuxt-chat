import type { Message, ChatEvent, JoinRoomPayload, LeaveRoomPayload, SendMessagePayload, MessageReaction, TypingIndicator, ReadReceipt } from '~/types/chat'

export const useChatRoom = (roomId: Ref<string> | string) => {
  const chatStore = useChatStore()
  const socket = useSocket()

  const _roomId = isRef(roomId) ? roomId : ref(roomId)
  const messages = ref<Message[]>([])
  const onlineUsers = ref(0)
  const subscriptionId = ref<string | null>(null)
  const isJoined = ref(false)

  // Typing indicator handler (will be set by ChatWindow)
  let typingHandler: ((data: any) => void) | null = null

  // Read receipt handler (will be set by ChatWindow)
  let readReceiptHandler: ((data: any) => void) | null = null

  // 아바타 색상 생성 함수
  const generateAvatarColor = (userId: string) => {
    const colors = [
      'bg-blue-500', 'bg-green-500', 'bg-purple-500',
      'bg-pink-500', 'bg-yellow-500', 'bg-red-500'
    ]
    const index = userId.charCodeAt(0) % colors.length
    return colors[index]
  }

  // Get current user info (mock for now, should come from auth)
  const currentUser = computed(() => {
    const userId = chatStore.currentUserId || 'user-' + Math.random().toString(36).substring(7)
    return {
      userId,
      username: chatStore.currentUsername || 'Anonymous',
      avatarColor: generateAvatarColor(userId)
    }
  })

  const handleRoomMessage = (payload: Message | ChatEvent | MessageReaction | TypingIndicator | ReadReceipt) => {
    // Check if it's a TypingIndicator first
    if ('isTyping' in payload) {
      // It's a TypingIndicator - delegate to typing handler
      if (typingHandler) {
        typingHandler(payload)
      }
      return
    }

    // Check if it's a ReadReceipt (has messageId, userId, timestamp but NOT content or reactionId)
    if ('messageId' in payload && 'userId' in payload && !('content' in payload) && !('reactionId' in payload)) {
      // It's a ReadReceipt - delegate to read receipt handler
      if (readReceiptHandler) {
        readReceiptHandler(payload)
      }
      return
    }

    // Check if it's a ChatMessage, ChatEvent, or MessageReaction
    if ('messageId' in payload && 'content' in payload) {
      // It's a ChatMessage
      const message = payload as Message

      // Avoid duplicates
      if (!messages.value.find(m => m.messageId === message.messageId)) {
        messages.value.push(message)
      }
    } else if ('reactionId' in payload) {
      // It's a MessageReaction
      const reaction = payload as MessageReaction
      handleReactionUpdate(reaction)
    } else if ('eventType' in payload) {
      // It's a ChatEvent
      const event = payload as ChatEvent

      // Use onlineCount from metadata if available (more accurate)
      if (event.metadata && typeof event.metadata.onlineCount === 'number') {
        onlineUsers.value = event.metadata.onlineCount
      } else {
        // Fallback to increment/decrement
        if (event.eventType === 'USER_JOINED') {
          onlineUsers.value++
        } else if (event.eventType === 'USER_LEFT') {
          onlineUsers.value = Math.max(0, onlineUsers.value - 1)
        }
      }
    }
  }

  const handleReactionUpdate = (reaction: MessageReaction) => {
    // Find the message and update its reactions
    const message = messages.value.find(m => m.messageId === reaction.messageId)
    if (!message) {
      console.warn('[ChatRoom] Message not found for reaction:', reaction.messageId)
      return
    }

    // Initialize reactions object if it doesn't exist
    if (!message.reactions) {
      message.reactions = {}
    }

    const emoji = reaction.emoji
    const userId = reaction.userId

    if (reaction.action === 'ADD') {
      // Add reaction
      if (!message.reactions[emoji]) {
        message.reactions[emoji] = []
      }
      if (!message.reactions[emoji].includes(userId)) {
        message.reactions[emoji].push(userId)
      }
    } else if (reaction.action === 'REMOVE') {
      // Remove reaction
      if (message.reactions[emoji]) {
        message.reactions[emoji] = message.reactions[emoji].filter(id => id !== userId)
        // Remove emoji key if no more users
        if (message.reactions[emoji].length === 0) {
          delete message.reactions[emoji]
        }
      }
    }

    console.log('[ChatRoom] Reaction updated:', {
      messageId: reaction.messageId,
      emoji: reaction.emoji,
      action: reaction.action,
      reactions: message.reactions
    })
  }

  const joinRoom = async () => {
    if (!socket.connected.value || isJoined.value) {
      return
    }

    try {
      // 1. Load message history from PostgreSQL
      const { fetchMessageHistory } = useMessageHistory()
      const history = await fetchMessageHistory(_roomId.value, 50)

      // Add history messages to the local state
      messages.value = history

      console.log(`[ChatRoom] Loaded ${history.length} messages from history`)

      // 2. Subscribe to room topic FIRST
      const destination = `/topic/room/${_roomId.value}`
      subscriptionId.value = socket.subscribe(destination, handleRoomMessage)

      // 3. Send join message AFTER subscription
      // Format timestamp as LocalDateTime (yyyy-MM-dd'T'HH:mm:ss) without 'Z'
      const now = new Date()
      const timestamp = now.toISOString().substring(0, 19) // Remove milliseconds and 'Z'

      const payload: JoinRoomPayload = {
        eventId: crypto.randomUUID(),
        eventType: 'USER_JOINED',
        roomId: _roomId.value,
        userId: currentUser.value.userId,
        username: currentUser.value.username,
        timestamp: timestamp,
        metadata: {}
      }
      socket.send('/app/chat.join', payload)

      isJoined.value = true
      chatStore.setCurrentRoom(_roomId.value)

      console.log('[ChatRoom] Joined room:', _roomId.value)
    } catch (error) {
      console.error('[ChatRoom] Failed to join room:', error)
    }
  }

  const leaveRoom = () => {
    if (!isJoined.value) {
      return
    }

    try {
      // 1. Unsubscribe from room topic
      if (subscriptionId.value) {
        socket.unsubscribe(subscriptionId.value)
        subscriptionId.value = null
      }

      // 2. Send leave message
      if (socket.connected.value) {
        // Format timestamp as LocalDateTime (yyyy-MM-dd'T'HH:mm:ss) without 'Z'
        const now = new Date()
        const timestamp = now.toISOString().substring(0, 19) // Remove milliseconds and 'Z'

        const payload: LeaveRoomPayload = {
          eventId: crypto.randomUUID(),
          eventType: 'USER_LEFT',
          roomId: _roomId.value,
          userId: currentUser.value.userId,
          timestamp: timestamp,
          metadata: {}
        }
        socket.send('/app/chat.leave', payload)
      }

      isJoined.value = false
      messages.value = []
      onlineUsers.value = 0

      console.log('[ChatRoom] Left room:', _roomId.value)
    } catch (error) {
      console.error('[ChatRoom] Failed to leave room:', error)
    }
  }

  const sendMessage = (content: string) => {
    if (!content || content.trim().length === 0) {
      console.warn('[ChatRoom] Cannot send empty message')
      return
    }

    if (content.length > 1000) {
      console.warn('[ChatRoom] Message too long (max 1000 characters)')
      return
    }

    if (!socket.connected.value || !isJoined.value) {
      console.warn('[ChatRoom] Cannot send message: not connected or not joined')
      return
    }

    try {
      // Format timestamp as LocalDateTime (yyyy-MM-dd'T'HH:mm:ss) without 'Z'
      const now = new Date()
      const timestamp = now.toISOString().substring(0, 19) // Remove milliseconds and 'Z'

      const payload: SendMessagePayload = {
        messageId: crypto.randomUUID(),
        roomId: _roomId.value,
        userId: currentUser.value.userId,
        username: currentUser.value.username,
        content: content.trim(),
        timestamp: timestamp,
        type: 'TEXT'
      }
      socket.send('/app/chat.send', payload)
    } catch (error) {
      console.error('[ChatRoom] Failed to send message:', error)
    }
  }

  // Auto-rejoin on reconnect
  watch(() => socket.connected.value, (connected, wasConnected) => {
    if (connected && !wasConnected && isJoined.value) {
      console.log('[ChatRoom] Reconnected, rejoining room')
      isJoined.value = false // Reset state
      joinRoom()
    }
  })

  // Clean up on unmount
  onUnmounted(() => {
    leaveRoom()
  })

  /**
   * Register typing event handler
   * This allows ChatWindow to pass typing events to useTyping composable
   */
  const setTypingHandler = (handler: (data: any) => void) => {
    typingHandler = handler
  }

  /**
   * Register read receipt event handler
   * This allows ChatWindow to pass read receipt events to useReadReceipts composable
   */
  const setReadReceiptHandler = (handler: (data: any) => void) => {
    readReceiptHandler = handler
  }

  return {
    messages: readonly(messages),
    onlineUsers: readonly(onlineUsers),
    isJoined: readonly(isJoined),
    currentUser,
    joinRoom,
    leaveRoom,
    sendMessage,
    setTypingHandler,
    setReadReceiptHandler
  }
}
