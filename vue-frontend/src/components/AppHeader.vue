<template>
  <header class="app-header">
    <div class="header-inner">
      <!-- 로고 -->
      <router-link to="/" class="logo">
        <img src="@/assets/images/logo/main_logo.png" alt="SkillFit AI" class="logo-img" />
        <span class="logo-text">SkillFit AI</span>
      </router-link>

      <!-- 네비게이션 -->
      <nav class="nav-links" v-if="auth.isAuthenticated">
        <router-link
          v-for="item in navLinks"
          :key="item.to"
          :to="item.to"
          class="nav-link"
          :class="{ active: item.active }"
        >
          {{ item.label }}
        </router-link>
      </nav>

      <!-- 우측 액션 -->
      <div class="header-actions">
        <template v-if="auth.isAuthenticated">
          <router-link to="/mypage" class="user-chip" :title="auth.user?.name">
            <span class="user-avatar">{{ auth.user?.name?.charAt(0) || '?' }}</span>
            <span class="user-copy">
              <strong>{{ auth.user?.name || '사용자' }}</strong>
              <span>{{ roleLabel }}</span>
            </span>
          </router-link>
          <button class="btn btn-ghost btn-sm" @click="handleLogout">로그아웃</button>
        </template>
        <template v-else>
          <router-link to="/login" class="btn btn-ghost btn-sm">로그인</router-link>
          <router-link to="/login" class="btn btn-primary btn-sm">시작하기</router-link>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/store/auth.js'
import { useRouter, useRoute } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const isInstructor = computed(() => auth.user?.role === 'INSTRUCTOR')

const roleLabel = computed(() => (isInstructor.value ? 'HR 담당자' : '직원'))

const navLinks = computed(() => {
  const inCourses = route.path.startsWith('/courses') && route.path !== '/courses/new'

  if (isInstructor.value) {
    return [
      { to: '/courses', label: '교육과정', active: inCourses },
      { to: '/mypage', label: '승인 관리', active: route.path === '/mypage' },
      { to: '/courses/new', label: '과정 등록', active: route.path === '/courses/new' }
    ]
  }

  return [
    { to: '/courses', label: '교육과정', active: inCourses },
    { to: '/mypage', label: 'AI 추천', active: route.path === '/mypage' },
    { to: '/enrollments', label: '내 강의 목록', active: route.path === '/enrollments' }
  ]
})

function handleLogout() {
  auth.logout()
  router.push('/')
}
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  height: 72px;
  background: rgba(250, 251, 255, 0.86);
  backdrop-filter: blur(18px);
  border-bottom: 1px solid var(--sf-border);
}
.header-inner {
  max-width: var(--sf-shell);
  margin: 0 auto;
  padding: 0 24px;
  height: 100%;
  display: flex;
  align-items: center;
  gap: 32px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}
.logo-img {
  width: 34px;
  height: 34px;
  object-fit: contain;
  border-radius: 9px;
}
.logo-text {
  font-size: 18px;
  font-weight: 800;
  color: var(--sf-ink);
  letter-spacing: -0.04em;
}
.nav-links {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
}
.nav-link {
  height: 42px;
  padding: 0 16px;
  display: inline-flex;
  align-items: center;
  border-radius: 13px;
  font-size: 14px;
  font-weight: 650;
  color: #565d72;
  transition: var(--transition);
}
.nav-link:hover {
  color: var(--sf-indigo-dark);
}
.nav-link.active {
  color: var(--sf-indigo-dark);
  background: var(--sf-indigo-soft);
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: auto;
}
.btn-sm {
  padding: 7px 16px;
  font-size: 13px;
}
.user-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 10px 4px 4px;
  border-radius: 14px;
  transition: var(--transition);
}
.user-chip:hover {
  background: var(--sf-indigo-soft);
}
.user-avatar {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 750;
  color: var(--sf-indigo-dark);
  background: linear-gradient(145deg, #e7e5ff, #f5f4ff);
  border: 1px solid rgba(91, 80, 230, 0.12);
}
.user-copy {
  display: flex;
  flex-direction: column;
  line-height: 1.35;
}
.user-copy strong {
  font-size: 13px;
  color: var(--sf-ink);
}
.user-copy span {
  font-size: 11px;
  color: var(--sf-muted);
}
</style>
