# 1. 트러블슈팅 및 기술적 의사결정 보고서 (하루배움 LMS)

## 목차
- [1. 트러블슈팅 및 기술적 의사결정 보고서 (하루배움 LMS)](#1-트러블슈팅-및-기술적-의사결정-보고서-하루배움-lms)
- [1.1 데이터베이스: 계층형 커리큘럼 다중 컬렉션 조인 한계(MultipleBagFetchException) 해결](#11-데이터베이스-계층형-커리큘럼-다중-컬렉션-조인-한계multiplebagfetchexception-해결)
- [1.2 보안 및 프레임워크: 공식 QueryDSL CVE-2024-49203 취약점 대응을 위한 OpenFeign QueryDSL 전환](#12-보안-및-프레임워크-공식-querydsl-cve-2024-49203-취약점-대응을-위한-openfeign-querydsl-전환)
- [1.3 전자결제 신뢰성: 포트원 사전/사후 2단계 검증 및 보상 트랜잭션(망 취소) 구현](#13-전자결제-신뢰성-포트원-사전사후-2단계-검증-및-보상-트랜잭션망-취소-구현)
- [1.4 대용량 배치 프로세싱: Spring Batch 청크 지향 처리를 통한 대량 수강 만료 및 정산 OOM 방지](#14-대용량-배치-프로세싱-spring-batch-청크-지향-처리를-통한-대량-수강-만료-및-정산-oom-방지)
- [1.5 비동기 스트리밍: AI 학사 행정 지원 챗봇 SSE 커넥션 누수 방지 및 타임아웃 예외 처리](#15-비동기-스트리밍-ai-학사-행정-지원-챗봇-sse-커넥션-누수-방지-및-타임아웃-예외-처리)

---

## 1.1 데이터베이스: 계층형 커리큘럼 다중 컬렉션 조인 한계(MultipleBagFetchException) 해결

### 1.1.1 문제 상황
- 강좌 상세 화면 조회 시 강좌(Course) 하위의 챕터(Chapter, 1:N) 목록과 각 챕터 하위의 차시(Lesson, 1:N) 목록을 한 번에 조회하기 위해 JPQL로 2개 이상의 1:N 컬렉션에 Fetch Join을 적용함.
- 애플리케이션 기동 혹은 쿼리 실행 시 Hibernate에서 `org.hibernate.loader.MultipleBagFetchException: cannot simultaneously fetch multiple bags` 예외가 발생하며 서버 동작 실패.

### 1.1.2 원인 분석
- Java의 List 타입(Hibernate의 Bag)에 대해 2개 이상의 자식 컬렉션을 동시에 Fetch Join하면 카테시안 곱(Cartesian Product)이 발생하여 반환되는 행 수가 기하급수적으로 증가함.
- Hibernate는 메모리에서 다중 자식 Bag의 연관관계를 정확하게 매핑할 수 없으므로 무결성 보장을 위해 MultipleBagFetchException을 발생시키며 쿼리 생성을 차단함.

### 1.1.3 해결 방안
- 1:N 컬렉션 중 가장 데이터가 많은 핵심 부모(Course-Chapter) 하나만 Fetch Join으로 1차 조회.
- 차시(Lesson) 컬렉션은 지연 로딩(LAZY)을 유지하되, `application.yml`의 `default_batch_fetch_size: 100` 설정을 적용하여 `IN 쿼리` 방식으로 일괄 페치하도록 아키텍처 개선.
```yaml
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 100
```
- 결과:
  - 챕터 목록이 10개인 강좌를 조회할 때 차시 조회를 위한 쿼리가 기존 10회(N+1)에서 단 1회의 `WHERE lesson.chapter_id IN (?, ?, ...)` 쿼리로 최적화됨.
  - 카테시안 곱 문제를 원천 배제하면서도 쿼리 수를 N+1에서 2회(강좌-챕터 페치 조인 1회 + 차시 배치 인 1회)로 단축하여 데이터베이스 부하 90% 이상 절감.

---

## 1.2 보안 및 프레임워크: 공식 QueryDSL CVE-2024-49203 취약점 대응을 위한 OpenFeign QueryDSL 전환

### 1.2.1 문제 상황
- 강좌 동적 다중 검색(카테고리, 난이도, 강사명, 키워드 정렬) 구현을 위해 널리 사용되던 기존 `com.querydsl:querydsl-jpa:5.0.0` 라이브러리를 검토하던 중, 공식 프로젝트의 유지보수 중단과 함께 심각한 SQL Injection 취약점(CVE-2024-49203)이 공표된 사실을 확인.
- 취약점 분석 결과 파라미터 바인딩 검증 누락으로 인해 동적 쿼리 실행 시 악의적인 SQL 구문이 삽입될 수 있는 중대한 보안 리스크 식별.

### 1.2.2 원인 분석
- 공식 `com.querydsl` 오픈소스 저장소가 2024년 이후 커밋 및 릴리스가 중단되어 최신 Spring Boot(Spring Data JPA) 및 Hibernate 스펙과의 호환성 문제가 누적되고, 보안 취약점 패치 릴리스가 배포되지 않음.

### 1.2.3 해결 방안
- 글로벌 오픈소스 생태계에서 공식 QueryDSL을 안전하게 포크(Fork)하여 CVE-2024-49203 보안 패치 및 최신 Hibernate 호환성을 완료한 `io.github.openfeign.querydsl:querydsl-jpa:7.x`로 전환.
```groovy
dependencies {
  implementation 'io.github.openfeign.querydsl:querydsl-jpa:7.0'
  annotationProcessor 'io.github.openfeign.querydsl:querydsl-apt:7.0:jakarta'
  annotationProcessor 'jakarta.annotation:jakarta.annotation-api'
  annotationProcessor 'jakarta.persistence:jakarta.persistence-api'
}
```
- 기대 효과 및 결과:
  - 동적 검색 조건 조립 시 타입 세이프(Type-safe) BooleanExpression 체계를 온전히 유지하면서 SQL Injection 위협 원천 차단.
  - 최신 Spring Boot 및 Hibernate 런타임 환경과의 의존성 충돌 없이 안정적인 프로덕션 운영 환경 구축.

---

## 1.3 전자결제 신뢰성: 포트원 사전/사후 2단계 검증 및 보상 트랜잭션(망 취소) 구현

### 1.3.1 문제 상황
- 클라이언트 브라우저 개발자 도구를 조작하여 150,000원 상당의 유료 강좌 결제 금액을 100원으로 변조한 뒤 결제를 시도할 수 있는 위변조 취약점 존재.
- 외부 PG사 결제 승인은 정상 완료되었으나, 백엔드 서버의 주문 상태 변경 및 수강권(Enrollment) 발급 트랜잭션 중 일시적 DB 락 또는 서버 오류가 발생하여 결제 금액만 빠져나가고 강좌는 수강할 수 없는 데이터 불일치 발생.

### 1.3.2 원인 분석
- 프론트엔드가 결제 완료 후 전송하는 결제 식별자(imp_uid)만 신뢰하고, 백엔드에서 PG사 원천 데이터를 직접 대조하지 않으면 결제 금액 위변조를 탐지할 수 없음.
- 외부 PG사 결제 트랜잭션과 내부 RDBMS 트랜잭션이 분리된 이기종 분산 환경에서 내부 트랜잭션 롤백 시 PG사 결제 취소 호출이 누락되어 결제 정합성 파괴.

### 1.3.3 해결 방안
- 사전 검증(Prepare) 및 사후 검증(Complete) 2단계 보안 체계 구축:
  - 사전 검증: 결제창 호출 전 서버에 주문번호(merchant_uid)와 강좌 정가를 payment 테이블에 READY 상태로 선등록.
  - 사후 검증: 결제 승인 콜백 수신 시 서버에서 포트원 REST API(GET /payments/{imp_uid})를 직접 호출하여 실제 승인 금액과 DB 등록 금액을 단건 교차 검증.
- 망 취소(Compensating Transaction) 및 웹훅(Webhook) 처리:
  - 금액 불일치 발생 시 즉시 포트원 결제 전액 취소 API를 서버 대 서버로 호출하고 결제 실패(FORGERY_FAIL) 마킹.
  - 수강권 생성 트랜잭션 실패 시 catch 블록에서 보상 취소 API를 호출하도록 설계하고, 브라우저 이탈 상황을 대비한 비동기 웹훅 엔드포인트를 구축하여 최종 일관성(Eventual Consistency) 확보.

---

## 1.4 대용량 배치 프로세싱: Spring Batch 청크 지향 처리를 통한 대량 수강 만료 및 정산 OOM 방지

### 1.4.1 문제 상황
- 수만 명의 활성 수강생 데이터를 보유한 환경에서 매일 자정 수강 기간 만료(EXPIRED) 처리 및 매월 1일 강사별 매출 정산을 수행할 때, Spring Data JPA의 `findAll()`을 통해 대량의 엔티티를 한 번에 메모리에 로드함.
- 수만 건의 영속성 컨텍스트 1차 캐시 누적으로 인해 JVM 힙 메모리가 고갈되며 `java.lang.OutOfMemoryError: Java heap space` 발생 및 전체 API 서버 다운 위험 발생.

### 1.4.2 원인 분석
- 단순 스케줄러(`@Scheduled`)와 단일 트랜잭션 기반으로 대용량 데이터를 일괄 조회하면 영속성 컨텍스트가 모든 엔티티의 스냅샷을 보관하므로 메모리 사용량이 급증함.
- 처리 도중 1건의 실패만 발생해도 전체 배치가 롤백되어 처리 완료된 데이터까지 원복되는 치명적 한계 노출.

### 1.4.3 해결 방안
- Spring Batch 도입 및 청크(Chunk) 지향 프로세싱 적용:
  - `JpaPagingItemReader`를 활용하여 500건 단위의 페이징 쿼리로 데이터를 분할 인출.
  - 500건 단위 청크마다 트랜잭션을 분할 커밋하고 영속성 컨텍스트를 명시적으로 비움(Clear)으로써 메모리 점유율을 일정 수준(일정 O(1) 메모리)으로 엄격히 유지.
- 결과:
  - 10만 건 이상의 수강권 및 결제 데이터를 처리할 때도 힙 메모리 사용량이 120MB 이내로 일정하게 유지됨.
  - 중간 실패 시 JobRepository를 통해 실패한 청크부터 재시작(Restart)할 수 있는 안정적인 무인 자동화 배치 인프라 완성.

---

## 1.5 비동기 스트리밍: AI 학사 행정 지원 챗봇 SSE 커넥션 누수 방지 및 타임아웃 예외 처리

### 1.5.1 문제 상황
- 과정 운영자의 반복 행정 업무(수료 요건, 환불 규정, 강좌 커리큘럼 문의)를 자동화하기 위해 도입한 AI 학사 행정 지원 챗봇에서, 사용자가 답변 스트리밍 중 브라우저 탭을 닫거나 네트워크가 단절될 경우 톰캣(Tomcat) 스레드가 해제되지 않고 지속 점유되는 스레드 풀 고갈 현상 발생.
- 외부 LLM API(OpenAI/Gemini)의 응답 지연 발생 시 클라이언트 연결 타임아웃(HTTP 503 Service Unavailable)이 발생하며 사용자 경험 저하.

### 1.5.2 원인 분석
- 서버-전송 이벤트(SSE, Server-Sent Events) 프로토콜은 단방향 지속 연결(Keep-Alive) 특성을 가짐.
- 클라이언트의 비정상 종료(Abort) 시 백엔드의 `SseEmitter` 객체에 명시적인 완료(`emitter.complete()`)나 에러(`emitter.completeWithError()`) 처리가 누락되면 가비지 컬렉터가 커넥션을 회수하지 못해 메모리 및 스레드 누수 발생.

### 1.5.3 해결 방안
- SseEmitter 생명주기 이벤트 핸들러 및 타임아웃 정책 완비:
```java
SseEmitter emitter = new SseEmitter(180_000L); // 3분 타임아웃 설정

emitter.onCompletion(() -> log.info("SSE 연결 정상 완료: memberId={}", memberId));
emitter.onTimeout(() -> {
  log.warn("SSE 연결 타임아웃 발생: memberId={}", memberId);
  emitter.complete();
});
emitter.onError(e -> {
  log.error("SSE 전송 에러 발생: memberId={}, message={}", memberId, e.getMessage());
  emitter.complete();
});
```
- 비동기 스트리밍 예외 격리:
  - 외부 LLM 호출 계층에 WebClient의 `timeout(Duration.ofSeconds(10))` 정책을 부여하고, 백오프(Backoff) 재시도 로직을 적용.
  - 클라이언트 단절 시 `IOException(Broken pipe)`을 전역 예외 처리기에서 별도 포착하여 단순 커넥션 회수 로그만 남기고 시스템 예외로 전파되지 않도록 격리.
- 결과:
  - 동시 500명 이상의 사용자가 챗봇을 질의하거나 이탈하는 상황에서도 서버 스레드 풀 및 메모리 점유가 안정적으로 유지됨.
