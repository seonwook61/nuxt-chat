<template>
  <div class="login-container">
    <div class="login-box">
      <div class="header">
        <div class="icon">💬</div>
        <h1>실시간 채팅</h1>
        <p class="subtitle">로그인하여 채팅을 시작하세요</p>
      </div>

      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="userid">아이디</label>
          <input
            type="text"
            id="userid"
            v-model="formData.userid"
            placeholder="아이디를 입력하세요"
            required
          />
        </div>

        <div class="form-group">
          <label for="password">비밀번호</label>
          <input
            type="password"
            id="password"
            v-model="formData.password"
            placeholder="비밀번호를 입력하세요"
            required
          />
        </div>

        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <button type="submit" class="login-btn" :disabled="isLoading">
          {{ isLoading ? '로그인 중...' : '로그인' }}
        </button>
      </form>

      <div class="social-login">
        <p>소셜 계정으로 로그인</p>
        <a href="/api/auth/google" class="google-btn">
          <span class="google-icon">G</span>
          구글 로그인
        </a>
        <a href="/api/auth/naver" class="naver-btn">
          <span class="naver-icon">N</span>
          네이버 로그인
        </a>
      </div>

      <div class="register-link">
        <p>아직 회원이 아니신가요?</p>
        <button @click="goToRegister" class="register-btn">
          회원가입
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const router = useRouter()
const chatStore = useChatStore()

const formData = ref({
  userid: '',
  password: ''
})

const isLoading = ref(false)
const errorMessage = ref('')

const handleLogin = async () => {
  if (isLoading.value) return

  isLoading.value = true
  errorMessage.value = ''

  try {
    // TODO: 실제 API 연동 필요
    // const response = await $fetch('/api/auth/login', {
    //   method: 'POST',
    //   body: formData.value
    // })

    // 임시: 로컬 개발용 (아무 ID/PW로 로그인 가능)
    const username = formData.value.userid
    const userId = 'user-' + Date.now() + '-' + Math.random().toString(36).substring(7)

    // Save to store
    chatStore.setCurrentUser(userId, username)

    // Redirect to lobby
    router.push('/')
  } catch (error) {
    console.error('Login error:', error)
    errorMessage.value = '로그인 중 오류가 발생했습니다.'
  } finally {
    isLoading.value = false
  }
}

const goToRegister = () => {
  router.push('/register')
}

// Check if already logged in
onMounted(() => {
  if (chatStore.currentUserId && chatStore.currentUsername) {
    router.push('/')
  }
})
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-box {
  background: white;
  padding: 40px;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  width: 100%;
  max-width: 420px;
}

.header {
  text-align: center;
  margin-bottom: 32px;
}

.icon {
  font-size: 64px;
  margin-bottom: 16px;
}

h1 {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.subtitle {
  color: #666;
  font-size: 14px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 600;
  font-size: 14px;
}

.form-group input {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 15px;
  box-sizing: border-box;
  transition: all 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.error-message {
  padding: 12px;
  background-color: #fee;
  color: #c62828;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 14px;
  text-align: center;
  border-left: 4px solid #c62828;
}

.login-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

.login-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  box-shadow: none;
}

.social-login {
  margin-top: 30px;
  padding-top: 24px;
  border-top: 1px solid #eee;
}

.social-login p {
  color: #666;
  margin-bottom: 15px;
  font-size: 14px;
  font-weight: 600;
  text-align: center;
}

.google-btn,
.naver-btn {
  width: 100%;
  padding: 14px;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: all 0.3s;
  text-decoration: none;
  box-sizing: border-box;
  margin-bottom: 10px;
}

.google-btn {
  background-color: #4285F4;
  color: white;
}

.google-btn:hover {
  background-color: #3367D6;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(66, 133, 244, 0.4);
}

.google-icon {
  width: 22px;
  height: 22px;
  background-color: white;
  color: #4285F4;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
}

.naver-btn {
  background-color: #03C75A;
  color: white;
}

.naver-btn:hover {
  background-color: #02b351;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(3, 199, 90, 0.4);
}

.naver-icon {
  width: 22px;
  height: 22px;
  background-color: white;
  color: #03C75A;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
}

.register-link {
  margin-top: 30px;
  text-align: center;
}

.register-link p {
  color: #666;
  margin-bottom: 10px;
  font-size: 14px;
}

.register-btn {
  padding: 10px 24px;
  background-color: #f5f5f5;
  color: #333;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s;
}

.register-btn:hover {
  background-color: #e0e0e0;
  border-color: #ccc;
  transform: translateY(-1px);
}

@media (max-width: 640px) {
  .login-box {
    padding: 24px;
  }

  .icon {
    font-size: 48px;
  }

  h1 {
    font-size: 24px;
  }
}
</style>
