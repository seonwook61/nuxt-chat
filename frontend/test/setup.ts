// Vitest setup file
import { vi } from 'vitest'
import { ref, computed, reactive } from 'vue'

// Global Vue APIs
global.ref = ref
global.computed = computed
global.reactive = reactive

// Mock Nuxt auto-imports
global.defineNuxtPlugin = vi.fn((fn) => fn)
global.defineNuxtConfig = vi.fn((config) => config)
global.useRoute = vi.fn()
global.useRouter = vi.fn()
global.useRuntimeConfig = vi.fn()

// Mock useChatStore - will be overridden in tests that use Pinia
global.useChatStore = vi.fn(() => ({
  rooms: ref(new Map()),
  currentRoomId: ref(null),
  connected: ref(false),
  currentRoom: computed(() => null),
  currentMessages: computed(() => []),
  setCurrentRoom: vi.fn(),
  addMessage: vi.fn(),
  clearRoom: vi.fn(),
  setConnected: vi.fn()
}))
