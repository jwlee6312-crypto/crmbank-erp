# 1단계: 빌드 스테이지
FROM eclipse-temurin:21-jdk-alpine AS build-stage
WORKDIR /app

# Gradle 래퍼 및 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 💡 윈도우 줄바꿈 제거 및 실행 권한 부여 (도커 빌드 안전성 강화)
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# 의존성 다운로드
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사 및 빌드
COPY src src
RUN ./gradlew bootJar --no-daemon

# 2단계: 실행 스테이지
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build-stage /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
