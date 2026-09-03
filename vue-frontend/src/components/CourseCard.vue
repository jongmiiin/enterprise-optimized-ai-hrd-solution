<template>
  <router-link :to="`/courses/${course.id}`" class="course-card">
    <!-- 썸네일 -->
    <div class="card-thumb" :class="thumbBg">
      <img v-if="thumbSrc" :src="thumbSrc" :alt="course.title" class="thumb-img" />
      <span class="thumb-shape"></span>
    </div>

    <!-- 내용 -->
    <div class="card-body">
      <div class="card-top-row">
        <span class="badge badge-indigo">{{ course.category }}</span>
        <span v-if="recommendation" class="recommend-score">↗ 추천 {{ recommendation.score }}%</span>
      </div>
      <h3 class="card-title">{{ course.title }}</h3>
      <p v-if="!recommendation" class="card-desc">{{ course.description }}</p>
      <p v-else class="card-reason">{{ recommendation.reason }}</p>

      <div class="card-meta">
        <span class="price">₩{{ Number(course.price).toLocaleString() }}</span>
        <span class="enrolled">◯ {{ course.enrollmentCount?.toLocaleString() || 0 }}명 수강</span>
      </div>
    </div>
  </router-link>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  course: { type: Object, required: true },
  recommendation: { type: Object, default: null }
})

const categoryConfig = {
  '백엔드':     { bg: 'thumb-blue',  thumb: 'spring_boot' },
  '프론트엔드': { bg: 'thumb-lilac', thumb: 'vue_js' },
  'DevOps':    { bg: 'thumb-green', thumb: 'docker' },
  '데이터':     { bg: 'thumb-slate', thumb: 'python' },
  'AI':        { bg: 'thumb-violet', thumb: 'generative_ai' },
}

const config = computed(() => categoryConfig[props.course.category] || { bg: 'thumb-slate' })
const thumbBg = computed(() => config.value.bg)

// 썸네일 이미지 동적 import
const thumbSrc = computed(() => {
  const key = props.course.thumbnail || config.value.thumb
  if (!key) return null
  try {
    return new URL(`../assets/images/courses/${key}.png`, import.meta.url).href
  } catch {
    return null
  }
})
</script>

<style scoped>
.course-card {
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(255, 255, 255, 0.86);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: var(--sf-shadow-sm);
  transition: var(--transition);
  cursor: pointer;
}
.course-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--sf-shadow-md);
}
.card-thumb {
  height: 132px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: #e9ebf5;
}
.card-thumb::before {
  content: '';
  position: absolute;
  width: 190px;
  height: 190px;
  right: -40px;
  top: -74px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.7);
}
.thumb-shape {
  position: absolute;
  width: 120px;
  height: 120px;
  left: 30px;
  bottom: -48px;
  border-radius: 36px;
  transform: rotate(22deg);
  background: rgba(255, 255, 255, 0.55);
  border: 1px solid rgba(255, 255, 255, 0.7);
}
.thumb-blue   { background: linear-gradient(135deg, #dfe8fa, #eaf0fa 55%, #b8ccf3); }
.thumb-violet { background: linear-gradient(135deg, #eee8ff, #d9cdfa 55%, #b9a5ee); }
.thumb-green  { background: linear-gradient(135deg, #dfeee9, #c6dfd6 55%, #8ab9a8); }
.thumb-amber  { background: linear-gradient(135deg, #fff1d8, #f7ddb0 55%, #e3b45e); }
.thumb-slate  { background: linear-gradient(135deg, #e6e9ef, #c9d0dc 55%, #9ca9bc); }
.thumb-lilac  { background: linear-gradient(135deg, #f0edff, #ded8ff 55%, #b9adf5); }
.thumb-img {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: 20px;
}
.card-body {
  padding: 17px 18px 18px;
  display: flex;
  flex-direction: column;
  flex: 1;
}
.card-top-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.badge-indigo {
  color: var(--sf-indigo-dark);
  background: var(--sf-indigo-soft);
}
.recommend-score {
  color: var(--sf-success);
  font-weight: 850;
  font-size: 12px;
}
.card-title {
  margin: 12px 0 7px;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: -0.025em;
  color: var(--sf-ink);
}
.card-desc {
  height: 34px;
  margin: 0;
  color: var(--sf-muted);
  font-size: 12px;
  line-height: 1.55;
  overflow: hidden;
}
.card-reason {
  margin: 0;
  padding: 10px 11px;
  border-radius: 11px;
  background: #f6f5ff;
  color: #5a5670;
  font-size: 12px;
  line-height: 1.5;
}
.card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 14px;
  padding-top: 13px;
  border-top: 1px solid var(--sf-border);
}
.price {
  font-size: 14px;
  font-weight: 800;
  color: var(--sf-ink);
}
.enrolled {
  color: var(--sf-subtle);
  font-size: 10px;
}
</style>
