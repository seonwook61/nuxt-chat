// Emoji reaction types (Instagram-style)
export type EmojiType = 'HEART' | 'LAUGH' | 'WOW' | 'SAD' | 'THUMBS_UP' | 'FIRE'

// Reaction summary for a message
export interface ReactionSummary {
  reactions: Record<EmojiType, string[]> // emoji -> array of userIds
}

// Message reaction event
export interface MessageReaction {
  reactionId: string
  messageId: string
  roomId: string
  userId: string
  username: string
  emoji: EmojiType
  timestamp: string
  action: 'ADD' | 'REMOVE'
}

// Chat message types (Backend DTO format)
export interface Message {
  messageId: string
  roomId: string
  userId: string
  username: string
  userAvatar?: string          // NEW: 프로필 이미지 URL
  content: string
  timestamp: string // ISO 8601 format from backend
  type: 'CHAT' | 'TEXT'
  reactions?: Record<string, string[]> // NEW: emoji -> array of userIds

  // NEW: UI 표시용 (computed)
  isOwn?: boolean              // 본인 메시지 여부
  showAvatar?: boolean         // 아바타 표시 여부 (연속 메시지)
  showTimestamp?: boolean      // 타임스탬프 표시 여부
  showUsername?: boolean       // 사용자명 표시 여부
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
  messageId: string
  roomId: string
  userId: string
  username: string
  content: string
  timestamp: string
  type: 'TEXT' | 'CHAT'
}

export interface JoinRoomPayload {
  eventId: string
  eventType: 'USER_JOINED'
  roomId: string
  userId: string
  username: string
  timestamp: string
  metadata: Record<string, any>
}

export interface LeaveRoomPayload {
  eventId: string
  eventType: 'USER_LEFT'
  roomId: string
  userId: string
  timestamp: string
  metadata: Record<string, any>
}

export interface ReactionPayload {
  reactionId: string
  messageId: string
  roomId: string
  userId: string
  username: string
  emoji: EmojiType
  timestamp: string
  action: 'ADD' | 'REMOVE'
}

// NEW: 사용자 프로필 인터페이스
export interface UserProfile {
  userId: string
  username: string
  avatar?: string              // 프로필 이미지 URL
  avatarColor?: string         // 기본 아바타 배경색
  isOnline?: boolean
}

// Chat room state
export interface ChatRoom {
  id: string
  name: string
  onlineUsers: number
  messages: Message[]
}
