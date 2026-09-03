<template>
  <div class="page-wrapper">
    <AppHeader />

    <main class="page shell">
      <!-- 직원 화면 -->
      <template v-if="!isInstructor">
        <div class="profile-card fade-in-up">
          <div class="profile-avatar">{{ auth.user?.name?.charAt(0) || '?' }}</div>
          <div class="profile-info">
            <h2 class="profile-name">{{ auth.user?.name || '사용자' }}</h2>
            <p class="profile-email">{{ auth.user?.email || '-' }}</p>
            <span class="badge badge-indigo">직원</span>
          </div>
        </div>

        <section class="recommend-section">
          <h3 class="section-title">추천 강의</h3>

          <p v-if="recommendMessage" class="recommend-message">
            {{ recommendMessage }}
          </p>

          <div v-if="recommendLoading" class="loading-row">
            <div v-for="i in 3" :key="i" class="skeleton-card">
              <div class="skeleton-thumb"></div>
              <div class="skeleton-body">
                <div class="skeleton-line short"></div>
                <div class="skeleton-line"></div>
              </div>
            </div>
          </div>

          <div v-else-if="recommendations.length" class="recommend-grid fade-in">
            <CourseCard v-for="c in recommendations" :key="c.id" :course="c" />
          </div>

          <p v-else-if="recommendError" class="empty-text">
            {{ recommendError }}
          </p>

          <p v-else class="empty-text">
            아직 추천할 강의가 없습니다.
          </p>
        </section>
      </template>

      <!-- HR 화면: 교육 승인 관리 -->
      <template v-else>
        <div class="title-row">
          <div>
            <p class="eyebrow">LEARNING ADMIN</p>
            <h1 class="page-title">교육 신청을 검토해 주세요</h1>
            <p class="page-desc">직원이 신청한 AI 추천 과정을 확인하고 승인할 수 있어요.</p>
          </div>

          <select v-model="selectedJob" class="select-small">
            <option value="전체 직무">전체 직무</option>
            <option v-for="job in jobTitles" :key="job" :value="job">{{ job }}</option>
          </select>
        </div>

        <section class="metric-grid">
          <div class="panel metric">
            <div class="metric-label">승인 대기 <span class="dot dot-amber"></span></div>
            <div class="metric-value">{{ pendingCount }}건</div>
          </div>
          <div class="panel metric">
            <div class="metric-label">예상 결제 금액</div>
            <div class="metric-value">{{ expectedAmount.toLocaleString() }}원</div>
          </div>
          <div class="panel metric">
            <div class="metric-label">이번 달 승인 <span class="dot dot-mint"></span></div>
            <div class="metric-value">{{ approvedCount }}건</div>
          </div>
        </section>

        <div class="admin-toolbar">
          <div>
            <h2 class="section-title">교육 신청 현황</h2>
            <p class="section-desc">승인 대기와 완료 내역을 한곳에서 확인하세요.</p>
          </div>
          <div class="segmented">
            <button
              type="button"
              :class="['segment', { active: approvalTab === 'pending' }]"
              @click="approvalTab = 'pending'"
            >
              승인 대기 {{ pendingCount }}
            </button>
            <button
              type="button"
              :class="['segment', { active: approvalTab === 'approved' }]"
              @click="approvalTab = 'approved'"
            >
              승인 완료 {{ approvedCount }}
            </button>
          </div>
        </div>

        <section class="panel data-table fade-in">
          <div class="table-head approval-cols">
            <span>직원</span>
            <span>신청 교육</span>
            <span>AI 추천</span>
            <span>금액</span>
            <span>상태</span>
            <span>처리</span>
          </div>

          <div v-for="row in filteredApprovalRows" :key="row.id" class="table-row approval-cols">
            <div>
              <div class="cell-title">{{ row.employee }}</div>
              <div class="cell-sub">{{ row.jobTitle }}</div>
            </div>
            <span class="cell-title">{{ row.courseTitle }}</span>
            <span class="recommend-score">{{ row.recommendScore }}%</span>
            <span>{{ row.amount.toLocaleString() }}원</span>
            <span>
              <span class="badge" :class="row.status === 'ACTIVE' ? 'badge-mint' : 'badge-amber'">
                {{ row.status === 'ACTIVE' ? '승인 완료' : '승인 대기' }}
              </span>
            </span>
            <span>
              <button v-if="row.status !== 'ACTIVE'" type="button" class="status-button" @click="approveRow(row)">
                승인·결제
              </button>
              <span v-else class="cell-sub">승인 완료</span>
            </span>
          </div>

          <div v-if="!filteredApprovalRows.length" class="empty-row">
            해당 조건의 신청 내역이 없습니다.
          </div>
        </section>

        <p class="helper-note">승인과 동시에 결제가 진행되며 직원의 수강 상태가 활성화됩니다.</p>
      </template>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import CourseCard from '@/components/CourseCard.vue'
import { useAuthStore } from '@/store/auth.js'
import { enrollmentApi } from '@/api/enrollment.js'

const router = useRouter()
const auth = useAuthStore()

const isInstructor = computed(() => auth.user?.role === 'INSTRUCTOR')

/* 직원용 */
const recommendations = ref([])
const recommendLoading = ref(true)
const recommendError = ref('')
const recommendMessage = ref('')

/* HR 승인 관리용 (백엔드에 전체 직원 신청 조회 API가 아직 없어 더미 데이터 사용) */
const approvalRows = ref([
  { id: 1, employee: '김민수', jobTitle: '백엔드 개발자', courseTitle: '생성형 AI로 업무 자동화하기', recommendScore: 96, amount: 90000, status: 'PENDING' },
  { id: 2, employee: '박준호', jobTitle: '데이터 분석가', courseTitle: '좋은 프롬프트를 만드는 법', recommendScore: 91, amount: 80000, status: 'PENDING' },
  { id: 3, employee: '최민혁', jobTitle: '보안 엔지니어', courseTitle: '개발자를 위한 책임 있는 AI', recommendScore: 87, amount: 100000, status: 'PENDING' },
  { id: 4, employee: '이서연', jobTitle: '프론트엔드 개발자', courseTitle: '프론트엔드 개발자를 위한 AI 코딩 어시스턴트', recommendScore: 94, amount: 80000, status: 'ACTIVE' },
  { id: 5, employee: '정하은', jobTitle: 'DevOps 엔지니어', courseTitle: '개발자를 위한 책임 있는 AI 활용', recommendScore: 89, amount: 100000, status: 'ACTIVE' },
  { id: 6, employee: '강태우', jobTitle: '데이터 분석가', courseTitle: '데이터 분석 업무에 AI 활용하기', recommendScore: 93, amount: 90000, status: 'ACTIVE' },
  { id: 7, employee: '윤소희', jobTitle: '프로덕트 매니저', courseTitle: '비개발자를 위한 생성형 AI 활용법', recommendScore: 85, amount: 70000, status: 'ACTIVE' },
  { id: 8, employee: '한지훈', jobTitle: '백엔드 개발자', courseTitle: '생성형 AI로 업무 자동화하기', recommendScore: 90, amount: 90000, status: 'ACTIVE' },
  { id: 9, employee: '오다은', jobTitle: 'HR 담당자', courseTitle: '좋은 프롬프트를 만드는 법', recommendScore: 88, amount: 80000, status: 'ACTIVE' },
  { id: 10, employee: '배주현', jobTitle: '마케터', courseTitle: '비개발자를 위한 생성형 AI 활용법', recommendScore: 92, amount: 70000, status: 'ACTIVE' },
  { id: 11, employee: '조민재', jobTitle: '프론트엔드 개발자', courseTitle: 'AI 협업 도구 실전', recommendScore: 86, amount: 80000, status: 'ACTIVE' }
])
const approvalTab = ref('pending')
const selectedJob = ref('전체 직무')

const jobTitles = computed(() => [...new Set(approvalRows.value.map(r => r.jobTitle))])

const pendingCount = computed(() => approvalRows.value.filter(r => r.status !== 'ACTIVE').length)
const approvedCount = computed(() => approvalRows.value.filter(r => r.status === 'ACTIVE').length)
const expectedAmount = computed(() =>
  approvalRows.value
    .filter(r => r.status !== 'ACTIVE')
    .reduce((sum, r) => sum + r.amount, 0)
)

const filteredApprovalRows = computed(() => {
  return approvalRows.value.filter(r => {
    const matchesTab = approvalTab.value === 'pending' ? r.status !== 'ACTIVE' : r.status === 'ACTIVE'
    const matchesJob = selectedJob.value === '전체 직무' || r.jobTitle === selectedJob.value
    return matchesTab && matchesJob
  })
})

function approveRow(row) {
  // 실제 결제/승인 API가 아직 없어 프론트 상태만 즉시 전환 (데모용)
  row.status = 'ACTIVE'
}

function handleLogout() {
  auth.logout()
  router.push('/')
}

async function loadStudentRecommendations() {
  try {
    if (!auth.user) {
      console.warn('[MyPage] auth.user is missing')
      recommendError.value = '추천 강의를 준비 중입니다.'
      return
    }

    if (!auth.user.id) {
      console.warn('[MyPage] auth.user.id is missing:', auth.user)
      recommendError.value = '추천 강의를 준비 중입니다.'
      return
    }

    const res = await enrollmentApi.getRecommendations(auth.user.id)
    console.log('[MyPage] recommendation response:', res.data)

    const payload = res.data

    if (Array.isArray(payload?.recommendedCourses)) {
      recommendations.value = payload.recommendedCourses
      recommendMessage.value = payload.message ?? ''
    } else if (Array.isArray(payload?.data)) {
      recommendations.value = payload.data
      recommendMessage.value = payload.message ?? ''
    } else if (Array.isArray(payload)) {
      recommendations.value = payload
      recommendMessage.value = ''
    } else {
      console.warn('[MyPage] unexpected recommendation response shape:', payload)
      recommendations.value = []
      recommendMessage.value = ''
    }
  } catch (error) {
    console.error('[MyPage] failed to load recommendations:', error)
    recommendError.value = '현재 추천 강의를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.'
  } finally {
    recommendLoading.value = false
  }
}

onMounted(async () => {
  if (isInstructor.value) {
    recommendLoading.value = false
  } else {
    await loadStudentRecommendations()
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
  display: flex;
  flex-direction: column;
  gap: 32px;
}

/* 공통 타이틀 영역 (HR) */
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

.select-small {
  height: 36px;
  padding: 0 34px 0 13px;
  border: 1px solid var(--sf-border);
  border-radius: 11px;
  color: var(--sf-muted);
  background: rgba(255, 255, 255, 0.74);
  font-size: 12px;
}

/* 직원 프로필 카드 */
.profile-card {
  display: flex;
  align-items: center;
  gap: 20px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid var(--sf-border);
  border-radius: var(--sf-radius-md);
  padding: 28px;
  box-shadow: var(--sf-shadow-sm);
}

.profile-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: var(--sf-indigo-soft);
  color: var(--sf-indigo-dark);
  font-size: 24px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.profile-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.profile-name {
  font-size: 20px;
  font-weight: 700;
  color: var(--sf-ink);
}

.profile-email {
  font-size: 14px;
  color: var(--sf-muted);
}

.badge {
  display: inline-flex;
  align-items: center;
  width: fit-content;
  min-height: 25px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 750;
}

.badge-indigo {
  color: var(--sf-indigo-dark);
  background: var(--sf-indigo-soft);
}

.badge-mint {
  color: #087858;
  background: var(--sf-success-bg);
}

.badge-amber {
  color: #ad5e00;
  background: var(--sf-pending-bg);
}

.section-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--sf-ink);
}

.section-desc {
  margin: 5px 0 0;
  font-size: 13px;
  color: var(--sf-muted);
}

.recommend-message {
  margin-bottom: 14px;
  font-size: 13px;
  color: var(--sf-muted);
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
}

.loading-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.skeleton-card {
  background: rgba(255, 255, 255, 0.82);
  border-radius: 20px;
  overflow: hidden;
  border: 1px solid var(--sf-border);
}

.skeleton-thumb {
  height: 132px;
  background: linear-gradient(90deg, #eef0f6 25%, #e2e5ee 50%, #eef0f6 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}

.skeleton-body {
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.skeleton-line {
  height: 12px;
  border-radius: 6px;
  background: linear-gradient(90deg, #eef0f6 25%, #e2e5ee 50%, #eef0f6 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}

.skeleton-line.short {
  width: 40%;
}

.empty-text {
  color: var(--sf-muted);
  font-size: 14px;
}

/* HR 요약 카드 */
.metric-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.panel {
  background: #ffffff;
  border: 1px solid var(--sf-border);
  border-radius: var(--sf-radius-md);
  box-shadow: var(--sf-shadow-sm);
}

.metric {
  padding: 19px 21px;
  position: relative;
  overflow: hidden;
}

.metric::after {
  content: '';
  position: absolute;
  width: 90px;
  height: 90px;
  right: -35px;
  top: -35px;
  border-radius: 50%;
  background: var(--sf-indigo-soft);
}

.metric-label {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--sf-muted);
  font-size: 12px;
}

.metric-value {
  position: relative;
  z-index: 1;
  margin-top: 9px;
  font-size: 26px;
  font-weight: 850;
  letter-spacing: -0.045em;
  color: var(--sf-ink);
}

.dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  display: inline-block;
}

.dot-amber {
  background: var(--sf-pending);
}

.dot-mint {
  background: var(--sf-success);
}

/* 승인 테이블 툴바 */
.admin-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.segmented {
  display: inline-flex;
  gap: 4px;
  padding: 4px;
  border: 1px solid var(--sf-border);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.64);
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

/* 승인 테이블 */
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

.approval-cols {
  grid-template-columns: 1fr 2fr 0.65fr 0.7fr 0.75fr 0.78fr;
}

.cell-title {
  font-weight: 750;
  font-size: 13px;
}

.cell-sub {
  margin-top: 4px;
  color: var(--sf-muted);
  font-size: 11px;
}

.recommend-score {
  color: var(--sf-success);
  font-weight: 850;
  font-size: 13px;
}

.status-button {
  height: 35px;
  padding: 0 13px;
  border: 0;
  border-radius: 11px;
  color: white;
  background: linear-gradient(135deg, var(--sf-indigo), #7254ef);
  font-size: 12px;
  font-weight: 750;
  cursor: pointer;
}

.empty-row {
  padding: 60px 0;
  text-align: center;
  color: var(--sf-muted);
  font-size: 13px;
}

.helper-note {
  margin: -12px 0 0;
  color: var(--sf-muted);
  font-size: 12px;
}

@keyframes shimmer {
  to {
    background-position: -200% 0;
  }
}

@media (max-width: 992px) {
  .recommend-grid,
  .loading-row {
    grid-template-columns: 1fr;
  }

  .metric-grid {
    grid-template-columns: 1fr;
  }

  .title-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .admin-toolbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}

@media (max-width: 640px) {
  .profile-card {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>