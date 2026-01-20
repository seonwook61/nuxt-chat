// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2024-01-01',
  devtools: { enabled: true },

  srcDir: 'app/',

  modules: [
    '@nuxtjs/tailwindcss',
    '@pinia/nuxt',
  ],

  css: [
    '~/assets/css/main.css'
  ],

  runtimeConfig: {
    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:8080',
      wsUrl: process.env.NUXT_PUBLIC_WS_URL || 'http://localhost:8080/ws',
    }
  },

  typescript: {
    strict: true,
    typeCheck: false,
  },

  app: {
    head: {
      title: 'Chat Application',
      meta: [
        { charset: 'utf-8' },
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
        { name: 'description', content: 'Real-time chat application supporting 1000+ concurrent users' }
      ],
    }
  },

  vite: {
    server: {
      hmr: {
        protocol: 'ws',
        host: 'localhost',
      }
    }
  },
})
