# 02_ERD

> 현재 JPA 엔티티 구현을 기준으로 작성한 ERD입니다.

## 핵심 관계 요약

```mermaid
erDiagram
 MEMBER ||--o{ REFRESH_TOKEN : issues
 PRODUCT_MODEL ||--o{ PRODUCT : defines
 CUSTOMER ||--o{ PRODUCT : owns
 CUSTOMER ||--o{ AS_RECEPTION : receptions
 PRODUCT ||--o{ AS_RECEPTION : serviced
 MEMBER ||--o{ AS_RECEPTION : receives
 MEMBER ||--o{ AS_RECEPTION : assigned
 AS_RECEPTION ||--o{ AS_STATUS_HISTORY : transitions
 MEMBER ||--o{ AS_STATUS_HISTORY : changes
 AS_RECEPTION ||--o{ AS_TECHNICIAN_ASSIGNMENT_HISTORY : assignments
 MEMBER ||--o{ AS_TECHNICIAN_ASSIGNMENT_HISTORY : previous
 MEMBER ||--o{ AS_TECHNICIAN_ASSIGNMENT_HISTORY : new
 MEMBER ||--o{ AS_TECHNICIAN_ASSIGNMENT_HISTORY : changed_by
 SYMPTOM_PRESET ||--o{ REPAIR_SYMPTOM_RELATION : links
 REPAIR_PRESET ||--o{ REPAIR_SYMPTOM_RELATION : linked
 AS_RECEPTION ||--o| REPAIR : repair
 MEMBER ||--o{ REPAIR : performs
 REPAIR ||--o{ REPAIR_SYMPTOM_PRESET : symptoms
 REPAIR ||--o{ REPAIR_WORK : work
 REPAIR ||--o{ REPAIR_PHOTO : photos
 PART ||--o{ PART_COMPATIBILITY : compatible
 PRODUCT_MODEL ||--o{ PART_COMPATIBILITY : supports
 REPAIR_PRESET ||--o{ REPAIR_PRESET_PART : needs
 PART ||--o{ REPAIR_PRESET_PART : included
 REPAIR_WORK ||--o{ REPAIR_PART_USAGE : consumes
 PART ||--o{ REPAIR_PART_USAGE : used
 PART ||--o{ PART_STOCK_HISTORY : movements
 REPAIR_PART_USAGE ||--o{ PART_STOCK_HISTORY : outbound
 MEMBER ||--o{ PART_STOCK_HISTORY : processed
 AS_RECEPTION ||--o| PAYMENT_BILL : billed
 PAYMENT_BILL ||--o{ PAYMENT_ATTEMPT : attempts
 PAYMENT_ATTEMPT ||--o| VIRTUAL_ACCOUNT : account
 PAYMENT_ATTEMPT ||--o{ PAYMENT_REFUND : refunds
 AS_RECEPTION ||--o| SHIPMENT : returns
 SHIPMENT ||--o{ SHIPMENT_EVENT : events
 AS_RECEPTION ||--o| CUSTOMER_PORTAL_LINK : portal
 AS_RECEPTION ||--o{ NOTIFICATION_LOG : notices
 MEMBER ||--o{ NOTIFICATION_LOG : sent_by
 MEMBER ||--o{ CHAT : chats
```

## 테이블별 상세 컬럼

## 1.2.1 직원 계정 및 권한 관리

### 1) MEMBER

- 테이블 설명: 내부 직원 로그인 계정

| 컬럼명           | 데이터 타입       | 제약조건                      | 기본값               | 인덱스 | 설명                                             |
|---------------|--------------|---------------------------|-------------------|-----|------------------------------------------------|
| id            | BIGINT       | PK, AUTO_INCREMENT        | -                 | PK  | 직원 ID                                          |
| email         | VARCHAR(255) | NOT NULL, UNIQUE          | -                 | -   | 로그인 이메일                                        |
| password_hash | VARCHAR(255) | NOT NULL                  | -                 | -   | 암호 해시                                          |
| name          | VARCHAR(255) | NOT NULL                  | -                 | -   | 직원명                                            |
| phone         | VARCHAR(255) | NOT NULL                  | -                 | -   | 연락처                                            |
| address       | VARCHAR(255) | NOT NULL                  | -                 | -   | 주소                                             |
| role          | VARCHAR(255) | NOT NULL                  | -                 | -   | ROLE_ADMIN, ROLE_RECEPTIONIST, ROLE_TECHNICIAN |
| status        | VARCHAR(255) | NOT NULL                  | ACTIVE            | -   | ACTIVE, INACTIVE, DELETED                      |
| deleted_at    | DATETIME     | NULL                      | -                 | -   | 퇴사(삭제) 시각                                      |
| created_at    | DATETIME     | NOT NULL                  | CURRENT_TIMESTAMP | -   | 생성 시각                                          |
| updated_at    | DATETIME     | NOT NULL                  | CURRENT_TIMESTAMP | -   | 수정 시각                                          |
| created_by    | BIGINT       | NOT NULL, FK -> Member.id | -                 | -   | 생성자                                            |
| updated_by    | BIGINT       | NOT NULL, FK -> Member.id | -                 | -   | 수정자                                            |

### 2) REFRESH_TOKEN

- 테이블 설명: 직원 로그인 세션 갱신용 토큰

| 컬럼명        | 데이터 타입       | 제약조건               | 기본값               | 인덱스             | 설명               |
|------------|--------------|--------------------|-------------------|-----------------|------------------|
| id         | BIGINT       | PK, AUTO_INCREMENT | -                 | PK              | 토큰 ID            |
| member_id  | BIGINT       | FK → MEMBER.id     | -                 | -               | 직원 ID            |
| token_hash | VARCHAR(255) | NOT NULL, UNIQUE   | -                 | UK_REFRESH_HASH | Refresh Token 해시 |
| issued_at  | DATETIME     | NOT NULL           | CURRENT_TIMESTAMP | -               | 발급 시각            |
| expires_at | DATETIME     | NOT NULL           | -                 | -               | 만료 시각            |
| revoked_at | DATETIME     | NULL               | -                 | -               | 폐기 시각            |

## 1.2.2 고객 정보 관리

### 1) CUSTOMER

- 테이블 설명: 회원가입하지 않는 A/S 고객의 연락처 및 주소

| 컬럼명            | 데이터 타입       | 제약조건               | 기본값               | 인덱스 | 설명    |
|----------------|--------------|--------------------|-------------------|-----|-------|
| id             | BIGINT       | PK, AUTO_INCREMENT | -                 | PK  | 고객 ID |
| name           | VARCHAR(255) | NOT NULL           | -                 | -   | 고객명   |
| phone          | VARCHAR(255) | NOT NULL           | -                 | -   | 연락처   |
| email          | VARCHAR(255) | NOT NULL           | -                 | -   | 이메일   |
| zipcode        | VARCHAR(255) | NULL               | -                 | -   | 우편번호  |
| basic_address  | VARCHAR(255) | NULL               | -                 | -   | 기본 주소 |
| detail_address | VARCHAR(255) | NULL               | -                 | -   | 상세 주소 |
| created_at     | DATETIME     | NOT NULL           | CURRENT_TIMESTAMP | -   | 생성 시각 |
| updated_at     | DATETIME     | NOT NULL           | CURRENT_TIMESTAMP | -   | 수정 시각 |
| created_by     | BIGINT       | NOT NULL           | -                 | -   | 생성자   |
| updated_by     | BIGINT       | NOT NULL           | -                 | -   | 수정자   |

## 1.2.3 제품 및 수리 이력 관리

### 1) PRODUCT_MODEL

- 테이블 설명: 수리 대상 모델 마스터

| 컬럼명             | 데이터 타입       | 제약조건               | 기본값               | 인덱스 | 설명            |
|-----------------|--------------|--------------------|-------------------|-----|---------------|
| id              | BIGINT       | PK, AUTO_INCREMENT | -                 | PK  | 모델 ID         |
| name            | VARCHAR(255) | NOT NULL           | -                 | -   | 제품명           |
| model_name      | VARCHAR(255) | NOT NULL           | -                 | -   | 모델명           |
| category        | VARCHAR(255) | NOT NULL           | -                 | -   | LAPTOP, PHONE |
| warranty_months | INT          | NULL               | -                 | -   | 기본 제조 보증 개월 수 |
| is_active       | BOOLEAN      | NOT NULL           | TRUE              | -   | 신규 접수 가능 여부   |
| created_at      | DATETIME     | NOT NULL           | CURRENT_TIMESTAMP | -   | 생성 시각         |
| updated_at      | DATETIME     | NOT NULL           | CURRENT_TIMESTAMP | -   | 수정 시각         |
| created_by      | BIGINT       | NOT NULL           | -                 | -   | 생성자           |
| updated_by      | BIGINT       | NOT NULL           | -                 | -   | 수정자           |

### 2) PRODUCT

- 테이블 설명: 자사가 제조·판매한 제품을 시리얼번호 단위로 등록하고 관리한다.
- 고객에게 판매되기 전에도 제품 정보를 보유할 수 있다.
- A/S 접수 시 시리얼번호를 통해 모델과 제품 정보를 자동 조회한다.

| 컬럼명              | 데이터 타입       | 제약조건               | 기본값               | 인덱스                       | 설명          |
|------------------|--------------|--------------------|-------------------|---------------------------|-------------|
| id               | BIGINT       | PK, AUTO_INCREMENT | -                 | PK                        | 자사 제품 ID    |
| product_model_id | BIGINT       | NOT NULL           | -                 | IDX_COMPANY_PRODUCT_MODEL | 제품 모델 ID    |
| customer_id      | BIGINT       | NULL               | -                 | IDX_CP_CUSTOMER           | 소유 고객       |
| serial_number    | VARCHAR(255) | NOT NULL, UNIQUE   | -                 | UK_COMPANY_PRODUCT_SERIAL | 제품 고유 시리얼번호 |
| manufactured_at  | DATETIME     | NOT NULL           | -                 | -                         | 제조 시각       |
| purchased_at     | DATETIME     | NULL               | -                 | -                         | 구매 시각       |
| created_at       | DATETIME     | NOT NULL           | CURRENT_TIMESTAMP | -                         | 등록 시각       |
| updated_at       | DATETIME     | NOT NULL           | CURRENT_TIMESTAMP | -                         | 수정 시각       |
| created_by       | BIGINT       | NOT NULL           | -                 | -                         | 생성자         |
| updated_by       | BIGINT       | NOT NULL           | -                 | -                         | 수정자         |

## 1.2.4 A/S 요청 및 접수 관리

### 1) SYMPTOM_PRESET

- 테이블 설명: 고장 증상 프리셋

| 컬럼명              | 데이터 타입       | 제약조건               | 기본값 | 인덱스 | 설명        |
|------------------|--------------|--------------------|-----|-----|-----------|
| id               | BIGINT       | PK, AUTO_INCREMENT | -   | PK  | 증상 프리셋 ID |
| product_category | VARCHAR(255) | NULL               | -   | -   | 적용 제품군    |
| name             | VARCHAR(255) | NULL               | -   | -   | 증상 프리셋명   |

### 2) AS_RECEPTION

- 테이블 설명: A/S 접수 건과 현재 업무 상태. 한 건은 하나의 제품을 대상으로 한다.

| 컬럼명               | 데이터 타입        | 제약조건                       | 기본값               | 인덱스                    | 설명                                                                                                                                                  |
|-------------------|---------------|----------------------------|-------------------|------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| id                | BIGINT        | PK, AUTO_INCREMENT         | -                 | PK                     | A/S ID                                                                                                                                              |
| reception_number  | VARCHAR(255)  | NOT NULL, UNIQUE           | -                 | UK_AS_RECEPTION_NUMBER | 접수번호                                                                                                                                                |
| customer_id       | BIGINT        | NOT NULL, FK → CUSTOMER.id | -                 | IDX_AS_CUSTOMER        | 고객 ID                                                                                                                                               |
| product_id        | BIGINT        | NOT NULL, FK → PRODUCT.id  | -                 | IDX_AS_PRODUCT         | 접수 제품물                                                                                                                                              |
| receptionist_id   | BIGINT        | NOT NULL, FK → MEMBER.id   | -                 | IDX_AS_RECEPTIONIST    | 접수 직원                                                                                                                                               |
| technician_id     | BIGINT        | NOT NULL, FK → MEMBER.id   | -                 | IDX_AS_TECHNICIAN      | 현재 배정 기사                                                                                                                                            |
| symptom           | VARCHAR(255)  | NOT NULL                   | -                 | -                      | 고장증상 요약                                                                                                                                             |
| symptom_detail    | VARCHAR(255)  | NOT NULL                   | -                 | -                      | 고장 증상 상세                                                                                                                                            |
| memo              | VARCHAR(1000) | NULL                       | -                 | -                      | 요청 메모                                                                                                                                               |
| return_method     | VARCHAR(255)  | NOT NULL                   | DELIVERY          | -                      | DELIVERY, PICKUP                                                                                                                                    |
| file_url          | VARCHAR(255)  | NULL                       | -                 | -                      | 구매 영수증 url                                                                                                                                          |
| status            | VARCHAR(255)  | NOT NULL                   | RECEIVED          | IDX_AS_STATUS          | RECEIVED, INSPECTION, AWAITING_QUOTE_APPROVAL, AWAITING_REPAIR, REPAIR_IN_PROGRESS, AWAITING_PAYMENT, AWAITING_SHIPMENT, AWAITING_PICKUP, COMPLETED |
| status_changed_at | DATETIME      | NOT NULL                   | CURRENT_TIMESTAMP | -                      | 마지막 상태 변경                                                                                                                                           |
| closed_at         | DATETIME      | NULL                       | -                 | -                      | 종결 시각                                                                                                                                               |
| created_at        | DATETIME      | NOT NULL                   | CURRENT_TIMESTAMP | IDX_AS_CREATED         | 접수 시각                                                                                                                                               |
| updated_at        | DATETIME      | NOT NULL                   | CURRENT_TIMESTAMP | -                      | 수정 시각                                                                                                                                               |
| created_by        | BIGINT        | NOT NULL, FK -> Member.id  | -                 | -                      | 생성자                                                                                                                                                 |
| updated_by        | BIGINT        | NOT NULL, FK -> Member.id  | -                 | -                      | 수정자                                                                                                                                                 |

## 1.2.5 A/S 배정 및 진행 관리

### 1) AS_STATUS_HISTORY

- 테이블 설명: A/S 상태 전이 이력

| 컬럼명             | 데이터 타입        | 제약조건                           | 기본값               | 인덱스                    | 설명                |
|-----------------|---------------|--------------------------------|-------------------|------------------------|-------------------|
| id              | BIGINT        | PK, AUTO_INCREMENT             | -                 | PK                     | 이력 ID             |
| as_reception_id | BIGINT        | NOT NULL, FK → AS_RECEPTION.id | -                 | IDX_ASH_RECEPTION_TIME | A/S ID            |
| from_status     | VARCHAR(255)  | NULL                           | -                 | -                      | 이전 상태             |
| to_status       | VARCHAR(255)  | NOT NULL                       | -                 | -                      | 변경 상태             |
| reason          | VARCHAR(1000) | NULL                           | -                 | -                      | 사유                |
| changed_by      | BIGINT        | NULL, FK → MEMBER.id           | -                 | -                      | 처리 직원; 시스템이면 NULL |
| created_at      | DATETIME      | NOT NULL                       | CURRENT_TIMESTAMP | -                      | 상태 변경 시각          |

### 2) AS_TECHNICIAN_ASSIGNMENT_HISTORY

- 테이블 설명: 담당 기사 배정 및 재배정 이력

| 컬럼명                    | 데이터 타입        | 제약조건                           | 기본값               | 인덱스 | 설명       |
|------------------------|---------------|--------------------------------|-------------------|-----|----------|
| id                     | BIGINT        | PK, AUTO_INCREMENT             | -                 | PK  | 이력 ID    |
| as_reception_id        | BIGINT        | NOT NULL, FK → AS_RECEPTION.id | -                 | -   | A/S ID   |
| previous_technician_id | BIGINT        | NULL, FK → MEMBER.id           | -                 | -   | 이전 담당 기사 |
| new_technician_id      | BIGINT        | NOT NULL, FK → MEMBER.id       | -                 | -   | 신규 담당 기사 |
| reason                 | VARCHAR(1000) | NULL                           | -                 | -   | 재배정 사유   |
| changed_by             | BIGINT        | NOT NULL, FK → MEMBER.id       | -                 | -   | 처리 직원    |
| created_at             | DATETIME      | NOT NULL                       | CURRENT_TIMESTAMP | -   | 배정 변경 시각 |

## 1.2.6 수리 작업 및 비용 관리

### 1) LABOR_RATE

- 테이블 설명: 제품군별 공임·점검 단가

| 컬럼명              | 데이터 타입       | 제약조건                      | 기본값               | 인덱스 | 설명                          |
|------------------|--------------|---------------------------|-------------------|-----|-----------------------------|
| id               | BIGINT       | PK, AUTO_INCREMENT        | -                 | PK  | 공임 단가 ID                    |
| product_category | VARCHAR(255) | NOT NULL                  | -                 | -   | LAPTOP, PHONE               |
| cost             | INT          | NOT NULL                  | -                 | -   | 비용                          |
| rate_type        | VARCHAR(255) | NOT NULL                  | -                 | -   | INSPECTION_COST, LABOR_RATE |
| effective_date   | DATE         | NOT NULL                  | -                 | -   | 적용일                         |
| created_at       | DATETIME     | NOT NULL                  | CURRENT_TIMESTAMP | -   | 생성 시각                       |
| updated_at       | DATETIME     | NOT NULL                  | CURRENT_TIMESTAMP | -   | 수정 시각                       |
| created_by       | BIGINT       | NOT NULL, FK -> Member.id | -                 | -   | 생성자                         |
| updated_by       | BIGINT       | NOT NULL, FK -> Member.id | -                 | -   | 수정자                         |

### 2) REPAIR_PRESET

- 테이블 설명: 표준 수리 작업 프리셋

| 컬럼명         | 데이터 타입       | 제약조건                      | 기본값               | 인덱스 | 설명     |
|-------------|--------------|---------------------------|-------------------|-----|--------|
| id          | BIGINT       | PK, AUTO_INCREMENT        | -                 | PK  | 프리셋 ID |
| name        | VARCHAR(255) | NOT NULL                  | -                 | -   | 작업명    |
| description | VARCHAR(255) | NULL                      | -                 | -   | 작업 설명  |
| created_at  | DATETIME     | NOT NULL                  | CURRENT_TIMESTAMP | -   | 생성 시각  |
| updated_at  | DATETIME     | NOT NULL                  | CURRENT_TIMESTAMP | -   | 수정 시각  |
| created_by  | BIGINT       | NOT NULL, FK -> Member.id | -                 | -   | 생성자    |
| updated_by  | BIGINT       | NOT NULL, FK -> Member.id | -                 | -   | 수정자    |

### 3) REPAIR_SYMPTOM_RELATION

- 테이블 설명: 고장 증상 프리셋과 수리 프리셋 관계 테이블

| 컬럼명               | 데이터 타입   | 제약조건                             | 기본값               | 인덱스 | 설명        |
|-------------------|----------|----------------------------------|-------------------|-----|-----------|
| id                | BIGINT   | PK, AUTO_INCREMENT               | -                 | PK  | 관계 ID     |
| symptom_preset_id | BIGINT   | NOT NULL, FK → SYMPTOM_PRESET.id | -                 | FK  | 증상 프리셋 ID |
| repair_preset_id  | BIGINT   | NOT NULL, FK → REPAIR_PRESET.id  | -                 | FK  | 수리 프리셋 ID |
| created_at        | DATETIME | NOT NULL                         | CURRENT_TIMESTAMP | -   | 생성 시각     |

### 4) REPAIR

- 테이블 설명: A/S별 점검 및 수리 결과.
- A/S 요청에 기사 배정되는 시점에 새 Row 생성

| 컬럼명                  | 데이터 타입       | 제약조건                                   | 기본값               | 인덱스             | 설명                                                              |
|----------------------|--------------|----------------------------------------|-------------------|-----------------|-----------------------------------------------------------------|
| id                   | BIGINT       | PK, AUTO_INCREMENT                     | -                 | PK              | 수리 ID                                                           |
| as_reception_id      | BIGINT       | NOT NULL, UNIQUE, FK → AS_RECEPTION.id | -                 | UK_REPAIR_AS    | A/S 1:1                                                         |
| technician_id        | BIGINT       | NOT NULL, FK → MEMBER.id               | -                 | IDX_REPAIR_TECH | 담당 기사                                                           |
| diagnosis            | VARCHAR(255) | NULL                                   | -                 | -               | 점검 결과                                                           |
| work_note            | VARCHAR(255) | NULL                                   | -                 | -               | 수리 메모                                                           |
| repair_decision      | VARCHAR(255) | NULL                                   | -                 | -               | REPAIRABLE, UNREPAIRABLE                                        |
| warranty_result      | VARCHAR(255) | NOT NULL                               | -                 | -               | FREE, PAID, UNDECIDED                                           |
| reason               | VARCHAR(255) | NULL                                   | -                 | -               | 보증 판정 사유                                                        |
| labor_rate           | INT          | NULL                                   | -                 | -               | 당시 적용된 공임 비용                                                    |
| inspection_cost      | INT          | NULL                                   | -                 | -               | 당시 적용된 점검 비용                                                    |
| final_amount         | INT          | NULL                                   | -                 | -               | 최종 확정 수리비(원)                                                    |
| status               | VARCHAR(255) | NOT NULL                               | DIAGNOSING        | -               | DIAGNOSING, REQUESTED, APPROVED, REJECTED, REPAIRING, COMPLETED |
| reason_for_rejection | VARCHAR(255) | NULL                                   | -                 | -               | 고객이 입력한 견적 거부 사유                                                |
| confirmed_at         | DATETIME     | NULL                                   | -                 | -               | 최종 비용 확정 시각                                                     |
| created_at           | DATETIME     | NOT NULL                               | CURRENT_TIMESTAMP | -               | 생성 시각                                                           |
| updated_at           | DATETIME     | NOT NULL                               | CURRENT_TIMESTAMP | -               | 수정 시각                                                           |
| created_by           | BIGINT       | NOT NULL, FK -> Member.id              | -                 | -               | 생성자                                                             |
| updated_by           | BIGINT       | NOT NULL, FK -> Member.id              | -                 | -               | 수정자                                                             |

### 5) REPAIR_SYMPTOM_PRESET

- 1개의 A/S에서 사용한 고장 증상 프리셋의 이름

| 컬럼명                | 데이터 타입       | 제약조건                     | 기본값 | 인덱스              | 설명           |
|--------------------|--------------|--------------------------|-----|------------------|--------------|
| id                 | BIGINT       | PK, AUTO_INCREMENT       | -   | PK               | 작업 항목 ID     |
| repair_id          | BIGINT       | NOT NULL, FK → REPAIR.id | -   | IDX_RITEM_REPAIR | 수리 ID        |
| symptom_preset_nam | VARCHAR(255) | NOT NULL                 | -   | -                | 고장 증상 프리셋 이름 |

> 코드 필드명 `symptomPresetNam` 기준 컬럼명입니다.

### 6) REPAIR_WORK

- 테이블 설명: 실제 수행한 수리 작업 항목

| 컬럼명                | 데이터 타입       | 제약조건                      | 기본값               | 인덱스              | 설명        |
|--------------------|--------------|---------------------------|-------------------|------------------|-----------|
| id                 | BIGINT       | PK, AUTO_INCREMENT        | -                 | PK               | 작업 항목 ID  |
| repair_id          | BIGINT       | NOT NULL, FK → REPAIR.id  | -                 | IDX_RITEM_REPAIR | 수리 ID     |
| repair_preset_name | VARCHAR(255) | NOT NULL                  | -                 | -                | 선택 프리셋 이름 |
| work_name          | VARCHAR(255) | NOT NULL                  | -                 | -                | 작업명 스냅샷   |
| created_at         | DATETIME     | NOT NULL                  | CURRENT_TIMESTAMP | -                | 생성 시각     |
| updated_at         | DATETIME     | NOT NULL                  | CURRENT_TIMESTAMP | -                | 수정 시각     |
| created_by         | BIGINT       | NOT NULL, FK -> Member.id | -                 | -                | 생성자       |
| updated_by         | BIGINT       | NOT NULL, FK -> Member.id | -                 | -                | 수정자       |

### 7) REPAIR_PHOTO

- 테이블 설명: 수리 전후 사진

| 컬럼명        | 데이터 타입       | 제약조건                     | 기본값               | 인덱스               | 설명                       |
|------------|--------------|--------------------------|-------------------|-------------------|--------------------------|
| id         | BIGINT       | PK, AUTO_INCREMENT       | -                 | PK                | 사진 ID                    |
| repair_id  | BIGINT       | NOT NULL, FK → REPAIR.id | -                 | IDX_RPHOTO_REPAIR | 수리 ID                    |
| photo_type | VARCHAR(255) | NOT NULL                 | -                 | -                 | BEFORE, AFTER, DIAGNOSIS |
| file_url   | VARCHAR(255) | NOT NULL                 | -                 | -                 | 사진 경로                    |
| created_at | DATETIME     | NOT NULL                 | CURRENT_TIMESTAMP | -                 | 등록 시각                    |
| updated_at | DATETIME     | NOT NULL                 | CURRENT_TIMESTAMP | -                 | 수정 시각                    |
| created_by | VARCHAR(255) | NOT NULL                 | -                 | -                 | 생성자                      |
| updated_by | VARCHAR(255) | NOT NULL                 | -                 | -                 | 수정자                      |

## 1.2.7 부품 재고 관리

### 1) PART

- 테이블 설명: 부품과 현재 재고 수량

| 컬럼명              | 데이터 타입       | 제약조건                      | 기본값               | 인덱스 | 설명                                 |
|------------------|--------------|---------------------------|-------------------|-----|------------------------------------|
| id               | BIGINT       | PK, AUTO_INCREMENT        | -                 | PK  | 부품 ID                              |
| name             | VARCHAR(255) | NOT NULL                  | -                 | -   | 부품명                                |
| unit_price       | INT          | NOT NULL                  | -                 | -   | 기준 단가(원)                           |
| standard_minutes | INT          | NOT NULL                  | -                 | -   | 표준 작업시간 (10분 단위로 저장)               |
| stock_quantity   | INT          | NOT NULL                  | -                 | -   | 현재 수량                              |
| status           | VARCHAR(255) | NOT NULL                  | -                 | -   | ACTIVE, LOW_QUANTITY, DISCONTINUED |
| version          | BIGINT       | NOT NULL                  | -                 | -   | 재고 동시성 제어(@Version)                |
| created_at       | DATETIME     | NOT NULL                  | CURRENT_TIMESTAMP | -   | 생성 시각                              |
| updated_at       | DATETIME     | NOT NULL                  | CURRENT_TIMESTAMP | -   | 수정 시각                              |
| created_by       | BIGINT       | NOT NULL, FK -> Member.id | -                 | -   | 생성자                                |
| updated_by       | BIGINT       | NOT NULL, FK -> Member.id | -                 | -   | 수정자                                |

### 2) PART_COMPATIBILITY

- 테이블 설명: 부품과 호환 가능한 제품 모델의 다대다 관계

| 컬럼명              | 데이터 타입 | 제약조건                          | 기본값 | 인덱스                          | 설명       |
|------------------|--------|-------------------------------|-----|------------------------------|----------|
| part_id          | BIGINT | PK(복합), FK → PART.id          | -   | PK_PART_COMPAT               | 부품 ID    |
| product_model_id | BIGINT | PK(복합), FK → PRODUCT_MODEL.id | -   | PK_PART_COMPAT, IDX_PC_MODEL | 호환 모델 ID |

### 3) REPAIR_PRESET_PART

- 테이블 설명: 프리셋 작업에 필요한 기본 부품

| 컬럼명              | 데이터 타입 | 제약조건                          | 기본값 | 인덱스                          | 설명     |
|------------------|--------|-------------------------------|-----|------------------------------|--------|
| repair_preset_id | BIGINT | PK(복합), FK → REPAIR_PRESET.id | -   | PK_PRESET_PART               | 프리셋 ID |
| part_id          | BIGINT | PK(복합), FK → PART.id          | -   | PK_PRESET_PART, IDX_RPP_PART | 부품 ID  |
| quantity         | INT    | NOT NULL                      | -   | -                            | 기본 수량  |

### 4) REPAIR_PART_USAGE

- 테이블 설명: 실제 수리에서 사용한 부품 및 확정 단가

| 컬럼명            | 데이터 타입   | 제약조건                          | 기본값               | 인덱스 | 설명          |
|----------------|----------|-------------------------------|-------------------|-----|-------------|
| id             | BIGINT   | PK, AUTO_INCREMENT            | -                 | PK  | 부품 사용 ID    |
| repair_work_id | BIGINT   | NOT NULL, FK → REPAIR_WORK.id | -                 | FK  | 수리 항목 ID    |
| part_id        | BIGINT   | NOT NULL, FK → PART.id        | -                 | FK  | 부품 ID       |
| quantity       | INT      | NOT NULL                      | -                 | -   | 사용 수량       |
| unit_price     | INT      | NOT NULL                      | -                 | -   | 당시 적용 단가(원) |
| created_at     | DATETIME | NOT NULL                      | CURRENT_TIMESTAMP | -   | 사용 기록 시각    |

### 5) PART_STOCK_HISTORY

- 테이블 설명: 부품 입고·출고·조정의 불변 이력

| 컬럼명                  | 데이터 타입       | 제약조건                            | 기본값               | 인덱스               | 설명              |
|----------------------|--------------|---------------------------------|-------------------|-------------------|-----------------|
| id                   | BIGINT       | PK, AUTO_INCREMENT              | -                 | PK                | 재고 변동 ID        |
| part_id              | BIGINT       | FK → PART.id                    | -                 | IDX_PSH_PART_TIME | 부품 ID           |
| repair_part_usage_id | BIGINT       | NULL, FK → REPAIR_PART_USAGE.id | -                 | -                 | 수리 출고 연결        |
| movement_type        | VARCHAR(255) | NULL                            | -                 | -                 | IN, OUT, ADJUST |
| quantity_delta       | INT          | NOT NULL                        | -                 | -                 | 증감 수량(+/-)      |
| reason               | VARCHAR(255) | NULL                            | -                 | -                 | 변동 사유           |
| created_at           | DATETIME     | NOT NULL                        | CURRENT_TIMESTAMP | -                 | 재고 변동 시각        |
| created_by           | BIGINT       | NULL                            | -                 | -                 | 처리 직원           |

## 1.2.8 결제 및 입금 관리

### 1) PAYMENT_BILL

- 테이블 설명: A/S에 대한 확정 청구 금액과 최종 수납 상태. 결제 재시도 시 유지

| 컬럼명             | 데이터 타입       | 제약조건                                   | 기본값               | 인덱스 | 설명                     |
|-----------------|--------------|----------------------------------------|-------------------|-----|------------------------|
| id              | BIGINT       | PK, AUTO_INCREMENT                     | -                 | PK  | 청구 ID                  |
| as_reception_id | BIGINT       | NOT NULL, UNIQUE, FK → AS_RECEPTION.id | -                 | FK  | A/S당 청구 1건             |
| amount          | BIGINT       | NOT NULL                               | -                 | -   | 청구 금액(원)               |
| status          | VARCHAR(255) | NOT NULL                               | UNPAID            | -   | UNPAID, PAID, REFUNDED |
| type            | VARCHAR(255) | NOT NULL                               | -                 | -   | REPAIR, INSPECTION     |
| paid_at         | DATETIME     | NULL                                   | -                 | -   | 수납 확정 시각               |
| created_at      | DATETIME     | NOT NULL                               | CURRENT_TIMESTAMP | -   | 생성 시각                  |
| updated_at      | DATETIME     | NOT NULL                               | CURRENT_TIMESTAMP | -   | 수정 시각                  |

### 2) PAYMENT_ATTEMPT

- 테이블 설명: PG 또는 현장 수납의 개별 시도

| 컬럼명             | 데이터 타입       | 제약조건                           | 기본값               | 인덱스 | 설명                                                    |
|-----------------|--------------|--------------------------------|-------------------|-----|-------------------------------------------------------|
| id              | BIGINT       | PK, AUTO_INCREMENT             | -                 | PK  | 결제 시도 ID                                              |
| payment_bill_id | BIGINT       | NOT NULL, FK → PAYMENT_BILL.id | -                 | FK  | 청구 ID                                                 |
| order_id        | VARCHAR(255) | NOT NULL, UNIQUE               | -                 | -   | 서버 생성 주문번호                                            |
| payment_key     | VARCHAR(255) | NULL, UNIQUE                   | -                 | -   | PG 결제 식별자                                             |
| amount          | BIGINT       | NOT NULL                       | -                 | -   | 시도 금액(원)                                              |
| method          | VARCHAR(255) | NULL                           | -                 | -   | CARD, EASY_PAY, VIRTUAL_ACCOUNT, CASH                 |
| status          | VARCHAR(255) | NOT NULL                       | READY             | -   | READY, IN_PROGRESS, WAITING_FOR_DEPOSIT, DONE, FAILED |
| failure_code    | VARCHAR(255) | NULL                           | -                 | -   | 실패 코드                                                 |
| failure_message | VARCHAR(255) | NULL                           | -                 | -   | 실패 사유                                                 |
| approved_at     | DATETIME     | NULL                           | -                 | -   | 승인/수납 확정 시각                                           |
| created_by      | BIGINT       | NULL                           | -                 | -   | 생성자(현장 현금 결제 시)                                       |
| created_at      | DATETIME     | NOT NULL                       | CURRENT_TIMESTAMP | -   | 생성 시각                                                 |
| updated_at      | DATETIME     | NOT NULL                       | CURRENT_TIMESTAMP | -   | 수정 시각                                                 |

### 3) VIRTUAL_ACCOUNT

- 테이블 설명: 온라인 가상계좌 발급 정보

| 컬럼명                | 데이터 타입       | 제약조건                                      | 기본값               | 인덱스 | 설명              |
|--------------------|--------------|-------------------------------------------|-------------------|-----|-----------------|
| id                 | BIGINT       | PK, AUTO_INCREMENT                        | -                 | PK  | 가상계좌 ID         |
| payment_attempt_id | BIGINT       | NOT NULL, UNIQUE, FK → PAYMENT_ATTEMPT.id | -                 | FK  | 발급 시도 ID        |
| bank_code          | VARCHAR(255) | NOT NULL                                  | -                 | -   | 은행 코드           |
| account_number     | VARCHAR(255) | NOT NULL                                  | -                 | -   | 가상계좌 번호         |
| webhook_secret     | VARCHAR(255) | NOT NULL                                  | -                 | -   | 가상계좌 웹훅 검증용 비밀값 |
| due_at             | DATETIME     | NOT NULL                                  | -                 | -   | 입금 기한           |
| created_at         | DATETIME     | NOT NULL                                  | CURRENT_TIMESTAMP | -   | 발급 시각           |
| updated_at         | DATETIME     | NOT NULL                                  | CURRENT_TIMESTAMP | -   | 수정 시각           |

### 4) PAYMENT_REFUND

- 테이블 설명: 결제 완료 건에 대한 환불 요청 및 처리 결과

| 컬럼명                | 데이터 타입       | 제약조건                              | 기본값               | 인덱스 | 설명                        |
|--------------------|--------------|-----------------------------------|-------------------|-----|---------------------------|
| id                 | BIGINT       | PK, AUTO_INCREMENT                | -                 | PK  | 환불 ID                     |
| payment_attempt_id | BIGINT       | NOT NULL, FK → PAYMENT_ATTEMPT.id | -                 | FK  | 환불 대상 결제                  |
| transaction_key    | VARCHAR(255) | NULL, UNIQUE                      | -                 | -   | PG 취소 거래 식별자              |
| cancel_amount      | BIGINT       | NULL                              | -                 | -   | 환불 요청 금액                  |
| cancel_reason      | VARCHAR(255) | NOT NULL                          | -                 | -   | 환불 사유                     |
| status             | VARCHAR(255) | NOT NULL                          | IN_PROGRESS       | -   | IN_PROGRESS, DONE, FAILED |
| failure_code       | VARCHAR(255) | NULL                              | -                 | -   | 실패 코드                     |
| failure_message    | VARCHAR(255) | NULL                              | -                 | -   | 실패 사유                     |
| canceled_at        | DATETIME     | NULL                              | -                 | -   | PG 취소 처리 시각               |
| created_by         | BIGINT       | NOT NULL                          | -                 | -   | 생성자                       |
| created_at         | DATETIME     | NOT NULL                          | CURRENT_TIMESTAMP | -   | 생성 시각                     |
| updated_at         | DATETIME     | NOT NULL                          | CURRENT_TIMESTAMP | -   | 수정 시각                     |

## 1.2.9 배송 및 제품 반환 관리

### 1) SHIPMENT

- 테이블 설명: 택배 배송 또는 방문 수령 정보

| 컬럼명                  | 데이터 타입       | 제약조건                                   | 기본값               | 인덱스                 | 설명                                    |
|----------------------|--------------|----------------------------------------|-------------------|---------------------|---------------------------------------|
| id                   | BIGINT       | PK, AUTO_INCREMENT                     | -                 | PK                  | 반환 ID                                 |
| as_reception_id      | BIGINT       | NOT NULL, UNIQUE, FK → AS_RECEPTION.id | -                 | -                   | A/S 1:1                               |
| return_method        | VARCHAR(255) | NOT NULL                               | -                 | -                   | DELIVERY, PICKUP                      |
| recipient_name       | VARCHAR(255) | NOT NULL                               | -                 | -                   | 수령인                                   |
| recipient_phone      | VARCHAR(255) | NOT NULL                               | -                 | -                   | 수령인 연락처                               |
| address              | VARCHAR(255) | NULL                                   | -                 | -                   | 배송지 기본 주소                             |
| address_detail       | VARCHAR(255) | NULL                                   | -                 | -                   | 배송지 상세 주소                             |
| courier              | VARCHAR(255) | NULL                                   | -                 | -                   | 택배사명                                  |
| courier_code         | VARCHAR(255) | NULL                                   | -                 | -                   | 스마트택배 택배사 코드                          |
| tracking_number      | VARCHAR(255) | NULL                                   | -                 | -                   | 운송장번호                                 |
| status               | VARCHAR(255) | NOT NULL                               | READY             | IDX_SHIPMENT_STATUS | READY, SHIPPING, DELIVERED, PICKED_UP |
| shipped_at           | DATETIME     | NULL                                   | -                 | -                   | 배송 시작 시각                              |
| completed_at         | DATETIME     | NULL                                   | -                 | -                   | 배송 또는 방문수령 완료 시각                      |
| last_checked_at      | DATETIME     | NULL                                   | -                 | -                   | 스마트택배 API 마지막 조회 시각                   |
| last_tracking_status | VARCHAR(255) | NULL                                   | -                 | -                   | 외부 API에서 받은 배송 상태                     |
| created_at           | DATETIME     | NOT NULL                               | CURRENT_TIMESTAMP | -                   | 생성 시각                                 |
| updated_at           | DATETIME     | NOT NULL                               | CURRENT_TIMESTAMP | -                   | 수정 시각                                 |
| created_by           | BIGINT       | NOT NULL, FK -> Member.id              | -                 | -                   | 배송 등록 직원 ID                           |
| updated_by           | BIGINT       | NOT NULL, FK -> Member.id              | -                 | -                   | 마지막 수정 직원 ID                          |

### 2) SHIPMENT_EVENT

- 테이블 설명: 배송 추적 이벤트 이력

| 컬럼명         | 데이터 타입       | 제약조건                       | 기본값               | 인덱스 | 설명                                                             |
|-------------|--------------|----------------------------|-------------------|-----|----------------------------------------------------------------|
| id          | BIGINT       | PK, AUTO_INCREMENT         | -                 | PK  | 이벤트 ID                                                         |
| shipment_id | BIGINT       | NOT NULL, FK → SHIPMENT.id | -                 | -   | 배송 ID                                                          |
| event_key   | VARCHAR(255) | NOT NULL, UNIQUE           | -                 | -   | 동일 이벤트 중복 처리 방지                                                |
| status      | VARCHAR(255) | NOT NULL                   | -                 | -   | REGISTERED, PICKED_UP, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED |
| description | VARCHAR(255) | NULL                       | -                 | -   | 이벤트 설명                                                         |
| occurred_at | DATETIME     | NULL                       | -                 | -   | 이벤트 발생 시각                                                      |
| created_at  | DATETIME     | NOT NULL                   | CURRENT_TIMESTAMP | -   | 이벤트 저장 시각                                                      |
| updated_at  | DATETIME     | NOT NULL                   | CURRENT_TIMESTAMP | -   | 수정 시각                                                          |

## 1.2.10 고객 통합 페이지 및 알림 관리

### 1) CUSTOMER_PORTAL_LINK

- 테이블 설명: 고객의 A/S 단일 통합 페이지 접근 링크 (접수당 1건)
- 직원이 재조회·재전송할 수 있도록 토큰 원문을 저장한다.

| 컬럼명             | 데이터 타입       | 제약조건                                   | 기본값 | 인덱스 | 설명        |
|-----------------|--------------|----------------------------------------|-----|-----|-----------|
| id              | BIGINT       | PK, AUTO_INCREMENT                     | -   | PK  | 링크 ID     |
| as_reception_id | BIGINT       | NOT NULL, UNIQUE, FK → AS_RECEPTION.id | -   | FK  | A/S 접수 ID |
| token           | VARCHAR(255) | NOT NULL, UNIQUE                       | -   | -   | 포털 접근 토큰  |
| expires_at      | DATETIME     | NOT NULL                               | -   | -   | 만료 시각     |

### 2) EMAIL_PRESET

- 테이블 설명: 고객 이메일 알림 본문 프리셋

| 컬럼명           | 데이터 타입       | 제약조건               | 기본값               | 인덱스 | 설명     |
|---------------|--------------|--------------------|-------------------|-----|--------|
| id            | BIGINT       | PK, AUTO_INCREMENT | -                 | PK  | 프리셋 ID |
| name          | VARCHAR(255) | NOT NULL           | -                 | -   | 프리셋 이름 |
| message_title | VARCHAR(255) | NOT NULL           | -                 | -   | 이메일 제목 |
| message       | TEXT         | NOT NULL           | -                 | -   | 이메일 본문 |
| created_at    | DATETIME     | NOT NULL           | CURRENT_TIMESTAMP | -   | 생성 시각  |
| updated_at    | DATETIME     | NOT NULL           | CURRENT_TIMESTAMP | -   | 수정 시각  |

### 3) NOTIFICATION_LOG

- 테이블 설명: 고객 이메일 발송 이력

| 컬럼명               | 데이터 타입       | 제약조건                       | 기본값               | 인덱스 | 설명                                                    |
|-------------------|--------------|----------------------------|-------------------|-----|-------------------------------------------------------|
| id                | BIGINT       | PK, AUTO_INCREMENT         | -                 | PK  | 알림 발송 ID                                              |
| as_reception_id   | BIGINT       | NULL, FK → AS_RECEPTION.id | -                 | -   | 관련 A/S                                                |
| event_key         | VARCHAR(255) | NOT NULL, UNIQUE           | -                 | -   | 업무 이벤트 중복 발송 방지                                       |
| notification_type | VARCHAR(255) | NOT NULL                   | -                 | -   | RECEIVED, ESTIMATE, REPAIR, PAYMENT, SHIPPING, CANCEL |
| recipient_email   | VARCHAR(255) | NOT NULL                   | -                 | -   | 수신 이메일                                                |
| message_title     | VARCHAR(255) | NOT NULL                   | -                 | -   | 이메일 제목                                                |
| message           | TEXT         | NOT NULL                   | -                 | -   | 이메일 본문                                                |
| status            | VARCHAR(255) | NOT NULL                   | PENDING           | -   | PENDING, SENDING, SENT, FAILED                        |
| retry_count       | INT          | NOT NULL                   | 0                 | -   | 재시도 횟수                                                |
| error_message     | VARCHAR(255) | NULL                       | -                 | -   | 오류 메시지                                                |
| sent_at           | DATETIME     | NULL                       | -                 | -   | 발송 시각                                                 |
| created_by        | BIGINT       | NULL, FK → MEMBER.id       | -                 | -   | 알림을 발송한 직원 ID                                         |
| created_at        | DATETIME     | NOT NULL                   | CURRENT_TIMESTAMP | -   | 생성 시각                                                 |
| updated_at        | DATETIME     | NOT NULL                   | CURRENT_TIMESTAMP | -   | 수정 시각                                                 |

## 1.2.11 대시보드 및 운영 통계

### 1) 통계 전용 테이블 없음

- 테이블 설명: 초기 구현은 AS_RECEPTION, REPAIR, PAYMENT_BILL, SHIPMENT, MEMBER, PRODUCT_MODEL을 집계 조회한다.

## 1.2.12 AI 채팅

### 1) CHAT

- 테이블 설명: 직원 AI 어시스턴트 대화 이력

| 컬럼명        | 데이터 타입       | 제약조건               | 기본값               | 인덱스 | 설명                            |
|------------|--------------|--------------------|-------------------|-----|-------------------------------|
| id         | BIGINT       | PK, AUTO_INCREMENT | -                 | PK  | 메시지 ID                        |
| member_id  | VARCHAR(255) | NOT NULL           | -                 | -   | 직원 식별자                        |
| content    | TEXT         | NOT NULL           | -                 | -   | 메시지 내용                        |
| type       | VARCHAR(255) | NOT NULL           | -                 | -   | USER, ASSISTANT, SYSTEM, TOOL |
| created_at | DATETIME     | NOT NULL           | CURRENT_TIMESTAMP | -   | 생성 시각                         |

### 2) SPRING_AI_CHAT_MEMORY

- 테이블 설명: Spring AI `JdbcChatMemoryRepository`용 대화 메모리 테이블

| 컬럼명             | 데이터 타입   | 제약조건             | 기본값 | 인덱스 | 설명                            |
|-----------------|----------|------------------|-----|-----|-------------------------------|
| conversation_id | CHAR(36) | PK(복합), NOT NULL | -   | PK  | 대화 ID                         |
| sequence_id     | BIGINT   | PK(복합), NOT NULL | -   | PK  | 메시지 순번                        |
| content         | TEXT     | NOT NULL         | -   | -   | 메시지 내용                        |
| type            | ENUM     | NOT NULL         | -   | -   | USER, ASSISTANT, SYSTEM, TOOL |
| timestamp       | DATETIME | NOT NULL         | -   | -   | 메시지 시각                        |
