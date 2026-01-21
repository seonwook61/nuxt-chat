<template>
  <div class="h-screen flex flex-col bg-gray-100">
    <!-- Header -->
    <header class="bg-white shadow-sm border-b border-gray-200">
      <div class="container mx-auto px-4 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-3">
            <button
              @click="router.push('/')"
              class="p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors"
              title="홈으로 돌아가기"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
              </svg>
            </button>
            <div>
              <h1 class="text-xl font-bold text-gray-900">
                {{ roomId }}
              </h1>
              <p class="text-xs text-gray-500">채팅방</p>
            </div>
          </div>

          <div class="flex items-center space-x-4">
            <span class="text-sm text-gray-600">
              Online: <span class="font-semibold">0</span>
            </span>
          </div>
        </div>
      </div>
    </header>

    <!-- Chat Window -->
    <div class="flex-1 container mx-auto px-4 py-6 overflow-hidden">
      <ClientOnly>
        <ChatWindow :room-id="roomId" />
      </ClientOnly>
    </div>
  </div>
</template>

<script setup lang="ts">
const route = useRoute()
const router = useRouter()
const roomId = computed(() => route.params.roomId as string)

onMounted(() => {
  // Socket connection logic will be added in Phase 2
  console.log('Joined room:', roomId.value)
})

onUnmounted(() => {
  // Socket disconnect logic will be added in Phase 2
  console.log('Left room:', roomId.value)
})
</script>
