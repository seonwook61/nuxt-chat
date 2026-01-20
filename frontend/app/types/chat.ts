// Chat message types (Backend DTO format)
export interface Message {
  messageId: string
  roomId: string
  userId: string
  username: string
  content: string
  timestamp: string // ISO 8601 format from backend
  type: 'CHAT'
}

// Chat event types (Backend DTO format)
export interface ChatEvent {
  eventId: string
  roomId: string
  userId: string
  username: string
  eventType: 'USER_JOINED' | 'USER_LEFT'
  timestamp: string // ISO 8601 format
  metadata?: Record<string, any>
}

// STOMP message wrapper
export interface StompMessage {
  body: string
  headers: Record<string, string>
}

// Chat event payloads for STOMP
export interface ChatEventPayload {
  roomId: string
  userId: string
  username: string
  content?: string
}

export interface SendMessagePayload {
  roomId: string
  userId: string
  username: string
  content: string
}

export interface JoinRoomPayload {
  roomId: string
  userId: string
  username: string
}

export interface LeaveRoomPayload {
  roomId: string
  userId: string
}

// Chat room state
export interface ChatRoom {
  id: string
  name: string
  onlineUsers: number
  messages: Message[]
}
