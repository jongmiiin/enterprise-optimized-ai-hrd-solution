import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth.js'

const AUTH_SERVER_URL = import.meta.env.VITE_AUTH_SERVER_URL || 'http://localhost:8080'
const CLIENT_ID = import.meta.env.VITE_CLIENT_ID || 'web-client'
const REDIRECT_URI = import.meta.env.VITE_REDIRECT_URI || `${window.location.origin}/callback`

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(sessionStorage.getItem('access_token') || null)
  const user = ref(JSON.parse(sessionStorage.getItem('user') || 'null'))

  const isAuthenticated = computed(() => !!accessToken.value)
  const isInstructor = computed(() => user.value?.role === 'INSTRUCTOR')

  function setToken(token) {
    accessToken.value = token
    sessionStorage.setItem('access_token', token)
  }

  function setUser(userData) {
    user.value = userData
    sessionStorage.setItem('user', JSON.stringify(userData))
  }

  async function fetchUser() {
    try {
      const res = await authApi.getMe()
      console.log('[AuthStore] /me response =', res.data)

      const userData = res?.data?.data ?? res?.data

      if (!userData || typeof userData !== 'object') {
        throw new Error('사용자 정보 형식이 올바르지 않습니다.')
      }

      setUser(userData)
    } catch (error) {
      console.error('[AuthStore] 사용자 정보 조회 실패:', error)
      logout(false)
    }
  }

  function logout(redirect = true) {
    accessToken.value = null
    user.value = null
    sessionStorage.removeItem('access_token')
    sessionStorage.removeItem('user')

    if (redirect) {
      // Auth Server의 SSO 세션(JSESSIONID)까지 끊어야 재로그인 시 계정을 바꿀 수 있다.
      // 세션 쿠키가 SameSite=Lax라 fetch/iframe으로는 전송되지 않으므로,
      // 팝업으로 실제 top-level GET 이동을 발생시켜 로그아웃을 완료한 뒤 닫는다.
      try {
        const popup = window.open(`${AUTH_SERVER_URL}/logout`, 'sf-logout', 'width=1,height=1,left=-1000,top=-1000')
        setTimeout(() => {
          try { popup?.close() } catch { /* noop */ }
          window.location.href = '/login'
        }, 600)
      } catch {
        window.location.href = '/login'
      }
    }
  }

  // OAuth2 Authorization Code Flow
  function redirectToLogin() {
    const params = new URLSearchParams({
      response_type: 'code',
      client_id: CLIENT_ID,
      redirect_uri: REDIRECT_URI,
      scope: 'openid profile read write'
    })

    window.location.href = `${AUTH_SERVER_URL}/oauth2/authorize?${params.toString()}`
  }

  async function handleCallback(code) {
    const res = await authApi.exchangeCode(code)
    console.log('[AuthStore] token response =', res.data)

    const token = res?.data?.access_token

    if (!token) {
      throw new Error('액세스 토큰을 받지 못했습니다.')
    }

    setToken(token)
    await fetchUser()
  }

  return {
    accessToken,
    user,
    isAuthenticated,
    isInstructor,
    setToken,
    setUser,
    fetchUser,
    logout,
    redirectToLogin,
    handleCallback
  }
})
