import type { EmojiType } from '~/types/chat'

/**
 * Instagram-style emoji reactions
 * 6 predefined emojis for message reactions
 */
export interface EmojiConfig {
  type: EmojiType
  emoji: string
  label: string
  color: string // Tailwind color class for hover/active state
}

export const REACTION_EMOJIS: EmojiConfig[] = [
  {
    type: 'HEART',
    emoji: '❤️',
    label: 'Love',
    color: 'text-red-500'
  },
  {
    type: 'LAUGH',
    emoji: '😂',
    label: 'Laugh',
    color: 'text-yellow-500'
  },
  {
    type: 'WOW',
    emoji: '😮',
    label: 'Wow',
    color: 'text-blue-500'
  },
  {
    type: 'SAD',
    emoji: '😢',
    label: 'Sad',
    color: 'text-blue-400'
  },
  {
    type: 'THUMBS_UP',
    emoji: '👍',
    label: 'Like',
    color: 'text-blue-600'
  },
  {
    type: 'FIRE',
    emoji: '🔥',
    label: 'Fire',
    color: 'text-orange-500'
  }
]

/**
 * Get emoji character by type
 */
export function getEmojiChar(type: EmojiType): string {
  return REACTION_EMOJIS.find(e => e.type === type)?.emoji || '❤️'
}

/**
 * Get emoji config by type
 */
export function getEmojiConfig(type: EmojiType): EmojiConfig | undefined {
  return REACTION_EMOJIS.find(e => e.type === type)
}
