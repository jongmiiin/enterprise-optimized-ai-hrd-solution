<template>
  <div class="login-page">
    <div class="login-layout">
      <!-- 좌측 브랜딩 -->
      <div class="login-left">
        <div class="brand">
          <img src="@/assets/images/logo/main_logo.png" alt="SkillFit AI" class="brand-logo" />
          <span class="brand-name">SkillFit</span>
        </div>
        <div class="brand-content">
          <span class="brand-eyebrow">PERSONALIZED LEARNING</span>
          <h2>우리 조직에 필요한 AI 역량,<br>맞춤 교육으로 연결하세요.</h2>
          <p>직원에게는 필요한 교육을, HR에게는 더 정확한 교육 투자를 제공합니다.</p>
          <ul class="feature-list">
            <li v-for="f in features" :key="f">
              <span class="check" aria-hidden="true">✓</span>{{ f }}
            </li>
          </ul>
        </div>
      </div>

      <!-- 우측 -->
      <div class="login-right">
        <div class="login-box fade-in-up">
          <router-link to="/" class="back-link">← 홈으로</router-link>

          <!-- 로그인 영역 -->
          <div v-if="!showRegister" class="section">
            <div class="welcome-heading">
              <h1 class="section-title">다시 오신 것을 환영해요</h1>
            </div>
            <p class="section-desc">회사에서 제공한 계정으로 로그인해 주세요.</p>
            <button type="button" class="oauth-button" @click="handleOAuth">계정으로 로그인 <span aria-hidden="true">→</span></button>
            <div class="switch-link">
              계정이 없으신가요?
              <button class="text-btn" @click="setAuthMode('register')">회원가입</button>
            </div>
          </div>

          <!-- 회원가입 영역 -->
          <div v-else class="section">
            <h3 class="section-title">회원가입</h3>
            <p class="register-desc">SkillFit AI를 시작할 계정 정보를 입력해 주세요.</p>
            <form @submit.prevent="handleRegister" class="form">
              <div class="form-group">
                <label class="form-label">이름</label>
                <input v-model="registerForm.name" type="text" class="form-input" placeholder="홍길동" required />
              </div>
              <div class="form-group">
                <label class="form-label">이메일</label>
                <input v-model="registerForm.email" type="email" class="form-input" placeholder="user@example.com" required />
              </div>
              <div class="form-group">
                <label class="form-label">비밀번호</label>
                <input v-model="registerForm.password" type="password" class="form-input" placeholder="8자 이상" required />
              </div>
              <div class="form-group">
                <label class="form-label">역할</label>
                <select v-model="registerForm.role" class="form-input">
                  <option value="STUDENT">직원</option>
                  <option value="INSTRUCTOR">HR 담당자</option>
                </select>
              </div>
              <div v-if="error" class="error-msg">{{ error }}</div>
              <div v-if="success" class="success-msg">{{ success }}</div>
              <button type="submit" class="oauth-button register-button" :disabled="loading">
                <span v-if="loading">가입 중...</span>
                <span v-else>회원가입</span>
              </button>
            </form>
            <div class="switch-link">
              이미 계정이 있으신가요?
              <button class="text-btn" @click="setAuthMode('login')">로그인</button>
            </div>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/store/auth.js'
import { authApi } from '@/api/auth.js'

const auth = useAuthStore()

const showRegister = ref(window.location.hash === '#register')
const loading = ref(false)
const error = ref('')
const success = ref('')

const registerForm = ref({ name: '', email: '', password: '', role: 'STUDENT' })
const features = ['직무와 역량 기반 AI 교육 추천', '직원 신청부터 HR 승인·결제까지', '승인된 교육을 내 강의 목록에서 확인']

function handleOAuth() {
  auth.redirectToLogin()
}

function setAuthMode(mode) {
  showRegister.value = mode === 'register'
  window.history.replaceState(null, '', mode === 'register' ? '#register' : window.location.pathname)
  error.value = ''
  success.value = ''
}

async function handleRegister() {
  error.value = ''
  success.value = ''
  loading.value = true
  try {
    await authApi.register(registerForm.value)
    success.value = '회원가입 완료! 로그인 페이지로 이동합니다.'
    registerForm.value = { name: '', email: '', password: '', role: 'STUDENT' }
    setTimeout(() => {
      setAuthMode('login')
    }, 2000)
  } catch (e) {
    error.value = e.response?.data?.message || '회원가입에 실패했습니다.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: stretch;
  width: 100%;
  background: #fff;
}
.login-layout {
  display: grid;
  grid-template-columns: minmax(0, 55fr) minmax(0, 45fr);
  width: 100%;
  min-height: 100vh;
  overflow: hidden;
  background: #fff;
}
.login-left {
  position: relative;
  overflow: hidden;
  background: linear-gradient(145deg, #16132d 0%, #282054 55%, #35266d 100%);
  padding: clamp(48px, 5vw, 76px);
  display: flex;
  flex-direction: column;
  gap: 0;
}
.login-left::before,
.login-left::after {
  content: '';
  position: absolute;
  width: 330px;
  height: 330px;
  border: 1px solid rgba(159, 139, 255, 0.18);
  border-radius: 50%;
}
.login-left::before { top: -190px; right: -165px; }
.login-left::after { bottom: -205px; left: -190px; }
.brand, .brand-content { position: relative; z-index: 1; }
.brand-content { width: 100%; max-width: 720px; margin-top: clamp(100px, 16vh, 168px); }
.brand { display: flex; align-items: center; gap: 10px; }
.brand-logo { width: 46px; height: 46px; border-radius: 12px; object-fit: contain; }
.brand-name { color: #fff; font-size: 21px; font-weight: 700; letter-spacing: -0.3px; }
.brand-eyebrow {
  display: block;
  margin-bottom: 24px;
  color: #b9aaff;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 1.8px;
}
.brand-content h2 {
  max-width: none;
  font-size: clamp(42px, 3.4vw, 56px);
  font-weight: 700;
  color: #fff;
  line-height: 1.3;
  letter-spacing: -1.2px;
  margin-bottom: 22px;
  white-space: nowrap;
}
.brand-content p { max-width: 680px; font-size: 18px; line-height: 1.75; color: rgba(255,255,255,0.68); margin-bottom: 40px; }
.feature-list { list-style: none; display: flex; flex-direction: column; gap: 17px; padding: 0; margin: 0; }
.feature-list li { display: flex; align-items: center; gap: 14px; color: rgba(255,255,255,0.86); font-size: 18px; line-height: 1.5; }
.check {
  display: grid;
  place-items: center;
  flex: 0 0 30px;
  height: 30px;
  border-radius: 9px;
  color: #d6ceff;
  background: rgba(138, 112, 255, 0.26);
  font-size: 15px;
  font-weight: 700;
}

.login-right {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: clamp(40px, 4vw, 64px);
  background: radial-gradient(circle at 100% 0, #f0efff 0%, transparent 28%), #fff;
}
.login-box { width: 100%; max-width: 560px; }
.back-link {
  display: inline-block;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-secondary);
  margin-bottom: 46px;
  transition: var(--transition);
}
.back-link:hover { color: var(--color-primary); }

.section { display: flex; flex-direction: column; gap: 20px; }
.welcome-heading {
  display: flex;
  align-items: flex-start;
  text-align: left;
}

.section-title { font-size: clamp(36px, 2.7vw, 43px); font-weight: 750; color: var(--color-text-primary); line-height: 1.25; letter-spacing: -1.4px; margin-bottom: 0; white-space: nowrap; }
.section-desc { font-size: 17px; line-height: 1.65; color: var(--color-text-secondary); margin-bottom: 12px; text-align: left; }
.register-desc { margin: -4px 0 12px; color: var(--color-text-secondary); font-size: 16px; line-height: 1.65; }
.oauth-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: 100%;
  min-height: 58px;
  padding: 16px 24px;
  border: none;
  border-radius: 15px;
  color: #fff;
  background: linear-gradient(90deg, #5d48e8 0%, #7958f4 100%);
  box-shadow: 0 10px 24px rgba(103, 76, 232, 0.24);
  font-size: 17px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.oauth-button:hover { transform: translateY(-2px); box-shadow: 0 13px 28px rgba(103, 76, 232, 0.30); }

.form { display: flex; flex-direction: column; gap: 18px; }
.form-group { display: flex; flex-direction: column; gap: 8px; }
.form-label { font-size: 14px; font-weight: 700; color: #394150; }
.form-input {
  min-height: 54px;
  padding: 13px 16px;
  border: 1.5px solid var(--color-border);
  border-radius: 14px;
  font-size: 16px;
  font-family: var(--font-sans);
  color: var(--color-text-primary);
  background: var(--color-bg-primary);
  transition: var(--transition);
  outline: none;
}
.form-input:focus { border-color: var(--color-primary); box-shadow: 0 0 0 3px var(--color-primary-light); }
.register-button { margin-top: 2px; }
.register-button:disabled { cursor: not-allowed; opacity: 0.65; transform: none; }

.switch-link {
  text-align: center;
  font-size: 15px;
  color: var(--color-text-secondary);
  margin-top: 6px;
}
.text-btn {
  background: none;
  border: none;
  color: var(--color-primary);
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  padding: 0 2px;
  text-decoration: underline;
}
.error-msg {
  padding: 10px 14px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: var(--radius-md);
  font-size: 13px;
  color: #dc2626;
}
.success-msg {
  padding: 10px 14px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: var(--radius-md);
  font-size: 13px;
  color: #16a34a;
}

@media (max-width: 900px) {
  .login-page { padding: 0; }
  .login-layout { grid-template-columns: 1fr; min-height: 100vh; }
  .login-left { min-height: auto; padding: 44px 32px; gap: 52px; }
  .brand-content h2 { font-size: 36px; }
  .brand-content h2 { white-space: normal; }
  .login-right { padding: 56px 32px 72px; }
  .section-title { white-space: normal; }
}

@media (max-width: 560px) {
  .login-left { padding: 32px 22px 38px; gap: 42px; }
  .brand-logo { width: 38px; height: 38px; }
  .brand-name { font-size: 19px; }
  .brand-content h2 { font-size: 34px; }
  .brand-content p { font-size: 16px; }
  .feature-list li { font-size: 16px; }
  .login-right { padding: 44px 22px 56px; }
  .back-link { margin-bottom: 34px; }
  .section-title { font-size: 32px; }
}
</style>
