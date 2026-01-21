import type { EmojiType, MessageReaction } from '~/types/chat'

/**
 * Composable for managing message reactions (Instagram-style)
 */
export function useReactions() {
  const socket = useSocket()

  /**
   * Add a reaction to a message
   */
  const addReaction = (
    messageId: string,
    roomId: string,
    emoji: EmojiType,
    userId: string,
    username: string
  ) => {
    if (!socket.connected.value) {
      console.warn('Cannot add reaction: WebSocket not connected')
      return
    }

    // Format timestamp as LocalDateTime (yyyy-MM-dd'T'HH:mm:ss) without 'Z'
    const now = new Date()
    const timestamp = now.toISOString().substring(0, 19)

    const reaction: MessageReaction = {
      reactionId: crypto.randomUUID(),
      messageId,
      roomId,
      userId,
      username,
      emoji,
      timestamp,
      action: 'ADD'
    }

    console.log('[useReactions] Adding reaction:', reaction)

    // Send reaction via WebSocket
    socket.send('/app/chat.reaction', reaction)
  }

  /**
   * Remove a reaction from a message
   */
  const removeReaction = (
    messageId: string,
    roomId: string,
    emoji: EmojiType,
    userId: string,
    username: string
  ) => {
    if (!socket.connected.value) {
      console.warn('Cannot remove reaction: WebSocket not connected')
      return
    }

    // Format timestamp as LocalDateTime (yyyy-MM-dd'T'HH:mm:ss) without 'Z'
    const now = new Date()
    const timestamp = now.toISOString().substring(0, 19)

    const reaction: MessageReaction = {
      reactionId: crypto.randomUUID(),
      messageId,
      roomId,
      userId,
      username,
      emoji,
      timestamp,
      action: 'REMOVE'
    }

    console.log('[useReactions] Removing reaction:', reaction)

    // Send reaction via WebSocket
    socket.send('/app/chat.reaction', reaction)
  }

  /**
   * Toggle a reaction (add if not present, remove if present)
   */
  const toggleReaction = (
    messageId: string,
    roomId: string,
    emoji: EmojiType,
    userId: string,
    username: string,
    currentReactions?: Record<string, string[]>
  ) => {
    const hasReacted = currentReactions?.[emoji]?.includes(userId) || false

    if (hasReacted) {
      removeReaction(messageId, roomId, emoji, userId, username)
    } else {
      addReaction(messageId, roomId, emoji, userId, username)
    }
  }

  /**
   * Check if current user has reacted with a specific emoji
   */
  const hasUserReacted = (
    emoji: EmojiType,
    userId: string,
    reactions?: Record<string, string[]>
  ): boolean => {
    return reactions?.[emoji]?.includes(userId) || false
  }

  /**
   * Get count for a specific emoji
   */
  const getReactionCount = (
    emoji: EmojiType,
    reactions?: Record<string, string[]>
  ): number => {
    return reactions?.[emoji]?.length || 0
  }

  /**
   * Get total reaction count
   */
  const getTotalReactionCount = (reactions?: Record<string, string[]>): number => {
    if (!reactions) return 0
    return Object.values(reactions).reduce((sum, users) => sum + users.length, 0)
  }

  /**
   * Get list of users who reacted with a specific emoji
   */
  const getUsersForEmoji = (
    emoji: EmojiType,
    reactions?: Record<string, string[]>
  ): string[] => {
    return reactions?.[emoji] || []
  }

  return {
    addReaction,
    removeReaction,
    toggleReaction,
    hasUserReacted,
    getReactionCount,
    getTotalReactionCount,
    getUsersForEmoji,
    isConnected: socket.connected
  }
}
