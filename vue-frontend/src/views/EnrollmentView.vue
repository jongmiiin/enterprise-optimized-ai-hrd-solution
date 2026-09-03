<template>
  <div class="page-wrapper">
    <AppHeader />

    <main class="page shell">
      <div class="title-row">
        <div>
          <p class="eyebrow">MY COURSES</p>
          <h1 class="page-title">내 강의 목록</h1>
          <p class="page-desc">신청한 교육의 승인 상태와 수강 가능한 과정을 확인하세요.</p>
        </div>

        <router-link to="/courses" class="btn btn-secondary">
          교육과정 둘러보기
        </router-link>
      </div>

      <!-- 로딩 -->
      <div v-if="loading" class="loading-center">
        <div class="spinner"></div>
      </div>

      <template v-else>
        <!-- 상태 필터 -->
        <div class="segmented">
          <button
            v-for="tab in tabs"
            :key="tab.value"
            type="button"
            :class="['segment', { active: selectedTab === tab.value }]"
            @click="selectedTab = tab.value"
          >
            {{ tab.label }} {{ tab.count }}
          </button>
        </div>

        <!-- 목록 테이블 -->
        <section v-if="filteredEnrollments.length" class="panel data-table fade-in">
          <div class="table-head learning-cols">
            <span>교육과정</span>
            <span>분야</span>
            <span>신청일</span>
            <span>상태</span>
            <span>바로가기</span>
          </div>

          <div
            v-for="item in filteredEnrollments"
            :key="item.id"
            class="table-row learning-cols"
          >
            <div class="course-cell">
              <div class="mini-thumb" :class="getThumbBg(item)"></div>
              <div>
                <div class="cell-title">{{ item.course?.title || '삭제된 과정' }}</div>
                <div class="cell-sub">{{ item.course?.description || 'AI 역량 향상 과정' }}</div>
              </div>
            </div>

            <span>{{ getCategoryLabel(item) }}</span>
            <span>{{ formatDate(item.createdAt) }}</span>

            <span>
              <span class="badge" :class="item.status === 'ACTIVE' ? 'badge-mint' : 'badge-amber'">
                {{ item.status === 'ACTIVE' ? '수강 가능' : '승인 대기' }}
              </span>
            </span>

            <span>
              <router-link v-if="item.status === 'ACTIVE'" :to="`/courses/${item.courseId}`" class="link">
                강의 보기 →
              </router-link>
              <span v-else class="cell-sub">승인 후 이용 가능</span>
            </span>
          </div>
        </section>

        <!-- 빈 상태 -->
        <div v-else class="empty-state">
          <p>{{ selectedTab === '전체' ? '신청한 교육과정이 없습니다.' : '해당 상태의 교육과정이 없습니다.' }}</p>
          <router-link to="/courses" class="btn btn-primary empty-action-btn">
            교육과정 둘러보기
          </router-link>
        </div>
      </template>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import { enrollmentApi } from '@/api/enrollment.js'
import { useAuthStore } from '@/store/auth.js'
import { useCourseStore } from '@/store/course.js'

const router = useRouter()
const auth = useAuthStore()
const courseStore = useCourseStore()

const enrollments = ref([])
const loading = ref(true)
const selectedTab = ref('전체')

const isInstructor = computed(() => auth.user?.role === 'INSTRUCTOR')

const tabs = computed(() => {
  const pendingCount = enrollments.value.filter(e => e.status !== 'ACTIVE').length
  const activeCount = enrollments.value.filter(e => e.status === 'ACTIVE').length

  return [
    { value: '전체', label: '전체', count: enrollments.value.length },
    { value: '승인 대기', label: '승인 대기', count: pendingCount },
    { value: '수강 가능', label: '수강 가능', count: activeCount }
  ]
})

const filteredEnrollments = computed(() => {
  if (selectedTab.value === '승인 대기') {
    return enrollments.value.filter(e => e.status !== 'ACTIVE')
  }
  if (selectedTab.value === '수강 가능') {
    return enrollments.value.filter(e => e.status === 'ACTIVE')
  }
  return enrollments.value
})

function getCategoryLabel(item) {
  return courseStore.normalizeCategory(item.course?.category) || '-'
}

function getThumbBg(item) {
  const category = getCategoryLabel(item)
  const map = {
    '백엔드': 'thumb-blue',
    '프론트엔드': 'thumb-lilac',
    'DevOps': 'thumb-green',
    '데이터': 'thumb-slate',
    'AI': 'thumb-violet'
  }
  return map[category] || 'thumb-slate'
}

function formatDate(value) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}. ${m}. ${d}`
}

onMounted(async () => {
  // 강사는 이 페이지 접근 불가 → 마이페이지로 이동
  if (isInstructor.value) {
    console.warn('[EnrollmentView] instructor tried to access /enrollments, redirect to /mypage')
    router.replace('/mypage')
    return
  }

  try {
    const res = await enrollmentApi.getMyEnrollments()
    console.log('[EnrollmentView] my enrollments response:', res.data)

    if (Array.isArray(res.data?.data)) {
      enrollments.value = res.data.data
    } else if (Array.isArray(res.data)) {
      enrollments.value = res.data
    } else {
      enrollments.value = []
    }
  } catch (error) {
    console.error('[EnrollmentView] failed to load enrollments:', error)
    enrollments.value = []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.page-wrapper {
  min-height: 100vh;
  background:
    radial-gradient(circle at 80% -10%, rgba(126, 108, 255, 0.08), transparent 30%),
    radial-gradient(circle at 0% 80%, rgba(84, 215, 180, 0.05), transparent 28%),
    var(--sf-canvas);
}

.page {
  padding: 48px 0 72px;
}

.shell {
  max-width: var(--sf-shell);
  margin: 0 auto;
  padding-left: 24px;
  padding-right: 24px;
}

.eyebrow {
  margin: 0 0 12px;
  color: var(--sf-indigo);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.title-row {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 32px;
}

.page-title {
  margin: 0;
  font-size: 38px;
  line-height: 1.1;
  letter-spacing: -0.04em;
  color: var(--sf-ink);
}

.page-desc {
  margin: 12px 0 0;
  color: var(--sf-muted);
  font-size: 15px;
  line-height: 1.6;
}

.btn {
  height: 44px;
  padding: 0 19px;
  border: 1px solid transparent;
  border-radius: 13px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  font-weight: 750;
  font-size: 13px;
  text-decoration: none;
  white-space: nowrap;
}

.btn-primary {
  color: #fff;
  background: linear-gradient(135deg, var(--sf-indigo), #6d52ef);
  box-shadow: 0 10px 24px rgba(91, 80, 230, 0.22);
}

.btn-secondary {
  color: var(--sf-indigo-dark);
  border-color: rgba(91, 80, 230, 0.22);
  background: rgba(255, 255, 255, 0.7);
}

/* 상태 필터 */
.segmented {
  display: inline-flex;
  gap: 4px;
  padding: 4px;
  border: 1px solid var(--sf-border);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.64);
  margin-bottom: 24px;
}

.segment {
  height: 33px;
  padding: 0 13px;
  border: 0;
  border-radius: 10px;
  background: transparent;
  color: var(--sf-muted);
  font-size: 12px;
  font-weight: 750;
  transition: var(--transition);
}

.segment.active {
  color: var(--sf-indigo-dark);
  background: white;
  box-shadow: 0 2px 8px rgba(16, 19, 35, 0.07);
}

/* 테이블 */
.panel {
  background: #ffffff;
  border: 1px solid var(--sf-border);
  border-radius: var(--sf-radius-md);
  box-shadow: var(--sf-shadow-sm);
}

.data-table {
  overflow: hidden;
}

.table-head,
.table-row {
  display: grid;
  align-items: center;
  padding: 0 22px;
}

.table-head {
  height: 45px;
  color: var(--sf-subtle);
  background: #f8f9fc;
  border-bottom: 1px solid var(--sf-border);
  font-size: 10px;
  font-weight: 750;
}

.table-row {
  min-height: 78px;
  border-bottom: 1px solid var(--sf-border);
  font-size: 13px;
  color: var(--sf-ink);
}

.table-row:last-child {
  border-bottom: 0;
}

.learning-cols {
  grid-template-columns: 2.2fr 0.8fr 0.85fr 0.8fr 0.75fr;
}

.course-cell {
  display: flex;
  align-items: center;
  gap: 13px;
  min-width: 0;
}

.mini-thumb {
  width: 72px;
  height: 46px;
  border-radius: 11px;
  flex: 0 0 auto;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.thumb-blue   { background: linear-gradient(135deg, #dfe8fa, #eaf0fa 55%, #b8ccf3); }
.thumb-violet { background: linear-gradient(135deg, #eee8ff, #d9cdfa 55%, #b9a5ee); }
.thumb-green  { background: linear-gradient(135deg, #dfeee9, #c6dfd6 55%, #8ab9a8); }
.thumb-slate  { background: linear-gradient(135deg, #e6e9ef, #c9d0dc 55%, #9ca9bc); }
.thumb-lilac  { background: linear-gradient(135deg, #f0edff, #ded8ff 55%, #b9adf5); }

.cell-title {
  font-weight: 750;
  font-size: 13px;
}

.cell-sub {
  margin-top: 4px;
  color: var(--sf-muted);
  font-size: 11px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.badge {
  display: inline-flex;
  align-items: center;
  min-height: 25px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 750;
}

.badge-mint {
  color: #087858;
  background: var(--sf-success-bg);
}

.badge-amber {
  color: #ad5e00;
  background: var(--sf-pending-bg);
}

.link {
  color: var(--sf-indigo);
  font-size: 13px;
  font-weight: 750;
}

/* 빈 상태 */
.empty-state {
  text-align: center;
  padding: 80px 0;
  color: var(--sf-muted);
  font-size: 15px;
}

.empty-action-btn {
  display: inline-flex;
  margin-top: 16px;
}

/* 로딩 */
.loading-center {
  display: flex;
  justify-content: center;
  padding: 120px 0;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--sf-border);
  border-top-color: var(--sf-indigo);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 900px) {
  .learning-cols {
    grid-template-columns: 2fr 1fr;
    row-gap: 6px;
  }

  .table-head {
    display: none;
  }

  .table-row {
    grid-template-columns: 1fr;
    padding: 16px;
    gap: 8px;
  }
}
</style>
