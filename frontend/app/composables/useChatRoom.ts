import type { Message, ChatEvent, JoinRoomPayload, LeaveRoomPayload, SendMessagePayload } from '~/types/chat'

export const useChatRoom = (roomId: Ref<string> | string) => {
  const chatStore = useChatStore()
  const socket = useSocket()

  const _roomId = isRef(roomId) ? roomId : ref(roomId)
  const messages = ref<Message[]>([])
  const onlineUsers = ref(0)
  const subscriptionId = ref<string | null>(null)
  const isJoined = ref(false)

  // Get current user info (mock for now, should come from auth)
  const currentUser = computed(() => ({
    userId: chatStore.currentUserId || 'user-' + Math.random().toString(36).substring(7),
    username: chatStore.currentUsername || 'Anonymous'
  }))

  const handleRoomMessage = (payload: Message | ChatEvent) => {
    // Check if it's a ChatMessage or ChatEvent
    if ('messageId' in payload) {
      // It's a ChatMessage
      const message = payload as Message

      // Avoid duplicates
      if (!messages.value.find(m => m.messageId === message.messageId)) {
        messages.value.push(message)
      }
    } else if ('eventType' in payload) {
      // It's a ChatEvent
      const event = payload as ChatEvent

      if (event.eventType === 'USER_JOINED') {
        onlineUsers.value++
      } else if (event.eventType === 'USER_LEFT') {
        onlineUsers.value = Math.max(0, onlineUsers.value - 1)
      }
    }
  }

  const joinRoom = async () => {
    if (!socket.connected.value || isJoined.value) {
      return
    }

    try {
      // 1. Subscribe to room topic FIRST
      const destination = `/topic/room/${_roomId.value}`
      subscriptionId.value = socket.subscribe(destination, handleRoomMessage)

      // 2. Send join message AFTER subscription
      const payload: JoinRoomPayload = {
        roomId: _roomId.value,
        userId: currentUser.value.userId,
        username: currentUser.value.username
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
        const payload: LeaveRoomPayload = {
          roomId: _roomId.value,
          userId: currentUser.value.userId
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
      const payload: SendMessagePayload = {
        roomId: _roomId.value,
        userId: currentUser.value.userId,
        username: currentUser.value.username,
        content: content.trim()
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

  return {
    messages: readonly(messages),
    onlineUsers: readonly(onlineUsers),
    isJoined: readonly(isJoined),
    joinRoom,
    leaveRoom,
    sendMessage
  }
}
