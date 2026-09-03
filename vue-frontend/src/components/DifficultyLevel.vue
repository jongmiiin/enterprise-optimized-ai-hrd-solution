<template>
  <span class="difficulty" :aria-label="`난이도 ${clampedLevel} / 5`">
    <span
      v-for="n in 5"
      :key="n"
      class="dot"
      :class="{ filled: n <= clampedLevel }"
    ></span>
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 강의 난이도 (1~5). 범위를 벗어나면 1~5로 보정
  level: { type: [Number, String], default: 1 }
})

const clampedLevel = computed(() => {
  const n = Math.round(Number(props.level))
  if (Number.isNaN(n)) return 1
  return Math.min(5, Math.max(1, n))
})
</script>

<style scoped>
.difficulty {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: transparent;
  border: 1.5px solid var(--dot-empty, var(--sf-border));
  transition: var(--transition);
}
.dot.filled {
  background: var(--dot-color, var(--sf-indigo));
  border-color: var(--dot-color, var(--sf-indigo));
}
</style>
