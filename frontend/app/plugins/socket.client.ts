export default defineNuxtPlugin(() => {
  if (!import.meta.client) {
    return
  }

  const socket = useSocket()

  // Connect on plugin initialization
  socket.connect()

  // Disconnect on page unload
  if (typeof window !== 'undefined') {
    window.addEventListener('beforeunload', () => {
      socket.disconnect()
    })
  }

  console.log('[Plugin] Socket plugin initialized')

  return {
    provide: {
      socket
    }
  }
})
