// Chat message types
export interface Message {
  id: string
  roomId: string
  userId: string
  content: string
  timestamp: number
}

// Socket event types
export interface ServerToClientEvents {
  message: (message: Message) => void
  system: (data: SystemMessage) => void
  error: (error: ErrorMessage) => void
  userJoined: (data: UserEvent) => void
  userLeft: (data: UserEvent) => void
}

export interface ClientToServerEvents {
  join_room: (roomId: string) => void
  leave_room: (roomId: string) => void
  send_message: (data: SendMessagePayload) => void
}

export interface SendMessagePayload {
  roomId: string
  content: string
}

export interface SystemMessage {
  type: 'info' | 'warning' | 'error'
  message: string
  timestamp: number
}

export interface ErrorMessage {
  code: string
  message: string
  timestamp: number
}

export interface UserEvent {
  userId: string
  roomId: string
  timestamp: number
}

// Chat room state
export interface ChatRoom {
  id: string
  name: string
  onlineUsers: number
  messages: Message[]
}
