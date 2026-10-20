# 1. 시스템 아키텍처 설계서 (하루배움 LMS)

## 목차
- [1. 시스템 아키텍처 설계서 (하루배움 LMS)](#1-시스템-아키텍처-설계서-하루배움-lms)
- [1.1 시스템 아키텍처 개요](#11-시스템-아키텍처-개요)
- [1.2 AWS 클라우드 및 컨테이너 인프라 아키텍처](#12-aws-클라우드-및-컨테이너-인프라-아키텍처)
- [1.3 GitHub Actions 기반 CI/CD 배포 자동화 파이프라인](#13-github-actions-기반-cicd-배포-자동화-파이프라인)
- [1.4 핵심 비즈니스 데이터 흐름도](#14-핵심-비즈니스-데이터-흐름도)

---

## 1.1 시스템 아키텍처 개요

하루배움 플랫폼은 프론트엔드와 백엔드가 물리적으로 분리된 계층형 RESTful API 아키텍처를 채택하며, Spring Boot 코어 서버와 Spring Data JPA, QueryDSL, Spring Batch, 외부 LLM AI 서비스 및 포트원 결제 게이트웨이가 유기적으로 결합된 구조로 동작.

```mermaid
graph TD
    Client["React SPA 클라이언트 (브라우저)"]
    CloudFront["AWS CloudFront (CDN / HTTPS)"]
    S3_Static["AWS S3 정적 웹 호스팅 (React SPA)"]
    S3_Media["AWS S3 미디어 버킷 (강의자료, 썸네일, 과제 파일)"]
    EC2["AWS EC2 인스턴스 (Amazon Linux 2023)"]
    Docker_Nginx["Nginx 리버스 프록시 (컨테이너 / 443, 80)"]
    Docker_App["Spring Boot API & Batch 서버 (컨테이너 / 8080)"]
    RDS_DB["AWS RDS / Docker MySQL (운영 DB)"]
    PG_Gateway["포트원 결제 게이트웨이 (PG사 API)"]
    AI_Service["외부 LLM API (OpenAI / Gemini)"]

    Client -->|정적 리소스 요청| CloudFront
    CloudFront -->|오리진 페치| S3_Static
    Client -->|미디어 파일 스트리밍/다운로드| CloudFront
    CloudFront -->|미디어 객체 페치| S3_Media

    Client -->|"REST API / SSE 요청 (JSON, JWT)"| EC2
    EC2 --> Docker_Nginx
    Docker_Nginx -->|리버스 프록시 포워딩| Docker_App
    Docker_App -->|Spring Data JPA / QueryDSL / JDBC| RDS_DB
    Docker_App -->|강의자료 및 과제물 파일 업로드| S3_Media

    Client -->|결제창 SDK 호출| PG_Gateway
    PG_Gateway -->|결제 승인 응답| Client
    Client -->|결제 사후검증 요청| Docker_Nginx
    Docker_App -->|REST API 결제 단건 조회 검증| PG_Gateway
    PG_Gateway -->|"비동기 웹훅 (Webhook)"| Docker_Nginx

    Docker_App -->|학사 규정 및 강좌 RAG 프롬프트 질의| AI_Service
    AI_Service -->|스트리밍 토큰 반환| Docker_App
    Docker_App -->|SSE 실시간 타자 효과 스트리밍| Client
```

---

## 1.2 AWS 클라우드 및 컨테이너 인프라 아키텍처

### 1.2.1 프론트엔드 호스팅 및 CDN 캐싱 (AWS S3 + CloudFront)
- React SPA 정적 빌드 결과물 배포:
  - Vite 기반으로 빌드된 React SPA 정적 아티팩트(HTML, JS, CSS, 이미지 에셋)를 AWS S3 정적 웹 호스팅 버킷에 배포.
  - S3 버킷 앞단에 AWS CloudFront CDN을 연동하여 전 세계 엣지 로케이션 캐싱 및 SSL/TLS(HTTPS) 인증서 종단 처리.
  - SPA 브라우저 히스토리 라우팅 특성을 지원하기 위해 CloudFront 사용자 정의 오류 응답(Custom Error Response)을 설정하여 403 및 404 발생 시 `/index.html`을 HTTP 200으로 반환하도록 구성.

### 1.2.2 백엔드 컨테이너 가상화 (AWS EC2 + Docker Compose)
- 도커 컨테이너 격리 및 리버스 프록시:
  - AWS EC2 t3.small/medium 인스턴스에 Docker Engine 및 Docker Compose 환경 구축.
  - Nginx 컨테이너(호스트 80/443 포트 바인딩)와 Spring Boot 애플리케이션 컨테이너(8080 포트)를 브리지 도커 네트워크로 상호 격리.
  - Nginx가 Let's Encrypt SSL 인증서를 관리하고 외부 클라이언트의 HTTPS 요청을 복호화한 후 내부 8080 포트의 Spring Boot 컨테이너로 리버스 프록시 포워딩.

### 1.2.3 관리형 데이터베이스 (AWS RDS / Docker MySQL)
- 고가용성 및 데이터 영속성 보장:
  - 백엔드 EC2 인스턴스와 격리된 전용 서브넷 그룹에 AWS RDS 또는 Docker 컨테이너 기반 MySQL 인스턴스를 구축하여 데이터 무결성 보장.
  - 보안 그룹(Security Group) 규칙을 통해 백엔드 EC2 인스턴스의 프라이빗 IP로부터의 3306 포트 인바운드 트래픽만 엄격히 허용.
  - HikariCP 커넥션 풀을 최적화(MaximumPoolSize: 10~20)하여 대량 트래픽 및 배치 처리 시 안정적인 데이터베이스 커넥션 유지.

### 1.2.4 미디어 및 파일 스토리지 (AWS S3)
- 강의 자료 및 과제물 파일 업로드 파이프라인:
  - 강좌 썸네일, 강의 첨부파일, 수강생 과제 제출물 업로드 시 Spring Boot 서버가 Multipart 요청 수신.
  - AWS SDK for Java를 사용하여 S3 버킷의 용도별 디렉토리(`courses/`, `materials/`, `assignments/`)에 UUID 기반 고유 파일명으로 저장.
  - 민감한 강의 자료 및 과제물은 S3 Presigned URL 방식을 도입하여 인가된 수강생만 일정 시간 동안 다운로드하도록 보안 강화.

---

## 1.3 GitHub Actions 기반 CI/CD 배포 자동화 파이프라인

코드 형상 관리부터 서버 무중단 배포까지 전 과정을 GitHub Actions 워크플로우로 자동화.

```mermaid
sequenceDiagram
    autonumber
    actor Dev as 개발자
    participant GH as GitHub Repository
    participant Action as GitHub Actions Runner
    participant DockerHub as Docker Registry
    participant EC2 as AWS EC2 서버

    Dev->>GH: git push origin main (또는 PR 머지)
    GH->>Action: 워크플로우 트리거 (CI/CD Pipeline)
    
    rect rgb(240, 248, 255)
    Note over Action: 1단계: CI (지속적 통합)
    Action->>Action: JDK 및 Gradle 캐싱 환경 설정
    Action->>Action: ./gradlew test (JUnit5 단위/통합 테스트)
    end

    rect rgb(255, 250, 240)
    Note over Action: 2단계: 이미지 빌드 및 푸시
    Action->>Action: ./gradlew bootJar (Spring Boot 빌드)
    Action->>Action: Docker 멀티 스테이지 빌드
    Action->>DockerHub: 경량화된 도커 이미지 푸시 (tag: latest / commit_sha)
    end

    rect rgb(240, 255, 240)
    Note over Action: 3단계: CD (지속적 배포)
    Action->>EC2: SSH 원격 접속 및 배포 스크립트 실행
    EC2->>DockerHub: docker pull 신규 이미지 다운로드
    EC2->>EC2: docker compose up -d (신규 컨테이너 가동)
    EC2->>EC2: Nginx 헬스체크 (/actuator/health 검증)
    EC2->>EC2: 이전 컨테이너 정리 (docker image prune)
    end

    Action-->>Dev: 배포 완료 알림 (GitHub Status 성공)
```

---

## 1.4 핵심 비즈니스 데이터 흐름도

### 1.4.1 수강 신청 및 결제 사전/사후 검증 흐름
결제 금액 위변조를 원천 차단하고 결제 승인과 수강권(Enrollment) 생성을 원자적으로 보장.

```mermaid
sequenceDiagram
    autonumber
    actor User as 수강생
    participant Front as React SPA
    participant Back as Spring Boot API
    participant DB as MySQL DB
    participant PG as 포트원 PG사

    User->>Front: 수강 신청 및 결제하기 클릭
    Front->>Back: POST /api/v1/payments/prepare (강좌ID, 결제예정금액)
    Back->>DB: 결제 대기(READY) 주문 레코드 저장
    Back-->>Front: 주문번호(merchant_uid), 사전 등록 완료 반환
    
    Front->>PG: 포트원 브라우저 SDK 결제창 호출
    User->>PG: 카드 결제 정보 입력 및 결제 승인
    PG-->>Front: 결제 완료 응답 (imp_uid, merchant_uid)
    
    Front->>Back: POST /api/v1/payments/complete (imp_uid, merchant_uid)
    Back->>PG: GET /payments/{imp_uid} (실결제 내역 단건 조회)
    PG-->>Back: 실제 결제 승인 금액 및 상태 반환
    
    alt 결제 금액 불일치 (위변조 감지)
        Back->>PG: POST /payments/cancel (즉시 결제 전액 취소)
        Back->>DB: 결제 상태 위변조 실패(FORGERY_FAIL) 갱신
        Back-->>Front: 400 Bad Request 결제 검증 실패 반환
    else 결제 금액 일치 (검증 성공)
        Back->>DB: @Transactional (결제 완료 PAID 갱신 및 수강권 ENROLLED 생성)
        Back-->>Front: 200 OK 수강 등록 성공 및 마이페이지 이동
    end
```

### 1.4.2 동영상 진도율 저장 및 80% 차시 완료 판정 흐름
동영상 시청 중 주기적으로 위치를 기록하고 진도율 요건 충족 시 차시 완료 상태를 동기화.

```mermaid
sequenceDiagram
    autonumber
    actor User as 수강생
    participant Player as React 동영상 플레이어
    participant Back as Spring Boot API
    participant DB as MySQL DB

    loop 5초 주기 인터벌
        Player->>Back: POST /api/v1/lessons/{lessonId}/progress (현재초: 450, 전체초: 600)
        Back->>DB: lesson_progress 현재 위치(last_view_second) 업데이트
        Back->>Back: 진도율 계산 (450 / 600 = 75%)
        Back-->>Player: 200 OK (진도율 75%, 완료 여부: false)
    end

    Note over Player: 동영상 재생 위치 480초(80%) 도달
    Player->>Back: POST /api/v1/lessons/{lessonId}/progress (현재초: 485, 전체초: 600)
    Back->>Back: 진도율 계산 (485 / 600 = 80.8% >= 80%)
    Back->>DB: is_completed = true (차시 완료 갱신)
    Back->>DB: 강좌 종합 진도율(Course Progress %) 재계산 및 갱신
    Back-->>Player: 200 OK (진도율 80.8%, 완료 여부: true)
    Player-->>User: 차시 완료 체크 뱃지 시각화
```

### 1.4.3 AI 기반 학사 행정 지원 챗봇 비동기 스트리밍(SSE) 흐름
과정 운영자의 행정 업무를 무인화하기 위해 강좌 정보 및 학사 규정 FAQ를 실시간 스트리밍으로 제공.

```mermaid
sequenceDiagram
    autonumber
    actor User as 사용자(수강생/비회원)
    participant Front as React 챗봇 컴포넌트
    participant Back as Spring Boot API
    participant DB as MySQL DB (FAQ/강좌)
    participant LLM as 외부 LLM API (OpenAI/Gemini)

    User->>Front: 질문 입력 (예: "환불 규정과 취소 절차가 어떻게 되나요?")
    Front->>Back: GET /api/v1/ai/admin-chat/stream?prompt=... (SSE 연결)
    Back->>DB: 강좌 정보 및 학사 규정 FAQ 지식 데이터 조회
    Back->>Back: 시스템 프롬프트 구성 (운영자 역할 부여 및 RAG 지식 주입)
    
    Back->>LLM: 스트리밍 질의 요청 (Stream Request)
    
    loop 토큰 생성 시마다 스트리밍 반환
        LLM-->>Back: 청크 토큰 수신 ("수강 시작 전에는...")
        Back-->>Front: SSE data: {"text": "수강 시작 전에는..."}
        Front-->>User: 챗봇 창에 실시간 타자 효과 렌더링
    end

    Back-->>Front: SSE event: complete (스트리밍 연결 정상 종료)
```

### 1.4.4 Spring Batch 자정 수강 만료 및 월간 정산 흐름
대용량 데이터를 메모리 부하 없이 페이징 청크 단위로 안전하게 일괄 처리.

```mermaid
sequenceDiagram
    autonumber
    participant Cron as Spring Scheduler (자정 트리거)
    participant Batch as Spring Batch JobLauncher
    participant Step1 as ExpiredEnrollmentStep (청크 500)
    participant Step2 as MonthlySettlementStep (청크 100)
    participant DB as MySQL DB

    Cron->>Batch: 00:00:00 자정 스케줄러 트리거
    Batch->>Step1: 수강 만료 일괄 처리 Job 실행
    
    loop 청크 단위 처리 (Reader -> Processor -> Writer)
        Step1->>DB: 만료일 <= 당일 AND 상태 == ACTIVE 페이징 조회 (500건)
        Step1->>Step1: 상태를 EXPIRED로 변경
        Step1->>DB: 500건 일괄 벌크 업데이트
    end

    alt 매월 1일인 경우
        Batch->>Step2: 월간 강사 매출 정산 Job 실행
        loop 청크 단위 처리
            Step2->>DB: 전월 결제 완료 내역 조회 및 강사별 매출 집계
            Step2->>Step2: 플랫폼 수수료(예: 20%) 공제 및 정산금 산출
            Step2->>DB: settlement 테이블에 강사별 정산 레코드 일괄 삽입
        end
    end
    
    Batch-->>Cron: Batch Job 정상 종료 및 실행 로그 기록
```
