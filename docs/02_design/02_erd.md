# 1. 데이터베이스 모델링 및 ERD 명세서 (하루배움 LMS)

## 목차
- [1. 데이터베이스 모델링 및 ERD 명세서 (하루배움 LMS)](#1-데이터베이스-모델링-및-erd-명세서-하루배움-lms)
- [1.1 엔티티 관계 다이어그램 (ERD)](#11-엔티티-관계-다이어그램-erd)
- [1.2 테이블별 상세 컬럼 명세](#12-테이블별-상세-컬럼-명세)

---

## 1.1 엔티티 관계 다이어그램 (ERD)

하루배움 플랫폼의 16개 핵심 엔티티 간의 관계도이며, 회원 계정, 강좌 및 계층 커리큘럼, 주문/결제, 수강권 및 진도율, 과제/피드백, 커뮤니티, 정산 및 AI 학사 FAQ 테이블 간의 물리적 외래키(FK) 연관관계를 도식화.

```mermaid
erDiagram
    MEMBER ||--|| MEMBER_DETAIL : "1:1 수직 분할"
    MEMBER ||--o{ COURSE : "1:N 강좌 개설"
    COURSE ||--o{ CHAPTER : "1:N 챕터 구성"
    CHAPTER ||--o{ LESSON : "1:N 차시 구성"

    MEMBER ||--o{ ORDER_INFO : "1:N 주문 생성"
    ORDER_INFO ||--|| PAYMENT : "1:1 결제 내역"
    ORDER_INFO ||--o{ ENROLLMENT : "1:N 수강권 발급"
    COURSE ||--o{ ENROLLMENT : "1:N 강좌 수강"
    MEMBER ||--o{ ENROLLMENT : "1:N 수강생 보유"

    ENROLLMENT ||--o{ LESSON_PROGRESS : "1:N 차시별 진도"
    LESSON ||--o{ LESSON_PROGRESS : "1:N 차시 진도 기록"

    LESSON ||--o| ASSIGNMENT : "1:1 과제 출제"
    ASSIGNMENT ||--o{ ASSIGNMENT_SUBMISSION : "1:N 과제 제출"
    MEMBER ||--o{ ASSIGNMENT_SUBMISSION : "1:N 수강생 제출"

    LESSON ||--o{ QNA_QUESTION : "1:N 강의 Q&A"
    MEMBER ||--o{ QNA_QUESTION : "1:N 질문 작성"
    QNA_QUESTION ||--o{ QNA_ANSWER : "1:N 질문 답변"
    MEMBER ||--o{ QNA_ANSWER : "1:N 답변 작성"

    COURSE ||--o{ COURSE_REVIEW : "1:N 수강 후기"
    MEMBER ||--o{ COURSE_REVIEW : "1:N 수강생 작성"

    MEMBER ||--o{ SETTLEMENT : "1:N 강사 정산"
    ADMIN_FAQ {
        bigint id PK "FAQ 고유 식별자"
        varchar category "카테고리"
        varchar question_pattern "질문 패턴"
        text answer_content "표준 답변 텍스트"
        varchar reference_link "참조 링크 URL"
        varchar is_active "활성 여부 (Y/N)"
    }

    MEMBER {
        bigint id PK "회원 고유 식별자"
        varchar email "이메일 (UK)"
        varchar password "암호화된 비밀번호"
        varchar name "실명"
        varchar nickname "닉네임"
        varchar role "회원 역할 (STUDENT, INSTRUCTOR, ADMIN)"
        varchar profile_image_url "프로필 이미지 S3 URL"
        datetime created_at "가입 일시"
        datetime updated_at "수정 일시"
    }

    MEMBER_DETAIL {
        bigint member_id PK, FK "회원 기본 키"
        text bio "강사/수강생 한 줄 소개"
        varchar phone_number "연락처"
        varchar marketing_agreed "마케팅 수신 동의 (Y/N)"
    }

    COURSE {
        bigint id PK "강좌 고유 식별자"
        bigint instructor_id FK "개설 강사 회원 ID"
        varchar title "강좌 제목"
        text description "강좌 소개 본문"
        varchar category "카테고리 (BACKEND, FRONTEND, INFRA)"
        varchar level "난이도 (BEGINNER, INTERMEDIATE, ADVANCED)"
        int price "수강료 (원 단위)"
        int access_days "수강 가능 기간 (일수)"
        varchar status "승인 상태 (DRAFT, PENDING, APPROVED, REJECTED)"
        varchar thumbnail_url "썸네일 이미지 S3 URL"
        decimal average_rating "평균 평점"
        datetime created_at "개설 일시"
        datetime updated_at "수정 일시"
    }

    CHAPTER {
        bigint id PK "챕터 고유 식별자"
        bigint course_id FK "소속 강좌 ID"
        int sequence "챕터 순서"
        varchar title "챕터 제목"
    }

    LESSON {
        bigint id PK "차시 고유 식별자"
        bigint chapter_id FK "소속 챕터 ID"
        int sequence "차시 순서"
        varchar title "차시 제목"
        varchar video_url "동영상 스트리밍 S3 URL"
        int duration_seconds "동영상 총 재생 시간 (초)"
        varchar material_url "강의 첨부자료 S3 URL"
        varchar is_free_preview "무료 미리보기 여부 (Y/N)"
    }

    ORDER_INFO {
        bigint id PK "주문 고유 식별자"
        varchar merchant_uid "주문 고유 식별 번호 (UK)"
        bigint student_id FK "주문 수강생 ID"
        int total_amount "총 결제 금액"
        varchar status "주문 상태 (READY, PAID, CANCELLED)"
        datetime ordered_at "주문 생성 일시"
    }

    PAYMENT {
        bigint id PK "결제 고유 식별자"
        bigint order_id FK "주문 식별자 (UK)"
        varchar imp_uid "포트원 결제 고유 번호 (UK)"
        varchar pay_method "결제 수단 (CARD, EASY_PAY, TRANS)"
        int paid_amount "실결제 승인 금액"
        datetime paid_at "결제 승인 일시"
        int refund_amount "환불 금액"
        datetime refunded_at "환불 일시"
        varchar status "결제 상태 (PAID, CANCELLED, FORGERY_FAIL)"
    }

    ENROLLMENT {
        bigint id PK "수강권 고유 식별자"
        bigint student_id FK "수강생 회원 ID"
        bigint course_id FK "수강 강좌 ID"
        bigint order_id FK "원천 주문 ID"
        datetime start_at "수강 시작 일시"
        datetime end_at "수강 만료 일시"
        varchar status "수강 상태 (ACTIVE, COMPLETED, EXPIRED)"
        varchar certificate_issued "수료증 발급 여부 (Y/N)"
        datetime completed_at "수료 일시"
    }

    LESSON_PROGRESS {
        bigint id PK "차시 진도 고유 식별자"
        bigint enrollment_id FK "소속 수강권 ID"
        bigint lesson_id FK "대상 차시 ID"
        int last_view_second "마지막 재생 위치 (초)"
        int max_view_second "최대 시청 위치 (초)"
        varchar is_completed "차시 완료 여부 (Y/N)"
        datetime updated_at "진도 갱신 일시"
    }

    ASSIGNMENT {
        bigint id PK "과제 고유 식별자"
        bigint lesson_id FK "연계 차시 ID (UK)"
        bigint instructor_id FK "출제 강사 ID"
        varchar title "과제 제목"
        text content "과제 가이드 본문"
        varchar attachment_url "첨부 가이드 파일 S3 URL"
        datetime deadline "과제 제출 마감 일시"
    }

    ASSIGNMENT_SUBMISSION {
        bigint id PK "과제 제출 고유 식별자"
        bigint assignment_id FK "대상 과제 ID"
        bigint student_id FK "제출 수강생 ID"
        text submission_content "제출 설명 텍스트"
        varchar file_url "제출 소스코드/과제물 S3 URL"
        int score "평가 점수 (0~100)"
        text instructor_feedback "강사 평가 피드백"
        varchar status "평가 상태 (PENDING, PASSED, FAILED)"
        datetime submitted_at "제출 일시"
        datetime graded_at "채점 일시"
    }

    QNA_QUESTION {
        bigint id PK "Q&A 질문 고유 식별자"
        bigint lesson_id FK "연계 차시 ID"
        bigint student_id FK "질문 작성자 ID"
        varchar title "질문 제목"
        text content "질문 본문"
        int timestamp_second "동영상 타임스탬프 (초)"
        varchar is_resolved "해결 여부 (Y/N)"
        datetime created_at "등록 일시"
    }

    QNA_ANSWER {
        bigint id PK "Q&A 답변 고유 식별자"
        bigint question_id FK "대상 질문 ID"
        bigint responder_id FK "답변자 ID (강사/관리자)"
        text content "답변 본문"
        varchar is_accepted "공식 채택 여부 (Y/N)"
        datetime created_at "등록 일시"
    }

    COURSE_REVIEW {
        bigint id PK "후기 고유 식별자"
        bigint course_id FK "대상 강좌 ID"
        bigint student_id FK "작성 수강생 ID"
        int rating "별점 (1~5점)"
        text content "수강 후기 본문"
        datetime created_at "등록 일시"
    }

    SETTLEMENT {
        bigint id PK "정산 고유 식별자"
        bigint instructor_id FK "정산 대상 강사 ID"
        varchar settlement_month "정산 대상 연월 (YYYY-MM)"
        int total_sales "총 수강료 매출"
        int platform_fee "플랫폼 수수료 공제액"
        int net_settlement_amount "최종 정산 지급액"
        varchar status "지급 상태 (PENDING, PAID)"
        datetime executed_at "배치 정산 실행 일시"
    }
```

---

## 1.2 테이블별 상세 컬럼 명세

### 1.2.1 회원 및 인증 도메인 (MEMBER, MEMBER_DETAIL)

#### 1) MEMBER (회원 기본 계정 테이블)

#### MEMBER (내부 직원 로그인 계정)

- 테이블 설명: 내부 직원 로그인 계정 

 | 컬럼명             | 데이터 타입       | 제약조건               | 기본값                                           | 인덱스               | 설명                       |
 |-----------------|--------------|--------------------|-----------------------------------------------|-------------------|--------------------------|
 | id              | BIGINT       | PK, AUTO_INCREMENT | -                                             | PK                | 직원 ID                    |
 | role_id         | BIGINT       | FK                 | -                                             | IDX_MEMBER_ROLE   | 직책                       |
 | email           | VARCHAR(120) | NOT NULL, UNIQUE   | -                                             | UK_MEMBER_EMAIL   | 직원용 이메일                  |
 | password_hash   | VARCHAR(255) | NOT NULL           | -                                             | -                 | BCrypt 등 단방향 해시          |
 | name            | VARCHAR(60)  | NOT NULL           | -                                             | -                 | 직원명                      |
 | phone           | VARCHAR(20)  | NULL               | -                                             | -                 | 직원 전화번호                  |
 | profile_img_url | TEXT         | NULL               | 기본이미지                                         | -                 | 직원 사진                    |
 | status          | VARCHAR(20)  | NOT NULL           | ACTIVE                                        | IDX_MEMBER_STATUS | ACTIVE, INACTIVE, DELETE |
 | created_at      | DATETIME     | NOT NULL           | CURRENT_TIMESTAMP                             | -                 | 생성 일시                    |
 | updated_at      | DATETIME     | NOT NULL           | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | -                 | 수정 일시                    |

#### 2) ROLE (한 직원의 다중 역할(RBAC) 매핑)

- 테이블 설명: 한 직원의 다중 역할(RBAC) 매핑

| 컬럼명       | 데이터 타입      | 제약조건               | 기본값  | 인덱스  | 설명                              |
|-----------|-------------|--------------------|------|------|---------------------------------|
| id        | BIGINT      | PK, AUTO_INCREMENT | -    | PK   | 역할 ID                           |
| role_name | VARCHAR(30) | -                  | -    | -    | ADMIN, RECEPTIONIST, TECHNICIAN |
---

#### 3) PERMISSION (역할에 부여된 권한)

- 테이블 설명:

| 컬럼명            | 데이터 타입      | 제약조건               | 기본값  | 인덱스                   | 설명                              |
|----------------|-------------|--------------------|------|-----------------------|---------------------------------|
| id             | BIGINT      | PK, AUTO_INCREMENT | -    | PK                    | 권한 ID                           |
| role_id        | BIGINT      | FK                 | -    | IDX_PERMITION_ROLE_ID | ADMIN, RECEPTIONIST, TECHNICIAN |
| permition_name | VARCHAR(30) | NOT_NULL           | -    | -                     | 권한 이름                           |

#### 4) REFRESH_TOKEN
- 로그인 시 발급된 Refresh_Token을 저장
- Access Token 재발급 시 Refresh 토큰도 동시에 재발급 되며 기존 저장된 토큰값을 업데이트 (기존 토큰 파기)
- 스케쥴러를 통해 만료된 토큰 정보를 주기적으로 삭제

| 컬럼명                 | 데이터 타입   | 제약조건               | 기본값          | 인덱스  | 설명          |
|---------------------|----------|--------------------|--------------|------|-------------|
| id                  | BIGINT   | PK, AUTO_INCREMENT | -            | PK   |             |
| member_id           | BIGINT   | FK                 | -            | -    | 발급 받은 직원 ID |
| refresh_token_value | TEXT     | NOT_NULL           | -            |      | 리프레시 토큰 값   |
| issued_at           | DATETIME | NOT_NULL           | Now()        | -    | 토큰 발급 시점    |
| expire_at           | DATETIME | NOT_NULL           | 발급 시점에서 7일 후 | -    | 토큰 만료 시점    |

### 1.2.2 고객 도메인 (CUSTOMER)

#### 1) CUSTOMER (고객)

- 테이블 설명: 회원가입하지 않는 고객

| 컬럼명            | 데이터 타입       | 제약조건               | 기본값                                           | 인덱스  | 설명     |
|----------------|--------------|--------------------|-----------------------------------------------|------|--------|
| id             | BIGINT       | PK, AUTO_INCREMENT | -                                             | PK   | 고객 ID  |
| name           | VARCHAR(120) | NOT NULL           | -                                             | -    | 고객명    |
| phone          | VARCHAR(20)  | NULL               | -                                             | -    | 고객 연락처 |
| email          | VARCHAR(120) | NULL               | -                                             | -    | 이메일    |
| zipcode        | VARCHAR(5)   | NULL               | -                                             | -    | 우편번호   |
| basic_address  | VARCHAR(255) | NULL               | -                                             | -    | 도로명주소  |
| detail_address | VARCHAR(255) | NULL               | -                                             | -    | 상세주소   |
| created_at     | DATETIME     | NOT NULL           | CURRENT_TIMESTAMP                             | -    | 생성 일시  |
| updated_at     | DATETIME     | NOT NULL           | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | -    | 수정 일시  |

### 1.2.3 제품 도메인 (PRODUCT_MODEL)

#### 1) PRODUCT (관리자가 등록하는 수리 가능 모델)

- 테이블 설명: 관리자가 등록하는 수리 가능 모델

| 컬럼명          | 데이터 타입       | 제약조건               | 기본값                                           | 인덱스  | 설명          |
|--------------|--------------|--------------------|-----------------------------------------------|------|-------------|
| id           | BIGINT       | PK, AUTO_INCREMENT | -                                             | PK   | 제품 모델 ID    |
| manufacturer | VARCHAR(100) | NULL               | -                                             | -    | 제조사         |
| name         | VARCHAR(120) | NOT NULL           | -                                             | -    | 제품명         |
| model_name   | VARCHAR(120) | NOT NULL           | -                                             | -    | 모델명         |
| category     | VARCHAR(60)  | NULL               | -                                             | -    | 제품 분류       |
| is_active    | BOOLEAN      | NOT NULL           | TRUE                                          | -    | 신규 접수 가능 여부 |
| created_at   | DATETIME     | NOT NULL           | CURRENT_TIMESTAMP                             | -    | 생성 일시       |
| updated_at   | DATETIME     | NOT NULL           | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | -    | 수정 일시       |

### 1.2.4 접수 및 배정 도메인 (AS_REQUEST, AS_STATUS_HISTORY, ASSIGNMENT_HISTORY)

#### 1) AS_REQUEST (한 제품당 한 건의 A/S 접수 및 업무 상태)

- 테이블 설명: 한 제품당 한 건의 A/S 접수 및 업무 상태

| 컬럼명               | 데이터 타입      | 제약조건                       | 기본값                                           | 인덱스                  | 설명                                                                       |
|-------------------|-------------|----------------------------|-----------------------------------------------|----------------------|--------------------------------------------------------------------------|
| id                | BIGINT      | PK, AUTO_INCREMENT         | -                                             | PK                   | A/S 요청 ID                                                                |
| request_number    | VARCHAR(40) | NOT NULL                   | -                                             | UK_AS_REQUEST_NUMBER | 접수번호                                                                     |
| receptionist_id   | BIGINT      | NOT NULL, FK (MEMBER.id)   | -                                             | IDX_AS_RECEPTIONIST  | 접수 담당자                                                                   |
| technician_id     | BIGINT      | NULL, FK (MEMBER.id)       | -                                             | IDX_AS_TECHNICIAN    | 배정 기사, 미배정 시 NULL                                                        |
| customer_id       | BIGINT      | NOT NULL, FK (CUSTOMER.id) | -                                             | IDX_AS_CUSTOMER      | 고객 ID                                                                    |
| product_id        | BIGINT      | NOT NULL, FK (PRODUCT.id)  | -                                             | IDX_AS_PRODUC        | 접수 제품 ID                                                                 |
| serial_number     | VARCHAR(50) | NOT NULL                   | -                                             | IDX_SERIAL_NUMBER    | 접수된 제품의 시리얼번호                                                            |
| purchased_at      | DATE        | NULL                       | -                                             | -                    | 구매 시점을 기준으로 일정 기간 이내면 무상 수리                                              |
| symptom           | TEXT        | NOT NULL                   | -                                             | -                    | 고장 증상                                                                    |
| request_memo      | TEXT        | NULL                       | -                                             | -                    | 요청 사항                                                                    |
| return_method     | VARCHAR(20) | NOT NULL                   | DELIVERY                                      | -                    | DELIVERY, PICKUP                                                         |
| status            | VARCHAR(25) | NOT NULL                   | PENDING                                       | IDX_AS_STATUS        | RECEIVED, REPAIRING, WAITING_PAYMENT, READY_RETURN, COMPLETED, CANCELLED |
| status_changed_at | DATETIME    | NOT NULL                   | CURRENT_TIMESTAMP                             | IDX_AS_STATUS_TIME   | 마지막 상태 변경 시간                                                             |
| closed_at         | DATETIME    | NULL                       | -                                             | -                    | 완료 또는 예외 종료 시간                                                           |
| version           | BIGINT      | NOT NULL                   | 0                                             | -                    | 낙관적 락 버전                                                                 |
| created_at        | DATETIME    | NOT NULL                   | CURRENT_TIMESTAMP                             | -                    | 생성 일시                                                                    |
| updated_at        | DATETIME    | NOT NULL                   | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | -                    | 수정 일시                                                                    |

#### 2) AS_STATUS_HISTORY (A/S 상태 변경 및 취소·반려 사유 이력)

- 테이블 설명: A/S 상태 변경 및 취소·반려 사유 이력

| 컬럼명           | 데이터 타입       | 제약조건                         | 기본값               | 인덱스                  | 설명                  |
|---------------|--------------|------------------------------|-------------------|----------------------|---------------------|
| id            | BIGINT       | PK, AUTO_INCREMENT           | -                 | PK                   | 상태 이력 ID            |
| as_request_id | BIGINT       | NOT NULL, FK (AS_REQUEST.id) | -                 | IDX_ASH_REQUEST_TIME | 대상 A/S 요청           |
| from_status   | VARCHAR(25)  | NULL                         | -                 | -                    | 변경 전 상태             |
| to_status     | VARCHAR(25)  | NOT NULL                     | -                 | -                    | 변경 후 상태             |
| reason        | VARCHAR(500) | NULL                         | -                 | -                    | 변경 사유               |
| changed_by    | BIGINT       | NULL, FK (MEMBER.id)         | -                 | IDX_ASH_MEMBER       | 직원 또는 시스템 처리 시 NULL |
| created_at    | DATETIME     | NOT NULL                     | CURRENT_TIMESTAMP | -                    | 변경 시각               |

#### 3) ASSIGNMENT_HISTORY (기사 재배정 감사 이력)

- 테이블 설명: 기사 재배정 감사 이력

| 컬럼명                    | 데이터 타입       | 제약조건                         | 기본값               | 인덱스                | 설명       |
|------------------------|--------------|------------------------------|-------------------|--------------------|----------|
| id                     | BIGINT       | PK, AUTO_INCREMENT           | -                 | PK                 | 배정 이력 ID |
| as_request_id          | BIGINT       | NOT NULL, FK (AS_REQUEST.id) | -                 | IDX_ASSIGN_REQUEST | 대상 요청    |
| previous_technician_id | BIGINT       | NULL, FK (MEMBER.id)         | -                 | -                  | 변경 전 기사  |
| new_technician_id      | BIGINT       | NOT NULL, FK (MEMBER.id)     | -                 | -                  | 변경 후 기사  |
| assigned_by            | BIGINT       | NOT NULL, FK (MEMBER.id)     | -                 | -                  | 배정한 직원   |
| reason                 | VARCHAR(500) | NULL                         | -                 | -                  | 배정 사유    |
| created_at             | DATETIME     | NOT NULL                     | CURRENT_TIMESTAMP | -                  | 배정 시각    |

### 1.2.5 수리 및 비용 도메인 (REPAIR, REPAIR_PRESET, REPAIR_ITEM)

#### 1) REPAIR (요청당 한 개의 수리 작업 및 확정 금액)

- 테이블 설명: 요청당 한 개의 수리 작업 및 확정 금액

| 컬럼명           | 데이터 타입      | 제약조건                                 | 기본값                                           | 인덱스               | 설명                     |
|---------------|-------------|--------------------------------------|-----------------------------------------------|-------------------|------------------------|
| id            | BIGINT      | PK, AUTO_INCREMENT                   | -                                             | PK                | 수리 ID                  |
| as_request_id | BIGINT      | NOT NULL, UNIQUE, FK (AS_REQUEST.id) | -                                             | UK_REPAIR_REQUEST | A/S 요청 1:1             |
| technician_id | BIGINT      | NOT NULL, FK (MEMBER.id)             | -                                             | -                 | 수리 담당 기사               |
| diagnosis     | TEXT        | NULL                                 | -                                             | -                 | 진단 및 고장 원인             |
| work_note     | TEXT        | NULL                                 | -                                             | -                 | 수리 작업 메모               |
| total_amount  | BIGINT      | NOT NULL                             | 0                                             | -                 | 원 단위 수리비 합계            |
| status        | VARCHAR(20) | NOT NULL                             | IN_PROGRESS                                   | -                 | IN_PROGRESS, CONFIRMED |
| confirmed_at  | DATETIME    | NULL                                 | -                                             | -                 | 수리비 확정 시각              |
| created_at    | DATETIME    | NOT NULL                             | CURRENT_TIMESTAMP                             | -                 | 생성 일시                  |
| updated_at    | DATETIME    | NOT NULL                             | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | -                 | 수정 일시                  |

#### 2) REPAIR_PRESET (수리 항목 기본 단가)

- 테이블 설명: 수리 항목 기본 단가

| 컬럼명           | 데이터 타입       | 제약조건               | 기본값                                           | 인덱스  | 설명        |
|---------------|--------------|--------------------|-----------------------------------------------|------|-----------|
| id            | BIGINT       | PK, AUTO_INCREMENT | -                                             | PK   | 프리셋 ID    |
| name          | VARCHAR(120) | NOT NULL           | -                                             | -    | 프리셋 이름    |
| device        | VARCHAR(100) | NOT NULL           | -                                             | -    | 수리된 기기 종류 |
| description   | VARCHAR(500) | NULL               | -                                             | -    | 항목 설명     |
| default_price | INT          | NOT NULL           | 0                                             | -    | 기본 단가 (원) |
| is_active     | BOOLEAN      | NOT NULL           | TRUE                                          | -    | 사용 여부     |
| created_at    | DATETIME     | NOT NULL           | CURRENT_TIMESTAMP                             | -    | 생성 일시     |
| updated_at    | DATETIME     | NOT NULL           | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | -    | 수정 일시     |


### 1.2.6 결제 및 입금 도메인 (PAYMENT, VIRTUAL_ACCOUNT, PAYMENT_EVENT)

#### 1) PAYMENT (수리비 청구 및 최종 결제 상태 관리)

- 테이블 설명: A/S 수리 완료 후 확정된 수리비의 청구 정보와 최종 결제 상태를 관리한다. 하나의 A/S 접수 건에 대해 하나의 `PAYMENT`를 생성하며, 결제 재시도가 발생하더라도 기존 `PAYMENT`를
  유지한다.

| 컬럼명                   | 데이터 타입      | 제약조건                                 | 기본값               | 인덱스                         | 설명                            |
|-----------------------|-------------|--------------------------------------|-------------------|-----------------------------|-------------------------------|
| id                    | BIGINT      | PK, AUTO_INCREMENT                   | -                 | PK                          | 결제 청구 ID                      |
| as_request_id         | BIGINT      | NOT NULL, UNIQUE, FK (AS_REQUEST.id) | -                 | UK_PAYMENT_AS_REQUEST       | 관련 A/S 접수 ID                  |
| amount                | BIGINT      | NOT NULL                             | -                 | -                           | 확정된 수리비 청구 금액                 |
| status                | VARCHAR(20) | NOT NULL                             | READY             | IDX_PAYMENT_STATUS          | 결제 상태 (READY, PAID, CANCELED) |
| successful_attempt_id | BIGINT      | NULL, FK (PAYMENT_ATTEMPT.id)        | NULL              | IDX_PAYMENT_SUCCESS_ATTEMPT | 최종 결제에 성공한 시도 ID              |
| paid_at               | DATETIME    | NULL                                 | NULL              | -                           | 최종 결제 완료 시각                   |
| created_at            | DATETIME    | NOT NULL                             | CURRENT_TIMESTAMP | -                           | 청구 생성 시각                      |
| updated_at            | DATETIME    | NOT NULL                             | CURRENT_TIMESTAMP | -                           | -                             |

#### 2) PAYMENT_ATTEMPT (결제 시도 이력)

- 테이블 설명: 고객의 온라인 결제 시도별 주문번호, 결제수단, PG 승인 결과 및 실패 이력을 관리한다. 하나의 `PAYMENT`에 여러 `PAYMENT_ATTEMPT`가 연결될 수 있다.

| 컬럼명             | 데이터 타입       | 제약조건                      | 기본값               | 인덱스                    | 설명                                                                      |
|-----------------|--------------|---------------------------|-------------------|------------------------|-------------------------------------------------------------------------|
| id              | BIGINT       | PK, AUTO_INCREMENT        | -                 | PK                     | 결제 시도 ID                                                                |
| payment_id      | BIGINT       | NOT NULL, FK (PAYMENT.id) | -                 | IDX_ATTEMPT_PAYMENT    | 관련 결제 청구                                                                |
| order_id        | VARCHAR(64)  | NOT NULL, UNIQUE          | -                 | UK_ATTEMPT_ORDER       | 서버에서 생성하는 고유 주문번호                                                       |
| payment_key     | VARCHAR(200) | NULL, UNIQUE              | NULL              | UK_ATTEMPT_PAYMENT_KEY | 토스페이먼츠 결제 식별자                                                           |
| amount          | BIGINT       | NOT NULL                  | -                 | -                      | 결제 시도 금액                                                                |
| method          | VARCHAR(30)  | NULL                      | NULL              | -                      | 결제수단 (CARD, TRANSFER, VIRTUAL_ACCOUNT 등)                                |
| status          | VARCHAR(30)  | NOT NULL                  | READY             | -                      | READY, CONFIRMING, WAITING_FOR_DEPOSIT, DONE, FAILED, CANCELED, UNKNOWN |
| failure_code    | VARCHAR(100) | NULL                      | NULL              | -                      | 결제 실패 코드                                                                |
| failure_message | VARCHAR(500) | NULL                      | NULL              | -                      | 결제 실패 사유                                                                |
| approved_at     | DATETIME     | NULL                      | NULL              | -                      | PG 승인 완료 시각                                                             |
| created_at      | DATETIME     | NOT NULL                  | CURRENT_TIMESTAMP | -                      | 결제 시도 생성 시각                                                             |
| updated_at      | DATETIME     | NOT NULL                  | CURRENT_TIMESTAMP | -                      |                                                                         |

#### 3) VIRTUAL_ACCOUNT (가상계좌 발급 시점 정보)

- 테이블 설명: 가상계좌 발급 시점 정보

| 컬럼명            | 데이터 타입       | 제약조건                      | 기본값               | 인덱스                  | 설명                      |
|----------------|--------------|---------------------------|-------------------|----------------------|-------------------------|
| id             | BIGINT       | PK, AUTO_INCREMENT        | -                 | PK                   | 가상계좌 ID                 |
| payment_id     | BIGINT       | NOT NULL, FK (PAYMENT.id) | -                 | IDX_VACCOUNT_PAYMENT | 관련 결제                   |
| bank_code      | VARCHAR(20)  | NULL                      | -                 | -                    | 은행 코드                   |
| account_number | VARCHAR(100) | NULL                      | -                 | -                    | 가상계좌 번호(민감정보)           |
| holder_name    | VARCHAR(100) | NULL                      | -                 | -                    | 예금주                     |
| due_at         | DATETIME     | NULL                      | -                 | -                    | 입금 기한                   |
| status         | VARCHAR(20)  | NOT NULL                  | ISSUED            | -                    | ISSUED, EXPIRED, CLOSED |
| created_at     | DATETIME     | NOT NULL                  | CURRENT_TIMESTAMP | -                    | 발급 시각                   |

#### 4) PAYMENT_EVENT (PG 웹훅 중복 수신과 재처리를 위한 이벤트 저장소)

- 테이블 설명: PG 웹훅 중복 수신과 재처리를 위한 이벤트 저장소

| 컬럼명          | 데이터 타입       | 제약조건                  | 기본값               | 인덱스                | 설명                          |
|--------------|--------------|-----------------------|-------------------|--------------------|-----------------------------|
| id           | BIGINT       | PK, AUTO_INCREMENT    | -                 | PK                 | 이벤트 ID                      |
| payment_id   | BIGINT       | NULL, FK (PAYMENT.id) | -                 | IDX_PEVENT_PAYMENT | 대상 결제(미매칭 시 NULL)           |
| event_key    | VARCHAR(200) | NOT NULL, UNIQUE      | -                 | UK_PEVENT_KEY      | PG 제공 식별자 또는 정규화 이벤트 고유키    |
| event_type   | VARCHAR(60)  | NOT NULL              | -                 | -                  | 입금 등 이벤트 유형                 |
| payload      | JSON         | NOT NULL              | -                 | -                  | 검증 필요 최소 원본 이벤트             |
| status       | VARCHAR(20)  | NOT NULL              | RECEIVED          | -                  | RECEIVED, PROCESSED, FAILED |
| received_at  | DATETIME     | NOT NULL              | CURRENT_TIMESTAMP | -                  | 수신 시각                       |
| processed_at | DATETIME     | NULL                  | -                 | -                  | 처리 완료 시각                    |

#### 5) PAYMENT_LINK (고객 결제 URL 관리)

- 테이블 설명: A/S 수리비 결제를 위한 URL의 접근 토큰, 유효기간 및 폐기 여부를 관리한다. 하나의 `PAYMENT`에 여러 결제 링크가 발급될 수 있다.

| 컬럼명        | 데이터 타입   | 제약조건                      | 기본값               | 인덱스                | 설명                     |
|------------|----------|---------------------------|-------------------|--------------------|------------------------|
| id         | BIGINT   | PK, AUTO_INCREMENT        | -                 | PK                 | 결제 링크 ID               |
| payment_id | BIGINT   | NOT NULL, FK (PAYMENT.id) | -                 | IDX_LINK_PAYMENT   | 연결된 결제 청구 ID           |
| token_hash | CHAR(64) | NOT NULL, UNIQUE          | -                 | UK_LINK_TOKEN_HASH | URL 접근 토큰의 SHA-256 해시값 |
| expires_at | DATETIME | NOT NULL                  | -                 | -                  | URL 만료 시각              |
| revoked_at | DATETIME | NULL                      | NULL              | -                  | 링크 폐기 시각               |
| created_at | DATETIME | NOT NULL                  | CURRENT_TIMESTAMP | -                  | 링크 발급 시각               |

### 1.2.7 배송 및 알림 도메인 (SHIPMENT, SHIPMENT_EVENT, SMS_LOG)

#### 1) SHIPMENT (택배 배송 또는 방문 수령 정보)

- 테이블 설명: 택배 배송 또는 방문 수령 정보

| 컬럼명             | 데이터 타입       | 제약조건                                 | 기본값                                           | 인덱스                 | 설명                         |
|-----------------|--------------|--------------------------------------|-----------------------------------------------|---------------------|----------------------------|
| id              | BIGINT       | PK, AUTO_INCREMENT                   | -                                             | PK                  | 반환 ID                      |
| as_request_id   | BIGINT       | NOT NULL, UNIQUE, FK (AS_REQUEST.id) | -                                             | UK_SHIPMENT_REQUEST | A/S 요청 1:1                 |
| courier         | VARCHAR(60)  | NULL                                 | -                                             | -                   | 택배사                        |
| tracking_number | VARCHAR(100) | NULL                                 | -                                             | -                   | 운송장번호                      |
| status          | VARCHAR(25)  | NOT NULL                             | READY                                         | -                   | READY, SHIPPING, COMPLETED |
| shipped_at      | DATETIME     | NULL                                 | -                                             | -                   | 배송 시작                      |
| completed_at    | DATETIME     | NULL                                 | -                                             | -                   | 반환 완료                      |
| created_at      | DATETIME     | NOT NULL                             | CURRENT_TIMESTAMP                             | -                   | 생성 일시                      |
| updated_at      | DATETIME     | NOT NULL                             | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | -                   | 수정 일시                      |

#### 2) SHIPMENT_EVENT (배송 상태 변경·모의 웹훅의 멱등성 기록)

- 테이블 설명: 배송 상태 변경·모의 웹훅의 멱등성 기록

| 컬럼명         | 데이터 타입       | 제약조건                       | 기본값               | 인덱스                 | 설명                          |
|-------------|--------------|----------------------------|-------------------|---------------------|-----------------------------|
| id          | BIGINT       | PK, AUTO_INCREMENT         | -                 | PK                  | 배송 이벤트 ID                   |
| shipment_id | BIGINT       | NOT NULL, FK (SHIPMENT.id) | -                 | IDX_SEVENT_SHIPMENT | 관련 배송                       |
| event_key   | VARCHAR(150) | NOT NULL, UNIQUE           | -                 | UK_SEVENT_KEY       | 모의 웹훅 중복 방지 식별자             |
| event_type  | VARCHAR(50)  | NOT NULL                   | -                 | -                   | 배송 이벤트 유형                   |
| status      | VARCHAR(20)  | NOT NULL                   | RECEIVED          | -                   | RECEIVED, PROCESSED, FAILED |
| created_at  | DATETIME     | NOT NULL                   | CURRENT_TIMESTAMP | -                   | 발생 시각                       |

#### 3) SMS_LOG (A/S 단계별 문자 발송 결과 및 중복 방지)

- 테이블 설명: A/S 단계별 문자 발송 결과 및 중복 방지

| 컬럼명                 | 데이터 타입       | 제약조건                     | 기본값               | 인덱스             | 설명                                                  |
|---------------------|--------------|--------------------------|-------------------|-----------------|-----------------------------------------------------|
| id                  | BIGINT       | PK, AUTO_INCREMENT       | -                 | PK              | 발송 ID                                               |
| as_request_id       | BIGINT       | NULL, FK (AS_REQUEST.id) | -                 | IDX_SMS_REQUEST | 관련 A/S 건                                            |
| event_key           | VARCHAR(150) | NOT NULL, UNIQUE         | -                 | UK_SMS_EVENT    | 같은 업무 이벤트 중복 발송 방지 키                                |
| type                | VARCHAR(40)  | NOT NULL                 | -                 | -               | RECEIVED, ACCOUNT_ISSUED, PAID, SHIPPING, COMPLETED |
| recipient_phone     | VARCHAR(20)  | NOT NULL                 | -                 | -               | 문자 수신번호                                             |
| message             | TEXT         | NOT NULL                 | -                 | -               | 발송 메시지                                              |
| provider_message_id | VARCHAR(120) | NULL                     | -                 | -               | 외부 SMS 응답 식별자                                       |
| status              | VARCHAR(20)  | NOT NULL                 | PENDING           | -               | PENDING, SENT, FAILED                               |
| error_message       | VARCHAR(500) | NULL                     | -                 | -               | 실패 사유                                               |
| sent_at             | DATETIME     | NULL                     | -                 | -               | 발송 시각                                               |
| created_at          | DATETIME     | NOT NULL                 | CURRENT_TIMESTAMP | -               | 생성 시각                                               |

#### 4) SMS_PRESET

| 컬럼명         | 데이터 타입      | 제약조건               | 기본값 | 인덱스 | 설명     |
|-------------|-------------|--------------------|-----|-----|--------|
| id          | BIGINT      | PK, AUTO_INCREMENT | -   | PK  | 발송 ID  |
| preset_name | VARCHAR(30) | -                  | -   | -   | 프리셋 이름 |
| title       | VARCHAR(50) | -                  | -   | -   | 문자 제목  |
| content     | TEXT        | -                  | -   | -   | 문자 내용  |

### 1.2.9 운영 감사 도메인 (AUDIT_LOG)

#### 1) AUDIT_LOG (관리자 중요 작업의 추적 로그(선택 구현))

- 테이블 설명: 관리자 중요 작업의 추적 로그(선택 구현)

| 컬럼명         | 데이터 타입       | 제약조건                 | 기본값               | 인덱스  | 설명             |
|-------------|--------------|----------------------|-------------------|------|----------------|
| id          | BIGINT       | PK, AUTO_INCREMENT   | -                 | PK   | 감사 ID          |
| actor_id    | BIGINT       | NULL, FK (MEMBER.id) | -                 | -    | 수행 계정          |
| action      | VARCHAR(80)  | NOT NULL             | -                 | -    | 작업 유형          |
| target_type | VARCHAR(60)  | NOT NULL             | -                 | -    | 대상 엔티티 유형      |
| target_id   | BIGINT       | NULL                 | -                 | -    | 대상 엔티티 ID      |
| description | VARCHAR(500) | NULL                 | -                 | -    | 변경 개요(민감정보 제외) |
| created_at  | DATETIME     | NOT NULL             | CURRENT_TIMESTAMP | -    | 작업 시각          |

---

### 1.2.10 선택 확장 도메인 (OUTBOX_EVENT)

#### 1) OUTBOX_EVENT (SMS 발송 요청 유실 방지)

- 설명: 업무 트랜잭션과 함께 SMS 발송 요청을 저장하고 스케줄러로 재시도한다. **2순위 구현**이다.

| 컬럼명           | 데이터 타입       | 제약조건                     | 기본값               | 인덱스                 | 설명         |
|---------------|--------------|--------------------------|-------------------|---------------------|------------|
| id            | BIGINT       | PK, AUTO_INCREMENT       | -                 | PK                  | 이벤트 ID     |
| event_key     | VARCHAR(150) | NOT NULL, UNIQUE         | -                 | UK_OUTBOX_EVENT_KEY | 중복 생성 방지   |
| as_request_id | BIGINT       | NULL, FK (AS_REQUEST.id) | -                 | IDX_OUTBOX_REQUEST  | 관련 접수      |
| event_type    | VARCHAR(50)  | NOT NULL                 | -                 | -                   | SMS 이벤트 유형 |
| payload       | JSON         | NOT NULL                 | -                 | -                   | 발송 데이터     |
| status        | VARCHAR(20)  | NOT NULL                 | 'PENDING'         | IDX_OUTBOX_RETRY    | 처리 상태      |
| retry_count   | INT          | NOT NULL                 | 0                 | -                   | 재시도 횟수     |
| next_retry_at | DATETIME     | NULL                     | -                 | IDX_OUTBOX_RETRY    | 재시도 예정     |
| created_at    | DATETIME     | NOT NULL                 | CURRENT_TIMESTAMP | -                   | 생성 시각      |
| processed_at  | DATETIME     | NULL                     | -                 | -                   | 처리 완료      |


## 1.3 핵심 데이터 모델링 정책

### 1.3.1 단일 업체·단일 지점

- `COMPANY` 테이블 및 `company_id`는 사용하지 않는다.
- 고객은 로그인하지 않으며, 직원이 전화·이메일·방문 접수를 등록한다.
- `CUSTOMER.type`으로 개인(`INDIVIDUAL`)과 기업(`BUSINESS`)을 구분하고 기업 담당자 정보를 선택 저장한다.
- 직원 권한은 `ADMIN`, `RECEPTIONIST`, `TECHNICIAN`이며 `MEMBER_ROLE`로 복수 역할을 지원한다.

### 1.3.2 고객·제품·접수

- 고객 한 명이 여러 제품을 보유할 수 있으며 같은 제품에 여러 A/S 요청을 연결할 수 있다.
- 시리얼번호는 선택 입력이므로 전역 UNIQUE로 강제하지 않는다.
- `AS_REQUEST.customer_id`와 `CUSTOMER_PRODUCT.customer_id`가 일치하는지 검증한다.
- 고객은 기존 수리·결제 이력 보존을 위해 비활성화 방식으로 삭제한다.

### 1.3.3 상태·금액·동시성

- `AS_REQUEST.status`를 현재 업무 상태의 기준으로 사용하고 `AS_STATUS_HISTORY`를 남긴다.
- 수리비 확정 금액은 `REPAIR`, 항목별 적용 단가는 `REPAIR_ITEM`에 스냅샷으로 저장한다.
- 상태 전이는 공통 서비스에서 검증하고 동시 수정은 `version` 등으로 제어한다.

### 1.3.4 결제·배송·알림

- `PAYMENT_EVENT`는 **들어오는** PG 웹훅의 중복 처리 기록이고 `OUTBOX_EVENT`는 **나가는** SMS 요청 유실 방지용이다.
- `VIRTUAL_ACCOUNT`는 가상계좌 발급·재발급 기록을 보존한다.
- `SHIPMENT_EVENT`는 모의 배송 웹훅 이력을 기록한다.
- `SMS_LOG`는 발송 결과를 기록한다. Outbox를 사용해도 외부 SMS의 중복 발송 가능성은 별도로 관리해야 한다.