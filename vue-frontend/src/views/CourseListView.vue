<template>
  <div class="page-wrapper">
    <AppHeader />

    <main class="page shell">
      <div class="title-row">
        <div>
          <p class="eyebrow">COURSE CATALOG</p>
          <h1 class="page-title">교육과정</h1>
          <p class="page-desc">업무에 필요한 AI 역량을 과정별로 살펴보세요.</p>
        </div>

        <div class="title-row-actions">
          <router-link
            v-if="isInstructor"
            to="/courses/new"
            class="btn btn-primary create-course-btn"
          >
            교육과정 등록
          </router-link>

          <div class="segmented">
            <button
              v-for="sort in sortOptions"
              :key="sort.value"
              type="button"
              :class="['segment', { active: selectedSort === sort.value }]"
              @click="selectedSort = sort.value"
            >
              {{ sort.label }}
            </button>
          </div>
        </div>
      </div>

      <!-- 카테고리 필터 -->
      <div class="filter-row">
        <button
          v-for="cat in categories"
          :key="cat"
          type="button"
          :class="['filter-chip', { active: selectedCategory === cat }]"
          @click="selectCategory(cat)"
        >
          {{ cat }}
        </button>
      </div>

      <!-- 로딩 -->
      <div v-if="loading" class="course-grid">
        <div v-for="i in 6" :key="i" class="skeleton-card">
          <div class="skeleton-thumb"></div>
          <div class="skeleton-body">
            <div class="skeleton-line short"></div>
            <div class="skeleton-line"></div>
            <div class="skeleton-line medium"></div>
          </div>
        </div>
      </div>

      <!-- 강의 그리드 -->
      <section v-else-if="sortedCourses.length" class="course-grid fade-in">
        <CourseCard
          v-for="course in sortedCourses"
          :key="course.id"
          :course="course"
        />
      </section>

      <!-- 빈 상태 -->
      <div v-else class="empty-state">
        <p>해당 카테고리의 교육과정이 없습니다.</p>

        <router-link
          v-if="isInstructor"
          to="/courses/new"
          class="btn btn-primary empty-action-btn"
        >
          첫 교육과정 등록하기
        </router-link>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import AppHeader from '@/components/AppHeader.vue'
import CourseCard from '@/components/CourseCard.vue'
import { useCourseStore } from '@/store/course.js'
import { useAuthStore } from '@/store/auth.js'

const courseStore = useCourseStore()
const auth = useAuthStore()

const { categories, loading } = courseStore

const sortOptions = [
  { value: 'recommend', label: '추천순' },
  { value: 'difficulty', label: '난이도순' },
  { value: 'price', label: '가격순' }
]
const selectedSort = ref('recommend')

const selectedCategory = computed(() => courseStore.selectedCategory)
const isInstructor = computed(() => auth.user?.role === 'INSTRUCTOR')

const filteredCourses = computed(() => {
  if (!Array.isArray(courseStore.courses)) return []
  if (selectedCategory.value === '전체') return courseStore.courses
  return courseStore.courses.filter(c => c.category === selectedCategory.value)
})

const sortedCourses = computed(() => {
  const list = [...filteredCourses.value]

  if (selectedSort.value === 'difficulty') {
    return list.sort((a, b) => (a.enrollmentCount || 1) - (b.enrollmentCount || 1))
  }

  if (selectedSort.value === 'price') {
    return list.sort((a, b) => (a.price || 0) - (b.price || 0))
  }

  return list
})

function selectCategory(cat) {
  courseStore.setCategory(cat)
}

onMounted(() => {
  courseStore.fetchCourses()
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

.title-row-actions {
  display: flex;
  align-items: center;
  gap: 12px;
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

.create-course-btn {
  white-space: nowrap;
  text-decoration: none;
}

/* 정렬 세그먼트 */
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

/* 카테고리 필터 */
.filter-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 26px;
}

.filter-chip {
  height: 36px;
  padding: 0 16px;
  border-radius: 20px;
  border: 1.5px solid var(--sf-border);
  color: var(--sf-muted);
  background: rgba(255, 255, 255, 0.7);
  font-size: 13px;
  font-weight: 600;
  transition: var(--transition);
}

.filter-chip:hover {
  border-color: var(--sf-indigo);
  color: var(--sf-indigo-dark);
}

.filter-chip.active {
  color: var(--sf-indigo-dark);
  background: var(--sf-indigo-soft);
  border-color: rgba(91, 80, 230, 0.2);
}

/* 강의 그리드 */
.course-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
}

/* 스켈레톤 */
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
  padding: 17px 18px 18px;
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

.skeleton-line.medium {
  width: 70%;
}

@keyframes shimmer {
  to {
    background-position: -200% 0;
  }
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
  text-decoration: none;
}

@media (max-width: 1200px) {
  .course-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 720px) {
  .title-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .title-row-actions {
    width: 100%;
    justify-content: space-between;
  }

  .course-grid {
    grid-template-columns: 1fr;
  }
}
</style>
