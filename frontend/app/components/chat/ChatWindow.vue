<template>
  <div class="bg-white rounded-lg shadow-lg h-full flex flex-col">
    <!-- Message List -->
    <div class="flex-1 overflow-hidden">
      <MessageList :messages="messages" />
    </div>

    <!-- Message Input -->
    <div class="border-t border-gray-200">
      <MessageInput @send="handleSendMessage" />
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Message } from '~/types/chat'

interface Props {
  roomId: string
}

const props = defineProps<Props>()

// Placeholder data - will be replaced with real data in Phase 2
const messages = ref<Message[]>([])

const handleSendMessage = (content: string) => {
  console.log('Send message:', content, 'to room:', props.roomId)
  // Socket emit logic will be added in Phase 2
}
</script>
