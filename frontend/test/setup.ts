// Vitest setup file
import { vi } from 'vitest'
import { ref, computed, reactive, watch, onMounted, onUnmounted, onBeforeUnmount, nextTick, readonly, isRef, toRef } from 'vue'

// Global Vue APIs
global.ref = ref
global.computed = computed
global.reactive = reactive
global.watch = watch
global.onMounted = onMounted
global.onUnmounted = onUnmounted
global.onBeforeUnmount = onBeforeUnmount
global.nextTick = nextTick
global.readonly = readonly
global.isRef = isRef
global.toRef = toRef

// Mock Nuxt auto-imports
global.defineNuxtPlugin = vi.fn((fn) => fn)
global.defineNuxtConfig = vi.fn((config) => config)
global.useRoute = vi.fn()
global.useRouter = vi.fn()
global.useRuntimeConfig = vi.fn(() => ({
  public: {
    apiBase: 'http://localhost:8080',
    wsUrl: 'http://localhost:8080/ws'
  }
}))

// Mock useChatStore - will be overridden in tests that use Pinia
global.useChatStore = vi.fn(() => ({
  rooms: ref(new Map()),
  currentRoomId: ref(null),
  connected: ref(false),
  currentUserId: ref(null),
  currentUsername: ref(null),
  currentRoom: computed(() => null),
  currentMessages: computed(() => []),
  setCurrentRoom: vi.fn(),
  addMessage: vi.fn(),
  clearRoom: vi.fn(),
  setConnected: vi.fn(),
  setCurrentUser: vi.fn()
}))
