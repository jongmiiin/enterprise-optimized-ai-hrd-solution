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

          <button type="button" class="btn btn-secondary" :disabled="listLoading" @click="loadApprovalLists">
            {{ listLoading ? '불러오는 중...' : '새로고침' }}
          </button>
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

        <p v-if="approvalActionError" class="action-error">{{ approvalActionError }}</p>

        <div v-if="listLoading" class="panel data-table">
          <div class="table-head approval-cols">
            <span>직원</span>
            <span>신청 교육</span>
            <span>AI 추천</span>
            <span>금액</span>
            <span>상태</span>
            <span>처리</span>
          </div>
          <div v-for="i in 3" :key="i" class="table-row approval-cols">
            <div class="skeleton-line short"></div>
            <div class="skeleton-line"></div>
            <div class="skeleton-line short"></div>
            <div class="skeleton-line short"></div>
            <div class="skeleton-line short"></div>
            <div class="skeleton-line short"></div>
          </div>
        </div>

        <p v-else-if="listError" class="empty-text">{{ listError }}</p>

        <section v-else class="panel data-table fade-in">
          <div class="table-head approval-cols">
            <span>직원</span>
            <span>신청 교육</span>
            <span>AI 추천</span>
            <span>금액</span>
            <span>상태</span>
            <span>처리</span>
          </div>

          <div v-for="row in currentRows" :key="row.enrollmentId" class="table-row approval-cols">
            <div>
              <div class="cell-title">{{ row.userName }}</div>
              <div class="cell-sub">{{ row.userEmail }}</div>
            </div>
            <span class="cell-title">{{ row.courseTitle }}</span>
            <span class="recommend-score">{{ pseudoScore(row.enrollmentId) }}%</span>
            <span>{{ Number(row.price).toLocaleString() }}원</span>
            <span>
              <span class="badge" :class="row.status === 'ACTIVE' ? 'badge-mint' : 'badge-amber'">
                {{ row.status === 'ACTIVE' ? '승인 완료' : '승인 대기' }}
              </span>
            </span>
            <span>
              <button
                v-if="row.status !== 'ACTIVE'"
                type="button"
                class="status-button"
                :disabled="approvingId === row.enrollmentId"
                @click="approveRow(row)"
              >
                {{ approvingId === row.enrollmentId ? '처리 중...' : '승인·결제' }}
              </button>
              <span v-else class="cell-sub">승인 완료</span>
            </span>
          </div>

          <div v-if="!currentRows.length" class="empty-row">
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

/* HR 승인 관리용 */
const pendingRows = ref([])
const activeRows = ref([])
const listLoading = ref(true)
const listError = ref('')
const approvalTab = ref('pending')
const approvingId = ref(null)
const approvalActionError = ref('')

const currentRows = computed(() => (approvalTab.value === 'pending' ? pendingRows.value : activeRows.value))
const pendingCount = computed(() => pendingRows.value.length)
const approvedCount = computed(() => activeRows.value.length)
const expectedAmount = computed(() =>
  pendingRows.value.reduce((sum, r) => sum + Number(r.price || 0), 0)
)

// 백엔드에 아직 AI 추천 점수 필드가 없어, 신청 ID 기반 고정 의사난수로 임시 표시
function pseudoScore(enrollmentId) {
  const id = Number(enrollmentId) || 0
  return 85 + (id * 7) % 13
}

async function loadApprovalLists({ silent = false } = {}) {
  if (!silent) {
    listLoading.value = true
    listError.value = ''
  }

  try {
    const [pendingRes, activeRes] = await Promise.all([
      enrollmentApi.getAdminEnrollments('PENDING'),
      enrollmentApi.getAdminEnrollments('ACTIVE')
    ])

    pendingRows.value = Array.isArray(pendingRes.data?.data) ? pendingRes.data.data : []
    activeRows.value = Array.isArray(activeRes.data?.data) ? activeRes.data.data : []
  } catch (error) {
    console.error('[MyPage] failed to load admin enrollments:', error)

    if (!silent) {
      if (error.response?.status === 403) {
        listError.value = 'HR 담당자만 접근할 수 있습니다.'
      } else {
        listError.value = '신청 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.'
      }
    }
  } finally {
    if (!silent) listLoading.value = false
  }
}

async function approveRow(row) {
  approvalActionError.value = ''
  approvingId.value = row.enrollmentId

  try {
    const res = await enrollmentApi.approve(row.enrollmentId)
    console.log('[MyPage] approve response:', res.data)

    // 결제는 응답 시점에 완료가 보장되므로 대기 목록에서는 바로 제거한다.
    // ACTIVE 전환은 Kafka 이벤트로 비동기 처리되므로, 잠시 뒤 조용히 재조회해
    // (스켈레톤 없이) 최종 상태로 동기화한다.
    pendingRows.value = pendingRows.value.filter(r => r.enrollmentId !== row.enrollmentId)
    activeRows.value = [{ ...row, status: 'ACTIVE' }, ...activeRows.value]

    setTimeout(() => loadApprovalLists({ silent: true }), 1200)
  } catch (error) {
    console.error('[MyPage] approve failed:', error)
    const status = error.response?.status
    const message = error.response?.data?.message

    if (status === 409) {
      approvalActionError.value = message || '이미 처리된 수강 신청입니다.'
      await loadApprovalLists()
    } else if (status === 404) {
      approvalActionError.value = message || '수강 신청을 찾을 수 없습니다.'
      await loadApprovalLists()
    } else if (status === 403) {
      approvalActionError.value = 'HR 담당자만 접근할 수 있습니다.'
    } else if (status === 502 || status === 503) {
      approvalActionError.value = message || '일시적인 오류입니다. 잠시 후 다시 시도해 주세요.'
    } else {
      approvalActionError.value = message || '승인 처리 중 오류가 발생했습니다.'
    }
  } finally {
    approvingId.value = null
  }
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
    await loadApprovalLists()
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

.btn {
  height: 40px;
  padding: 0 16px;
  border: 1px solid transparent;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-weight: 700;
  font-size: 13px;
  white-space: nowrap;
}

.btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.btn-secondary {
  color: var(--sf-indigo-dark);
  border-color: rgba(91, 80, 230, 0.22);
  background: rgba(255, 255, 255, 0.7);
}

.action-error {
  margin: 0;
  padding: 10px 14px;
  border-radius: 11px;
  background: #fef2f2;
  color: #dc2626;
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