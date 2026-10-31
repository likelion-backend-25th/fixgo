# 1. REST API 명세서 및 규격서 (FixGo MS)

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
  "thumbnailUrl": "<https://harubaeum-s3.s3.ap-northeast-2.amazonaws.com/courses/sample-01.jpg>",
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
      "thumbnailUrl": "<https://harubaeum-s3.s3.ap-northeast-2.amazonaws.com/courses/sample-01.jpg>"
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

- 엔드포인트:
- 접근 권한:
- Request Body:

```json
{
}
```

- Response Body (HTTP 201 Created):

```json
{
}
```

### 1.2.2 JWT 로그인

- 엔드포인트:
- 접근 권한
- Request Body:

```json
{
}
```

- Response Body (HTTP 200 OK):

```json
{
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

- 조회 엔드포인트:
- 수정 엔드포인트:
- Request Body:

```json
{
}
```

- Response Body (HTTP 200 OK):

```json
{
}
```

---

# A/S 접수 관리 API

## A/S 목록

- 엔드포인트: `POST /api/v1/as-requests`
- 접근 권한: 관리자, 접수 담당자
- 등록된 A/S 요청 목록을 조회하고 검색 조건, 처리 상태, 담당 기사에 따라 필터링한다.

Response Body (HTTP 200 OK)

```json
{
  "content": [
    {
      "id": 1,
      "requestNumber": "AS-2026-1101",
      "customerId": 1,
      "customerName": "김하늘",
      "customerPhone": "010-1234-5678",
      "productId": 3,
      "modelName": "SZ-P24",
      "status": "RECEIVED",
      "technicianId": 5,
      "technicianName": "김기사",
      "createdAt": "2026-10-29T09:30:00"
    },
    {
      "id": 2,
      "requestNumber": "AS-2026-1102",
      "customerId": 2,
      "customerName": "홍길동",
      "customerPhone": "010-2222-3333",
      "productId": 4,
      "modelName": "SZ-P24",
      "status": "IN_PROGRESS",
      "technicianId": 8,
      "technicianName": "정기사",
      "createdAt": "2026-10-29T10:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 2,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

## A/S 접수

엔드포인트:

- 엔드포인트: `POST /api/v1/as-requests`
- 접근 권한: 관리자, 접수 담당자
- 고객, 제품, 고장 증상, 담당 기사 및 수령 방식 정보를 입력하여 A/S 접수를 등록한다.

Request Body

```json
{
  "customerId": 1,
  "productId": 3,
  "technicianId": 5,
  "symptom": "전원 불량",
  "symptomDetail": "전원 버튼을 눌러도 화면이 켜지지 않음",
  "requestMemo": "수리 완료 후 고객에게 연락 요청",
  "returnMethod": "DELIVERY"
}
```

Response Body (HTTP 201 Created)

```json
{
  "id": 1,
  "requestNumber": "AS-2026-0001",
  "customerId": 1,
  "productId": 3,
  "receptionistId": 2,
  "technicianId": 5,
  "symptom": "전원 불량",
  "symptomDetail": "전원 버튼을 눌러도 화면이 켜지지 않음",
  "requestMemo": "수리 완료 후 고객에게 연락 요청",
  "returnMethod": "DELIVERY",
  "status": "RECEIVED",
  "createdAt": "2026-10-29T09:30:00",
  "message": "A/S 접수가 완료되었습니다."
}
```

## A/S 접수 상세

- 엔드포인트: `GET /api/v1/as-requests/{requestId}`
- 접근 권한: 관리자, 접수 담당자
- 선택한 A/S 접수 건의 고객 정보, 제품 정보, 고장 내용, 담당 기사 및 처리 상태를 조회한다.

Response Body (HTTP 200 OK)

```json
{
  "id": 1,
  "requestNumber": "AS-2026-1112",
  "customer": {
    "id": 1,
    "name": "한소희",
    "phone": "010-5555-1212",
    "email": "user@gmail.com",
    "postalCode": "12345",
    "address": "서울특별시 강남구 테헤란로 123",
    "addressDetail": "101동 1001호"
  },
  "product": {
    "id": 3,
    "serialNumber": "SZP24-22018-51542",
    "productCategory": "스마트폰",
    "modelName": "SZP24",
    "purchaseDate": "2025-05-14"
  },
  "receptionistId": 2,
  "technician": {
    "id": 5,
    "name": "김기사"
  },
  "symptom": "스피커 불량",
  "symptomDetail": "스피커가 지지직거리고 소리가 잘 안 들립니다.",
  "requestMemo": "-",
  "returnMethod": "PICKUP",
  "status": "RECEIVED",
  "statusChangedAt": "2026-11-10T09:30:00",
  "createdAt": "2026-11-10T09:30:00",
  "updatedAt": "2026-11-10T09:30:00"
}
```

## 고객 관리 - 고객 조회

- 엔드포인트: `GET /api/v1/customers`
- 접근 권한: 관리자, 접수 담당자
- 등록된 고객 목록을 조회하고 고객명 또는 연락처를 기준으로 검색한다.

Response Body (HTTP 200 OK)

```json
{
  "content": [
    {
      "id": 1,
      "name": "김민수",
      "phone": "010-2222-1818",
      "email": "minsu@example.com",
      "createdAt": "2026-10-28T09:30:00"
    },
    {
      "id": 2,
      "name": "한소희",
      "phone": "010-5555-1212",
      "email": "sohee@example.com",
      "createdAt": "2026-10-28T10:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 2,
  "totalPages": 1
}
```

## 고객 관리 - 고객 상세

- 엔드포인트: `GET /api/v1/customers/{customerId}`
- 접근 권한: 관리자, 접수 담당자
- 고객 목록에서 특정 고객을 선택하면 등록된 고객 상세 정보를 조회한다.

Response Body (HTTP 200 OK)

```json
{
  "id": 1,
  "name": "김민수",
  "phone": "010-2222-1818",
  "email": "minsu@example.com",
  "postalCode": "12345",
  "address": "서울특별시 강남구 테헤란로 123",
  "addressDetail": "101동 1001호",
  "createdAt": "2026-10-28T09:30:00",
  "updatedAt": "2026-10-28T09:30:00"
}
```

---

# 1.3 배송 관리 API

## 1.3.1 배송 목록 및 통계 조회

- 엔드포인트: `GET /api/v1/shipments`
- 접근 권한: 관리자, 접수 담당자
- Response Body (HTTP 200 OK)

```json
{
  "statistics": {
    "deliveryWaiting": 2,
    "deliveryStarted": 1,
    "deliveryCompleted": 4,
    "visitPickup": 1
  },
  "totalCount": 8
  "content": [
    {
      "id": 1,
      "receiptNumber": "AS-20261028-001",
      "returnMethod": "DELIVERY",
      "recipientName": "김수민",
      "recipientPhone": "010-1234-5678",
      "courier": "CJ대한통운",
      "trackingNumber": "123456789012",
      "status": "DELIVERY",
      "shippedAt": "2026-10-28T14:10:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

## 1.3.2 배송 상세 조회

- 엔드포인트: `GET /api/v1/shipments/{shipmentId}`
- 접근 권한: 관리자, 접수 담당자
- 배송 목록에서 상세보기 버튼 클릭 시 배송 정보와 배송 이벤트 이력을 조회한다.

Response Body (HTTP 200 OK)

```json
{
  "id": 1,
  "receiptNumber": "AS-20261028-001",
  "returnMethod": "DELIVERY",
  "recipientName": "김수민",
  "recipientPhone": "010-1234-5678",
  "courier": "CJ대한통운",
  "trackingNumber": "123456789012",
  "status": "SHIPPING",
  "zipcode": "06234"
  "address": "서울시 강남구 테헤란로 152",
  "addressDetail": "사자빌딩 3층"
  "shippedAt": "2026-10-28T14:10:00"
  "completedAt": null,
  "events": [
    {
      "id": 2,
      "eventType": "DELIVERD",
      "description": "집화완료",
      "processStatus": "PROCESSED",
      "occurredAt": "2026-10-25T16:40:00"
    },
    {
      "id": 1,
      "eventType": "TRACKING_REGISTERED",
      "description": "운송장 등록",
      "processStatus": "PROCESSED",
      "occurredAt": "2026-10-25T14:10:00"
    }
  ]
}
```

## 1.3.3 배송 등록

- 엔드포인트: `POST /api/v1/shipments/registration`
- 접근 권한: 관리자, 접수 담당자

Request Body

```json
{
  "asRequestId": 1062,
  "courier": "CJ대한통운",
  "courierCode": "SMARTTRACKER_COURIER_CODE",
  "trackingNumber": "5100-2219"
}
```

Response Body

```json
{
  "id": 1,
  "requestNumber": "AS-2026-1062",
  "recipientName": "정하늘",
  "courier": "CJ대한통운",
  "trackingNumber": "123456789012",
  "status": "READY",
  "createdAt": "2026-10-29T09:30:00"
}
```

## 1.3.4 배송 시작 알림 발송

- 엔드포인트: `POST /api/v1/shipments/{shipmentId}/start`
- 접근 권한: 관리자, 접수 담당자

Response Body (HTTP 200 OK)

```json
{
  "id": 1,
  "status": "SHIPPING",
  "shippedAt": "2026-10-29T09:35:00",
  "message": "배송 시작 처리되었습니다."
}
```

## 1.3.5 운송장 수정

- 엔드포인트: `PATCH /api/v1/shipments/tracking`
- 접근 권한: 관리자, 접수 담당자

Request Body

```json
{
  "courier": "한진택배",
  "courierCode": "SMARTTRACKER_COURIER_CODE",
  "trackingNumber": "123456789012"
}
```

Response Body (HTTP 200 OK)

```json
{
  "id": 1,
  "courier": "한진택배",
  "trackingNumber": "123456789012",
  "updatedAt": "2026-10-29T09:40:00",
  "message": "운송장 정보가 수정되었습니다."
}
```

## 1.3.7 배송 완료 및 방문수령 완료 처리

- 엔드포인트: `PATCH /api/v1/shipments/{shipmentId}/complete`
- 접근 권한: 관리자, 접수 담당자

Request Body

```json
{
  "reason": "택배 배송 완료"
}
```

Response Body (HTTP 200 OK)

```json
{
  "id": 18,
  "returnMethod": "DELIVERY",
  "status": "DELIVERED",
  "completedAt": "2026-10-29T10:05:00",
  "message": "배송 완료 처리되었습니다."
}
```

---

## 1.4 결제 API

### 1.4.1 결제 목록 조회

- 엔드포인트: `GET /api/v1/manage/payments`
- Request Body: 없음
- Response Body (HTTP 200 OK)

```json
{
  "content": [
    {
      "billId": 10,
      "asRequestNumber": "AS-2026-1076",
      "customerName": "최유진",
      "amount": 45000,
      "status": "UNPAID",
      "paymentMethod": "VIRTUAL_ACCOUNT"
    },
    {
      "billId": 11,
      "asRequestNumber": "AS-2026-1062",
      "customerName": "정하늘",
      "amount": 120000,
      "status": "UNPAID",
      "paymentMethod": "CARD"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 25,
  "totalPages": 3
}
```

### 1.4.2 결제 상세 조회

- 엔드포인트: `GET /api/v1/manage/payments/{billId}`
- Request Body: 없음
- Response Body (HTTP 200 OK)

```json
{
  "billId": 10,
  "asRequestNumber": "AS-2026-1076",
  "customerName": "최유진",
  "productName": "시저북 SZ-N14",
  "symptom": "키보드 불량",
  "amount": 120000,
  "billStatus": "UNPAID",
  "payment": null,
  "channel": "ONLINE"
  "virtualAccount": {
    "bankName": "국민은행",
    "accountNumber": "110-220-3300",
    "dueAt": "2026-10-30T23:59:00"
  },
  "refundedAmount": 0,
  "refundableAmount": 0,
  "refunds": []
}
```

### 1.4.3 결제 시도 생성 및 결제 가능 여부 검증

- 엔드포인트: `POST /api/v1/portal/{token}/payments/prepare`
- Request Body: 없음
- Response Body (HTTP 201 Created)

```json
{
  "orderId": "ORD_20261019_1001",
  "amount": 40000
}
```

### 1.4.4 결제 사후 검증

- 엔드포인트: `POST /api/v1/portal/{token}/payments/complete`
- Request Body:

```json
{
  "orderId": "ORD_20261019_1001",
  "paymentKey": "PK_9876543210",
  "amount": 40000
}
```

- Response Body (HTTP 200 OK):

```json
{
  "orderId": "ORD_20261019_1001",
  "amount": 40000,
  "status": "PAID"
}
```

### 1.4.5 결제 비동기 웹훅 수신

- 엔드포인트: `POST /api/v1/payments/webhook/deposit`
- 접근 권한: 전체 공개 (토스페이먼츠 웹훅 검증)
- Request Body:

```json
{
  "createdAt": "2022-01-01T00:00:00.000000",
  "secret": "4ipXAIss_MYROs3VT8E32",
  "status": "DONE",
  "transactionKey": "9FF15E1A29D0E77C218F57262BFA4986",
  "orderId": "ORD_20261019_1001"
}
```

- Response Body (HTTP 200 OK):

```json
{
  "result": "SUCCESS"
}
```

### 1.4.6 고객용 결제 정보 조회

- 엔드포인트: `GET /api/v1/portal/{token}/payments`
- Request Body: 없음
- Response Body (HTTP 200 OK):

```json
{
  "billId": 10,
  "amount": 40000,
  "status": "PAID"
}
```

### 1.4.7 직원용 미결제 목록 조회

- 엔드포인트: `GET /api/v1/manage/payments?status=UNPAID`
- Request Body: 없음
- Response Body (HTTP 200 OK):

```json
{
  "payments": [
    {
      "billId": 10,
      "amount": 40000,
      "status": "UNPAID"
    },
    {
      "billId": 11,
      "amount": 55000,
      "status": "UNPAID"
    }
    [
      }
```

### 1.4.8 가상계좌 취소

- 엔드포인트: `POST /api/v1/manage/payments/{billId}/virtual-account/cancel`
- Request Body:

```json
{
  "cancelReason": "결제 수단 변경"
}
```

- Response Body (HTTP 204 No Content)

### 1.4.9 전액 환불

- 엔드포인트: `POST /api/v1/manage/payments/{billId}/refunds/all`
- 요청 헤더: `Idempotency-Key`
- Request Body:

```json
{
  "cancelReason": "동일 증상 3회"
}
```

- Response Body (HTTP 204 No Content)

### 1.4.10 부분 환불

- 엔드포인트: `POST /api/v1/manage/payments/{billId}/refunds/partial`
- 요청 헤더: `Idempotency-Key`
- Request Body:

```json
{
  "cancelReason": "센터 과실",
  "cancelAmount": 40000
}
```

- Response Body (HTTP 204 No Content)

### 1.4.11 현장 결제 등록

- 엔드포인트: `POST /api/v1/manage/payments/{billId}/onsite`
- Request Body:

```json
{
  "method": "CARD",
  "approvalNumber": "12345678"
  ->
  카드
  단말기
  승인번호
  (현재는
  생략)
}
```

- Response Body (HTTP 204 No Content):

# 1.5 고객 포탈 통합 조회 API

## 1.5.1 고객 포탈 조회

- 엔드포인트: `GET /api/v1/portal/{token}`
- Request Body: 없음
- Response Body (HTTP 200 OK):

```json
{
  "asRequestNumber": "AS-2026-1076",
  "status": "PAYMENT_PENDING",
  "customerName": "한소희",
  "phone": "010-5555-1212",
  "productName": "시저북 SZ-P24",
  "symptom": "키보드 불량"
}
```

## 1.5.2 고객 포탈 점검 및 견적 조회

- 엔드포인트: `GET /api/v1/portal/{token}/inspection`
- Request Body: 없음
- Response Body (HTTP 200 OK):

```json
{
  "inspectionStatus": "IN_PROGRESS",
  "inspection": {
    "startedAt": "2026-10-28T10:00:00",
    "description": "제품 점검 진행 중"
  },
  "estimate": null
}
```

```json
{
  "inspectionStatus": "COMPLETED",
  "inspection": {
    "startedAt": "2026-10-28T10:00:00",
    "completedAt": "2026-10-29T11:00:00",
    "description": "키보드 연결부 불량 확인"
  },
  "estimate": {
    "estimateId": 15,
    "status": "PENDING",
    "items": [
      {
        "name": "키보드 교체",
        "quantity": 1,
        "unitPrice": 40000,
        "amount": 40000
      },
      {
        "name": "수리 공임",
        "quantity": 1,
        "unitPrice": 5000,
        "amount": 5000
      }
    ],
    "totalAmount": 45000,
    "createdAt": "2026-10-29T11:00:00"
  }
}
```

## 1.5.3 고객 포탈 수리 조회

- 엔드포인트: `GET /api/v1/portal/{token}/repair`
- Request Body: 없음
- Response Body (HTTP 200 OK):

```json
{
  "repairStatus": "IN_PROGRESS",
  "repair": {
    "startedAt": "2026-10-29T10:00:00",
    "completedAt": null,
    "description": "키보드 교체 작업 진행 중"
  }
}
```

```json
{
  "repairStatus": "COMPLETED",
  "repair": {
    "startedAt": "2026-10-29T10:00:00",
    "completedAt": "2026-10-29T15:00:00",
    "description": "키보드 교체 및 정상 작동 확인"
  }
}
```

## 1.5.4 고객 포탈 결제 조회

- 엔드포인트: `GET /api/v1/portal/{token}/payments`
- Request Body: 없음
- Response Body (HTTP 200 OK):

```json
1. 결제 대기
{
  "billId": 15,
  "amount": 120000,
  "status": "UNPAID",
  "paymentMethod": null,
  "paidAt": null,
  "virtualAccount": null
}
```

```json
2. 가상계좌 입금 대기
{
  "billId": 15,
  "amount": 120000,
  "status": "UNPAID",
  "paymentMethod": "VIRTUAL_ACCOUNT",
  "paidAt": null,
  "virtualAccount": {
    "bankCode": "20",
    "accountNumber": "12345678901234",
    "dueAt": "2026-10-30T23:59:59"
  }
}
```

```json
3. 결제 완료
{
  "billId": 15,
  "amount": 120000,
  "status": "PAID",
  "paymentMethod": "CARD",
  "paidAt": "2026-10-29T15:30:00",
  "virtualAccount": null
}
```

---

# 1.5 알림/SMS 관리 API

## 1.5.1 SMS/알림 화면 조회

- 엔드포인트: `GET /api/v1/notifications`
- 접근 권한: 관리자, 접수 담당자

Response Body (HTTP 200 OK)

```json
{
  "statistics": {
    "todaySent": 12,
    "success": 10,
    "failed": 2
  },
  "templates": [
    {
      "type": "RECEIVED",
      "name": "접수 완료 안내",
      "content": "[FixGo] {접수번호} 접수가 완료되었습니다."
    },
    {
      "type": "INSPECTION",
      "name": "점검 완료 안내",
      "content": "[FixGo] {접수번호} 제품 점검이 완료되었습니다."
    },
    {
      "type": "ESTIMATE",
      "name": "견적서 발급 안내",
      "content": "[FixGo] {접수번호} 견적서가 발급되었습니다."
    },
    {
      "type": "REPAIR",
      "name": "수리 완료 안내",
      "content": "[FixGo] {접수번호} 제품 수리가 완료되었습니다."
    },
    {
      "type": "PAYMENT",
      "name": "결제 요청 안내",
      "content": "[FixGo] {접수번호} 결제 내용을 확인해 주세요."
    },
    {
      "type": "SHIPPING",
      "name": "배송 시작 안내",
      "content": "[FixGo] {접수번호} 제품 배송이 시작되었습니다."
    }
  ]
}
```

## 1.5.2 알림 발송

- 엔드포인트: `POST /api/v1/notifications/send`
- 접근 권한: 관리자, 접수 담당자

Request Body (HTTP 200 OK)

```json
{
  "asRequestId": 1112,
  "recipientPhone": "01055551212",
  "notificationType": "INSPECTION",
  "requestKey": "AS-1112-INSPECTION-001"
}
```

Response Body (HTTP 202 Accepted)

```json
{
  "id": 128,
  "notificationType": "INSPECTION",
  "recipientPhone": "01055551212",
  "requestedChannel": "ATA",
  "status": "PENDING",
  "message": "점검 완료 안내 발송 요청이 접수되었습니다."
}
```

## 1.5.3 알림/SMS 발송 내역 조회

- 엔드포인트: `GET /api/v1/notifications/history`
- 접근 권한: 관리자, 접수 담당자

Response Body (HTTP 200 OK)

```json
{
  "totalCount": 3,
  "content": [
    {
      "id": 128,
      "sentAt": "2026-10-29T10:24:00",
      "customerName": "김수민",
      "recipientPhone": "010-5555-1212",
      "notificationType": "RECEIVED",
      "content": "접수 완료 안내",
      "status": "SENT"
    },
    {
      "id": 127,
      "sentAt": "2026-10-29T09:18:00",
      "customerName": "정우진",
      "recipientPhone": "010-3333-9090",
      "notificationType": "REPAIR",
      "content": "수리 완료 안내",
      "status": "SENT"
    },
    {
      "id": 126,
      "sentAt": "2026-10-28T16:40:00",
      "customerName": "한소희",
      "recipientPhone": "010-7777-4040",
      "notificationType": "PAYMENT",
      "content": "결제 요청 안내",
      "status": "FAILED"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 3,
  "totalPages": 1
}
```

## 1.5.4 알림/SMS 발송 상세 조회

- 엔드포인트: `GET /api/v1/notifications/{notificationId}`
- 접근 권한: 관리자, 접수 담당자

Response Body (HTTP 200 OK)

```json
{
  "id": 128,
  "sentAt": "2026-10-29T10:24:00",
  "notificationType": "REPAIR",
  "requestedChannel": "ATA",
  "finalChannel": "SMS",
  "status": "SENT",
  "recipient": {
    "customerName": "김미영",
    "recipientPhone": "010-1234-1234",
    "email": "mi32@naver.com",
    "requestNumber": "AS-2026-1112"
  },
  "message": "[FixGo] 고객님의 수리가 완료되었습니다.",
  "providerMessageId": "SOLAPI_MESSAGE_ID",
  "providerGroupId": "SOLAPI_GROUP_ID",
  "providerStatusCode": "4000",
  "results": [
    {
      "channel": "ATA",
      "status": "FAILED",
      "failureReason": "알림톡 발송 실패"
    },
    {
      "channel": "SMS",
      "status": "SENT",
      "failureReason": null
    }
  ]
}
```

## 1.5.5 SMS 발송 결과 웹훅 수신

- 엔드포인트: `POST /api/v1/webhooks/solapi`
- SOLAPI 발송 결과를 수신하고 `NOTIFICATION_LOG`의 상태를 갱신

Response Body (HTTP 200 OK)

```json
{
  "message": "발송 결과가 정상적으로 처리되었습니다."
}
```

## 1.6 고객 관리 API

### 1.6.1 고객 목록 조회

- 엔드포인트: `GET /api/v1/customer`
- 접근 권한: 관리자, 접수 담당자, A/S 기사

Query Parameter

| 파라미터      | 타입      | 설명            |
|-----------|---------|---------------|
| `keyword` | String  | 고객명 또는 연락처 검색 |
| `page`    | Integer | 페이지 번호        |
| `size`    | Integer | 페이지당 조회 개수    |

Response Body (HTTP 200 OK)

```json
{
  "content": [
    {
      "id": 1,
      "name": "홍길동",
      "phone": "010-1234-5678",
      "email": "hong@gmail.com",
      "created_at": "2026-10-28T09:30:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### 1.6.2 고객 등록 엑셀 양식 다운로드

- 엔드포인트: `GET /api/v1/customer`
- 접근 권한: 관리자, 접수 담당자

Response Body (HTTP 200 OK)

| 항목                  | 값                                                                                                              | 설명         |
|---------------------|----------------------------------------------------------------------------------------------------------------|------------|
| Content-Type        | `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`                                            | xlsx 파일 형식 |
| Content-Disposition | `attachment; filename*=UTF-8''%EA%B3%A0%EA%B0%9D_%EC%9D%BC%EA%B4%84%EB%93%B1%EB%A1%9D_%EC%96%91%EC%8B%9D.xlsx` | 파일 다운로드 처리 |
| Body                | `고객_일괄등록_양식.xlsx`                                                                                              | 바이너리 스트림   |

Response Body (HTTP 400 Bad Request)

```json
{
  "code": "INVALID_FILE_REQUEST",
  "message": "파일이 없거나 올바른 엑셀 형식이 아닙니다."
}
```

Response Body (HTTP 403 Bad Request)

```json
{
  "code": "FORBIDDEN",
  "message": "접근 권한이 없습니다."
}
```

Response Body (HTTP 500 Bad Request)

```json
{
  "code": "TEMPLATE_GENERATION_FAILED",
  "message": "양식 파일 생성에 실패했습니다."
}
```

### 1.6.3 엑셀 파일 업로드 및 검증

- 엔드포인트: `GET /api/v1/customer`
- 접근 권한: 관리자, 접수 담당자

Query Parameter

| 파라미터   | 타입   | 설명         |
|--------|------|------------|
| `file` | File | 업로드할 엑셀 파일 |

Response Body (HTTP 200 OK)

```json
{
  "fileName": "고객_일괄등록_양식.xlsx",
  "totalCount": 3,
  "validCount": 1,
  "invalidCount": 2,
  "rows": [
    {
      "rowNumber": 1,
      "name": "홍길동",
      "phone": "010-1234-5678",
      "email": "hong@gmail.com",
      "registrable": true,
      "reason": null
    },
    {
      "rowNumber": 2,
      "name": "김철수",
      "phone": "010-1111-2222",
      "email": null,
      "registrable": false,
      "reason": "이메일은 필수입니다."
    },
    {
      "rowNumber": 3,
      "name": "이영희",
      "phone": "01033",
      "email": "lee@gmail.com",
      "registrable": false,
      "reason": "연락처 형식이 올바르지 않습니다."
    }
  ]
}
```

고객명 (`name`), 연락처 (`phone`), 이메일 (`email`)은 필수 입력 항목

### 1.6.4 고객 일괄 등록

- 엔드포인트: `POST /api/v1/customer`
- 접근 권한: 관리자, 접수 담당자

Request Body

```json
{
  "customers": [
    {
      "rowNumber": 1,
      "name": "홍길동",
      "phone": "010-1234-5678",
      "email": "hong@gmail.com"
    },
    {
      "rowNumber": 2,
      "name": "김철수",
      "phone": "010-1111-2222",
      "email": "kim@gmail.com"
    }
  ]
}
```

Response Body (HTTP 200 OK)

```json
{
  "successCount": 2,
  "failCount": 0,
  "failures": []
}
```

Response Body (HTTP 200 OK) - 일부 실패

```json
{
  "successCount": 1,
  "failCount": 1,
  "failures": [
    {
      "rowNumber": 2,
      "reason": "이미 등록된 고객입니다."
    }
  ]
}
```

Response Body (HTTP 400 Bad Request)

```json
{
  "code": "INVALID_BULK_REQUEST",
  "message": "등록할 고객 정보가 올바르지 않습니다."
}
```

Response Body (HTTP 403 Bad Request)

```json
{
  "code": "FORBIDDEN",
  "message": "접근 권한이 없습니다."
}
```

### 1.6.5 고객 상세 - 상세 정보 조회

- 엔드포인트: `GET /api/v1/customer/{customerId}`
- 접근 권한: 관리자, 접수 담당자, A/S 기사

Path Parameter

| 파라미터         | 타입   | 설명    |
|--------------|------|-------|
| `customerId` | Long | 고객 ID |

Response Body (HTTP 200 OK)

```json
{
  "id": 1,
  "name": "김민수",
  "phone": "010-2222-1818",
  "email": "minsu@example.com",
  "zipcode": "12345",
  "basicAddress": "서울특별시 강남구 테헤란로 123",
  "detailAddress": "강남타워 5층 501호"
}
```

Response Body (HTTP 404 Bad Request)

```json
{
  "code": "CUSTOMER_NOT_FOUND",
  "message": "존재하지 않는 고객입니다."
}
```

### 1.6.6 고객 상세 - 상세 정보 수정

- 엔드포인트: `PATCH /api/v1/customer/{customerId}`
- 접근 권한: 관리자, 접수 담당자, A/S 기사

Request Body

```json
{
  "phone": "010-2222-1818",
  "email": "minsu@example.com",
  "zipcode": "12345",
  "basicAddress": "서울특별시 강남구 테헤란로 123",
  "detailAddress": "강남타워 5층 501호"
}
```

Response Body (HTTP 200 OK)

```json
{
  "phone": "010-2222-1818",
  "email": "minsu@example.com",
  "zipcode": "12345",
  "basicAddress": "서울특별시 강남구 테헤란로 123",
  "detailAddress": "강남타워 5층 501호"
}
```

Response Body (HTTP 409 Bad Request)

```json
{
  "code": "DUPLICATE_CUSTOMER_PHONE",
  "message": "이미 등록된 연락처입니다."
}
```

### 1.6.7 고객 상세 - 이전 수리내역 조회

- 엔드포인트: `GET /api/v1/customer/{customerId}`
- 접근 권한: 관리자, 접수 담당자, A/S 기사

Query Parameter

| 파라미터   | 타입      | 설명         |
|--------|---------|------------|
| `page` | Integer | 페이지 번호     |
| `size` | Integer | 페이지당 조회 개수 |

Response Body (HTTP 200 OK)

```json
{
  "content": [
    {
      "id": 15,
      "receiptNumber": "AS-20260-1108",
      "receivedAt": "2026-10-23T09:30:00",
      "modelName": "SZ-P24",
      "symptom": "충전 안됨",
      "status": "INSPECTION_WAITING",
      "technicianName": "김기사",
      "repairCost": null
    },
    {
      "id": 9,
      "receiptNumber": "AS-20260915-004",
      "receivedAt": "2026-09-02T14:10:00",
      "modelName": "SZ-N16",
      "symptom": "액정 파손",
      "status": "COMPLETED",
      "technicianName": "김기사",
      "repairCost": 85000
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 2,
  "totalPages": 1
}
```

## 1.7 제품 등록 API

### 1.7.1 지원 품목 목록 조회

- 엔드포인트: `GET /api/v1/admin/products/list`
- 접근 권한: 관리자

Query Parameter

| 파라미터        | 타입      | 설명                        |
|-------------|---------|---------------------------|
| `keyword`   | String  | 모델명 또는 제품명 검색             |
| `sortBy`    | String  | 정렬 기준 항목(기본값 `modelName`) |
| `sortOrder` | String  | 정렬 순서 오름/내림차순(기본값 `asc`)  |
| `page`      | Integer | 페이지 번호                    |
| `size`      | Integer | 페이지당 조회 개수                |

Response Body (HTTP 200 OK)

```json
{
  "content": [
    {
      "id": 1,
      "modelName": "SZ-P24",
      "productName": "스마트폰",
      "productGroup": "PHONE",
      "warrantyMonths": 24,
      "active": true
    },
    {
      "id": 2,
      "modelName": "SZ-N16",
      "productName": "노트북",
      "productGroup": "LAPTOP",
      "warrantyMonths": 12,
      "active": false
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 2,
  "totalPages": 1
}
```

# 2.0 A/S 관리 API

## 2.1 A/S 접수 목록 조회

- 엔드포인트: `GET /api/v1/as`
- 접근 권한: 관리자, A/S 기사

Query Parameters

| 파라미터           | 타입      | 설명             |
|----------------|---------|----------------|
| `keyword`      | String  | 접수번호 또는 고객명 검색 |
| `status`       | String  | A/S 진행 상태      |
| `productGroup` | String  | 제품군 필터         |
| `page`         | Integer | 페이지 번호         |
| `size`         | Integer | 페이지당 조회 개수     |

Response Body (HTTP 200 OK)

```json
{
  "content": [
    {
      "id": 1,
      "receiptNumber": "AS-20261028-001",
      "customerName": "홍길동",
      "productModel": "게이밍 PC",
      "symptom": "전원 불량",
      "status": "INSPECTION_WAITING",
      "assignedAt": "2026-10-28T09:30:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

## 2.1.1 내 담당 A/S 목록 조회

- 엔드포인트: `GET /api/v1/as/my-work`
- 접근 권한: A/S 기사

Query Parameters

| 파라미터           | 타입      | 설명             |
|----------------|---------|----------------|
| `keyword`      | String  | 접수번호 또는 고객명 검색 |
| `status`       | String  | A/S 진행 상태      |
| `productGroup` | String  | 제품군 필터         |
| `page`         | Integer | 페이지 번호         |
| `size`         | Integer | 페이지당 조회 개수     |

Response Body (HTTP 200 OK)

```json
{
  "content": [
    {
      "id": 1,
      "receiptNumber": "AS-20261028-001",
      "customerName": "홍길동",
      "productModel": "게이밍 PC",
      "symptom": "전원 불량",
      "status": "INSPECTION_WAITING",
      "assignedAt": "2026-10-28T09:30:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

## 2.1.2 A/S 상세 정보 조회

- 엔드포인트: `GET /api/v1/as/{id}`
- 접근 권한: 관리자, 담당 A/S 기사

Path Parameters

| 파라미터 | 타입   | 설명        |
|------|------|-----------|
| `id` | Long | A/S 접수 ID |

Response Body (HTTP 200 OK)

```json
{
  "id": 1,
  "receiptNumber": "AS-20261028-001",
  "customer": {
    "name": "홍길동",
    "phone": "010-1234-5678"
  },
  "product": {
    "productModel": "게이밍 PC",
    "serialNumber": "PC-2026-0001",
    "warrantyMonths": 24
  },
  "symptom": "전원 불량",
  "receivedMethod": "VISIT",
  "receivedAt": "2026-10-28T09:00:00",
  "assignedTechnician": "김기사",
  "status": "INSPECTION_WAITING"
}
```

## 2.1.3 A/S 점검 정보 등록 및 수정

- 엔드포인트: `PUT /api/v1/as/{id}/inspection`
- 접근 권한: 담당 A/S 기사

Request Body

```json
{
  "inspectionContent": "전원 공급 장치 이상 확인",
  "inspectionImages": [
    "https://example.com/images/inspection-001.jpg"
  ],
  "repairItems": [
    {
      "itemType": "PART",
      "itemName": "파워 서플라이",
      "quantity": 1,
      "unitPrice": 80000
    },
    {
      "itemType": "LABOR",
      "itemName": "부품 교체 공임",
      "workHours": 1,
      "unitPrice": 20000
    }
  ]
}
```

Response Body (HTTP 200 OK)

```json
{
  "asId": 1,
  "inspectionContent": "전원 공급 장치 이상 확인",
  "inspectionCompleted": true,
  "estimatedRepairCost": 100000,
  "status": "INSPECTION_COMPLETED"
}
```

## 2.1.4 수리 프리셋 목록 조회

- 엔드포인트: `GET /api/v1/as/repair-presets`
- 접근 권한: 관리자, A/S 기사

Query Parameters

| 파라미터        | 타입     | 설명       |
|-------------|--------|----------|
| `symptomId` | Long   | 고장 증상 ID |
| `keyword`   | String | 프리셋명 검색  |

Response Body (HTTP 200 OK)

```json
{
  "content": [
    {
      "id": 1,
      "presetName": "파워 불량 교체",
      "symptomName": "전원 불량",
      "description": "파워 서플라이 점검 및 교체",
      "items": [
        {
          "itemType": "PART",
          "itemName": "파워 서플라이",
          "partCode": "PSU-001",
          "quantity": 1,
          "unitPrice": 80000,
          "amount": 80000
        },
        {
          "itemType": "LABOR",
          "itemName": "부품 교체 공임",
          "quantity": 1,
          "unitPrice": 20000,
          "amount": 20000
        }
      ],
      "estimatedRepairCost": 100000
    }
  ]
}
```

## 2.1.5 수리 항목 등록

- 엔드포인트: `POST /api/v1/as/{id}/repair-items`
- 접근 권한: 담당 A/S 기사

Request Body

```json
{
  "items": [
    {
      "itemType": "PART",
      "itemName": "파워 서플라이",
      "partCode": "PSU-001",
      "quantity": 1,
      "unitPrice": 80000
    },
    {
      "itemType": "LABOR",
      "itemName": "부품 교체 공임",
      "quantity": 1,
      "workHours": 1,
      "unitPrice": 20000
    }
  ]
}
```

Response Body (HTTP 201 Created)

```json
{
  "asId": 1,
  "repairItems": [
    {
      "id": 101,
      "itemType": "PART",
      "itemName": "파워 서플라이",
      "quantity": 1,
      "unitPrice": 80000,
      "amount": 80000
    },
    {
      "id": 102,
      "itemType": "LABOR",
      "itemName": "부품 교체 공임",
      "quantity": 1,
      "unitPrice": 20000,
      "amount": 20000
    }
  ],
  "estimatedRepairCost": 100000
}
```

## 2.1.6 보증 판정 등록

- 엔드포인트: `PUT /api/v1/as/{id}/warranty`
- 접근 권한: 담당 A/S 기사

Request Body

```json
{
  "purchaseDate": "2025-10-28",
  "warrantyMonths": 24,
  "proofDocumentProvided": true,
  "customerFault": false,
  "warrantyApplied": true,
  "judgmentReason": "보증 기간 내 제품 자체 결함"
}
```

Response Body (HTTP 200 OK)

```json
{
  "asId": 1,
  "warrantyApplied": true,
  "repairType": "FREE",
  "judgmentReason": "보증 기간 내 제품 자체 결함",
  "status": "WARRANTY_CONFIRMED"
}
```

## 2.1.7 견적서 조회

- 엔드포인트: `GET /api/v1/as/{id}/estimate`
- 접근 권한: 관리자, 담당 A/S 기사

Response Body (HTTP 200 OK)

```json
{
  "asId": 1,
  "receiptNumber": "AS-20261028-001",
  "repairType": "PAID",
  "items": [
    {
      "itemType": "PART",
      "itemName": "파워 서플라이",
      "quantity": 1,
      "unitPrice": 80000,
      "amount": 80000
    },
    {
      "itemType": "LABOR",
      "itemName": "부품 교체 공임",
      "workHours": 1,
      "unitPrice": 20000,
      "amount": 20000
    }
  ],
  "totalAmount": 100000,
  "approvalStatus": "PENDING"
}
```

## 2.1.8 견적서 발송

- 엔드포인트: `POST /api/v1/as/{id}/estimate`
- 접근 권한: 담당 A/S 기사

Request Body

```json
{
  "items": [
    {
      "itemType": "PART",
      "itemName": "파워 서플라이",
      "quantity": 1,
      "unitPrice": 80000
    },
    {
      "itemType": "LABOR",
      "itemName": "부품 교체 공임",
      "workHours": 1,
      "unitPrice": 20000
    }
  ],
  "totalAmount": 100000,
  "message": "견적서를 확인하신 후 수리 승인 여부를 선택해 주세요."
}
```

Response Body (HTTP 201 Created)

```json
{
  "asId": 1,
  "estimateId": 201,
  "totalAmount": 100000,
  "approvalStatus": "PENDING",
  "sentAt": "2026-10-28T10:00:00",
  "status": "ESTIMATE_SENT"
}
```

## 2.1.9 수리 작업 시작

- 엔드포인트: `POST /api/v1/as/{id}/repair/start`
- 접근 권한: 담당 A/S 기사

Request Body

```json
{
  "startNote": "파워 서플라이 교체 작업 시작"
}
```

Response Body (HTTP 200 OK)

```json
{
  "asId": 1,
  "status": "REPAIRING",
  "startedAt": "2026-10-28T10:30:00",
  "technicianName": "김기사"
}
```

## 2.1.10 수리 작업 기록 등록

- 엔드포인트: `POST /api/v1/as/{id}/repair-records`
- 접근 권한: 담당 A/S 기사

Request Body

```json
{
  "workContent": "기존 파워 서플라이 분리 후 신규 부품 장착",
  "usedParts": [
    {
      "partCode": "PSU-001",
      "quantity": 1
    }
  ],
  "workHours": 1,
  "memo": "전원 정상 작동 확인"
}
```

Response Body (HTTP 201 Created)

```json
{
  "recordId": 301,
  "asId": 1,
  "workContent": "기존 파워 서플라이 분리 후 신규 부품 장착",
  "workHours": 1,
  "completedAt": "2026-10-28T11:30:00"
}
```

## 2.1.11 수리 완료 처리

- 엔드포인트: `POST /api/v1/as/{id}/repair/complete`
- 접근 권한: 담당 A/S 기사

Request Body

```json
{
  "repairResult": "파워 서플라이 교체 완료",
  "finalAmount": 100000,
  "memo": "전원 및 부팅 정상 작동 확인"
}
```

Response Body (HTTP 200 OK)

```json
{
  "asId": 1,
  "status": "REPAIR_COMPLETED",
  "repairResult": "파워 서플라이 교체 완료",
  "finalAmount": 100000,
  "completedAt": "2026-10-28T12:00:00"
}
```

## 2.1.11 A/S 거부 및 수리 불가 처리

- 엔드포인트: `POST /api/v1/as/{id}/disposition`
- 접근 권한: 담당 A/S 기사, 관리자

Request Body

```json
{
  "dispositionType": "REPAIR_IMPOSSIBLE",
  "reason": "단종 부품으로 인해 수리 불가",
  "inspectionFee": 10000,
  "customerPickupRequired": true,
  "memo": "고객에게 수리 불가 사유 안내 완료"
}
```

`dispositionType`은 처리 유형에 따라 `CUSTOMER_REJECTED`, `REPAIR_IMPOSSIBLE`, `EXCHANGE`, `REFUND` 등을 사용할 수 있어.

Response Body (HTTP 200 OK)

```json
{
  "asId": 1,
  "dispositionType": "REPAIR_IMPOSSIBLE",
  "reason": "단종 부품으로 인해 수리 불가",
  "inspectionFee": 10000,
  "customerPickupRequired": true,
  "status": "REPAIR_IMPOSSIBLE",
  "processedAt": "2026-10-28T12:30:00",
  "processedBy": "김기사"
}
```

## 2.1.13 A/S 진행 이력 조회

- 엔드포인트: `GET /api/v1/as/{id}/histories`
- 접근 권한: 관리자, 담당 A/S 기사

Response Body (HTTP 200 OK)

```json
{
  "content": [
    {
      "id": 1,
      "previousStatus": "RECEIVED",
      "currentStatus": "INSPECTION_WAITING",
      "changedBy": "김기사",
      "reason": "담당 기사 배정",
      "changedAt": "2026-10-28T09:30:00"
    },
    {
      "id": 2,
      "previousStatus": "INSPECTION_WAITING",
      "currentStatus": "INSPECTION_COMPLETED",
      "changedBy": "김기사",
      "reason": "점검 완료",
      "changedAt": "2026-10-28T10:00:00"
    }
  ]
}
```

## 2.1.14 A/S 상태 변경

- 엔드포인트: `PATCH /api/v1/as/{id}/status`
- 접근 권한: 관리자, 담당 A/S 기사

Request Body

```json
{
  "status": "INSPECTION_COMPLETED",
  "reason": "제품 점검 완료"
}
```

Response Body (HTTP 200 OK)

```json
{
  "asId": 1,
  "previousStatus": "INSPECTION_WAITING",
  "currentStatus": "INSPECTION_COMPLETED",
  "changedBy": "김기사",
  "changedAt": "2026-10-28T10:00:00"
}
```

### 공통 오류 응답

Response Body (HTTP 400 Bad Request)

```json
{
  "code": "INVALID_AS_STATUS_TRANSITION",
  "message": "허용되지 않은 A/S 상태 변경입니다."
}
```

Response Body (HTTP 403 Forbidden)

```json
{
  "code": "AS_ACCESS_DENIED",
  "message": "해당 A/S 건에 대한 접근 권한이 없습니다."
}
```

Response Body (HTTP 404 Not Found)

```json
{
  "code": "AS_NOT_FOUND",
  "message": "해당 A/S 접수 건을 찾을 수 없습니다."
}
```

# 3. 통계 API

## 3.1 통계 요약

- 엔드포인트: `GET /api/v1/admin/statistics`
- 접근 권한: 관리자

Response Body (HTTP 200 OK)

```json
{
  "period": {
    "from": "2026-10-01",
    "to": "2026-10-26"
  },
  "summary": {
    "receivedCount": 128,
    "receivedChangeRate": 12.0,
    "completedCount": 96,
    "completionRate": 75.0,
    "averageLeadTimeDays": 3.2,
    "unpaidAmount": 520000,
    "unpaidCount": 3,
    "paidAmount": 18400000,
    "collectionRate": 97.3
  }
  "statusCounts": {
    "RECEIVED": 12,
    "REPAIRING": 8,
    "WAITING_PAYMENT": 3,
    "READY_RETURN": 4,
    "COMPLETED": 96,
    "CANCELLED": 2
  },
  "deliveryCounts": {
    "READY": 4,
    "SHIPPING": 2,
    "COMPLETED": 90
  },
  "monthlyTrends": [
    {
      "term": "2026-01",
      "receivedCount": 80,
      "completedCount": 61
    },
    {
      "term": "2026-07",
      "receivedCount": 140,
      "completedCount": 112
    },
    {
      "term": "2026-10",
      "receivedCount": 128,
      "completedCount": 96
    }
  ],
  "repairTypeDistribution": [
    {
      "type": "WARRANTY",
      "count": 58,
      "rate": 45.3
    },
    {
      "type": "PAID",
      "count": 49,
      "rate": 38.3
    },
    {
      "type": "INSPECTION_ONLY",
      "count": 21,
      "rate": 16.4
    }
  ],
  "technicianRanks": [
    {
      "technicianId": 11,
      "technicianName": "김민수",
      "assignedCount": 42
    },
    {
      "technicianId": 12,
      "technicianName": "이서연",
      "assignedCount": 35
    },
    {
      "technicianId": 13,
      "technicianName": "박준호",
      "assignedCount": 28
    },
    {
      "technicianId": 14,
      "technicianName": "최유리",
      "assignedCount": 21
    }
  ],
  "modelFaults": [
    {
      "productId": 101,
      "modelName": "Galaxy S24",
      "receivedCount": 32,
      "completedCount": 28,
      "averageLeadTimeDays": 2.8,
      "primarySymptom": "배터리"
    },
    {
      "productId": 102,
      "modelName": "iPhone 15",
      "receivedCount": 27,
      "completedCount": 24,
      "averageLeadTimeDays": 3.1,
      "primarySymptom": "액정"
    }
  ]
}
}
```

## 3.2 입금 대기 내역 조회

- 엔드포인트: `GET /api/v1/admin/statistics/deposits/waiting`
- 접근 권한: 관리자

Response Body (HTTP 200 OK)

```json
{
  "content": [
    {
      "asRequestId": 1001,
      "requestNumber": "AS-202610-001",
      "customerName": "홍길동",
      "modelName": "Galaxy S24",
      "amount": 150000,
      "billedAt": "2026-10-20",
      "elapsedDays": 6,
      "depositStatus": "UNPAID"
    },
    {
      "asRequestId": 988,
      "requestNumber": "AS-202609-088",
      "customerName": "이철수",
      "modelName": "노트북",
      "amount": 90000,
      "billedAt": "2026-09-28",
      "elapsedDays": 28,
      "depositStatus": "DUNNING"
    }
  ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 3,
  "totalPages": 1,
  "last": true
}
```

## 3.3 기사별 수리 현황

- 엔드포인트: `GET /api/v1/admin/statistics/technicians`
- 접근 권한: 관리자

Response Body (HTTP 200 OK)

```json
{
  "period": {
    "from": "2026-10-01",
    "to": "2026-10-26"
  },
  "technicians": [
    {
      "technicianId": 11,
      "technicianName": "김민수",
      "assignedCount": 42,
      "completedCount": 38,
      "inProgressCount": 4,
      "cancelledCount": 0,
      "averageLeadTimeDays": 3.1,
      "salesAmount": 4820000
    },
    {
      "technicianId": 12,
      "technicianName": "이서연",
      "assignedCount": 35,
      "completedCount": 31,
      "inProgressCount": 4,
      "cancelledCount": 0,
      "averageLeadTimeDays": 2.8,
      "salesAmount": 3640000
    }
  ]
}
```

## 3.4 제품 모델 별 수리 접수 건수

- 엔드포인트: `GET /api/v1/admin/statistics/models`
- 접근 권한: 관리자

Response Body (HTTP 200 OK)

```json
{
  "period": {
    "from": "2026-10-01",
    "to": "2026-10-26"
  },
  "models": [
    {
      "productId": 101,
      "modelName": "Galaxy S24",
      "category": "스마트폰",
      "receivedCount": 32,
      "completedCount": 28,
      "inProgressCount": 4,
      "primarySymptom": "배터리"
    },
    {
      "productId": 103,
      "modelName": "Galaxy Book4",
      "category": "노트북",
      "receivedCount": 18,
      "completedCount": 14,
      "inProgressCount": 4,
      "primarySymptom": "키보드"
    }
  ]
}
```

## 3.5 모델 별 고장 증상 통계

- 엔드포인트: `GET /api/v1/admin/statistics/models/{id}`
- 접근 권한: 관리자

Response Body (HTTP 200 OK)

```json
{
  "period": {
    "from": "2026-10-01",
    "to": "2026-10-26"
  },
  "productId": 101,
  "modelName": "Galaxy S24",
  "receivedCount": 32,
  "symptoms": [
    {
      "symptom": "배터리",
      "count": 14
    },
    {
      "symptom": "액정",
      "count": 9
    },
    {
      "symptom": "충전",
      "count": 6
    },
    {
      "symptom": "카메라",
      "count": 3
    }
  ]
}
```

## 3.6 기간별 수리비 추이

- 엔드포인트: `GET /api/v1/admin/statistics/repair_costs`
- 접근 권한: 관리자

Response Body (HTTP 200 OK)

```json
{
  "period": {
    "from": "2026-10-01",
    "to": "2026-10-26"
  },
  "term": "MONTH",
  "totalAmount": 128400000,
  "points": [
    {
      "term": "2026-01",
      "amount": 9200000
    },
    {
      "term": "2026-07",
      "amount": 15200000
    },
    {
      "term": "2026-10",
      "amount": 18400000
    }
  ]
}
```

## 3.7 장기 미처리 조회

- 엔드포인트: `GET /api/v1/admin/statistics/incomplete`
- 접근 권한: 관리자

Response Body (HTTP 200 OK)

```json
{
  "minElapsedDays": 14,
  "totalCount": 4,
  "content": [
    {
      "asRequestId": 901,
      "requestNumber": "AS-202609-054",
      "customerName": "오세훈",
      "modelName": "MacBook Air",
      "status": "RECEIVED",
      "technicianId": 11,
      "technicianName": "김민수",
      "elapsedDays": 35,
      "receivedElapsedDays": 35,
      "lastActivityAt": "2026-09-21T09:12:00"
    },
    {
      "asRequestId": 988,
      "requestNumber": "AS-202609-088",
      "customerName": "이철수",
      "modelName": "Galaxy Book4",
      "status": "WAITING_PAYMENT",
      "technicianId": 12,
      "technicianName": "이서연",
      "elapsedDays": 28,
      "receivedElapsedDays": 28,
      "lastActivityAt": "2026-09-28T15:40:00"
    }
  ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 4,
  "totalPages": 1,
  "last": true
}
```