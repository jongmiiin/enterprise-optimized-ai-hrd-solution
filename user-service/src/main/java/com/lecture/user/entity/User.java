package com.lecture.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // ===== SkillFit AI: 직원 역량 프로필 (더미데이터, nullable) =====
    // recommend-service가 소비하는 프로필 계약(백엔드추천 api명세서 §3)과 동일 구조
    @Column(name = "employee_code")
    private String employeeCode;

    @Column(name = "job")
    private String job;              // 직무 코드 (예: DATA_PLATFORM_ENGINEER)

    @Column(name = "job_label")
    private String jobLabel;         // 한글 라벨 (예: 데이터 플랫폼 엔지니어)

    @Column(name = "overall_level")
    private String overallLevel;     // 상 / 중 / 하

    @Column(name = "career_goal", length = 500)
    private String careerGoal;

    // 5개 역량 점수 (한글키 그대로, 0~100)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_competency_scores", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "competency")
    @Column(name = "score")
    @Builder.Default
    private Map<String, Integer> competencyScores = new HashMap<>();

    // 보유 스킬 (직무별 가변 키, 0~5)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_skills", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "skill_code")
    @Column(name = "level")
    @Builder.Default
    private Map<String, Integer> skills = new HashMap<>();

    // 역량 오각형(radar)용 5개 역량 (ai-capability-model 체계, 영문키, 1~5)
    // 키: aiLiteracy, jobAiApplication, promptSkill, dataLiteracy, aiEthicsSecurity
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_competencies", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "competency_key")
    @Column(name = "score")
    @Builder.Default
    private Map<String, Integer> competencies = new HashMap<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum Role {
        STUDENT, INSTRUCTOR
    }

    // 프로필 세팅용 (DataInitializer)
    public void applyProfile(String employeeCode, String job, String jobLabel,
                             String overallLevel, String careerGoal,
                             Map<String, Integer> competencyScores, Map<String, Integer> skills) {
        this.employeeCode = employeeCode;
        this.job = job;
        this.jobLabel = jobLabel;
        this.overallLevel = overallLevel;
        this.careerGoal = careerGoal;
        this.competencyScores = competencyScores;
        this.skills = skills;
    }

    // radar용 5개 역량(1~5) 세팅 (DataInitializer)
    public void applyCompetencies(Map<String, Integer> competencies) {
        this.competencies = competencies;
    }
}
