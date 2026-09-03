# syntax=docker/dockerfile:1
# 1단계: Gradle로 JAR 빌드
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# 의존성 캐시 최적화 (소스 변경 시 재다운로드 방지)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY init-mirror.gradle .
RUN chmod +x gradlew
# Maven Central 미러(-I) + Gradle 캐시 마운트로 429(rate limit) 회피
RUN --mount=type=cache,target=/root/.gradle ./gradlew -I init-mirror.gradle dependencies --no-daemon

# 소스 복사 및 빌드
COPY src src
RUN --mount=type=cache,target=/root/.gradle ./gradlew -I init-mirror.gradle bootJar -x test --no-daemon

# 2단계: 실행 환경 (JRE만 포함 - 이미지 경량화)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]