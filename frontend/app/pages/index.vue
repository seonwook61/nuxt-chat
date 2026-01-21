<template>
  <div class="lobby-container">
    <div class="lobby-box">
      <div class="header">
        <div class="user-info">
          <div class="avatar">{{ userInitial }}</div>
          <div class="user-details">
            <h2>{{ chatStore.currentUsername }}</h2>
            <p>{{ chatStore.currentUserId }}</p>
          </div>
        </div>
        <button @click="handleLogout" class="logout-btn">로그아웃</button>
      </div>

      <div class="content">
        <h1>채팅방 선택</h1>
        <p class="subtitle">입장하실 채팅방을 선택하세요</p>

        <div class="custom-room">
          <input
            type="text"
            v-model="customRoomId"
            placeholder="채팅방 이름을 입력하세요"
            @keyup.enter="joinCustomRoom"
            maxlength="30"
          />
          <button @click="joinCustomRoom" :disabled="!customRoomId.trim()" class="join-custom-btn">
            입장
          </button>
        </div>

        <div class="quick-join">
          <p>인기 채팅방</p>
          <div class="room-grid">
            <button
              v-for="room in quickRooms"
              :key="room.id"
              @click="joinRoom(room.id)"
              class="room-btn"
            >
              <span class="room-icon">{{ room.icon }}</span>
              <div class="room-info">
                <span class="room-name">{{ room.name }}</span>
                <span class="room-desc">{{ room.description }}</span>
              </div>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const router = useRouter()
const chatStore = useChatStore()

const customRoomId = ref('')

const quickRooms = [
  { id: 'general', name: '일반', icon: '💬', description: '자유롭게 대화해요' },
  { id: 'random', name: '랜덤', icon: '🎲', description: '무작위 주제로 수다' },
  { id: 'tech', name: '기술', icon: '💻', description: '개발과 기술 이야기' },
  { id: 'gaming', name: '게임', icon: '🎮', description: '게임 얘기하실 분?' }
]

const userInitial = computed(() => {
  return chatStore.currentUsername?.charAt(0).toUpperCase() || '?'
})

const joinRoom = (roomId: string) => {
  router.push(`/rooms/${roomId}`)
}

const joinCustomRoom = () => {
  const roomId = customRoomId.value.trim()
  if (roomId) {
    router.push(`/rooms/${roomId}`)
  }
}

const handleLogout = () => {
  chatStore.setCurrentUser('', '')
  router.push('/login')
}

// Redirect to login if not authenticated
onMounted(() => {
  if (!chatStore.currentUserId || !chatStore.currentUsername) {
    router.push('/login')
  }
})
</script>

<style scoped>
.lobby-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.lobby-box {
  max-width: 800px;
  margin: 0 auto;
}

.header {
  background: white;
  padding: 20px 24px;
  border-radius: 12px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
}

.user-details h2 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 4px 0;
}

.user-details p {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.logout-btn {
  padding: 8px 16px;
  background-color: #f5f5f5;
  color: #666;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s;
}

.logout-btn:hover {
  background-color: #e0e0e0;
  color: #333;
}

.content {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

h1 {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin: 0 0 8px 0;
  text-align: center;
}

.subtitle {
  text-align: center;
  color: #666;
  font-size: 14px;
  margin-bottom: 32px;
}

.custom-room {
  display: flex;
  gap: 12px;
  margin-bottom: 32px;
}

.custom-room input {
  flex: 1;
  padding: 14px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 15px;
  transition: all 0.3s;
}

.custom-room input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.join-custom-btn {
  padding: 14px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
}

.join-custom-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.join-custom-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.quick-join > p {
  color: #666;
  margin-bottom: 16px;
  font-size: 14px;
  font-weight: 600;
  text-align: center;
}

.room-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.room-btn {
  padding: 20px;
  background-color: #f8f9fa;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 16px;
  text-align: left;
}

.room-btn:hover {
  background-color: #667eea;
  border-color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.room-btn:hover .room-name,
.room-btn:hover .room-desc {
  color: white;
}

.room-icon {
  font-size: 36px;
  flex-shrink: 0;
}

.room-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.room-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  transition: color 0.3s;
}

.room-desc {
  font-size: 12px;
  color: #999;
  transition: color 0.3s;
}

@media (max-width: 768px) {
  .content {
    padding: 24px;
  }

  .room-grid {
    grid-template-columns: 1fr;
  }

  .header {
    flex-direction: column;
    gap: 16px;
  }

  .logout-btn {
    width: 100%;
  }
}
</style>
