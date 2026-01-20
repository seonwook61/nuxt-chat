<template>
  <div :class="['relative', sizeClasses]">
    <!-- Avatar Circle -->
    <div
      v-if="user.avatar"
      class="w-full h-full rounded-full overflow-hidden ring-2 ring-white shadow-sm"
    >
      <img
        :src="user.avatar"
        :alt="user.username"
        class="w-full h-full object-cover"
      />
    </div>

    <!-- Initial Avatar (no image) -->
    <div
      v-else
      :class="[
        'w-full h-full rounded-full flex items-center justify-center',
        'ring-2 ring-white shadow-sm text-white font-semibold',
        avatarBgColor
      ]"
    >
      {{ getInitials(user.username) }}
    </div>

    <!-- Online Badge (추후 확장) -->
    <div
      v-if="user.isOnline && showOnlineBadge"
      class="absolute -bottom-0.5 -right-0.5 w-3 h-3 bg-green-500 rounded-full border-2 border-white"
    />
  </div>
</template>

<script setup lang="ts">
import type { UserProfile } from '~/types/chat'

interface Props {
  user: UserProfile
  size?: 'sm' | 'md' | 'lg'
  showOnlineBadge?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  size: 'md',
  showOnlineBadge: false
})

const sizeClasses = computed(() => {
  switch (props.size) {
    case 'sm': return 'w-6 h-6 text-xs'
    case 'md': return 'w-8 h-8 text-sm'
    case 'lg': return 'w-12 h-12 text-base'
    default: return 'w-8 h-8 text-sm'
  }
})

const avatarBgColor = computed(() => {
  const colors = [
    'bg-blue-500', 'bg-green-500', 'bg-purple-500',
    'bg-pink-500', 'bg-yellow-500', 'bg-red-500'
  ]
  const index = props.user.userId.charCodeAt(0) % colors.length
  return colors[index]
})

const getInitials = (username: string) => {
  return username.charAt(0).toUpperCase()
}
</script>
