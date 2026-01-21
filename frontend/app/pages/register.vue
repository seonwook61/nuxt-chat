<template>
  <div class="register-container">
    <div class="register-box">
      <div class="header">
        <div class="icon">✍️</div>
        <h1>회원가입</h1>
        <p class="subtitle">새 계정을 만들어보세요</p>
      </div>

      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label for="userid">아이디</label>
          <input
            type="text"
            id="userid"
            v-model="formData.userid"
            placeholder="아이디 (4-20자)"
            required
            minlength="4"
            maxlength="20"
          />
        </div>

        <div class="form-group">
          <label for="username">닉네임</label>
          <input
            type="text"
            id="username"
            v-model="formData.username"
            placeholder="닉네임 (2-20자)"
            required
            minlength="2"
            maxlength="20"
          />
        </div>

        <div class="form-group">
          <label for="password">비밀번호</label>
          <input
            type="password"
            id="password"
            v-model="formData.password"
            placeholder="비밀번호 (8자 이상)"
            required
            minlength="8"
          />
        </div>

        <div class="form-group">
          <label for="passwordConfirm">비밀번호 확인</label>
          <input
            type="password"
            id="passwordConfirm"
            v-model="formData.passwordConfirm"
            placeholder="비밀번호 확인"
            required
          />
        </div>

        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <button type="submit" class="register-btn" :disabled="isLoading">
          {{ isLoading ? '가입 중...' : '회원가입' }}
        </button>
      </form>

      <div class="login-link">
        <p>이미 계정이 있으신가요?</p>
        <button @click="goToLogin" class="login-link-btn">
          로그인
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const router = useRouter()

const formData = ref({
  userid: '',
  username: '',
  password: '',
  passwordConfirm: ''
})

const isLoading = ref(false)
const errorMessage = ref('')

const handleRegister = async () => {
  if (isLoading.value) return

  // Validation
  if (formData.value.password !== formData.value.passwordConfirm) {
    errorMessage.value = '비밀번호가 일치하지 않습니다.'
    return
  }

  if (formData.value.password.length < 8) {
    errorMessage.value = '비밀번호는 8자 이상이어야 합니다.'
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    // TODO: 실제 API 연동 필요
    // const response = await $fetch('/api/auth/register', {
    //   method: 'POST',
    //   body: {
    //     userid: formData.value.userid,
    //     username: formData.value.username,
    //     password: formData.value.password
    //   }
    // })

    // 임시: 가입 성공으로 처리
    alert('회원가입이 완료되었습니다!')
    router.push('/login')
  } catch (error) {
    console.error('Register error:', error)
    errorMessage.value = '회원가입 중 오류가 발생했습니다.'
  } finally {
    isLoading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-box {
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

.register-btn {
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

.register-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

.register-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  box-shadow: none;
}

.login-link {
  margin-top: 30px;
  text-align: center;
}

.login-link p {
  color: #666;
  margin-bottom: 10px;
  font-size: 14px;
}

.login-link-btn {
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

.login-link-btn:hover {
  background-color: #e0e0e0;
  border-color: #ccc;
  transform: translateY(-1px);
}

@media (max-width: 640px) {
  .register-box {
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
