# 1단계: 빌드 스테이지 - Gradle로 jar 파일을 생성
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# 의존성 캐시를 위해 빌드 스크립트만 먼저 복사 (소스 코드 변경 시에도 의존성은 재다운로드 안 하도록)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# 이제 소스 코드를 복사하고 빌드
COPY src src
RUN ./gradlew bootJar --no-daemon

# 2단계: 실행 스테이지 - JDK 없이 JRE만으로 가볍게 실행
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]