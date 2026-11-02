# 1단계: 빌드 환경 (JDK 25를 포함하여 Fat JAR 생성)
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /workspace

# 빌드 캐시 효율화를 위해 빌드 스크립트 메타데이터를 먼저 복사
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .

# 윈도우 환경에서 커밋 시 실행 권한 누락 방지 및 의존성 사전 다운로드
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

# 실제 비즈니스 소스코드를 복사하고 테스트를 제외한 프로덕션 빌드 수행
COPY src ./src
RUN ./gradlew clean bootJar -x test --no-daemon

# 2단계: 최종 프로덕션 런타임 환경 (최소화된 JRE 25 기반)
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# 보안 강화를 위해 루트 계정이 아닌 일반 사용자 계정 생성 및 적용
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser:appgroup

# 빌더 스테이지에서 컴파일 및 패키징 완료된 실행형 JAR 아티팩트만 복제
COPY --from=builder /workspace/build/libs/*.jar app.jar

# 운영 프로파일 활성화 (application-prod.yaml)
ENV SPRING_PROFILES_ACTIVE=prod

# 애플리케이션 포트 노출 선언
EXPOSE 8080

# 컨테이너 기동 엔트리포인트 정의
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70", "-jar", "app.jar"]