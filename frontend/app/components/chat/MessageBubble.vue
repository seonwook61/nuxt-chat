<template>
  <div
    :class="[
      'flex items-end gap-2 mb-2 group',
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
        'max-w-[70%] flex flex-col relative',
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

      <!-- Message Bubble Container (for long press detection) -->
      <div
        class="relative"
        @mousedown="handleMouseDown"
        @mouseup="handleMouseUp"
        @mouseleave="handleMouseLeave"
        @mouseenter="showActions = true"
        @touchstart="handleTouchStart"
        @touchend="handleTouchEnd"
        @touchcancel="handleTouchCancel"
      >
        <!-- Message Bubble -->
        <div
          :class="[
            'px-4 py-2 rounded-2xl break-words cursor-pointer select-none',
            message.isOwn
              ? 'bg-gradient-to-br from-blue-500 to-blue-600 text-white rounded-br-md'
              : 'bg-gray-100 text-gray-900 rounded-bl-md'
          ]"
        >
          {{ message.content }}
        </div>

        <!-- Reaction Picker (Instagram-style - on long press) -->
        <div
          v-if="showReactionPicker"
          :class="[
            'absolute bottom-full mb-2 z-50',
            message.isOwn ? 'right-0' : 'left-0'
          ]"
        >
          <ReactionPicker
            :message-id="message.messageId"
            :room-id="message.roomId"
            :user-id="props.currentUserId"
            :username="props.currentUsername"
            :current-reactions="message.reactions"
            @close="showReactionPicker = false"
            @reaction-added="showReactionPicker = false"
          />
        </div>
      </div>

      <!-- Message Reactions (below message) -->
      <MessageReactions
        v-if="message.reactions && Object.keys(message.reactions).length > 0"
        :message-id="message.messageId"
        :room-id="message.roomId"
        :user-id="props.currentUserId"
        :username="props.currentUsername"
        :reactions="message.reactions"
      />

      <!-- Timestamp + Read Receipt (본인 메시지에만 표시) -->
      <div
        v-if="message.showTimestamp"
        class="flex items-center gap-1 text-xs text-gray-500 mt-1 px-1"
      >
        <span>{{ formatTime(message.timestamp) }}</span>

        <!-- Read Receipt Icon (본인 메시지에만) -->
        <ReadReceiptIcon
          v-if="message.isOwn"
          :is-read="(message.readCount ?? 0) > 0"
          :read-count="message.readCount"
          :show-read-count="false"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Message } from '~/types/chat'
import UserAvatar from './UserAvatar.vue'
import ReactionPicker from '../ReactionPicker.vue'
import MessageReactions from '../MessageReactions.vue'
import ReadReceiptIcon from '../ReadReceiptIcon.vue'

interface Props {
  message: Message
  currentUserId: string
  currentUsername: string
}

const props = defineProps<Props>()
const showActions = ref(false)
const showReactionPicker = ref(false)

// Long press detection
let longPressTimer: NodeJS.Timeout | null = null
const LONG_PRESS_DURATION = 500 // ms

const formatTime = (timestamp: string) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString('ko-KR', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// Mouse long press handlers
const handleMouseDown = () => {
  longPressTimer = setTimeout(() => {
    showReactionPicker.value = true
  }, LONG_PRESS_DURATION)
}

const handleMouseUp = () => {
  if (longPressTimer) {
    clearTimeout(longPressTimer)
    longPressTimer = null
  }
}

const handleMouseLeave = () => {
  showActions.value = false
  if (longPressTimer) {
    clearTimeout(longPressTimer)
    longPressTimer = null
  }
}

// Touch long press handlers (mobile)
const handleTouchStart = (e: TouchEvent) => {
  longPressTimer = setTimeout(() => {
    showReactionPicker.value = true
    e.preventDefault() // Prevent context menu on mobile
  }, LONG_PRESS_DURATION)
}

const handleTouchEnd = () => {
  if (longPressTimer) {
    clearTimeout(longPressTimer)
    longPressTimer = null
  }
}

const handleTouchCancel = () => {
  if (longPressTimer) {
    clearTimeout(longPressTimer)
    longPressTimer = null
  }
}

// Cleanup on unmount
onBeforeUnmount(() => {
  if (longPressTimer) {
    clearTimeout(longPressTimer)
  }
})
</script>
