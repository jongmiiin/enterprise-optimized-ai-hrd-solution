<template>
  <div class="callback-page">
    <main class="callback-card" role="status" aria-live="polite">
      <div class="callback-brand">
        <img src="@/assets/images/logo/main_logo.png" alt="" />
        <strong>SkillFit</strong>
      </div>

      <div v-if="status === 'processing'" class="spinner" aria-hidden="true"></div>
      <div v-else class="status-icon" :class="status" aria-hidden="true">
        {{ status === 'success' ? '✓' : '!' }}
      </div>

      <h1>{{ title }}</h1>
      <p>{{ message }}</p>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth.js'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const status = ref('processing')
const title = ref('회사 계정을 확인하고 있어요')
const message = ref('잠시만 기다려 주세요.')
const processing = ref(false)
const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

onMounted(async () => {
  if (processing.value) return
  processing.value = true

  const code = route.query.code
  const error = route.query.error
  const errorDescription = route.query.error_description

  if (error) {
    console.error('OAuth callback error:', {
      error,
      errorDescription
    })
    status.value = 'error'
    title.value = '로그인에 실패했어요'
    message.value = errorDescription || '다시 시도해 주세요.'
    await delay(1200)
    router.replace('/login')
    return
  }

  if (!code) {
    console.error('OAuth callback error: code 파라미터가 없습니다.')
    status.value = 'error'
    title.value = '로그인 요청을 확인할 수 없어요'
    message.value = '로그인 화면에서 다시 시도해 주세요.'
    await delay(1200)
    router.replace('/login')
    return
  }

  try {
    await auth.handleCallback(code)
    status.value = 'success'
    title.value = '로그인이 완료됐어요'
    message.value = '교육과정으로 이동하고 있어요.'
    await delay(700)
    router.replace('/courses')
  } catch (err) {
    console.error('OAuth callback 처리 실패:', err)
    status.value = 'error'
    title.value = '로그인 처리에 실패했어요'
    message.value = '잠시 후 다시 시도해 주세요.'
    await delay(1200)
    router.replace('/login')
  }
})
</script>

<style scoped>
.callback-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  background: radial-gradient(circle at 50% 40%, #eeedff 0%, #f7f8fc 38%, #f4f6fb 70%);
}

.callback-card {
  width: min(100%, 500px);
  padding: 52px 48px 50px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(73, 61, 123, 0.14);
  border-radius: 20px;
  box-shadow: 0 20px 55px rgba(46, 37, 90, 0.10);
}
.callback-brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}
.callback-brand img { width: 34px; height: 34px; object-fit: contain; }
.callback-brand strong { color: var(--color-text-primary); font-size: 21px; letter-spacing: -0.5px; }
.callback-card h1 {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 27px;
  font-weight: 750;
  line-height: 1.35;
  letter-spacing: -1px;
}
.callback-card p {
  margin: 12px 0 0;
  color: var(--color-text-secondary);
  font-size: 15px;
  line-height: 1.6;
}

.spinner {
  width: 58px;
  height: 58px;
  margin: 36px auto 32px;
  border: 5px solid #e8e7fb;
  border-top-color: #654df1;
  border-radius: 50%;
  animation: spin 0.85s linear infinite;
}
.status-icon {
  display: grid;
  place-items: center;
  width: 58px;
  height: 58px;
  margin: 36px auto 32px;
  border-radius: 50%;
  font-size: 26px;
  font-weight: 800;
}
.status-icon.success { color: #087f5b; background: #e4f8f1; }
.status-icon.error { color: #c2414b; background: #feedef; }

@media (max-width: 560px) {
  .callback-card { padding: 42px 24px 40px; border-radius: 18px; }
  .callback-card h1 { font-size: 23px; }
  .callback-card p { font-size: 14px; }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
