import { Client, type StompSubscription } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

// Singleton STOMP client instance
let stompClient: Client | null = null
const subscriptions = new Map<string, StompSubscription>()

// For testing: allow reset
export function __resetStompClient() {
  if (stompClient?.connected) {
    stompClient.deactivate()
  }
  stompClient = null
  subscriptions.clear()
}

export const useSocket = () => {
  const connected = ref(false)
  const connecting = ref(false)

  const config = useRuntimeConfig()
  const wsUrl = config.public.wsUrl || 'http://localhost:8080/ws'

  const connect = () => {
    if (stompClient?.connected || connecting.value) {
      return
    }

    connecting.value = true

    stompClient = new Client({
      webSocketFactory: () => new SockJS(wsUrl) as any,
      debug: (str) => {
        if (import.meta.dev) {
          console.log('[STOMP]', str)
        }
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        connected.value = true
        connecting.value = false
        console.log('[STOMP] Connected')
      },
      onDisconnect: () => {
        connected.value = false
        connecting.value = false
        console.log('[STOMP] Disconnected')
      },
      onStompError: (frame) => {
        console.error('[STOMP] Error:', frame.headers['message'])
        console.error('[STOMP] Details:', frame.body)
        connected.value = false
        connecting.value = false
      }
    })

    stompClient.activate()
  }

  const disconnect = () => {
    if (!stompClient) return

    // Clear all subscriptions
    subscriptions.forEach((sub) => sub.unsubscribe())
    subscriptions.clear()

    stompClient.deactivate()
    connected.value = false
    connecting.value = false
  }

  const subscribe = (destination: string, callback: (message: any) => void): string => {
    if (!stompClient?.connected) {
      throw new Error('Cannot subscribe: STOMP client not connected')
    }

    const subscription = stompClient.subscribe(destination, (message) => {
      try {
        const payload = JSON.parse(message.body)
        callback(payload)
      } catch (error) {
        console.error('[STOMP] Failed to parse message:', error)
      }
    })

    const subId = `${destination}-${Date.now()}`
    subscriptions.set(subId, subscription)
    return subId
  }

  const unsubscribe = (subscriptionId: string) => {
    const subscription = subscriptions.get(subscriptionId)
    if (subscription) {
      subscription.unsubscribe()
      subscriptions.delete(subscriptionId)
    }
  }

  const send = (destination: string, payload: any) => {
    if (!stompClient?.connected) {
      throw new Error('Cannot send: STOMP client not connected')
    }

    stompClient.publish({
      destination,
      body: JSON.stringify(payload)
    })
  }

  return {
    connected: readonly(connected),
    connecting: readonly(connecting),
    connect,
    disconnect,
    subscribe,
    unsubscribe,
    send
  }
}
