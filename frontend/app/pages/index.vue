<template>
  <div class="container mx-auto px-4 py-8">
    <div class="max-w-2xl mx-auto">
      <h1 class="text-4xl font-bold text-gray-900 mb-8 text-center">
        Chat Application
      </h1>

      <div class="bg-white rounded-lg shadow-md p-6">
        <h2 class="text-2xl font-semibold text-gray-800 mb-4">
          Join a Chat Room
        </h2>

        <div class="space-y-4">
          <div>
            <label for="roomId" class="block text-sm font-medium text-gray-700 mb-2">
              Room ID
            </label>
            <input
              id="roomId"
              v-model="roomId"
              type="text"
              placeholder="Enter room ID"
              class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              @keyup.enter="joinRoom"
            >
          </div>

          <button
            @click="joinRoom"
            :disabled="!roomId.trim()"
            class="w-full bg-primary-600 text-white py-2 px-4 rounded-md hover:bg-primary-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
          >
            Join Room
          </button>
        </div>

        <div class="mt-6 pt-6 border-t border-gray-200">
          <h3 class="text-sm font-medium text-gray-700 mb-3">
            Quick Join
          </h3>
          <div class="grid grid-cols-2 gap-3">
            <button
              v-for="room in quickRooms"
              :key="room"
              @click="joinQuickRoom(room)"
              class="px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-50 transition-colors"
            >
              Room {{ room }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const router = useRouter()
const roomId = ref('')
const quickRooms = ['general', 'random', 'tech', 'gaming']

const joinRoom = () => {
  const id = roomId.value.trim()
  if (id) {
    router.push(`/rooms/${id}`)
  }
}

const joinQuickRoom = (room: string) => {
  router.push(`/rooms/${room}`)
}
</script>
