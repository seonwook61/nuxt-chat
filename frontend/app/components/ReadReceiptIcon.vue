<template>
  <div class="inline-flex items-center gap-0.5 text-xs">
    <!-- Single checkmark (sent) -->
    <svg
      v-if="!isRead"
      class="w-4 h-4 text-gray-400"
      fill="none"
      stroke="currentColor"
      viewBox="0 0 24 24"
      xmlns="http://www.w3.org/2000/svg"
    >
      <path
        stroke-linecap="round"
        stroke-linejoin="round"
        stroke-width="2"
        d="M5 13l4 4L19 7"
      />
    </svg>

    <!-- Double checkmark (read) -->
    <template v-else>
      <svg
        class="w-4 h-4 text-blue-500"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M5 13l4 4L19 7"
        />
      </svg>
      <svg
        class="w-4 h-4 text-blue-500 -ml-2"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M5 13l4 4L19 7"
        />
      </svg>
    </template>

    <!-- Read count (optional) -->
    <span v-if="showReadCount && readCount > 0" class="text-gray-500 ml-1">
      {{ readCount }}
    </span>
  </div>
</template>

<script setup lang="ts">
/**
 * Read Receipt Icon Component (Phase 6)
 *
 * Displays message read status:
 * - Single gray checkmark (✓) - Message sent
 * - Double blue checkmark (✓✓) - Message read
 * - Optional read count display
 *
 * Design matches Instagram/WhatsApp style.
 */

interface Props {
  /** Whether the message has been read by anyone */
  isRead: boolean

  /** Number of users who read the message */
  readCount?: number

  /** Whether to show read count next to checkmarks */
  showReadCount?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  readCount: 0,
  showReadCount: false
})
</script>
