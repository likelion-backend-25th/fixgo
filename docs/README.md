# 하루배움 (HaruBaeum) 프로젝트 산출물 종합 가이드 및 대시보드

## 목차
- [1. FixGo 프로젝트 개요](#1-fixgo-프로젝트-개요)
  - [1.1 서비스 소개](#11-서비스-소개)
  - [1.2 핵심 기술 스택](#12-핵심-기술-스택)
  - [1.3 팀 구성 및 도메인별 역할 분담](#13-팀-구성-및-도메인별-역할-분담)
- [2. 필수 산출물 요약 목록](#2-필수-산출물-요약-목록)
  - [2.1 기획 단계 산출물 (Planning)](#21-기획-단계-산출물-planning)
  - [2.2 설계 단계 산출물 (Design)](#22-설계-단계-산출물-design)
  - [2.3 구현 및 운영 보고 산출물 (Reports)](#23-구현-및-운영-보고-산출물-reports)
- [3. 필수 산출물 정의 및 작성 기준](#3-필수-산출물-정의-및-작성-기준)
  - [3.1 프로젝트 기획서 (Project Proposal)](#31-프로젝트-기획서-project-proposal)
  - [3.2 요구사항 기능 명세서 (PRD)](#32-요구사항-기능-명세서-prd)
  - [3.3 코드 컨벤션 정의서 (Code Convention Document)](#33-코드-컨벤션-정의서-code-convention-document)
  - [3.4 시스템 아키텍처 구성도 (System Architecture Diagram)](#34-시스템-아키텍처-구성도-system-architecture-diagram)
  - [3.5 데이터베이스 ERD (Entity Relationship Diagram)](#35-데이터베이스-erd-entity-relationship-diagram)
  - [3.6 화면 설계서 (UI Wireframe)](#36-화면-설계서-ui-wireframe)
  - [3.7 REST API 명세서 및 더미 데이터 규격서 (API Specification)](#37-rest-api-명세서-및-더미-데이터-규격서-api-specification)
  - [3.8 트러블슈팅 및 기술 문제 해결 기록 (Troubleshooting Log)](#38-트러블슈팅-및-기술-문제-해결-기록-troubleshooting-log)

---

# 1. FixGo 프로젝트 개요

## 1.1 서비스 소개
픽스고(FixGo)는 입니다.
- 접수담당자(STUDENT): 챕터 및 차시별 커리큘럼 스트리밍, 진도율 추적(80% 이상 수료 인정), 과제 제출 및 강사 피드백, 수료증 발급
- 강사(INSTRUCTOR): 강좌 개설, 차시별 동영상 및 학습 자료 업로드, 수강생 과제 채점 및 평가 피드백 제공
- 관리자(ADMIN): 플랫폼 전역 모니터링, 회원/강좌 승인 관리, 일일/월간 강사 매출 정산 배치 무인화, 24시간 실시간 AI 학사 행정 지원 챗봇 운영

## 1.2 핵심 기술 스택
- 백엔드: Java 25, Spring Boot 4.x, Spring Data JPA, OpenFeign QueryDSL 7.x, Spring Batch 6.x, Spring Security 6.x, JJWT
- 프론트엔드: React 19.x, TypeScript 6.x, Vite 8.x, Tailwind CSS 4.x, Zustand 5.x, Axios
- 데이터베이스: MySQL 9.x (운영), H2 Database (테스트)
- 인프라 및 DevOps: AWS EC2, S3, CloudFront, Docker, Docker Compose, GitHub Actions CI/CD 파이프라인
- 외부 연동: 포트원(PortOne) 결제 게이트웨이, OpenAI / Gemini API (Spring AI 비동기 스트리밍 SSE)

## 1.3 팀 구성 및 도메인별 역할 분담
- PM (Project Manager) 용쌤: 프로젝트 총괄, 일정 관리, 회원/인증(JWT RTR), 결제(PortOne 2단계 검증) 및 Spring Batch 정산 무인화 아키텍처
- PL (Project Leader) 나무: 기술 리딩, 공통 설계, 강좌/커리큘럼, 학습 진도율 추적 및 과제 평가 도메인
- 팀원 (Developer) 하루: 프론트엔드 UI/UX 구축, AI 학사 행정 지원 챗봇(SSE 비동기 스트리밍), GitHub Actions CI/CD 배포 파이프라인 연동

---

# 2. 필수 산출물 요약 목록

하루배움 프로젝트를 체계적으로 구축하기 위해 작성된 단계별 필수 산출물 문서 목록입니다.

## 2.1 기획 단계 산출물 (Planning)
| 산출물 번호 | 산출물 문서명 | 주요 내용 | 파일 링크 |
|---|---|---|---|
| 01 | 프로젝트 기획서 | 서비스 기획 배경, 비즈니스 가치, 주요 기능 요약 및 팀 구성 | [docs/01_planning/01_proposal.md](01_planning/01_proposal.md) |
| 02 | 요구사항 기능 명세서 (PRD) | 8대 핵심 도메인별 세부 요구사항 ID, 권한 분기(RBAC) 및 우선순위 규격 | [docs/01_planning/02_prd.md](01_planning/02_prd.md) |
| 03 | 코드 컨벤션 정의서 | Java 25/Spring Boot 표준 서식, 네이밍 규칙, 레이어드 아키텍처 및 Git 협업 규약 | [docs/01_planning/03_convention.md](01_planning/03_convention.md) |

## 2.2 설계 단계 산출물 (Design)
| 산출물 번호 | 산출물 문서명 | 주요 내용 | 파일 링크 |
|---|---|---|---|
| 01 | 시스템 아키텍처 구성도 | React SPA, Spring Boot REST/Batch, MySQL, AWS 클라우드 및 외부 연동 흐름도 | [docs/02_design/01_architecture.md](02_design/01_architecture.md) |
| 02 | 데이터베이스 ERD | 16개 핵심 엔티티 구조, 외래키 관계 및 Mermaid ERD 시각화 다이어그램 | [docs/02_design/02_erd.md](02_design/02_erd.md) |
| 03 | 화면 설계서 (UI Wireframe) | 메인, 강좌 상세, 학습 플레이어, 과제 관리, 정산 대시보드 화면 와이어프레임 | [docs/02_design/03_ui_wireframe.md](02_design/03_ui_wireframe.md) |
| 04 | REST API 명세서 및 더미 데이터 규격서 | HTTP Method, URI 엔드포인트, 요청/응답 JSON 페이로드 및 Mock 데이터 규격 | [docs/02_design/04_api_specification.md](02_design/04_api_specification.md) |

## 2.3 구현 및 운영 보고 산출물 (Reports)
| 산출물 번호 | 산출물 문서명 | 주요 내용 | 파일 링크 |
|---|---|---|---|
| 01 | 트러블슈팅 및 기술 문제 해결 기록 | JPA N+1 최적화, MultipleBagFetchException 해결, 결제 검증 및 배치 안정성 기록 | [docs/03_reports/troubleshooting.md](03_reports/troubleshooting.md) |

---

# 3. 필수 산출물 정의 및 작성 기준

## 3.1 프로젝트 기획서 (Project Proposal)
### 3.1.1 정의
프로젝트의 출발점으로서 개발하고자 하는 서비스(LMS, 하루배움)의 목적, 배경, 핵심 비즈니스 가치, 수강 결제 및 수료증 발급 모델, 팀원별 도메인 역할 분담을 명확히 규정하는 기본 기획 문서.

### 3.1.2 작성 필요성 및 목적
- 프로젝트 방향성 통일: 팀원 간 서비스 목표와 비즈니스 모델(무료/유료 강좌 수강, 차시별 진도 관리, 수강권 발급)에 대한 이해도를 일치시키고 기획 혼선 차단
- 개발 범위 명확화: 2주 개발 일정에 최적화된 현실적인 서비스 스코프 설정 및 프론트/백엔드 연동 목표 확립

### 3.1.3 필수 포함 항목
- 프로젝트명 및 한 줄 서비스 소개
- 프로젝트 기획 배경 및 목적
- 서비스 주요 특징 (강좌 탐색, 챕터/차시 계층형 학습, 과제 평가, 결제 및 수강권 발급, Spring Batch 정산 무인화, AI 학사 행정 지원)
- 주요 기능 요구사항 요약
- 팀 구성 및 도메인별 역할 분담표

- 💻 프로젝트 기획서 샘플: [docs/01_planning/01_proposal.md](01_planning/01_proposal.md)

---

## 3.2 요구사항 기능 명세서 (PRD)
### 3.2.1 정의
시스템이 제공해야 하는 모든 세부 기능을 도출하고, 각 기능별 구현 조건, 대상 역할(RBAC), 우선순위를 목록화하여 관리하는 문서.

### 3.2.2 작성 필요성 및 목적
- 개발 스코프 제어: 2주 개발 일정 동안 구현할 필수 기능(1순위)과 선택 기능(2, 3순위)을 명확하게 구분
- 개발 구현 기준 제공: 기능별 입력/처리/출력 조건과 예외 규칙을 구체화하여 개발 오차 감소
- 결제 및 배치 보안 명시: 포트원 단건 결제 사전/사후 2단계 검증, 자정 수강 만료 및 월간 강사 정산 배치, JWT 인증 세부 요구조건을 사전에 정립

### 3.2.3 필수 포함 항목 및 작성 방법
- 요구사항 식별 ID: `REQ-AUTH-01`, `REQ-CRS-01`, `REQ-ORD-01`, `REQ-LRN-01`, `REQ-COMM-01`, `REQ-AI-01`, `REQ-BAT-01`, `REQ-ADM-01` 등 도메인별 고유 코드 부여
- 구분: 회원/인증, 강좌/커리큘럼, 주문/결제, 학습/과제, 커뮤니티, AI 행정 챗봇, 배치 자동화, 관리자 등 도메인 명시
- 기능명 및 대상 역할: 비회원(GUEST), 수강생(STUDENT), 강사(INSTRUCTOR), 관리자(ADMIN) 구분
- 우선순위: 1순위(필수 구현), 2순위(주요 권장), 3순위(추가 선택) 구분
- 상세 내용: 기능의 세부 동작 조건 및 입력 데이터 검증 규칙 명시

- 💻 요구사항 기능 명세서 샘플: [docs/01_planning/02_prd.md](01_planning/02_prd.md)

---

## 3.3 코드 컨벤션 정의서 (Code Convention Document)
### 3.3.1 정의
팀원 전체가 하나의 개발자가 작성한 것처럼 일관된 코딩 스타일, 네이밍 규칙, 디렉토리 구조, 백엔드 레이어드 아키텍처 원칙(DTO/Entity 엄격 분리, 생성자 주입, 트랜잭션 경계 설정), Git 협업 규약을 명문화하여 준수하기 위한 개발 표준 규약 문서.

### 3.3.2 작성 필요성 및 목적
- 팀원 간 코드 가독성 제고: 통일된 서식과 명명법을 적용하여 타 팀원의 코드를 이해하는 시간과 인지 부하 감소
- 리뷰 생산성 극대화: 사소한 포맷팅이나 스타일 논쟁을 배제하고 핵심 비즈니스 로직과 설계 품질 검증에 집중
- 런타임 결함 예방: DTO와 Entity의 엄격한 분리, 생성자 주입 강제, @Transactional(readOnly) 최적화를 통해 순환 참조, 지연 로딩 오류, 메모리 누수를 원천 방지

### 3.3.3 필수 포함 항목
- 기본 언어 서식: 들여쓰기(공백 4칸), 최대 줄 너비(120자), K&R 중괄호 스타일, 와일드카드 import 금지
- 식별자 명명 규칙: 도메인 중심 패키지, 클래스 역할별 접미사(Controller, Service, Repository, DTO, Entity), 메서드 동사 규칙, 데이터베이스 스네이크 케이스
- 레이어드 아키텍처 및 구현 표준: 계층별 책임 분리, DTO와 Entity의 엄격한 분리, 불변 생성자 주입, 선언적 트랜잭션 최적화, 전역 예외 처리 표준
- 깃 협업 및 리뷰 컨벤션: Conventional Commits 커밋 규격, 브랜치 전략(develop, feature/*, fix/*), PR 자가 점검 체크리스트

- 💻 코드 컨벤션 정의서 샘플: [docs/01_planning/03_convention.md](01_planning/03_convention.md)

---

## 3.4 시스템 아키텍처 구성도 (System Architecture Diagram)
### 3.4.1 정의
React SPA 클라이언트부터 Spring Boot REST API 및 Batch 서버, MySQL 데이터베이스, AWS 클라우드 인프라, 결제 게이트웨이(PortOne) 및 외부 LLM AI 서비스 간의 전체적인 시스템 구조와 데이터 흐름을 시각화한 구성도.

### 3.4.2 작성 필요성 및 목적
- 클라이언트-서버 분리 아키텍처 이해: React 프론트엔드와 Spring Boot 백엔드 간 비동기 REST API 통신 및 SSE 스트리밍 정책 명확화
- AWS 클라우드 배포 구조 파악: S3 + CloudFront 정적 사이트 호스팅, EC2 + Docker Compose 백엔드 배포, S3 미디어 업로드 스토리지 구성 확인
- 데이터 흐름 정립: 결제 사전/사후 검증 흐름, 80% 차시 완료 판정 흐름, AI 챗봇 비동기 스트리밍 흐름, Spring Batch 청크 일괄 처리 흐름 명시

### 3.4.3 필수 포함 항목
- 프론트엔드 호스팅 영역: AWS S3, CloudFront (CDN), 브라우저 React SPA
- 백엔드 애플리케이션 영역: AWS EC2 (Amazon Linux 2023), Nginx 리버스 프록시, Docker Compose, Spring Boot REST API 및 Batch 서버 (8080 포트)
- 데이터베이스 영역: MySQL 컨테이너 (3306 포트), 볼륨 마운트
- 외부 연동 및 스토리지 영역: AWS S3 미디어 버킷 (/courses, /materials, /assignments), 포트원 결제 게이트웨이, 외부 LLM API

- 💻 시스템 아키텍처 구성도 샘플: [docs/02_design/01_architecture.md](02_design/01_architecture.md)

---

## 3.5 데이터베이스 ERD (Entity Relationship Diagram)
### 3.5.1 정의
시스템에서 사용하는 데이터 테이블 구조, 컬럼 데이터 타입, PK/FK 제약조건 및 테이블 간의 연관 관계(1:1, 1:N, N:M)를 규정한 개체 관계도.

### 3.5.2 작성 필요성 및 목적
- 데이터 무결성 확보: 회원 상세 1:1 수직 분할, 챕터-차시 계층 구조 외래키, 수강권 고유 제약 및 진도율 정합성 유지
- JPA 엔티티 연관관계 매핑 및 최적화: 다대일/일대다 단방향 및 양방향 매핑을 사전에 정립하고 N+1 및 다중 컬렉션 페치 조인 한계(MultipleBagFetchException) 회피 구조 확립

### 3.5.3 필수 포함 항목
- 테이블명 및 컬럼 명세: PK, FK, Not Null, Unique, Default 제약조건 명시
- 핵심 도메인 테이블: member, member_detail, course, chapter, lesson, order_info, payment, enrollment, lesson_progress, assignment, assignment_submission, qna_question, qna_answer, course_review, settlement, admin_faq (총 16개 핵심 엔티티)
- 작성 방식: 마크다운 내부 Mermaid `erDiagram` 문법 구문을 사용하여 시각적 다이어그램으로 렌더링되도록 작성

### 3.5.4 Mermaid ERD 주요 문법
- Mermaid ERD 공식 문법 문서: [https://docs.mermaidviewer.com/diagrams/er.html](https://docs.mermaidviewer.com/diagrams/er.html)
- Mermaid ERD 관계 표현 기호 규칙:
  - 1:1 관계 (필수 1 대 1): `||--||` (양쪽 모두 1개 필수)
  - 1:1 관계 (필수 1 대 선택 0 또는 1): `||--o|` (한쪽은 1개 필수, 반대쪽은 0개 또는 1개)
  - 1:N 관계 (필수 1 대 1 이상 N): `||--|{` (한쪽은 1개 필수, 반대쪽은 1개 이상 필수)
  - 1:N 관계 (필수 1 대 0 이상 N): `||--o{` (한쪽은 1개 필수, 반대쪽은 0개 이상 선택)
  - N:M 관계 (1 이상 N 대 1 이상 M): `}|--|{` (양쪽 모두 1개 이상 필수)
  - N:M 관계 (1 이상 N 대 0 이상 M): `}|--o{` (한쪽은 1개 이상 필수, 반대쪽은 0개 이상 선택)
- 관계선 기호 가이드:
  - `||`: 정확히 1개 (Exactly one)
  - `|{`: 1개 이상 (One or more)
  - `o{`: 0개 이상 (Zero or more)
  - `o|`: 0개 또는 1개 (Zero or one)
- 기호 구성 요소 해설:
  - Straight Line (`|`): 최소 1개 이상 존재해야 함 (Mandatory 필수 조건)
  - Circle (`o`): 0개일 수 있음 (Optional 선택 조건, 없어도 됨)
  - Crow Foot (`}` 또는 `{`): 다수 (Many, N개)를 의미

- 💻 데이터베이스 ERD 샘플: [docs/02_design/02_erd.md](02_design/02_erd.md)

---

## 3.6 화면 설계서 (UI Wireframe)
### 3.6.1 정의
사용자 관점에서의 LMS 서비스 화면 레이아웃, 강좌 탐색 카드, 차시별 동영상 플레이어, 과제 제출 모달, AI 챗봇 위젯 및 화면 간 이동 흐름을 시각적으로 구성한 화면 뼈대 설계 문서.

### 3.6.2 작성 필요성 및 목적
- AI 바이브 코딩 연계 최적화: 시각적 와이어프레임과 구성 요소 명세를 AI에게 전달하여 신속하고 일관된 React 컴포넌트 생성 유도
- 기획 오차 감소: 강좌 상세, 차시 학습 플레이어, 과제 제출 폼, 결제 모달, 관리자 정산 화면 배치를 미리 정의하여 프론트엔드 재작업 방지

### 3.6.3 필수 포함 항목
- 주요 화면 목록: 회원가입/로그인, 메인 홈/강좌 탐색, 강좌 상세/커리큘럼, 장바구니/결제, 나의 학습 대시보드, 동영상 플레이어, 과제 제출/채점, AI 학사 챗봇 위젯, 강사 강좌 관리, 관리자 통계/정산
- 출력 데이터 항목(Output Data) 및 화면 제어/동작 규칙(Behavior Rules) 수록

### 3.6.4 와이어프레임 도구 및 산출물 관리 가이드
- tldraw 웹사이트: [https://www.tldraw.com/](https://www.tldraw.com/)
- Excalidraw 웹사이트: [https://excalidraw.com/](https://excalidraw.com/)
- 제작 팁:
  - 복잡한 픽셀 단위 작업 대신 사각형 박스와 텍스트를 활용한 초간단 와이어프레임 또는 종이 손스케치를 캡처하여 활용
  - 이미지 파일은 `docs/images/` 폴더에 저장하고 마크다운에서 상대 경로로 링크하여 관리

- 💻 화면 설계서 샘플: [docs/02_design/03_ui_wireframe.md](02_design/03_ui_wireframe.md)

---

## 3.7 REST API 명세서 및 더미 데이터 규격서 (API Specification)
### 3.7.1 정의
프론트엔드와 백엔드가 JSON 비동기 통신을 수행하기 위해 사전에 합의하는 엔드포인트 URI, HTTP 메서드, 요청/응답 DTO 및 프론트 더미 데이터 규격 문서.

### 3.7.2 작성 필요성 및 목적
- 병렬 개발 보장: 백엔드 API 완성을 기다리지 않고, 프론트엔드가 정의된 더미 JSON 데이터를 활용해 UI와 기능을 먼저 완성(바이브 코딩) 가능
- 손쉬운 연동 전환: 실제 API 완성 시 더미 데이터를 실제 API 호출 엔드포인트로 즉시 교체하여 연동 소요 시간 극소화
- 데이터 규격 통일: 공통 DTO 포맷과 전역 예외 응답 규격을 사전에 정의하여 프론트엔드 예외 처리 공통화

### 3.7.3 필수 포함 항목
- 공통 응답 규격: 단건 및 페이징 목록 DTO 응답, 전역 에러 포맷
- 인증/회원 API: 회원가입, 로그인, RTR 토큰 갱신, 내 계정 관리
- 강좌/커리큘럼 API: 강좌 목록/다중 조건 검색, 강좌 등록/수정/승인, 챕터 및 차시 등록
- 주문/결제 API: 결제 사전 검증 (`/api/v1/payments/prepare`), 사후 검증 (`/api/v1/payments/complete`), 환불 처리, 결제 웹훅
- 학습/진도율 API: 차시 진도율 저장 (`POST /api/v1/lessons/{lessonId}/progress`), 80% 완료 자동 판정
- 과제 및 커뮤니티 API: 과제 출제/제출/채점, Q&A 질문/답변 등록
- AI 챗봇 API: AI 학사 행정 지원 실시간 스트리밍 (`GET /api/v1/ai/admin-chat/stream`, SSE)
- 관리자/정산 API: 통합 통계 대시보드 지표, 강사별 월간 정산 목록 조회

- 💻 REST API 명세서 샘플: [docs/02_design/04_api_specification.md](02_design/04_api_specification.md)

---

## 3.8 트러블슈팅 및 기술 문제 해결 기록 (Troubleshooting Log)
### 3.8.1 정의
개발 및 배포 과정에서 직면한 런타임 오류, 쿼리 병목, 분산 트랜잭션, 대용량 배치 OOM, 스트리밍 연결 누수 등 기술적 문제의 발생 원인, 해결 방법, 시사점을 기록하는 문서.

### 3.8.2 작성 필요성 및 목적
- 문제 재발 방지: 작성된 해결 기록을 공유하여 팀원들이 동일한 버그로 시간을 낭비하는 현상 차단
- 기술적 자산화: JPA MultipleBagFetchException 및 BatchSize N+1 최적화, OpenFeign QueryDSL CVE-2024-49203 보안 패치, 결제 사전/사후 2단계 검증 및 망 취소, Spring Batch 청크 OOM 방지, SSE 스트리밍 누수 방지 등 실무 수준의 기술 문제 해결 경험을 면접 및 포트폴리오 핵심 자산으로 활용

### 3.8.3 필수 포함 항목 및 작성 템플릿
- 작성자별 식별자 규칙: 동시 편집 충돌 방지를 위한 `TS-[작성자영문]-[순번]` 태그 사용 (예: `TS-YONG-01`, `TS-NAMU-01`, `TS-HARU-01`)
- 복사용 표준 템플릿:

````markdown
## TS-[작성자영문]-[순번]: [발생한 문제 및 에러 제목]

### 1) 발생 현상 및 에러
- 런타임 현상 설명 및 발생한 에러 로그 원문/메시지 수록.

### 2) 원인 분석
- 해당 문제나 버그가 발생한 근본 원인 및 코드 흐름 분석.

### 3) 해결 방법
- 문제를 해결하기 위해 변경하거나 새로 적용한 해결 코드 반영.

```java
// 해결 핵심 소스 코드 반영
```

### 4) 시사점 및 배운 점
- 버그 해결을 통해 새로 배우게 된 원리나 향후 재발 방지를 위한 시사점 작성.
````

- 💻 트러블슈팅 작성 샘플: [docs/03_reports/troubleshooting.md](03_reports/troubleshooting.md)

