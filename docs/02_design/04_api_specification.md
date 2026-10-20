# 1. REST API 명세서 및 규격서 (하루배움 LMS)

## 목차
- [1. REST API 명세서 및 규격서 (하루배움 LMS)](#1-rest-api-명세서-및-규격서-하루배움-lms)
- [1.1 공통 응답 규격](#11-공통-응답-규격)
- [1.2 인증 및 회원 관리 API](#12-인증-및-회원-관리-api)
- [1.3 강좌 및 커리큘럼 API](#13-강좌-및-커리큘럼-api)
- [1.4 주문 및 결제 API](#14-주문-및-결제-api)
- [1.5 수강 및 진도율 추적 API](#15-수강-및-진도율-추적-api)
- [1.6 과제 출제 및 평가 API](#16-과제-출제-및-평가-api)
- [1.7 커뮤니티 및 질의응답(Q&A) API](#17-커뮤니티-및-질의응답qa-api)
- [1.8 AI 학사 행정 지원 챗봇 API (SSE)](#18-ai-학사-행정-지원-챗봇-api-sse)
- [1.9 관리자 백오피스 및 정산 API](#19-관리자-백오피스-및-정산-api)

---

## 1.1 공통 응답 규격

### 1.1.1 성공 응답 포맷 (HTTP 200 / 201)
- 별도의 공통 래퍼 봉투 없이 각 도메인 컨트롤러 계층에서 DTO 단건 객체 또는 List 컬렉션, PageResponse 객체를 HTTP Body로 직접 반환.
- 단건 응답 샘플 (CourseDetailResponse):
```json
{
  "id": 1,
  "title": "Spring Boot & JPA 기반 엔터프라이즈 LMS 구축",
  "category": "BACKEND",
  "level": "INTERMEDIATE",
  "price": 150000,
  "accessDays": 90,
  "status": "APPROVED",
  "instructorName": "루카",
  "averageRating": 4.9,
  "thumbnailUrl": "https://harubaeum-s3.s3.ap-northeast-2.amazonaws.com/courses/sample-01.jpg",
  "createdAt": "2026-10-15T10:00:00"
}
```

- 페이징 목록 응답 샘플 (PageResponse<CourseCardResponse>):
```json
{
  "content": [
    {
      "id": 1,
      "title": "Spring Boot & JPA 기반 엔터프라이즈 LMS 구축",
      "category": "BACKEND",
      "level": "INTERMEDIATE",
      "price": 150000,
      "instructorName": "루카",
      "averageRating": 4.9,
      "reviewCount": 128,
      "thumbnailUrl": "https://harubaeum-s3.s3.ap-northeast-2.amazonaws.com/courses/sample-01.jpg"
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 42,
  "totalPages": 5,
  "last": false
}
```

### 1.1.2 실패 및 에러 응답 포맷 (ApiErrorResponse)
- 글로벌 예외 처리기(@RestControllerAdvice)를 통해 일관된 에러 JSON 규격을 반환.
```json
{
  "timestamp": "2026-10-19T08:30:00",
  "status": 400,
  "error": "BAD_REQUEST",
  "code": "PAYMENT_AMOUNT_MISMATCH",
  "message": "실제 결제 금액(130,000원)이 사전 등록된 주문 금액(150,000원)과 일치하지 않아 결제가 자동 취소되었습니다."
}
```

---

## 1.2 인증 및 회원 관리 API

### 1.2.1 자체 회원가입
- 엔드포인트: `POST /api/v1/auth/signup`
- 접근 권한: 전체 (비회원)
- Request Body:
```json
{
  "email": "student01@likelion.net",
  "password": "Password123!",
  "name": "홍길동",
  "nickname": "길동이",
  "role": "ROLE_STUDENT"
}
```
- Response Body (HTTP 201 Created):
```json
{
  "id": 1,
  "email": "student01@likelion.net",
  "name": "홍길동",
  "nickname": "길동이",
  "role": "ROLE_STUDENT",
  "createdAt": "2026-10-19T08:30:00"
}
```

### 1.2.2 JWT 로그인
- 엔드포인트: `POST /api/v1/auth/login`
- 접근 권한: 전체 (비회원)
- Request Body:
```json
{
  "email": "student01@likelion.net",
  "password": "Password123!"
}
```
- Response Body (HTTP 200 OK):
```json
{
  "memberId": 1,
  "email": "student01@likelion.net",
  "nickname": "길동이",
  "role": "ROLE_STUDENT",
  "accessToken": "eyJhbGciOiJIUzI1NiIsIn...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsIn..."
}
```

### 1.2.3 토큰 갱신 (RTR)
- 엔드포인트: `POST /api/v1/auth/refresh`
- 접근 권한: 전체 (회원)
- Request Body:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsIn..."
}
```
- Response Body (HTTP 200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsIn...newAccessToken",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsIn...newRefreshToken"
}
```

### 1.2.4 내 계정 프로필 조회 및 수정
- 조회 엔드포인트: `GET /api/v1/members/me` (권한: 로그인 회원)
- 수정 엔드포인트: `PATCH /api/v1/members/me` (권한: 로그인 회원)
- Request Body:
```json
{
  "nickname": "열공개발자",
  "profileImageUrl": "https://harubaeum-s3.s3.ap-northeast-2.amazonaws.com/profiles/profile-01.jpg",
  "bio": "매일 성장하는 백엔드 개발자 지망생입니다."
}
```
- Response Body (HTTP 200 OK):
```json
{
  "id": 1,
  "email": "student01@likelion.net",
  "name": "홍길동",
  "nickname": "열공개발자",
  "role": "ROLE_STUDENT",
  "profileImageUrl": "https://harubaeum-s3.s3.ap-northeast-2.amazonaws.com/profiles/profile-01.jpg",
  "bio": "매일 성장하는 백엔드 개발자 지망생입니다."
}
```

---

## 1.3 강좌 및 커리큘럼 API

### 1.3.1 강좌 다중 조건 검색 (OpenFeign QueryDSL 동적 쿼리)
- 엔드포인트: `GET /api/v1/courses`
- 접근 권한: 전체 공개
- Query Parameters:
  - `category`: BACKEND, FRONTEND, INFRA
  - `level`: BEGINNER, INTERMEDIATE, ADVANCED
  - `keyword`: 검색 키워드
  - `page`: 페이지 번호 (0부터 시작)
  - `size`: 페이지당 크기 (기본값: 12)
  - `sort`: LATEST, RATING, POPULAR
- Response Body (HTTP 200 OK):
```json
{
  "content": [
    {
      "id": 1,
      "title": "Spring Boot 4.1.1 & JPA 기반 엔터프라이즈 LMS 구축",
      "category": "BACKEND",
      "level": "INTERMEDIATE",
      "price": 150000,
      "instructorName": "루카",
      "averageRating": 4.9,
      "thumbnailUrl": "https://harubaeum-s3.s3.ap-northeast-2.amazonaws.com/courses/sample-01.jpg"
    }
  ],
  "pageNumber": 0,
  "pageSize": 12,
  "totalElements": 25,
  "totalPages": 3,
  "last": false
}
```

### 1.3.2 강좌 상세 정보 및 커리큘럼 조회
- 엔드포인트: `GET /api/v1/courses/{courseId}`
- 접근 권한: 전체 공개
- Response Body (HTTP 200 OK):
```json
{
  "id": 1,
  "title": "Spring Boot 4.1.1 & JPA 기반 엔터프라이즈 LMS 구축",
  "description": "실무 대규모 관리를 위한 엔터프라이즈 학습 플랫폼 개발 과정입니다.",
  "category": "BACKEND",
  "level": "INTERMEDIATE",
  "price": 150000,
  "accessDays": 90,
  "instructor": {
    "id": 2,
    "name": "루카",
    "bio": "10년차 백엔드 시니어 아키텍트"
  },
  "chapters": [
    {
      "id": 10,
      "sequence": 1,
      "title": "모던 자바 25와 Spring Core",
      "lessons": [
        {
          "id": 101,
          "sequence": 1,
          "title": "JVM 메모리 구조 및 가상 스레드",
          "durationSeconds": 1800,
          "isFreePreview": "Y"
        },
        {
          "id": 102,
          "sequence": 2,
          "title": "Spring IoC와 의존성 주입",
          "durationSeconds": 2400,
          "isFreePreview": "N"
        }
      ]
    }
  ]
}
```

### 1.3.3 강좌 신규 개설 (강사 전용)
- 엔드포인트: `POST /api/v1/courses`
- 접근 권한: 강사 (`ROLE_INSTRUCTOR`)
- Request Body:
```json
{
  "title": "OpenFeign QueryDSL 7.x 실전 동적 쿼리",
  "description": "보안 패치된 최신 QueryDSL을 활용한 고성능 동적 검색 구현.",
  "category": "BACKEND",
  "level": "ADVANCED",
  "price": 120000,
  "accessDays": 60,
  "thumbnailUrl": "https://harubaeum-s3.s3.ap-northeast-2.amazonaws.com/courses/sample-02.jpg"
}
```
- Response Body (HTTP 201 Created):
```json
{
  "id": 2,
  "title": "OpenFeign QueryDSL 7.x 실전 동적 쿼리",
  "status": "PENDING",
  "createdAt": "2026-10-19T08:30:00"
}
```

---

## 1.4 주문 및 결제 API

### 1.4.1 결제 사전 검증 (결제 예정 정보 등록)
- 엔드포인트: `POST /api/v1/payments/prepare`
- 접근 권한: 로그인 회원 (`ROLE_STUDENT`)
- Request Body:
```json
{
  "courseIds": [1],
  "totalAmount": 150000
}
```
- Response Body (HTTP 200 OK):
```json
{
  "merchantUid": "ORD_20261019_1001",
  "totalAmount": 150000,
  "orderStatus": "READY"
}
```

### 1.4.2 결제 사후 검증 및 수강권 발급 완료
- 엔드포인트: `POST /api/v1/payments/complete`
- 접근 권한: 로그인 회원 (`ROLE_STUDENT`)
- Request Body:
```json
{
  "merchantUid": "ORD_20261019_1001",
  "impUid": "imp_9876543210"
}
```
- Response Body (HTTP 200 OK):
```json
{
  "orderId": 101,
  "merchantUid": "ORD_20261019_1001",
  "paidAmount": 150000,
  "paymentStatus": "PAID",
  "enrollmentIds": [501],
  "message": "결제 금액 검증이 완료되었으며, 수강권이 정상 발급되었습니다."
}
```

### 1.4.3 결제 비동기 웹훅 수신
- 엔드포인트: `POST /api/v1/payments/webhook`
- 접근 권한: 전체 공개 (포트원 서버 IP 허용)
- Request Body:
```json
{
  "imp_uid": "imp_9876543210",
  "merchant_uid": "ORD_20261019_1001",
  "status": "paid"
}
```
- Response Body (HTTP 200 OK):
```json
{
  "result": "SUCCESS"
}
```

### 1.4.4 주문 및 결제 내역 목록 조회
- 엔드포인트: `GET /api/v1/orders/me`
- 접근 권한: 로그인 회원 (`ROLE_STUDENT`)
- Response Body (HTTP 200 OK):
```json
[
  {
    "orderId": 101,
    "merchantUid": "ORD_20261019_1001",
    "courseTitle": "Spring Boot 4.1.1 & JPA 기반 엔터프라이즈 LMS 구축",
    "paidAmount": 150000,
    "payMethod": "CARD",
    "paidAt": "2026-10-19T08:35:00",
    "status": "PAID"
  }
]
```

---

## 1.5 수강 및 진도율 추적 API

### 1.5.1 나의 수강 현황 대시보드 목록 조회
- 엔드포인트: `GET /api/v1/enrollments/me`
- 접근 권한: 로그인 회원 (`ROLE_STUDENT`)
- Response Body (HTTP 200 OK):
```json
[
  {
    "enrollmentId": 501,
    "courseId": 1,
    "courseTitle": "Spring Boot 4.1.1 & JPA 기반 엔터프라이즈 LMS 구축",
    "thumbnailUrl": "https://harubaeum-s3.s3.ap-northeast-2.amazonaws.com/courses/sample-01.jpg",
    "progressPercent": 75.0,
    "completedLessons": 15,
    "totalLessons": 20,
    "lastLessonId": 102,
    "lastLessonTitle": "Spring IoC와 의존성 주입",
    "endAt": "2026-11-30T23:59:59",
    "status": "ACTIVE"
  }
]
```

### 1.5.2 차시 동영상 진도율 저장 (5초 인터벌 동기화)
- 엔드포인트: `POST /api/v1/lessons/{lessonId}/progress`
- 접근 권한: 수강 완료 회원 (`ROLE_STUDENT`)
- Request Body:
```json
{
  "currentSecond": 1450,
  "durationSecond": 1800
}
```
- Response Body (HTTP 200 OK):
```json
{
  "lessonId": 101,
  "currentSecond": 1450,
  "isCompleted": "Y",
  "courseProgressPercent": 80.0,
  "message": "80% 이상 수강하여 차시 수강 완료 처리되었습니다."
}
```

---

## 1.6 과제 출제 및 평가 API

### 1.6.1 과제 제출 (수강생)
- 엔드포인트: `POST /api/v1/assignments/{assignmentId}/submissions`
- 접근 권한: 수강 중인 회원 (`ROLE_STUDENT`)
- Request Body:
```json
{
  "submissionContent": "요청하신 JPA Fetch Join 및 QueryDSL 동적 쿼리 실습 코드를 작성하여 첨부합니다.",
  "fileUrl": "https://harubaeum-s3.s3.ap-northeast-2.amazonaws.com/assignments/homework-01.zip"
}
```
- Response Body (HTTP 201 Created):
```json
{
  "submissionId": 301,
  "assignmentId": 11,
  "status": "PENDING",
  "submittedAt": "2026-10-19T08:40:00"
}
```

### 1.6.2 과제 평가 및 채점 (강사 전용)
- 엔드포인트: `POST /api/v1/submissions/{submissionId}/grade`
- 접근 권한: 담당 강사 (`ROLE_INSTRUCTOR`)
- Request Body:
```json
{
  "score": 95,
  "status": "PASSED",
  "feedback": "MultipleBagFetchException 예외를 BatchSize 설정으로 깔끔하게 회피하셨습니다. 모범 코드입니다."
}
```
- Response Body (HTTP 200 OK):
```json
{
  "submissionId": 301,
  "score": 95,
  "status": "PASSED",
  "gradedAt": "2026-10-19T09:00:00"
}
```

---

## 1.7 커뮤니티 및 질의응답(Q&A) API

### 1.7.1 차시 타임스탬프 질문 등록
- 엔드포인트: `POST /api/v1/lessons/{lessonId}/qna`
- 접근 권한: 수강 회원 (`ROLE_STUDENT`)
- Request Body:
```json
{
  "title": "Fetch Join 페이징 처리 시 out-of-memory 경고 문의",
  "content": "일대다 조인에서 fetch join을 쓰면 메모리에서 페이징을 처리한다는 경고가 발생하는데 BatchSize가 유일한 해결책인가요?",
  "timestampSecond": 860
}
```
- Response Body (HTTP 201 Created):
```json
{
  "questionId": 401,
  "title": "Fetch Join 페이징 처리 시 out-of-memory 경고 문의",
  "timestampSecond": 860,
  "isResolved": "N",
  "createdAt": "2026-10-19T08:45:00"
}
```

---

## 1.8 AI 학사 행정 지원 챗봇 API (SSE)

### 1.8.1 학사 행정 실시간 스트리밍 질의응답
- 엔드포인트: `GET /api/v1/ai/admin-chat/stream`
- 응답 프로토콜: Server-Sent Events (`text/event-stream`)
- 접근 권한: 전체 공개 (비회원 및 회원)
- Query Parameters:
  - `prompt`: 사용자의 학사/행정 문의 질문 (URL 인코딩)
- 스트리밍 데이터 청크 포맷:
```text
event: message
data: {"text": "결제 "}

event: message
data: {"text": "영수증은 "}

event: message
data: {"text": "[나의학습 > 주문/결제 내역] 메뉴에서 즉시 출력 가능합니다."}

event: complete
data: {"referenceLink": "/my/orders", "status": "DONE"}
```

---

## 1.9 관리자 백오피스 및 정산 API

### 1.9.1 관리자 핵심 KPI 지표 조회
- 엔드포인트: `GET /api/v1/admin/dashboard`
- 접근 권한: 최고 관리자 (`ROLE_ADMIN`)
- Response Body (HTTP 200 OK):
```json
{
  "todaySales": 4850000,
  "dailyActiveUsers": 1240,
  "pendingCourseCount": 3,
  "pendingSettlementCount": 2
}
```

### 1.9.2 강사 월간 정산 집계 조회 (Spring Batch 연계)
- 엔드포인트: `GET /api/v1/admin/settlements`
- 접근 권한: 최고 관리자 (`ROLE_ADMIN`)
- Query Parameters:
  - `month`: 대상 연월 (예: '2026-09')
- Response Body (HTTP 200 OK):
```json
[
  {
    "settlementId": 701,
    "instructorName": "루카",
    "settlementMonth": "2026-09",
    "totalSales": 45000000,
    "platformFee": 9000000,
    "netSettlementAmount": 36000000,
    "status": "PAID",
    "executedAt": "2026-10-01T00:05:00"
  }
]
```
