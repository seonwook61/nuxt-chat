<template>
  <div
    :class="[
      'flex items-end gap-2 mb-2',
      message.isOwn ? 'flex-row-reverse' : 'flex-row'
    ]"
  >
    <!-- Avatar (타인 메시지 + 첫 메시지만) -->
    <UserAvatar
      v-if="!message.isOwn && message.showAvatar"
      :user="{ userId: message.userId, username: message.username }"
      class="flex-shrink-0"
    />
    <div v-else-if="!message.isOwn" class="w-8 flex-shrink-0" />

    <!-- Message Content -->
    <div
      :class="[
        'max-w-[70%] flex flex-col',
        message.isOwn ? 'items-end' : 'items-start'
      ]"
    >
      <!-- Username (타인 메시지 + 첫 메시지만) -->
      <span
        v-if="!message.isOwn && message.showUsername"
        class="text-xs text-gray-600 font-medium mb-1 px-1"
      >
        {{ message.username }}
      </span>

      <!-- Message Bubble -->
      <div
        :class="[
          'px-4 py-2 rounded-2xl break-words',
          message.isOwn
            ? 'bg-gradient-to-br from-blue-500 to-blue-600 text-white rounded-br-md'
            : 'bg-gray-100 text-gray-900 rounded-bl-md'
        ]"
        @mouseenter="showActions = true"
        @mouseleave="showActions = false"
      >
        {{ message.content }}
      </div>

      <!-- Timestamp (마지막 메시지만) -->
      <span
        v-if="message.showTimestamp"
        class="text-xs text-gray-500 mt-1 px-1"
      >
        {{ formatTime(message.timestamp) }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Message } from '~/types/chat'
import UserAvatar from './UserAvatar.vue'

interface Props {
  message: Message
}

const props = defineProps<Props>()
const showActions = ref(false)

const formatTime = (timestamp: string) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString('ko-KR', {
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>
