<template>
  <Teleport to="body">
    <Transition name="modal-fade">
      <div v-if="visible" class="modal-backdrop" @click.self="$emit('close')">
        <div class="modal-card" role="dialog" aria-modal="true" :aria-label="`${courseTitle} 수강 신청 완료`">
          <button type="button" class="modal-close" aria-label="닫기" @click="$emit('close')">✕</button>

          <div class="modal-icon">✓</div>

          <h2 class="modal-title">수강 신청이 완료되었어요</h2>
          <p class="modal-course">{{ courseTitle }}</p>
          <p class="modal-desc">HR 승인이 완료되면 수강할 수 있어요.<br />승인 상태는 내 강의 목록에서 확인할 수 있어요.</p>

          <div class="modal-actions">
            <button type="button" class="btn btn-primary btn-block" @click="$emit('go-to-my-courses')">
              내 강의 목록으로 이동
            </button>
            <button type="button" class="btn btn-secondary btn-block" @click="$emit('close')">
              계속 둘러보기
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { onBeforeUnmount, onMounted, watch } from 'vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  courseTitle: { type: String, default: '' }
})

const emit = defineEmits(['close', 'go-to-my-courses'])

function handleKeydown(e) {
  if (e.key === 'Escape' && props.visible) {
    emit('close')
  }
}

onMounted(() => window.addEventListener('keydown', handleKeydown))
onBeforeUnmount(() => window.removeEventListener('keydown', handleKeydown))

watch(
  () => props.visible,
  (isVisible) => {
    document.body.style.overflow = isVisible ? 'hidden' : ''
  }
)
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(16, 19, 35, 0.56);
  backdrop-filter: blur(4px);
}

.modal-card {
  position: relative;
  width: 100%;
  max-width: 420px;
  padding: 40px 36px 32px;
  border-radius: var(--sf-radius-lg, 26px);
  background: #ffffff;
  box-shadow: 0 30px 80px rgba(16, 19, 35, 0.28);
  text-align: center;
}

.modal-close {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 30px;
  height: 30px;
  border: 0;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--sf-subtle, #9198aa);
  background: transparent;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.2s ease, color 0.2s ease;
}

.modal-close:hover {
  background: #f1f2f7;
  color: var(--sf-ink, #101323);
}

.modal-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  font-weight: 800;
  color: #087858;
  background: var(--sf-success-bg, #e8f8f2);
}

.modal-title {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--sf-ink, #101323);
}

.modal-course {
  margin: 0 0 14px;
  font-size: 14px;
  font-weight: 700;
  color: var(--sf-indigo-dark, #4438ca);
}

.modal-desc {
  margin: 0 0 28px;
  color: var(--sf-muted, #697086);
  font-size: 13px;
  line-height: 1.65;
}

.modal-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.btn {
  height: 46px;
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

.btn-block {
  width: 100%;
}

.btn-primary {
  color: #fff;
  background: linear-gradient(135deg, var(--sf-indigo, #5b50e6), #6d52ef);
  box-shadow: 0 10px 24px rgba(91, 80, 230, 0.22);
}

.btn-secondary {
  color: var(--sf-indigo-dark, #4438ca);
  border-color: rgba(91, 80, 230, 0.22);
  background: rgba(255, 255, 255, 0.7);
}

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.2s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-active .modal-card,
.modal-fade-leave-active .modal-card {
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.modal-fade-enter-from .modal-card,
.modal-fade-leave-to .modal-card {
  transform: translateY(12px) scale(0.98);
  opacity: 0;
}
</style>
