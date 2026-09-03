<template>
  <div class="landing">
    <AppHeader />

    <!-- 히어로 섹션 -->
    <section class="hero">
      <div class="hero-inner">
        <div class="hero-content fade-in-up">
          <span class="hero-badge">AI-POWERED HRD</span>
          <h1 class="hero-title">직무에 맞는 AI 교육,<br><em>더 똑똑하게</em></h1>
          <p class="hero-desc">직원에게는 필요한 교육을, HR에게는 더 정확한 교육 투자를 제공합니다.</p>
          <div class="hero-actions">
            <router-link to="/login" class="btn btn-primary btn-lg">무료로 시작하기</router-link>
            <router-link to="/courses" class="btn btn-outline btn-lg">강의 둘러보기</router-link>
          </div>
          <div class="hero-proof">
            <span>직무·역량 기반 추천</span>
            <span>HR 맞춤 설계</span>
            <span>빠른 수강 경험</span>
          </div>
        </div>
        <div class="hero-visual fade-in">
          <div class="roadmap-card" aria-label="AI 맞춤 학습 로드맵 과정">
            <div class="roadmap-heading">
              <span class="roadmap-eyebrow">PERSONALIZED AI LEARNING</span>
              <strong>나만의 성장 로드맵</strong>
            </div>
            <ol class="roadmap-steps">
              <li class="roadmap-step">
                <span class="step-icon step-icon-blue" aria-hidden="true">01</span>
                <span class="step-copy"><small>STEP 1</small><strong>현재 역량</strong></span>
              </li>
              <li class="roadmap-step">
                <span class="step-icon step-icon-purple" aria-hidden="true">02</span>
                <span class="step-copy"><small>STEP 2</small><strong>AI 역량 분석</strong></span>
              </li>
              <li class="roadmap-step">
                <span class="step-icon step-icon-teal" aria-hidden="true">03</span>
                <span class="step-copy"><small>STEP 3</small><strong>맞춤 학습 로드맵</strong></span>
              </li>
            </ol>
            <div class="roadmap-progress">
              <span>성장 경로 설계 완료</span>
              <strong>100%</strong>
            </div>
          </div>
        </div>
        <div class="hero-benefits">
          <div class="hero-benefit">
            <span class="benefit-icon">✦</span>
            <span><strong>직무·역량 기반 추천</strong><small>직원의 현재 역량에 꼭 맞는 과정을 제안합니다.</small></span>
          </div>
          <div class="hero-benefit">
            <span class="benefit-icon">✓</span>
            <span><strong>간편한 승인·관리</strong><small>신청부터 승인과 결제까지 한 흐름으로 연결합니다.</small></span>
          </div>
          <div class="hero-benefit">
            <span class="benefit-icon">↗</span>
            <span><strong>교육 투자 효율 향상</strong><small>필요한 교육에 예산을 집중할 수 있습니다.</small></span>
          </div>
        </div>
      </div>
    </section>

  </div>
</template>

<script setup>
import AppHeader from '@/components/AppHeader.vue'
</script>

<style scoped>
.landing { background: var(--color-bg-secondary); }

/* 히어로 */
.hero {
  min-height: calc(100vh - 64px);
  display: flex;
  align-items: flex-start;
  background:
    radial-gradient(circle at 75% 34%, rgba(117, 86, 255, 0.10), transparent 30%),
    linear-gradient(135deg, #f8f9ff 0%, #f5f7ff 55%, #f4fbff 100%);
  border-bottom: 1px solid var(--color-border);
  padding: clamp(64px, 8vh, 104px) 0 56px;
}
.hero-inner {
  width: 100%;
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 clamp(32px, 5vw, 80px);
  display: grid;
  grid-template-columns: minmax(0, 1.08fr) minmax(460px, 0.92fr);
  gap: clamp(56px, 7vw, 112px);
  align-items: center;
}
.hero-badge {
  display: inline-block;
  padding: 0;
  background: transparent;
  color: #6650e8;
  border-radius: 0;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 1.5px;
  margin-bottom: 18px;
}
.hero-title {
  font-size: clamp(48px, 4.2vw, 64px);
  font-weight: 700;
  line-height: 1.25;
  letter-spacing: -0.5px;
  color: var(--color-text-primary);
  margin-bottom: 16px;
}
.hero-title em { color: #6350e8; font-style: normal; }
.hero-desc {
  font-size: 18px;
  color: var(--color-text-secondary);
  line-height: 1.7;
  max-width: 620px;
  margin-bottom: 34px;
}
.hero-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}
.btn-lg { padding: 14px 32px; font-size: 16px; }
.hero-proof {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 20px;
}
.hero-proof span { color: var(--color-text-secondary); font-size: 13px; }
.hero-proof span::before { content: '✓'; margin-right: 6px; color: #6650e8; font-weight: 700; }
.hero-visual {
  display: flex;
  align-items: center;
  justify-content: center;
}
.roadmap-card {
  width: 100%;
  max-width: 560px;
  padding: clamp(28px, 2.4vw, 38px);
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(92, 113, 145, 0.16);
  border-radius: 24px;
  box-shadow: 0 24px 60px rgba(61, 84, 122, 0.14);
}
.roadmap-heading { display: flex; flex-direction: column; gap: 6px; margin-bottom: 22px; }
.roadmap-heading strong { font-size: 24px; color: var(--color-text-primary); }
.roadmap-eyebrow {
  color: var(--color-primary);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1.2px;
}
.roadmap-steps { display: flex; flex-direction: column; gap: 10px; margin: 0; padding: 0; list-style: none; }
.roadmap-step {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  min-height: 78px;
  padding: 14px 18px;
  background: #f8fbff;
  border: 1px solid #e8eef6;
  border-radius: 16px;
}
.roadmap-step:not(:last-child)::after {
  content: '';
  position: absolute;
  z-index: 1;
  left: 35px;
  bottom: -12px;
  width: 2px;
  height: 13px;
  background: #dce6f3;
}
.step-icon {
  position: relative;
  z-index: 2;
  display: grid;
  place-items: center;
  flex: 0 0 46px;
  height: 46px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 700;
}
.step-icon-blue { color: #2563eb; background: #e8f1ff; }
.step-icon-purple { color: #7c3aed; background: #f1eafe; }
.step-icon-teal { color: #087f5b; background: #e4f8f1; }
.step-copy { display: flex; flex-direction: column; gap: 3px; }
.step-copy small { color: var(--color-text-secondary); font-size: 10px; font-weight: 700; letter-spacing: 0.7px; }
.step-copy strong { color: var(--color-text-primary); font-size: 17px; }
.roadmap-progress {
  display: flex;
  justify-content: space-between;
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid #edf1f6;
  color: var(--color-text-secondary);
  font-size: 13px;
}
.roadmap-progress strong { color: var(--color-primary); }
.hero-benefits {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: clamp(28px, 4vw, 64px);
  width: 100%;
  margin-top: clamp(28px, 5vh, 56px);
  padding-top: 32px;
  border-top: 1px solid rgba(88, 99, 126, 0.12);
}
.hero-benefit { display: flex; align-items: flex-start; gap: 15px; }
.benefit-icon {
  display: grid;
  place-items: center;
  flex: 0 0 42px;
  height: 42px;
  color: #6650e8;
  background: #efedff;
  border-radius: 9px;
  font-size: 16px;
  font-weight: 700;
}
.hero-benefit > span:last-child { display: flex; flex-direction: column; gap: 7px; }
.hero-benefit strong { color: var(--color-text-primary); font-size: 18px; line-height: 1.35; }
.hero-benefit small { color: var(--color-text-secondary); font-size: 15px; line-height: 1.65; }

@media (max-width: 900px) {
  .hero { min-height: auto; padding: 64px 0 56px; }
  .hero-inner { grid-template-columns: 1fr; gap: 40px; }
  .hero-content { text-align: center; }
  .hero-desc { margin-right: auto; margin-left: auto; }
  .hero-actions, .hero-proof { justify-content: center; }
  .roadmap-card { max-width: 560px; }
  .hero-benefits { gap: 18px; }
}

@media (max-width: 600px) {
  .hero { padding: 48px 0 44px; }
  .hero-inner { padding-right: 18px; padding-left: 18px; }
  .hero-title { font-size: 36px; }
  .hero-desc { font-size: 16px; }
  .hero-actions { flex-direction: column; }
  .hero-actions .btn { width: 100%; text-align: center; }
  .hero-proof { gap: 8px 14px; }
  .roadmap-card { padding: 20px; border-radius: 20px; }
  .roadmap-step { padding: 10px 12px; }
  .hero-benefits { grid-template-columns: 1fr; margin-top: 4px; }
}
</style>
