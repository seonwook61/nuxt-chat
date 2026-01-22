<template>
  <transition name="slide-up">
    <div v-if="isTyping" class="typing-indicator">
      <div class="typing-dots">
        <span class="dot"></span>
        <span class="dot"></span>
        <span class="dot"></span>
      </div>
      <span class="typing-text">{{ typingText }}</span>
    </div>
  </transition>
</template>

<script setup lang="ts">
/**
 * Typing Indicator Component (Phase 5)
 *
 * Displays typing status with animated dots:
 * - "Alice님이 입력 중..."
 * - "Alice, Bob님이 입력 중..."
 * - "Alice 외 2명이 입력 중..."
 *
 * Features:
 * - Animated 3-dot indicator
 * - Smooth slide-up transition
 * - Auto-hide when no one is typing
 */

interface Props {
  typingText: string
  isTyping: boolean
}

const props = defineProps<Props>()
</script>

<style scoped>
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(0, 0, 0, 0.03);
  border-radius: 16px;
  margin: 0 16px 8px 16px;
  height: 32px;
}

.typing-dots {
  display: flex;
  align-items: center;
  gap: 4px;
}

.dot {
  width: 6px;
  height: 6px;
  background: #999;
  border-radius: 50%;
  animation: typing-bounce 1.4s infinite ease-in-out;
}

.dot:nth-child(1) {
  animation-delay: 0s;
}

.dot:nth-child(2) {
  animation-delay: 0.2s;
}

.dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing-bounce {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.5;
  }
  30% {
    transform: translateY(-8px);
    opacity: 1;
  }
}

.typing-text {
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

/* Slide-up transition */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
