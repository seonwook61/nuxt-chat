import type { Message } from '~/types/chat'

const API_BASE_URL = 'http://localhost:8080'

export const useMessageHistory = () => {
  /**
   * Fetch message history for a room from the backend API
   */
  const fetchMessageHistory = async (roomId: string, limit: number = 50): Promise<Message[]> => {
    try {
      const response = await fetch(
        `${API_BASE_URL}/api/messages/history/${roomId}?limit=${limit}`
      )

      if (!response.ok) {
        throw new Error(`Failed to fetch message history: ${response.statusText}`)
      }

      const data = await response.json()

      // Transform backend response to frontend Message format
      const messages: Message[] = data.map((msg: any) => {
        // Parse timestamp string to Date
        const timestamp = new Date(msg.timestamp)

        return {
          messageId: msg.messageId,
          roomId: msg.roomId,
          userId: msg.userId,
          username: msg.username,
          content: msg.content,
          timestamp: timestamp.toISOString(),
          type: msg.type || 'TEXT',
          reactions: {},
          // UI-specific fields (will be calculated by MessageList)
          isOwn: false,
          showAvatar: false,
          showUsername: false,
          showTimestamp: false
        }
      })

      // Backend returns messages in descending order (newest first)
      // Reverse to get ascending order (oldest first) for display
      return messages.reverse()
    } catch (error) {
      console.error('Error fetching message history:', error)
      return []
    }
  }

  return {
    fetchMessageHistory
  }
}
