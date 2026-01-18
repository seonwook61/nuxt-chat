import type { Message } from '~/types/chat'

// Placeholder composable - will be implemented in Phase 2
export const useChatRoom = (roomId: string) => {
  const chatStore = useChatStore()
  const messages = ref<Message[]>([])
  const onlineUsers = ref(0)

  const joinRoom = () => {
    console.log('Join room - to be implemented in Phase 2', roomId)
    chatStore.setCurrentRoom(roomId)
    // Implementation will be added in Phase 2
  }

  const leaveRoom = () => {
    console.log('Leave room - to be implemented in Phase 2', roomId)
    // Implementation will be added in Phase 2
  }

  const sendMessage = (content: string) => {
    console.log('Send message - to be implemented in Phase 2', content)
    // Implementation will be added in Phase 2
  }

  return {
    messages,
    onlineUsers,
    joinRoom,
    leaveRoom,
    sendMessage
  }
}
