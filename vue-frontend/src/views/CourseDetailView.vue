<template>
  <div class="page-wrapper">
    <AppHeader />

    <main v-if="course" class="page shell">
      <div class="detail-main">
        <!-- 좌측: 과정 소개 히어로 -->
        <section class="detail-hero fade-in-up">
          <span class="badge badge-indigo">{{ displayCategory }}</span>
          <h1 class="detail-title">{{ course.title }}</h1>
          <p class="detail-desc">
            {{ course.description || '실무 전문가가 직접 설계한 커리큘럼으로 체계적으로 학습하세요.' }}
          </p>

          <div class="detail-meta">
            <span>교육 담당&nbsp; {{ displayInstructorName }}</span>
            <span class="difficulty-wrap">
              난이도
              <DifficultyLevel
                :level="displayDifficulty"
                style="--dot-color: #c9bdff; --dot-empty: rgba(255, 255, 255, 0.28)"
              />
            </span>
          </div>

          <div class="benefit-row">
            <span class="benefit">✓ 직무·역량 기반 추천 과정</span>
            <span class="benefit">✓ HR 승인 후 수강 가능</span>
            <span class="benefit">✓ 기업 교육비 지원</span>
          </div>
        </section>

        <!-- 우측: 신청/결제 카드 -->
        <aside class="panel checkout-card fade-in">
          <div class="checkout-thumb" :class="thumbBg">
            <img v-if="thumbSrc" :src="thumbSrc" :alt="course.title" />
          </div>

          <div class="checkout-price">₩{{ displayPrice }}</div>

          <button
            class="btn btn-primary btn-block"
            @click="handlePrimaryAction"
            :disabled="buttonDisabled"
            :class="{ 'btn-disabled': buttonDisabled }"
          >
            <span v-if="enrolling">처리 중...</span>
            <span v-else>{{ buttonLabel }}</span>
          </button>

          <div v-if="enrollError" class="error-msg">{{ enrollError }}</div>

          <p class="helper">{{ helperText }}</p>

          <div class="certificate">
            <button class="btn btn-disabled btn-block" disabled>수료증 발급</button>
            <small>수강 완료 후 이용 가능</small>
          </div>
        </aside>
      </div>

      <section class="detail-info">
        <article class="panel info-block">
          <h3>과정 소개</h3>
          <p>
            {{ course.description || '실무 사례 중심으로 바로 적용할 수 있는 역량을 키우는 과정입니다.' }}
          </p>
        </article>
        <article class="panel info-block">
          <h3>이런 분께 추천해요</h3>
          <ul>
            <li>{{ displayCategory }} 관련 업무를 효율적으로 처리하고 싶은 직장인</li>
            <li>AI를 업무에 어떻게 활용할지 고민 중인 분</li>
          </ul>
        </article>
      </section>
    </main>

    <div v-else-if="loading" class="loading-center">
      <div class="spinner"></div>
    </div>

    <div v-else class="loading-center">
      <p class="empty-text">강의 정보를 불러오지 못했습니다.</p>
    </div>

    <EnrollSuccessModal
      :visible="showSuccessModal"
      :course-title="course?.title || ''"
      @close="showSuccessModal = false"
      @go-to-my-courses="goToMyCourses"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import DifficultyLevel from '@/components/DifficultyLevel.vue'
import EnrollSuccessModal from '@/components/EnrollSuccessModal.vue'
import { useCourseStore } from '@/store/course.js'
import { enrollmentApi } from '@/api/enrollment.js'
import { useAuthStore } from '@/store/auth.js'

const route = useRoute()
const router = useRouter()
const courseStore = useCourseStore()
const auth = useAuthStore()

const enrolling = ref(false)
const enrollError = ref('')
const enrollmentStatus = ref('NONE') // NONE | PENDING | ACTIVE
const showSuccessModal = ref(false)

const course = computed(() => courseStore.selectedCourse)
const loading = computed(() => courseStore.loading)
const isInstructor = computed(() => auth.user?.role === 'INSTRUCTOR')

const categoryConfig = {
  '백엔드': { bg: 'thumb-blue', thumb: 'spring_boot' },
  '프론트엔드': { bg: 'thumb-lilac', thumb: 'vue_js' },
  'DevOps': { bg: 'thumb-green', thumb: 'kubernetes' },
  '데이터': { bg: 'thumb-slate', thumb: 'python' },
  'AI': { bg: 'thumb-violet', thumb: 'generative_ai' },
}

const config = computed(() => categoryConfig[course.value?.category] || {})
const thumbBg = computed(() => config.value.bg || 'thumb-slate')

const displayCategory = computed(() => course.value?.category || '-')

const displayInstructorName = computed(() => {
  return (
    course.value?.instructorName ||
    course.value?.teacherName ||
    course.value?.instructor?.name ||
    course.value?.instructor_name ||
    course.value?.ownerName ||
    '강사 정보 없음'
  )
})

const displayDifficulty = computed(() => {
  const value = Number(
    course.value?.enrollmentCount ??
    course.value?.enrollment_count ??
    1
  )
  return Number.isNaN(value) ? 1 : value
})

const displayPrice = computed(() => {
  const value = Number(course.value?.price ?? 0)
  return Number.isNaN(value) ? '0' : value.toLocaleString()
})

const thumbSrc = computed(() => {
  const key = course.value?.thumbnail || config.value.thumb
  if (!key) return null

  try {
    return new URL(`../assets/images/courses/${key}.png`, import.meta.url).href
  } catch {
    return null
  }
})

const buttonLabel = computed(() => {
  if (isInstructor.value) return 'HR 담당자 계정은 신청 불가'
  if (enrollmentStatus.value === 'ACTIVE') return '내 강의 목록으로 이동'
  if (enrollmentStatus.value === 'PENDING') return '신청 완료 · HR 승인 대기'
  return '수강 신청'
})

const buttonDisabled = computed(() => {
  if (enrolling.value) return true
  if (isInstructor.value) return true
  if (enrollmentStatus.value === 'PENDING') return true
  return false
})

const helperText = computed(() => {
  if (isInstructor.value) {
    return 'HR 담당자 계정은 본인 과정을 수강 신청할 수 없습니다.'
  }

  if (enrollmentStatus.value === 'ACTIVE') {
    return '승인이 완료된 과정은 바로 수강할 수 있어요.'
  }

  if (enrollmentStatus.value === 'PENDING') {
    return '수강 신청이 접수되었습니다. HR 승인이 완료되면 내 강의 목록에서 확인할 수 있어요.'
  }

  return '신청 후 HR 승인이 완료되면 수강할 수 있어요.'
})

async function loadEnrollmentStatus() {
  if (!auth.user?.id || !course.value?.id || isInstructor.value) {
    enrollmentStatus.value = 'NONE'
    return
  }

  try {
    const res = await enrollmentApi.getMyEnrollments()
    console.log('[CourseDetail] my enrollments response =', res.data)

    const enrollments = Array.isArray(res.data?.data)
      ? res.data.data
      : Array.isArray(res.data)
        ? res.data
        : []

    const matched = enrollments.find(item => Number(item.courseId) === Number(course.value.id))

    if (!matched) {
      enrollmentStatus.value = 'NONE'
      return
    }

    enrollmentStatus.value = matched.status === 'ACTIVE' ? 'ACTIVE' : 'PENDING'
  } catch (e) {
    console.error('[CourseDetail] failed to load enrollment status:', e)
    enrollmentStatus.value = 'NONE'
  }
}

async function handlePrimaryAction() {
  enrollError.value = ''

  if (!course.value?.id) {
    enrollError.value = '강의 정보가 올바르지 않습니다.'
    return
  }

  if (isInstructor.value) {
    enrollError.value = 'HR 담당자 계정은 본인 과정을 수강 신청할 수 없습니다.'
    return
  }

  if (enrollmentStatus.value === 'ACTIVE') {
    router.push('/enrollments')
    return
  }

  if (enrollmentStatus.value === 'PENDING') {
    return
  }

  const confirmed = window.confirm(`"${course.value.title}" 과정을 수강 신청하시겠어요?`)
  if (!confirmed) return

  enrolling.value = true

  try {
    await enrollmentApi.enroll(course.value.id)
    enrollmentStatus.value = 'PENDING'
    showSuccessModal.value = true
  } catch (e) {
    console.error('[CourseDetail] enroll failed:', e)
    enrollError.value = e.response?.data?.message || '수강 신청에 실패했습니다.'
  } finally {
    enrolling.value = false
  }
}

function goToMyCourses() {
  showSuccessModal.value = false
  router.push('/enrollments')
}

onMounted(async () => {
  await courseStore.fetchCourse(route.params.id)
  console.log('[CourseDetail] selectedCourse =', courseStore.selectedCourse)
  await loadEnrollmentStatus()
})

watch(
  () => courseStore.selectedCourse,
  async (value) => {
    console.log('[CourseDetail] selectedCourse changed =', value)
    if (value?.id) {
      await loadEnrollmentStatus()
    }
  },
  { deep: true }
)
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

.detail-main {
  display: grid;
  grid-template-columns: 7fr 4fr;
  gap: 30px;
  align-items: start;
}

/* 좌측 히어로 */
.detail-hero {
  padding: 46px;
  min-height: 450px;
  border-radius: var(--sf-radius-lg);
  color: white;
  background: linear-gradient(145deg, var(--sf-ai-bg), var(--sf-ai-bg-mid) 60%, var(--sf-ai-bg-end));
  position: relative;
  overflow: hidden;
  box-shadow: var(--sf-shadow-md);
}

.detail-hero::after {
  content: '';
  position: absolute;
  width: 420px;
  height: 420px;
  border-radius: 50%;
  right: -170px;
  bottom: -220px;
  border: 1px solid rgba(170, 153, 255, 0.22);
  box-shadow: 0 0 0 80px rgba(140, 120, 255, 0.05);
}

.badge {
  display: inline-flex;
  align-items: center;
  min-height: 25px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 750;
  position: relative;
  z-index: 1;
}

.badge-indigo {
  color: #cfc8ff;
  background: rgba(141, 119, 255, 0.16);
}

.detail-title {
  position: relative;
  z-index: 1;
  max-width: 650px;
  margin: 20px 0 18px;
  font-size: 42px;
  line-height: 1.2;
  letter-spacing: -0.045em;
}

.detail-desc {
  position: relative;
  z-index: 1;
  max-width: 610px;
  margin: 0;
  color: rgba(255, 255, 255, 0.66);
  line-height: 1.7;
  font-size: 14px;
}

.detail-meta {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 24px;
  margin-top: 34px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 12px;
}

.benefit-row {
  position: absolute;
  left: 46px;
  right: 46px;
  bottom: 40px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.benefit {
  padding: 10px 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.83);
  font-size: 11px;
}

/* 우측 신청 카드 */
.panel {
  background: #ffffff;
  border: 1px solid var(--sf-border);
  border-radius: var(--sf-radius-md);
  box-shadow: var(--sf-shadow-sm);
}

.checkout-card {
  padding: 22px;
}

.checkout-thumb {
  height: 170px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.checkout-thumb img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: 20px;
}

.thumb-blue   { background: linear-gradient(135deg, #dfe8fa, #eaf0fa 55%, #b8ccf3); }
.thumb-violet { background: linear-gradient(135deg, #eee8ff, #d9cdfa 55%, #b9a5ee); }
.thumb-green  { background: linear-gradient(135deg, #dfeee9, #c6dfd6 55%, #8ab9a8); }
.thumb-slate  { background: linear-gradient(135deg, #e6e9ef, #c9d0dc 55%, #9ca9bc); }
.thumb-lilac  { background: linear-gradient(135deg, #f0edff, #ded8ff 55%, #b9adf5); }

.checkout-price {
  margin: 22px 0 16px;
  font-size: 28px;
  font-weight: 850;
  letter-spacing: -0.04em;
  color: var(--sf-ink);
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
}

.btn-primary {
  color: #fff;
  background: linear-gradient(135deg, var(--sf-indigo), #6d52ef);
  box-shadow: 0 10px 24px rgba(91, 80, 230, 0.22);
}

.btn-block {
  width: 100%;
}

.btn-disabled {
  color: #a2a8b8;
  border-color: var(--sf-border);
  background: #f6f7fa;
  box-shadow: none;
  cursor: not-allowed;
}

.error-msg {
  margin-top: 10px;
  font-size: 12px;
  color: #dc2626;
  padding: 8px 12px;
  background: #fef2f2;
  border-radius: 10px;
}

.helper {
  margin: 12px 0 16px;
  color: var(--sf-muted);
  text-align: center;
  font-size: 11px;
  line-height: 1.5;
}

.certificate {
  margin-top: 13px;
  padding-top: 13px;
  border-top: 1px solid var(--sf-border);
}

.certificate small {
  display: block;
  margin-top: 7px;
  text-align: center;
  color: var(--sf-subtle);
  font-size: 10px;
}

/* 하단 정보 패널 */
.detail-info {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 22px;
  margin-top: 24px;
}

.info-block {
  padding: 25px;
}

.info-block h3 {
  margin: 0 0 10px;
  font-size: 17px;
  color: var(--sf-ink);
}

.info-block p,
.info-block li {
  color: var(--sf-muted);
  font-size: 13px;
  line-height: 1.7;
}

.info-block ul {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-block li::before {
  content: '· ';
}

.empty-text {
  font-size: 14px;
  color: var(--sf-muted);
}

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
  .detail-main {
    grid-template-columns: 1fr;
  }

  .detail-info {
    grid-template-columns: 1fr;
  }

  .benefit-row {
    position: static;
    margin-top: 30px;
  }

  .detail-hero {
    min-height: 0;
  }
}
</style>
