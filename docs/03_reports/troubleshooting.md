# FixGo 트러블슈팅 및 기술 문제 해결 기록

개발·배포 과정에서 만난 런타임 오류, 도메인 상태 정합성, 외부 연동, 시드 데이터, 권한 처리 이슈의 원인과 해결을 남긴다.

## 목차

- [TS-BYEONGHO-01: Spring AI 채팅 메모리 테이블명 대소문자 불일치 (bad SQL grammar)](#ts-byeongho-01-spring-ai-채팅-메모리-테이블명-대소문자-불일치-bad-sql-grammar)
- [TS-JIWON-01: 배송 등록 대상 조회와 Shipment 상태 불일치로 중복 등록 예외](#ts-jiwon-01-배송-등록-대상-조회와-shipment-상태-불일치로-중복-등록-예외)
- [TS-SUHWAN-01: 공임비 기준 데이터 누락으로 인한 수리 비용 계산 오류](#ts-suhwan-01-공임비-기준-데이터-누락으로-인한-수리-비용-계산-오류)
- [TS-WONIL-01: 토스페이먼츠 API·웹훅 중복 반영 및 불필요 재전송](#ts-wonil-01-토스페이먼츠-api웹훅-중복-반영-및-불필요-재전송)
- [TS-NAMGYU-01: 제품 모델 수정 시 자기 자신을 중복으로 판정](#ts-namgyu-01-제품-모델-수정-시-자기-자신을-중복으로-판정)
- [TS-SEUNGHYEOK-01: 상태 이력 누락으로 평균 수리 기간 미집계](#ts-seunghyeok-01-상태-이력-누락으로-평균-수리-기간-미집계)
- [TS-HYERAN-01: 권한 체크 체이닝으로 인한 NPE 위험](#ts-hyeran-01-권한-체크-체이닝으로-인한-npe-위험)

---

## TS-BYEONGHO-01: Spring AI 채팅 메모리 테이블명 대소문자 불일치 (bad SQL grammar)

### 1) 발생 현상 및 에러

- AI 챗봇에서 「답변을 받지 못했습니다. (500)」 응답이 발생함.
- 로그에 채팅 히스토리 조회 중 `bad SQL grammar` 예외가 기록됨.
- 동일 코드는 로컬에서는 정상 동작했으나, 운영 RDS 환경에서만 실패함.

### 2) 원인 분석

- Hibernate의 물리 네이밍 전략은 따옴표로 감싸지 않은 식별자를 소문자로 변환함.
- Spring AI `JdbcChatMemoryRepository`는 SQL에서 대문자 테이블명 `SPRING_AI_CHAT_MEMORY`를 직접 사용함.
- 운영 RDS(MySQL)는 테이블명 대소문자를 구분함. `SHOW TABLES` 결과에는 소문자 `spring_ai_chat_memory`만 존재해 대문자 조회가 실패함.

### 3) 해결 방법

- 엔티티 `@Table` 이름을 백틱으로 감싸 대문자 테이블이 생성·조회되도록 맞춤.

```java
@Entity
@Table(name = "`SPRING_AI_CHAT_MEMORY`")
public class SpringAiChatMemory {
    // ...
}
```

### 4) 시사점 및 배운 점

- ORM 네이밍 전략과 외부 라이브러리가 가정하는 테이블명이 다를 수 있다.
- 로컬(대소문자 비구분)과 RDS(대소문자 구분) 차이를 배포 전에 반드시 검증해야 한다.

---

## TS-JIWON-01: 배송 등록 대상 조회와 Shipment 상태 불일치로 중복 등록 예외

### 1) 발생 현상 및 에러

- 배송 관리 목록에서는 `Shipment.status=SHIPPING`(배송중)으로 보이는데, 배송 등록 검색은 `AsReception.status=AWAITING_SHIPMENT`만 보고 대상을 조회함.
- 이미 배송이 등록된 A/S 건이 등록 대상에 다시 포함되고, 재등록 시 중복 등록 예외가 발생함.

### 2) 원인 분석

- A/S 접수 상태와 배송 상태가 서로 다른 도메인에서 관리됨.
- 등록 대상 조회가 접수 상태만 확인하고, 이미 `Shipment` 행이 존재하는지 보지 않음.

### 3) 해결 방법

- 등록·목록 조회 시 `Shipment` 존재 여부를 함께 확인하도록 변경함.
- 등록 API에서는 `existsByAsReceptionId`로 중복을 차단함.
- 배송 관리 목록 쿼리는 접수 상태와 배송 상태를 함께 조인해 필터링함.

```java
private void validateDuplicate(Long asReceptionId) {
    if (shipmentRepository.existsByAsReceptionId(asReceptionId)) {
        throw new IllegalStateException("이미 배송 또는 수령 내역이 등록되어 있습니다.");
    }
}
```

### 4) 시사점 및 배운 점

- 도메인 상태가 분리되어 있으면, 화면/조회 조건도 한쪽 상태만 보지 말고 연관 엔티티 존재 여부까지 맞춰야 한다.

---

## TS-SUHWAN-01: 공임비 기준 데이터 누락으로 인한 수리 비용 계산 오류

### 1) 발생 현상 및 에러

- 수리 비용 계산 과정에서 제품군별 공임비 기준 데이터가 없어 정확한 가격 산정이 어려웠음.

### 2) 원인 분석

- 공임비 계산 로직이 `labor_rate` 테이블을 조회하는데, 필요한 기준 데이터가 등록되어 있지 않아 단가를 가져오지 못함.

### 3) 해결 방법

- 제품군별 공임비·점검비 기준 데이터를 SQL로 `labor_rate`에 등록함.
- 이후 `data.sql`에 시드 데이터로 포함해 환경마다 초기 데이터가 올라가도록 함.

### 4) 시사점 및 배운 점

- 비즈니스 로직이 참조하는 마스터/기준 데이터는 초기 데이터(seed)로 명확히 관리해야 한다.

---

## TS-WONIL-01: 토스페이먼츠 API·웹훅 중복 반영 및 불필요 재전송

### 1) 발생 현상 및 에러

- 토스페이먼츠 API 응답과 웹훅을 통해 동일한 결제 상태가 중복 반영될 가능성이 있음.
- 이 상태에서 웹훅에 정상(OK) 응답을 주지 않으면 불필요한 재전송이 발생함.

### 2) 원인 분석

- 승인 API 콜백과 비동기 웹훅이 같은 결제 건을 각각 처리할 수 있음.
- 이미 처리된 건을 실패로 응답하면 PG가 재전송을 반복함.

### 3) 해결 방법

- 결제 시도 상태를 확인해 이미 처리된 요청은 상태를 다시 바꾸지 않도록 멱등 처리함.
- 웹훅 엔드포인트는 처리 성공 시 HTTP 200을 반환해 재전송을 막음.

```java
private void applyDeposit(PaymentAttempt attempt, PaymentBill bill, LocalDateTime depositedAt) {
    switch (attempt.getStatus()) {
        case DONE -> { /* 이미 반영된 입금 — 무시 */ }
        case FAILED -> log.error("만료/취소된 결제에 입금 웹훅. 확인 필요. orderId={}", attempt.getOrderId());
        default -> {
            attempt.markVirtualAccountDone(depositedAt);
            bill.markPaid(depositedAt);
        }
    }
}
```

```java
@PostMapping("/webhook")
public ResponseEntity<Void> handleTossWebhook(@RequestBody TossDto.DepositCallbackWebhook request) {
    paymentWebhookService.handleDepositCallback(request);
    return ResponseEntity.ok().build();
}
```

### 4) 시사점 및 배운 점

- 외부 시스템 연동 시 정상 요청뿐 아니라 중복 요청과 처리 순서까지 고려해야 한다.

---

## TS-NAMGYU-01: 제품 모델 수정 시 자기 자신을 중복으로 판정

### 1) 발생 현상 및 에러

- 제품 모델 수정 시 등록(create)용 중복 검사 메서드를 그대로 재사용해, 자기 자신을 중복으로 보고 수정이 실패함.

### 2) 원인 분석

- 등록 검사는 전체 데이터에서 이름/모델명 존재 여부만 확인함.
- 수정 시에는 현재 레코드의 기존 값도 “이미 존재”로 잡힘.

### 3) 해결 방법

- 수정 전용 중복 검사로 현재 ID를 제외하도록 변경함.

```java
// 등록
if (productModelRepository.existsByName(request.name())) { ... }
if (productModelRepository.existsByModelName(request.modelName())) { ... }

// 수정 — 자기 자신 제외
if (productModelRepository.existsByNameAndIdNot(request.name(), id)) { ... }
if (productModelRepository.existsByModelNameAndIdNot(request.modelName(), id)) { ... }
```

### 4) 시사점 및 배운 점

- 등록은 전체 중복 검사, 수정은 현재 레코드를 제외한 중복 검사가 필요하다.

---

## TS-SEUNGHYEOK-01: 상태 이력 누락으로 평균 수리 기간 미집계

### 1) 발생 현상 및 에러

- 통계 화면에서 평균 수리 기간이 집계되지 않음.

### 2) 원인 분석

- 평균 수리 기간은 완료 시각 컬럼이 아니라 `as_status_history`로 계산함.
- 수리 시작은 `to_status = REPAIRING`, 완료는 `from_status = REPAIRING` → `to_status = COMPLETED` 행이 필요함.
- 수리 상태만 `COMPLETED`로 넣고 접수 상태 이력만 남겨, 수리 시작·완료 이력 행이 없어 기간이 계산되지 않음.

### 3) 해결 방법

- 수리가 완료된 건을 대상으로, `created_at`을 수리 시작·`confirmed_at`을 수리 완료 시각으로 삼아 상태 이력 insert문을 작성해 누락분을 보정함.
- 이후 통계 쿼리는 아래와 같이 시작·완료 이력을 조인해 평균을 구함.

```sql
-- 개념: REPAIRING 시작 이력과 REPAIRING → COMPLETED 완료 이력의 시간 차로 평균 계산
JOIN as_status_history start_history
  ON start_history.to_status = 'REPAIRING'
WHERE complete_history.from_status = 'REPAIRING'
  AND complete_history.to_status = 'COMPLETED'
```

### 4) 시사점 및 배운 점

- 상태값만 바꾸지 말고, 통계·감사가 의존하는 상태 이력도 함께 남겨야 한다.
- 시드/마이그레이션 데이터도 실제 전이 경로와 동일한 이력을 채워야 집계가 깨지지 않는다.

---

## TS-HYERAN-01: 권한 체크 체이닝으로 인한 NPE 위험

### 1) 발생 현상 및 에러

- Service 계층에서 `userDetails.getMember().getRole()`처럼 수동 체이닝으로 권한을 확인할 때 NullPointerException 위험이 있었음.

### 2) 원인 분석

- 인증 객체·멤버·역할이 어느 한 단계라도 null이면 체이닝이 바로 NPE로 이어짐.
- 비즈니스 로직 안에서 권한을 직접 분기하면 누락·실수 가능성도 커짐.

### 3) 해결 방법

- `@PreAuthorize` 기반 선언적 권한 처리로 전환해, 메서드 진입 전에 역할을 검증하도록 변경함.

```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/...")
public ResponseEntity<?> adminOnlyApi() {
    // ...
}

@PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
@PostMapping
public ResponseEntity<?> createReception(...) {
    // ...
}
```

### 4) 시사점 및 배운 점

- 권한은 서비스 내부 체이닝보다 Spring Security의 선언적 인가로 처리하는 편이 안전하고 일관적이다.
