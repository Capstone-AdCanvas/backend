FROM gradle:8.12.1-jdk17 AS builder
WORKDIR /home/gradle/project

# 전체 소스 복사 및 소유자 변경 (빌드 캐시 활용)
COPY --chown=gradle:gradle . .

# Gradle Wrapper를 사용해 bootJar 실행 (클린 후 JAR 생성)
RUN ./gradlew clean bootJar --no-daemon

# alpine 대신 alpine이 아닌 기본 이미지 사용 (Alpine 태그가 없어서)
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 빌드 단계에서 생성된 JAR 파일을 복사
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

# 외부 통신 포트 지정
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]