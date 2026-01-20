<template>
  <div class="bg-white rounded-lg shadow-lg h-full flex flex-col">
    <!-- Connection status -->
    <div v-if="!socket.connected.value" class="bg-yellow-100 text-yellow-800 px-4 py-2 text-sm">
      Connecting to chat server...
    </div>

    <!-- Room info -->
    <div class="bg-gray-50 px-4 py-3 border-b border-gray-200">
      <div class="flex items-center justify-between">
        <h2 class="text-lg font-semibold text-gray-800">Room: {{ roomId }}</h2>
        <span class="text-sm text-gray-600">{{ onlineUsers }} online</span>
      </div>
    </div>

    <!-- Message List -->
    <div ref="messageContainer" class="flex-1 overflow-hidden">
      <MessageList :messages="messages" />
    </div>

    <!-- Message Input -->
    <div class="border-t border-gray-200">
      <MessageInput @send="handleSendMessage" />
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  roomId: string
}

const props = defineProps<Props>()

const socket = useSocket()
const chatRoom = useChatRoom(toRef(props, 'roomId'))
const messageContainer = ref<HTMLElement | null>(null)

const { messages, onlineUsers, joinRoom, leaveRoom, sendMessage } = chatRoom

const handleSendMessage = (content: string) => {
  sendMessage(content)
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
  if (socket.connected.value) {
    await joinRoom()
  } else {
    // Wait for connection
    watch(() => socket.connected.value, async (connected) => {
      if (connected) {
        await joinRoom()
      }
    }, { once: true })
  }
})

onBeforeUnmount(() => {
  leaveRoom()
})
</script>
