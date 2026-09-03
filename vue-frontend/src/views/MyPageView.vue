<template>
  <div class="page-wrapper">
    <AppHeader />

    <main class="page shell">
      <!-- 직원 화면 -->
      <template v-if="!isInstructor">
        <section class="growth-hero fade-in-up">
          <div class="growth-copy">
            <p class="growth-eyebrow">AI-POWERED LEARNING PATH</p>
            <h1><strong>{{ auth.user?.name || '사용자' }}</strong>님,<br>다음 성장을 준비해 볼까요?</h1>
            <p>현재 역량을 바탕으로 부족한 부분을 채우고, 직무 전문성을 높이는 학습을 추천해 드려요.</p>
          </div>

          <div class="competency-panel" aria-label="AI 핵심 역량">
            <div class="competency-heading">
              <div>
                <span>CORE COMPETENCY</span>
                <h2>나의 AI 핵심 역량</h2>
              </div>
              <small>5점 만점</small>
            </div>

            <div class="radar-wrap">
              <svg class="competency-radar" viewBox="0 0 420 330" role="img" aria-label="AI 핵심 역량 오각형 차트">
                <g class="radar-grid">
                  <polygon v-for="level in 5" :key="level" :points="radarGridPoints(level)" />
                  <line v-for="point in radarOuterPoints" :key="`${point.x}-${point.y}`" x1="210" y1="160" :x2="point.x" :y2="point.y" />
                </g>
                <polygon v-if="hasCompetencyScore" class="radar-score" :points="radarScorePoints" />
                <circle
                  v-for="point in radarScoreDots"
                  :key="point.key"
                  class="radar-dot"
                  :class="{ missing: point.score === null }"
                  :cx="point.x"
                  :cy="point.y"
                  r="4"
                />
                <g
                  v-for="point in radarLabelPoints"
                  :key="`label-${point.key}`"
                  class="radar-label"
                  :transform="`translate(${point.x} ${point.y})`"
                >
                  <text text-anchor="middle" dominant-baseline="middle">{{ point.label }}</text>
                  <text v-if="point.score !== null" class="radar-label-score" y="20" text-anchor="middle">{{ point.score }}점</text>
                </g>
              </svg>
            </div>
          </div>
        </section>

        <div v-if="recommendLoading" class="loading-row">
          <div v-for="i in 3" :key="i" class="skeleton-card">
            <div class="skeleton-thumb"></div>
            <div class="skeleton-body">
              <div class="skeleton-line short"></div>
              <div class="skeleton-line"></div>
            </div>
          </div>
        </div>

        <template v-else>
          <section class="learning-block fade-in">
            <div class="learning-heading">
              <div class="learning-icon score-icon">01</div>
              <div>
                <p>GROW BY SCORE</p>
                <h2>점수별 학습</h2>
                <span>보완이 필요한 핵심 역량을 우선으로 추천했어요.</span>
              </div>
            </div>

            <div v-if="scoreRecommendations.length" class="recommend-grid">
              <CourseCard v-for="c in scoreRecommendations" :key="`score-${c.id}`" :course="c" />
            </div>
            <p v-else class="empty-text">역량 점수가 등록되면 맞춤 강의를 추천해 드릴게요.</p>
          </section>

          <section class="learning-block fade-in">
            <div class="learning-heading">
              <div class="learning-icon job-icon">02</div>
              <div>
                <p>GROW BY ROLE</p>
                <h2>직무별 학습</h2>
                <span>현재 직무와 연관성이 높은 과정을 모았어요.</span>
              </div>
            </div>

            <div v-if="jobRecommendations.length" class="recommend-grid">
              <CourseCard v-for="c in jobRecommendations" :key="`job-${c.id}`" :course="c" />
            </div>
            <p v-else-if="recommendError" class="empty-text">{{ recommendError }}</p>
            <p v-else class="empty-text">직무 정보가 등록되면 관련 강의를 추천해 드릴게요.</p>
          </section>
        </template>
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
const scoreRecommendations = ref([])
const jobRecommendations = ref([])
const recommendLoading = ref(true)
const recommendError = ref('')

const competencyDefinitions = [
  { key: 'aiLiteracy', label: 'AI 리터러시' },
  { key: 'jobAiApplication', label: '직무별 AI 활용' },
  { key: 'promptSkill', label: '프롬프트 활용' },
  { key: 'dataLiteracy', label: '데이터 활용' },
  { key: 'aiEthicsSecurity', label: 'AI 윤리·보안' }
]
const competencyScores = ref({})
const competencies = computed(() => competencyDefinitions.map(item => ({
  ...item,
  score: normalizeCompetencyScore(competencyScores.value[item.key])
})))
const radarCenter = { x: 210, y: 160 }
const radarRadius = 112
const radarLabelRadius = 150
const radarOuterPoints = computed(() => competencyDefinitions.map((_, index) => radarPoint(index, radarRadius)))
const hasCompetencyScore = computed(() => competencies.value.some(item => item.score !== null))
const radarScoreDots = computed(() => competencies.value.map((item, index) => ({
  ...item,
  ...radarPoint(index, item.score === null ? 0 : radarRadius * item.score / 5)
})))
const radarScorePoints = computed(() => radarScoreDots.value.map(point => `${point.x},${point.y}`).join(' '))
const radarLabelPoints = computed(() => competencies.value.map((item, index) => ({
  ...item,
  ...radarPoint(index, radarLabelRadius)
})))

function normalizeCompetencyScore(value) {
  if (value === null || value === undefined || value === '') return null
  const score = Number(value)
  return Number.isFinite(score) ? Math.min(5, Math.max(1, score)) : null
}

function radarPoint(index, radius) {
  const angle = -Math.PI / 2 + index * (Math.PI * 2 / competencyDefinitions.length)
  return {
    x: Number((radarCenter.x + Math.cos(angle) * radius).toFixed(2)),
    y: Number((radarCenter.y + Math.sin(angle) * radius).toFixed(2))
  }
}

function radarGridPoints(level) {
  const radius = radarRadius * level / 5
  return competencyDefinitions.map((_, index) => {
    const point = radarPoint(index, radius)
    return `${point.x},${point.y}`
  }).join(' ')
}

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

    const body = payload?.data && !Array.isArray(payload.data) ? payload.data : payload
    const allCourses = Array.isArray(body?.recommendedCourses)
      ? body.recommendedCourses
      : Array.isArray(payload?.data)
        ? payload.data
        : Array.isArray(payload) ? payload : []

    competencyScores.value = body?.competencies ?? body?.capabilities ?? auth.user?.competencies ?? {}
    scoreRecommendations.value = Array.isArray(body?.scoreBasedCourses)
      ? body.scoreBasedCourses
      : allCourses.slice(0, 3)
    jobRecommendations.value = Array.isArray(body?.jobBasedCourses)
      ? body.jobBasedCourses
      : allCourses.slice(3)
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

/* 직원 AI 추천 */
.growth-hero {
  position: relative;
  overflow: hidden;
  display: grid;
  grid-template-columns: minmax(280px, 0.8fr) minmax(560px, 1.2fr);
  align-items: center;
  gap: 54px;
  min-height: 390px;
  padding: 48px 52px;
  border-radius: 30px;
  background: linear-gradient(135deg, #17132f 0%, #292054 55%, #423477 100%);
  color: white;
  box-shadow: 0 24px 55px rgba(35, 25, 81, 0.22);
}

.growth-hero::before,
.growth-hero::after {
  content: '';
  position: absolute;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  pointer-events: none;
}

.growth-hero::before { width: 330px; height: 330px; left: -180px; bottom: -210px; }
.growth-hero::after { width: 240px; height: 240px; right: -110px; top: -150px; }

.growth-copy,
.competency-panel { position: relative; z-index: 1; }

.growth-eyebrow {
  margin: 0 0 18px;
  color: #a99cff;
  font-size: 11px;
  font-weight: 850;
  letter-spacing: 0.18em;
}

.growth-copy h1 {
  margin: 0;
  font-size: clamp(30px, 3vw, 44px);
  line-height: 1.22;
  letter-spacing: -0.045em;
}

.growth-copy h1 strong { color: #b5a8ff; }

.growth-copy > p:last-child {
  max-width: 410px;
  margin: 20px 0 0;
  color: rgba(255, 255, 255, 0.62);
  font-size: 14px;
  line-height: 1.75;
}

.competency-panel {
  padding: 26px 26px 22px;
  border: 1px solid rgba(255, 255, 255, 0.13);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(16px);
}

.competency-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 26px;
}

.competency-heading span { color: #a99cff; font-size: 9px; font-weight: 850; letter-spacing: 0.16em; }
.competency-heading h2 { margin: 7px 0 0; font-size: 19px; letter-spacing: -0.02em; }
.competency-heading small { color: rgba(255, 255, 255, 0.48); font-size: 10px; }

.radar-wrap {
  position: relative;
  width: min(100%, 520px);
  height: 330px;
  margin: -12px auto -8px;
}

.competency-radar { width: 100%; height: 100%; overflow: visible; }
.radar-grid polygon,
.radar-grid line {
  fill: rgba(255, 255, 255, 0.018);
  stroke: rgba(255, 255, 255, 0.19);
  stroke-width: 1;
}

.radar-grid polygon:last-of-type {
  fill: rgba(137, 116, 255, 0.04);
  stroke: rgba(181, 168, 255, 0.56);
  stroke-width: 1.5;
}

.radar-score {
  fill: rgba(137, 116, 255, 0.34);
  stroke: #b5a8ff;
  stroke-width: 2.5;
  filter: drop-shadow(0 5px 9px rgba(108, 84, 239, 0.25));
}

.radar-dot { fill: #fff; stroke: #765fee; stroke-width: 2; }
.radar-dot.missing { display: none; }

.radar-label {
  fill: rgba(255, 255, 255, 0.78);
  font-size: 11px;
  font-weight: 700;
}

.radar-label-score {
  fill: #b5a8ff;
  font-size: 10px;
  font-weight: 800;
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

.learning-block {
  padding: 30px;
  border: 1px solid var(--sf-border);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.64);
  box-shadow: var(--sf-shadow-sm);
}

.learning-heading {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 24px;
}

.learning-icon {
  width: 48px;
  height: 48px;
  flex: 0 0 auto;
  display: grid;
  place-items: center;
  border-radius: 15px;
  font-size: 12px;
  font-weight: 850;
}

.score-icon { color: #5844dc; background: #ebe8ff; }
.job-icon { color: #087858; background: #dcf5ec; }
.learning-heading p { margin: 0 0 3px; color: var(--sf-indigo); font-size: 9px; font-weight: 850; letter-spacing: 0.14em; }
.learning-heading h2 { margin: 0; color: var(--sf-ink); font-size: 21px; letter-spacing: -0.03em; }
.learning-heading span { display: block; margin-top: 5px; color: var(--sf-muted); font-size: 12px; }

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
  .growth-hero { grid-template-columns: 1fr; gap: 34px; padding: 40px; }

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
  .page { padding-top: 24px; }
  .shell { padding-left: 16px; padding-right: 16px; gap: 22px; }
  .growth-hero { padding: 32px 20px; border-radius: 22px; }
  .growth-copy h1 { font-size: 29px; }
  .competency-panel { padding: 22px 14px 18px; }
  .radar-wrap { height: 285px; }
  .radar-label { font-size: 9px; }
  .radar-label-score { font-size: 8px; }
  .learning-block { padding: 22px 18px; }
}
</style>
