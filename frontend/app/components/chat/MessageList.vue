<template>
  <div class="h-full overflow-y-auto p-4 space-y-4">
    <div
      v-if="messages.length === 0"
      class="flex items-center justify-center h-full text-gray-500"
    >
      <div class="text-center">
        <svg class="w-16 h-16 mx-auto mb-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
        </svg>
        <p class="text-lg">No messages yet</p>
        <p class="text-sm text-gray-400 mt-2">Start a conversation!</p>
      </div>
    </div>

    <div
      v-for="message in messages"
      :key="message.id"
      class="flex items-start space-x-3"
    >
      <div class="flex-shrink-0">
        <div class="w-10 h-10 rounded-full bg-primary-500 flex items-center justify-center text-white font-semibold">
          {{ message.userId.charAt(0).toUpperCase() }}
        </div>
      </div>

      <div class="flex-1 min-w-0">
        <div class="flex items-baseline space-x-2">
          <span class="font-semibold text-gray-900">{{ message.userId }}</span>
          <span class="text-xs text-gray-500">
            {{ formatTimestamp(message.timestamp) }}
          </span>
        </div>
        <p class="mt-1 text-gray-800">{{ message.content }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Message } from '~/types/chat'

interface Props {
  messages: Message[]
}

defineProps<Props>()

const formatTimestamp = (timestamp: number) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>
