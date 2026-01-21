<template>
  <div
    ref="scrollContainer"
    class="flex-1 overflow-y-auto px-4 py-4 space-y-1"
  >
    <!-- Empty State -->
    <div
      v-if="messages.length === 0"
      class="h-full flex flex-col items-center justify-center text-gray-500"
    >
      <svg class="w-16 h-16 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
      </svg>
      <p class="text-lg font-medium">No messages yet</p>
      <p class="text-sm">Start the conversation!</p>
    </div>

    <!-- Messages -->
    <MessageBubble
      v-for="message in processedMessages"
      :key="message.messageId"
      :message="message"
      :current-user-id="currentUserId"
      :current-username="currentUsername"
    />
  </div>
</template>

<script setup lang="ts">
import type { Message } from '~/types/chat'
import MessageBubble from './MessageBubble.vue'

interface Props {
  messages: readonly Message[]
  currentUserId: string
  currentUsername?: string
}

const props = defineProps<Props>()

// Get currentUsername from chatStore if not provided
const currentUsername = computed(() => {
  return props.currentUsername || 'Anonymous'
})

const scrollContainer = ref<HTMLElement | null>(null)

// 메시지 그룹핑 및 UI 플래그 계산
const processedMessages = computed(() => {
  return props.messages.map((msg, index) => {
    const prevMsg = index > 0 ? props.messages[index - 1] : null
    const nextMsg = index < props.messages.length - 1 ? props.messages[index + 1] : null

    // 본인 메시지 여부
    const isOwn = msg.userId === props.currentUserId

    // 이전 메시지와 같은 사용자인지 (연속 메시지)
    const isSameUserAsPrev = prevMsg && prevMsg.userId === msg.userId

    // 다음 메시지와 같은 사용자인지
    const isSameUserAsNext = nextMsg && nextMsg.userId === msg.userId

    // 시간 차이 계산 (5분 이내면 그룹화)
    const timeDiffWithPrev = prevMsg
      ? (new Date(msg.timestamp).getTime() - new Date(prevMsg.timestamp).getTime()) / 1000 / 60
      : Infinity

    const shouldGroup = isSameUserAsPrev && timeDiffWithPrev < 5

    return {
      ...msg,
      isOwn,
      showAvatar: !shouldGroup,      // 그룹 첫 메시지만 아바타
      showUsername: !shouldGroup,    // 그룹 첫 메시지만 사용자명
      showTimestamp: !isSameUserAsNext  // 그룹 마지막 메시지만 시간
    }
  })
})

// 자동 스크롤 (새 메시지 도착 시)
watch(() => props.messages.length, () => {
  nextTick(() => {
    if (scrollContainer.value) {
      scrollContainer.value.scrollTop = scrollContainer.value.scrollHeight
    }
  })
})
</script>
