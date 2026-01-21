<template>
  <div v-if="hasReactions" class="message-reactions flex flex-wrap gap-1 mt-1">
    <button
      v-for="[emoji, users] in sortedReactions"
      :key="emoji"
      class="reaction-badge flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium transition-all duration-200"
      :class="[
        hasUserReacted(emoji as EmojiType)
          ? 'bg-blue-100 dark:bg-blue-900 border border-blue-500 text-blue-700 dark:text-blue-300'
          : 'bg-gray-100 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'
      ]"
      :title="getReactionTooltip(emoji, users)"
      @click.stop="handleReactionClick(emoji as EmojiType)"
    >
      <span class="text-sm">{{ getEmojiChar(emoji as EmojiType) }}</span>
      <span class="font-semibold">{{ users.length }}</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getEmojiChar } from '~/constants/emojis'
import type { EmojiType } from '~/types/chat'

interface Props {
  messageId: string
  roomId: string
  userId: string
  username: string
  reactions?: Record<string, string[]>
}

const props = defineProps<Props>()

const { toggleReaction, hasUserReacted: checkUserReacted } = useReactions()

const hasReactions = computed(() => {
  if (!props.reactions) return false
  return Object.keys(props.reactions).length > 0
})

// Sort reactions by count (descending)
const sortedReactions = computed(() => {
  if (!props.reactions) return []
  return Object.entries(props.reactions)
    .filter(([_, users]) => users.length > 0)
    .sort((a, b) => b[1].length - a[1].length)
})

const hasUserReacted = (emoji: EmojiType) => {
  return checkUserReacted(emoji, props.userId, props.reactions)
}

const getReactionTooltip = (emoji: string, users: string[]) => {
  if (users.length === 0) return ''
  if (users.length === 1) return users[0]
  if (users.length === 2) return `${users[0]} and ${users[1]}`
  return `${users[0]}, ${users[1]} and ${users.length - 2} others`
}

const handleReactionClick = (emoji: EmojiType) => {
  toggleReaction(
    props.messageId,
    props.roomId,
    emoji,
    props.userId,
    props.username,
    props.reactions
  )
}
</script>

<style scoped>
.reaction-badge {
  cursor: pointer;
  user-select: none;
  -webkit-tap-highlight-color: transparent;
}

.reaction-badge:active {
  transform: scale(0.95);
}
</style>
