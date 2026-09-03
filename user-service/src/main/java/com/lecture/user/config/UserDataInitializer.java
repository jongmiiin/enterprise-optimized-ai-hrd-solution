package com.lecture.user.config;

import com.lecture.user.entity.User;
import com.lecture.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * SkillFit AI: 직원 역량 프로필 더미데이터 시드.
 * init-db/02_skillfit_seed.sql 이 생성한 users(10001~10003)에 프로필을 채운다.
 * - competencyScores(한글키 0~100) + skills(0~5): recommend 알고리즘용(팀원 A 체계)
 * - competencies(영문키 1~5): 마이페이지 역량 오각형(radar)용(ai-capability-model 체계)
 * 멱등: 이미 채워진 항목은 건너뛴다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seed(10001L, "skala1", "DATA_PLATFORM_ENGINEER", "데이터 플랫폼 엔지니어", "중",
                "데이터 사이언스·온톨로지 데이터 플랫폼",
                Map.of("AI 기초 이해", 68, "데이터 활용", 84, "LLM 활용", 57, "AI 서비스 개발", 62, "AI 윤리·보안", 51),
                Map.of("DATA_ENGINEERING", 3, "CLOUD_INFRA", 2, "ONTOLOGY", 1, "MLOPS", 1, "LLM", 2),
                Map.of("aiLiteracy", 3, "jobAiApplication", 3, "promptSkill", 3, "dataLiteracy", 4, "aiEthicsSecurity", 3));

        seed(10002L, "skala2", "UX_UI_DESIGNER", "AI UX/UI 디자이너", "하",
                "UX/UI·AI 프로덕트 디자인",
                Map.of("AI 기초 이해", 27, "데이터 활용", 32, "LLM 활용", 24, "AI 서비스 개발", 35, "AI 윤리·보안", 43),
                Map.of("AI_BASIC", 0, "UX_UI", 1, "PROTOTYPING", 0, "UX_RESEARCH", 1, "AI_PRODUCT_DESIGN", 0),
                Map.of("aiLiteracy", 1, "jobAiApplication", 2, "promptSkill", 1, "dataLiteracy", 2, "aiEthicsSecurity", 2));

        seed(10003L, "skala3", "AI_BACKEND_DEVELOPER", "AI 백엔드 개발자", "중",
                "딥러닝·LLM 백엔드 서비스",
                Map.of("AI 기초 이해", 61, "데이터 활용", 55, "LLM 활용", 42, "AI 서비스 개발", 78, "AI 윤리·보안", 49),
                Map.of("BACKEND", 4, "MACHINE_LEARNING", 1, "DEEP_LEARNING", 1, "LLM", 1, "RAG", 0, "MLOPS", 2),
                Map.of("aiLiteracy", 3, "jobAiApplication", 4, "promptSkill", 2, "dataLiteracy", 3, "aiEthicsSecurity", 2));
    }

    private void seed(Long userId, String employeeCode, String job, String jobLabel,
                      String overallLevel, String careerGoal,
                      Map<String, Integer> competencyScores, Map<String, Integer> skills,
                      Map<String, Integer> competencies) {
        userRepository.findById(userId).ifPresent(user -> {
            boolean changed = false;

            if (user.getJob() == null) {
                user.applyProfile(employeeCode, job, jobLabel, overallLevel, careerGoal,
                        new HashMap<>(competencyScores), new HashMap<>(skills));
                changed = true;
            }
            // 역량 오각형(radar)용 competencies는 별도로 비어있을 때 시드 (기존 프로필 사용자도 보정)
            if (user.getCompetencies() == null || user.getCompetencies().isEmpty()) {
                user.applyCompetencies(new HashMap<>(competencies));
                changed = true;
            }

            if (changed) {
                userRepository.save(user);
                log.info("[UserDataInitializer] 역량 프로필 시드 - userId: {}, job: {}", userId, job);
            }
        });
    }
}
