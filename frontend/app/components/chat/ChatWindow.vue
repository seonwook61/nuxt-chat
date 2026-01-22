<template>
  <div class="bg-white rounded-lg shadow-lg h-full flex flex-col">
    <!-- Connection status -->
    <div
      v-if="!socket.connected.value"
      class="bg-gradient-to-r from-yellow-400 to-yellow-500 text-white px-4 py-2 text-sm font-medium flex items-center gap-2"
    >
      <div class="w-2 h-2 bg-white rounded-full animate-pulse" />
      Connecting to chat server...
    </div>

    <!-- Room info -->
    <div class="bg-gradient-to-r from-gray-50 to-gray-100 px-4 py-3 border-b border-gray-200">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 bg-gradient-to-br from-blue-500 to-purple-600 rounded-full flex items-center justify-center text-white font-bold shadow-md">
            {{ roomId.charAt(0).toUpperCase() }}
          </div>
          <div>
            <h2 class="text-lg font-semibold text-gray-800">{{ roomId }}</h2>
            <p class="text-xs text-gray-600">
              {{ onlineUsers }} {{ onlineUsers === 1 ? 'member' : 'members' }} online
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- Message List -->
    <MessageList
      :messages="messages"
      :current-user-id="currentUserId"
      :current-username="currentUser.username"
      class="flex-1"
    />

    <!-- Typing Indicator -->
    <TypingIndicator
      :typing-text="typingText"
      :is-typing="isTyping"
    />

    <!-- Message Input -->
    <MessageInput
      @send="handleSendMessage"
      @typing="handleTyping"
    />
  </div>
</template>

<script setup lang="ts">
import MessageList from './MessageList.vue'
import MessageInput from './MessageInput.vue'
import TypingIndicator from './TypingIndicator.vue'

interface Props {
  roomId: string
}

const props = defineProps<Props>()

console.log('[ChatWindow] Component mounted with roomId:', props.roomId)

const socket = useSocket()
console.log('[ChatWindow] Socket initialized:', socket)

const chatRoom = useChatRoom(toRef(props, 'roomId'))
console.log('[ChatWindow] ChatRoom initialized:', chatRoom)

const messageContainer = ref<HTMLElement | null>(null)

const { messages, onlineUsers, currentUser, joinRoom, leaveRoom, sendMessage, setTypingHandler } = chatRoom
console.log('[ChatWindow] Messages:', messages.value, 'Online:', onlineUsers.value)

// Get current user ID from chatRoom composable
const currentUserId = computed(() => currentUser.value.userId)

// Initialize typing indicator with reactive values
const typingRoomId = computed(() => props.roomId)
const typingUserId = computed(() => currentUser.value.userId)
const typingUsername = computed(() => currentUser.value.username)

const typing = useTyping(
  typingRoomId.value,
  typingUserId.value,
  typingUsername.value
)

const { startTyping, stopTyping, handleIncomingTyping, typingText, isTyping } = typing

// Connect typing handler to room subscription
setTypingHandler(handleIncomingTyping)

const handleSendMessage = (content: string) => {
  stopTyping() // Stop typing when message is sent
  sendMessage(content)
}

const handleTyping = () => {
  startTyping()
}

// Auto-scroll to bottom when new messages arrive
watch(messages, () => {
  nextTick(() => {
    if (messageContainer.value) {
      const container = messageContainer.value
      container.scrollTop = container.scrollHeight
    }
  })
})

// Watch for room changes
watch(() => props.roomId, async (newRoomId, oldRoomId) => {
  if (oldRoomId && oldRoomId !== newRoomId) {
    leaveRoom()
  }
  if (socket.connected.value) {
    await joinRoom()
  }
})

onMounted(async () => {
  console.log('[ChatWindow] onMounted - socket.connected:', socket.connected.value)

  if (socket.connected.value) {
    console.log('[ChatWindow] Socket already connected, joining room immediately')
    await joinRoom()
  } else {
    console.log('[ChatWindow] Socket not connected yet, setting up watch')
    // Wait for connection with unref to access the actual value
    const stopWatch = watch(
      () => socket.connected.value,
      async (connected) => {
        console.log('[ChatWindow] Watch triggered - connected:', connected)
        if (connected) {
          console.log('[ChatWindow] Socket connected via watch, joining room')
          await joinRoom()
          stopWatch()
        }
      },
      { immediate: true }
    )
  }
})

onBeforeUnmount(() => {
  leaveRoom()
})
</script>
