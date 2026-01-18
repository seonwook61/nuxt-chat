// Vitest setup file
import { vi } from 'vitest'

// Mock Nuxt auto-imports
global.defineNuxtPlugin = vi.fn((fn) => fn)
global.defineNuxtConfig = vi.fn((config) => config)
global.useRoute = vi.fn()
global.useRouter = vi.fn()
global.useRuntimeConfig = vi.fn()
global.useChatStore = vi.fn()
