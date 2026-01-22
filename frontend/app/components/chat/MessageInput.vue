<template>
  <div class="bg-white border-t border-gray-200 px-4 py-3">
    <div class="flex items-end gap-2 relative">
      <!-- Emoji Picker Popup -->
      <div
        v-if="showEmojiPicker"
        class="absolute bottom-14 left-0 bg-white rounded-lg shadow-xl border border-gray-200 p-3 z-50"
        @click.stop
      >
        <div class="grid grid-cols-8 gap-1 max-w-xs">
          <button
            v-for="emoji in commonEmojis"
            :key="emoji"
            type="button"
            class="text-2xl hover:bg-gray-100 rounded p-1 transition-colors"
            @click="insertEmoji(emoji)"
          >
            {{ emoji }}
          </button>
        </div>
      </div>

      <!-- Emoji Button -->
      <button
        type="button"
        class="flex-shrink-0 w-9 h-9 flex items-center justify-center text-gray-500 hover:text-gray-700 rounded-full hover:bg-gray-100 transition-colors"
        @click.stop="toggleEmojiPicker"
      >
        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      </button>

      <!-- Input Container -->
      <div class="flex-1 relative">
        <textarea
          ref="inputRef"
          v-model="message"
          placeholder="Message..."
          rows="1"
          class="w-full resize-none rounded-full border border-gray-300 px-4 py-2 pr-12 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
          :style="{ height: textareaHeight }"
          :disabled="isSending"
          @input="handleInput"
          @keydown.enter.exact.prevent="sendMessage"
        />

        <!-- Character Count -->
        <div
          v-if="message.length > 800"
          class="absolute right-14 bottom-2 text-xs"
          :class="message.length > 1000 ? 'text-red-500' : 'text-gray-500'"
        >
          {{ message.length }}/1000
        </div>
      </div>

      <!-- Send Button -->
      <button
        type="button"
        :disabled="!canSend"
        :class="[
          'flex-shrink-0 w-9 h-9 flex items-center justify-center rounded-full transition-all',
          canSend
            ? 'bg-blue-500 text-white hover:bg-blue-600 scale-100'
            : 'bg-gray-200 text-gray-400 scale-90'
        ]"
        @click="sendMessage"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
const emit = defineEmits<{
  send: [content: string]
  typing: []
}>()

const message = ref('')
const isSending = ref(false)
const inputRef = ref<HTMLTextAreaElement | null>(null)
const textareaHeight = ref('40px')
const showEmojiPicker = ref(false)

// Common emojis for quick access
const commonEmojis = [
  '😀', '😂', '🤣', '😊', '😍', '🥰', '😘', '😎',
  '🤔', '😅', '😆', '😉', '😌', '😏', '🙂', '😐',
  '😑', '😶', '🙄', '😬', '😮', '😯', '😲', '😳',
  '🥺', '😢', '😭', '😤', '😠', '😡', '🤬', '😱',
  '😨', '😰', '😥', '😓', '🤗', '🤩', '🥳', '😇',
  '👍', '👎', '👌', '✌️', '🤞', '🤝', '👏', '🙌',
  '💪', '🙏', '👋', '🤚', '✋', '🖐️', '👊', '✊',
  '❤️', '💕', '💖', '💗', '💓', '💞', '💝', '💘',
  '🔥', '✨', '💯', '💫', '⭐', '🌟', '💥', '💢'
]

const canSend = computed(() => {
  return message.value.trim().length > 0 &&
         message.value.length <= 1000 &&
         !isSending.value
})

const toggleEmojiPicker = () => {
  showEmojiPicker.value = !showEmojiPicker.value
}

const insertEmoji = (emoji: string) => {
  message.value += emoji
  showEmojiPicker.value = false
  inputRef.value?.focus()
  adjustHeight()
}

const adjustHeight = () => {
  if (!inputRef.value) return

  inputRef.value.style.height = 'auto'
  const newHeight = Math.min(inputRef.value.scrollHeight, 150)
  textareaHeight.value = `${newHeight}px`
}

const handleInput = () => {
  adjustHeight()
  // Emit typing event when user is typing
  if (message.value.trim().length > 0) {
    emit('typing')
  }
}

const sendMessage = async () => {
  if (!canSend.value) return

  const content = message.value.trim()
  isSending.value = true

  try {
    emit('send', content)
    message.value = ''
    textareaHeight.value = '40px'
  } catch (error) {
    console.error('Failed to send message:', error)
  } finally {
    isSending.value = false
    inputRef.value?.focus()
  }
}

// Close emoji picker when clicking outside
if (process.client) {
  onMounted(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (showEmojiPicker.value) {
        showEmojiPicker.value = false
      }
    }
    document.addEventListener('click', handleClickOutside)
    onBeforeUnmount(() => {
      document.removeEventListener('click', handleClickOutside)
    })
  })
}
</script>
