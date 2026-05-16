# 1단계: 빌드 스테이지
FROM gradle:8.10.2-jdk21 AS build
WORKDIR /home/gradle/project
COPY . .
RUN gradle clean build -x test && \
    cp build/libs/$(ls build/libs | grep -v plain | head -n 1) build/libs/app.jar

# 2단계: 실행 스테이지
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /home/gradle/project/build/libs/app.jar app.jar

# 서버 포트 및 시간대 설정
EXPOSE 8080
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 💡 [수정] additional-location을 사용하여 기존 설정을 보존하고 배포 설정만 덮어씀
ENTRYPOINT ["java", "-Xmx512m", "-Dspring.config.additional-location=file:/app/config/application.properties", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
