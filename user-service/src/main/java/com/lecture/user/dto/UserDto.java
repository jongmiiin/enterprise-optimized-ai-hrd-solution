package com.lecture.user.dto;

import com.lecture.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class UserDto {

    // 회원가입 요청
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegisterRequest {
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
        private String password;

        @NotBlank(message = "이름은 필수입니다")
        private String name;

        private User.Role role; // STUDENT or INSTRUCTOR
    }

    // 사용자 정보 응답
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserResponse {
        private Long id;
        private String email;
        private String name;
        private User.Role role;
        private LocalDateTime createdAt;

        public static UserResponse from(User user) {
            return UserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getRole())
                    .createdAt(user.getCreatedAt())
                    .build();
        }
    }

    // 역량 프로필 응답 (recommend-service 내부 호출용 — 백엔드추천 api명세서 §3)
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompetencyProfileResponse {
        private Long userId;
        private String employeeCode;
        private String name;
        private String job;
        private String jobLabel;
        private String overallLevel;
        private String careerGoal;
        private java.util.Map<String, Integer> competencyScores;
        private java.util.Map<String, Integer> skills;
        private java.util.Map<String, Integer> competencies;  // radar용 영문키 5개(1~5)

        public static CompetencyProfileResponse from(User user) {
            return CompetencyProfileResponse.builder()
                    .userId(user.getId())
                    .employeeCode(user.getEmployeeCode())
                    .name(user.getName())
                    .job(user.getJob())
                    .jobLabel(user.getJobLabel())
                    .overallLevel(user.getOverallLevel())
                    .careerGoal(user.getCareerGoal())
                    .competencyScores(user.getCompetencyScores())
                    .skills(user.getSkills())
                    .competencies(user.getCompetencies())
                    .build();
        }
    }

    // 공통 API 응답 래퍼
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public static <T> ApiResponse<T> success(T data) {
            return ApiResponse.<T>builder()
                    .success(true)
                    .message("성공")
                    .data(data)
                    .build();
        }

        public static <T> ApiResponse<T> error(String message) {
            return ApiResponse.<T>builder()
                    .success(false)
                    .message(message)
                    .build();
        }
    }
}
