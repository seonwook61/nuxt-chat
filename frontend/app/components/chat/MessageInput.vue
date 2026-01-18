<template>
  <div class="p-4">
    <div class="flex items-end space-x-3">
      <div class="flex-1">
        <textarea
          v-model="message"
          ref="textareaRef"
          placeholder="Type a message..."
          rows="1"
          class="w-full px-4 py-3 border border-gray-300 rounded-lg resize-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
          :disabled="isSending"
          @keydown.enter.exact.prevent="sendMessage"
          @input="adjustHeight"
        />
      </div>

      <button
        @click="sendMessage"
        :disabled="!canSend"
        class="px-6 py-3 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
        </svg>
      </button>
    </div>

    <div class="mt-2 flex items-center justify-between text-xs text-gray-500">
      <span>Press Enter to send, Shift+Enter for new line</span>
      <span :class="{ 'text-red-500': message.length > maxLength }">
        {{ message.length }} / {{ maxLength }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
const emit = defineEmits<{
  send: [content: string]
}>()

const message = ref('')
const isSending = ref(false)
const textareaRef = ref<HTMLTextAreaElement | null>(null)
const maxLength = 1000

const canSend = computed(() => {
  return message.value.trim().length > 0 &&
         message.value.length <= maxLength &&
         !isSending.value
})

const sendMessage = async () => {
  if (!canSend.value) return

  const content = message.value.trim()
  isSending.value = true

  try {
    emit('send', content)
    message.value = ''
    adjustHeight()
  } finally {
    isSending.value = false
  }
}

const adjustHeight = () => {
  const textarea = textareaRef.value
  if (!textarea) return

  textarea.style.height = 'auto'
  const newHeight = Math.min(textarea.scrollHeight, 150)
  textarea.style.height = `${newHeight}px`
}
</script>
