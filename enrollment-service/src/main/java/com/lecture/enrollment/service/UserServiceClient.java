package com.lecture.enrollment.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final WebClient.Builder webClientBuilder;

    /**
     * User Service가 소유한 사용자 정보를 내부 API로 조회한다.
     */
    public UserInfo getUser(Long userId) {
        try {
            UserInfo user = webClientBuilder.build()
                    .get()
                    .uri("http://user-service/api/users/internal/{id}", userId)
                    .retrieve()
                    .bodyToMono(UserInfo.class)
                    .block();

            if (user == null) {
                throw new IllegalStateException("User Service 응답 본문이 비어 있습니다");
            }
            return user;
        } catch (Exception e) {
            log.error("[UserServiceClient] 사용자 조회 실패 - userId: {}, error: {}", userId, e.getMessage());
            throw new RuntimeException("User Service 연결 실패");
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long id;
        private String email;
        private String name;
        private String role;
    }
}
