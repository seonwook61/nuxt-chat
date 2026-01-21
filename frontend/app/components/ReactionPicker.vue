<template>
  <div
    class="reaction-picker bg-white dark:bg-gray-800 rounded-full shadow-lg px-2 py-1 flex gap-1 border border-gray-200 dark:border-gray-700"
    @click.stop
  >
    <button
      v-for="emojiConfig in REACTION_EMOJIS"
      :key="emojiConfig.type"
      class="reaction-emoji-btn w-10 h-10 flex items-center justify-center rounded-full hover:bg-gray-100 dark:hover:bg-gray-700 transition-all duration-200 hover:scale-125"
      :class="{ [emojiConfig.color]: hasReacted(emojiConfig.type) }"
      :title="emojiConfig.label"
      @click="handleEmojiClick(emojiConfig.type)"
    >
      <span class="text-2xl">{{ emojiConfig.emoji }}</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { REACTION_EMOJIS } from '~/constants/emojis'
import type { EmojiType } from '~/types/chat'

interface Props {
  messageId: string
  roomId: string
  userId: string
  username: string
  currentReactions?: Record<string, string[]>
}

const props = defineProps<Props>()

const emit = defineEmits<{
  close: []
  reactionAdded: [emoji: EmojiType]
}>()

const { toggleReaction, hasUserReacted } = useReactions()

const hasReacted = (emoji: EmojiType) => {
  return hasUserReacted(emoji, props.userId, props.currentReactions)
}

const handleEmojiClick = (emoji: EmojiType) => {
  toggleReaction(
    props.messageId,
    props.roomId,
    emoji,
    props.userId,
    props.username,
    props.currentReactions
  )
  emit('reactionAdded', emoji)
  emit('close')
}
</script>

<style scoped>
.reaction-picker {
  animation: scaleIn 0.2s ease-out;
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.reaction-emoji-btn {
  user-select: none;
  -webkit-tap-highlight-color: transparent;
}
</style>
