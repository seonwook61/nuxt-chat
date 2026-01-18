import { defineStore } from 'pinia'
import type { Message, ChatRoom } from '~/types/chat'

export const useChatStore = defineStore('chat', () => {
  // State
  const rooms = ref<Map<string, ChatRoom>>(new Map())
  const currentRoomId = ref<string | null>(null)
  const connected = ref(false)

  // Getters
  const currentRoom = computed(() => {
    return currentRoomId.value ? rooms.value.get(currentRoomId.value) : null
  })

  const currentMessages = computed(() => {
    return currentRoom.value?.messages || []
  })

  // Actions
  const setCurrentRoom = (roomId: string) => {
    currentRoomId.value = roomId
    if (!rooms.value.has(roomId)) {
      rooms.value.set(roomId, {
        id: roomId,
        name: roomId,
        onlineUsers: 0,
        messages: []
      })
    }
  }

  const addMessage = (message: Message) => {
    const room = rooms.value.get(message.roomId)
    if (room) {
      room.messages.push(message)
    }
  }

  const clearRoom = (roomId: string) => {
    rooms.value.delete(roomId)
  }

  const setConnected = (status: boolean) => {
    connected.value = status
  }

  return {
    // State
    rooms,
    currentRoomId,
    connected,
    // Getters
    currentRoom,
    currentMessages,
    // Actions
    setCurrentRoom,
    addMessage,
    clearRoom,
    setConnected
  }
})
