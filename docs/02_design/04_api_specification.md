# **1. REST API 명세서 및 규격서 (FixGo)**

기준 문서: https://fixgo-api.likelion.shop/swagger-ui/index.html

API 목록은 실행 중인 Swagger의 전체 오퍼레이션이다. 요청·응답 필드는 같은 경로의 컨트롤러 DTO를 따른다. Swagger의 CreateRequest, UpdateRequest, Response처럼 이름이 같은 스키마는 마지막에 등록된 정의가 나머지를 덮으므로, 이 명세서의 JSON은 그 덮인 스키마가 아니라 각 API의 DTO를 적는다.

수록 API: 134개

## **1.1 공통 응답 규격**

### **1.1.1 성공 응답 포맷 (HTTP 200 / 201)**

별도의 공통 래퍼 없이 컨트롤러가 DTO 객체, List, 또는 Spring Data Page를 HTTP Body로 직접 반환한다.

단건 응답은 각 API의 Response Body와 같다. 목록 페이징은 아래 Page 형식이다. number는 0부터 센다.

```json
{
	"content": [],
	"totalElements": 0,
	"totalPages": 0,
	"size": 20,
	"number": 0,
	"first": true,
	"last": true,
	"empty": true
}
```

### **1.1.2 실패 및 에러 응답 포맷 (ErrorResponse)**

전역 예외 처리기가 아래 JSON을 반환한다. 인증 실패는 401, 권한 없음은 403, 잘못된 요청은 400, 대상 없음은 404, 그 밖은 500이다.

```json
{
	"code": "NOT_FOUND",
	"message": "요청한 리소스를 찾을 수 없습니다."
}
```

## **1.2 API 목록**

A/S Reception API

- **API:** GET /api/v1/as-receptions — 접수 목록 조회
- **API:** POST /api/v1/as-receptions — 접수 등록
- **API:** POST /api/v1/as-receptions/my/{id}/inspection/start — 점검 시작
- **API:** PATCH /api/v1/as-receptions/{receptionId}/technician — 기사 재배정
- **API:** PATCH /api/v1/as-receptions/{receptionId}/status — 접수 상태 수동 변경
- **API:** GET /api/v1/as-receptions/{id} — 접수 상세 조회
- **API:** PATCH /api/v1/as-receptions/{id} — 접수 수정
- **API:** GET /api/v1/as-receptions/{receptionId}/technician-history — 기사 배정 이력 조회
- **API:** GET /api/v1/as-receptions/{id}/portal-link — 고객 안내 링크 조회
- **API:** GET /api/v1/as-receptions/{id}/history — 접수 상태 변경 이력 조회
- **API:** GET /api/v1/as-receptions/{id}/documents/repair-statement — 수리내역서 조회
- **API:** GET /api/v1/as-receptions/{id}/documents/reception-receipt — 접수증 조회
- **API:** GET /api/v1/as-receptions/{id}/documents/estimate — 견적서 조회
- **API:** GET /api/v1/as-receptions/technician-histories —
- **API:** GET /api/v1/as-receptions/status-histories —
- **API:** GET /api/v1/as-receptions/my — 기사 전용 접수 목록 조회
- **API:** GET /api/v1/as-receptions/my/{id} — 기사 전용 접수 상세 조회

Admin Member API

- **API:** POST /api/v1/admin/member — 직원생성
- **API:** DELETE /api/v1/admin/member — 직원삭제
- **API:** PATCH /api/v1/admin/member/status — 직원 상태 변경
- **API:** PATCH /api/v1/admin/member/role — 직원역할 변경
- **API:** PATCH /api/v1/admin/member/profile — 직원 프로필 수정

AI API

- **API:** POST /api/v1/ai/chat/test — BrunoTest용
- **API:** POST /api/v1/ai/chat/stream — AI 스트리밍 채팅
- **API:** GET /api/v1/ai — 채팅 이력 조회

Auth API

- **API:** POST /api/v1/auth/refresh — 토큰 재발급
- **API:** POST /api/v1/auth/logout — 로그아웃
- **API:** POST /api/v1/auth/login — 로그인

Customer API

- **API:** GET /api/v1/customers — 고객 목록/검색
- **API:** POST /api/v1/customers — 고객 등록
- **API:** POST /api/v1/customers/bulk — 고객 일괄 등록
- **API:** POST /api/v1/customers/bulk/validate — 고객 일괄 등록 전 검증
- **API:** GET /api/v1/customers/{id} — 고객 단건 조회
- **API:** PATCH /api/v1/customers/{id} — 고객 정보 수정
- **API:** GET /api/v1/customers/check-duplicate — 연락처 중복 체크
- **API:** GET /api/v1/customers/bulk/template — 엑셀 양식 다운로드

Email Preset API

- **API:** PUT /api/v1/email-presets/{id} — 이메일 프리셋 수정
- **API:** DELETE /api/v1/email-presets/{id} — 이메일 프리셋 삭제
- **API:** GET /api/v1/email-presets — 이메일 프리셋 목록 조회
- **API:** POST /api/v1/email-presets — 이메일 프리셋 등록

Labor Rate API

- **API:** GET /api/v1/labor-rates — 공임비점검비 전체 목록 조회
- **API:** PATCH /api/v1/labor-rates — 공임비점검비 수정
- **API:** GET /api/v1/labor-rates/{id} — 공임비점검비 상세 조회

Long Pending Statistics API

- **API:** GET /api/v1/admin/statistics/long_pending — 장기 미처리 접수건 조회

Member API

- **API:** PATCH /api/v1/member/me/password — 비밀번호 변경
- **API:** GET /api/v1/member — 직원 목록 조회
- **API:** GET /api/v1/member/{memberId} — 직원 상세 조회
- **API:** GET /api/v1/member/technicians — 배정 가능 기술자 목록 조회
- **API:** GET /api/v1/member/me — 내 프로필 조회
- **API:** GET /api/v1/member/deleted_member — 삭제된 직원 목록 조회

Model Statistics API

- **API:** GET /api/v1/admin/statistics/models — 모델별 수리 통계 조회
- **API:** GET /api/v1/admin/statistics/models/{modelId}/symptoms — 모델 증상별 통계 조회

Notification API

- **API:** GET /api/v1/notifications/emails — 이메일 발송 내역 조회
- **API:** POST /api/v1/notifications/emails — 이메일 발송
- **API:** POST /api/v1/notifications/emails/{id}/retry — 실패 이메일 재발송
- **API:** GET /api/v1/notifications/statistics — 이메일 발송 통계 조회
- **API:** GET /api/v1/notifications/emails/{id} — 이메일 발송 상세 조회

Part API

- **API:** PATCH /api/v1/part/unit_price — 부품 단가 수정
- **API:** PATCH /api/v1/part/stocks — 재고 수량 다건 수정
- **API:** PATCH /api/v1/part/stock — 재고 수량 단건 수정
- **API:** PATCH /api/v1/part/standard_minutes — 표준 작업시간 수정
- **API:** GET /api/v1/part — 부품 목록 조회
- **API:** GET /api/v1/part/{partId} — 부품 상세 조회

Payment API

- **API:** POST /api/v1/payments/{billId}/refund — 결제 환불
- **API:** POST /api/v1/payments/{billId}/cash — 현금 결제
- **API:** POST /api/v1/payments/webhook — 토스 웹훅 처리
- **API:** POST /api/v1/payments/confirm — 결제 승인
- **API:** GET /api/v1/payments — 결제 목록 조회
- **API:** GET /api/v1/payments/{billId} — 결제 상세 조회

Payment Statistics API

- **API:** GET /api/v1/admin/statistics/unpaid-bills — 미수금 청구서 조회
- **API:** GET /api/v1/admin/statistics/payments — 결제 통계 조회
- **API:** GET /api/v1/admin/statistics/payments/history — 기간 결제 내역 조회

Portal API

- **API:** POST /api/v1/portal/{token}/quote/reject — 견적 거부
- **API:** POST /api/v1/portal/{token}/quote/approve — 견적 승인
- **API:** POST /api/v1/portal/{token}/payments/start — 결제 시작
- **API:** POST /api/v1/portal/{token}/payments/cancel — 가상계좌 취소
- **API:** GET /api/v1/portal/{token} — 포탈 조회

Product API

- **API:** GET /api/v1/products — 시리얼번호 검색
- **API:** POST /api/v1/products — 수리 대상 제품 정보 등록
- **API:** PATCH /api/v1/products/{id}/purchased_at — 수리 대상 제품 정보 수정
- **API:** GET /api/v1/products/{id} — 수리 대상 제품 정보 조회
- **API:** GET /api/v1/products/search — 제품 페이징 검색

Product Model API

- **API:** POST /api/v1/product-models — 제품 모델 등록
- **API:** GET /api/v1/product-models/{id} — 제품 모델 단건 조회
- **API:** PATCH /api/v1/product-models/{id} — 제품 모델 수정
- **API:** GET /api/v1/product-models/list — 제품 모델 목록 검색

Reception Statistics API

- **API:** GET /api/v1/admin/statistics/receptions — 접수 통계 조회

Repair API

- **API:** GET /api/v1/as/repairs/{id} — 수리 상세 조회
- **API:** PUT /api/v1/as/repairs/{id} — 수리 정보 수정
- **API:** DELETE /api/v1/as/repairs/{id} — 수리 삭제
- **API:** GET /api/v1/as/repairs — 전체 수리 목록 조회
- **API:** POST /api/v1/as/repairs — 수리 등록
- **API:** POST /api/v1/as/repairs/{id}/abort — A/S 중단
- **API:** PATCH /api/v1/as/repairs/{id}/status — 수리 상태 변경
- **API:** PATCH /api/v1/as/repairs/{id}/start — 수리 시작
- **API:** PATCH /api/v1/as/repairs/{id}/complete — 수리 완료
- **API:** GET /api/v1/as/repairs/reception/{receptionNumber} — 접수번호로 수리 상세 조회
- **API:** GET /api/v1/as/repairs/my — 본인에게 배정된 A/S 목록 조회
- **API:** GET /api/v1/as/repairs/my/{id} — 본인에게 배정된 A/S 상세 조회

Repair Cost API

- **API:** POST /api/v1/as/repairs/{repairId}/cost — 수리비 계산
- **API:** POST /api/v1/as/repairs/{repairId}/cost/confirm — 수리비 확정

Repair History API

- **API:** GET /api/v1/as/repair-histories/{asReceptionId} — A/S 수리 이력 조회

Repair Part Usage API

- **API:** POST /api/v1/as/repair-part-usages — 실제 수리 부품 사용 등록

Repair Preset API

- **API:** GET /api/v1/as/repair-presets/{id} — 단일 프리셋 조회
- **API:** PUT /api/v1/as/repair-presets/{id} — 프리셋 수정
- **API:** DELETE /api/v1/as/repair-presets/{id} — 프리셋 삭제
- **API:** GET /api/v1/as/repair-presets — 전체 프리셋 조회
- **API:** POST /api/v1/as/repair-presets — 프리셋 등록
- **API:** GET /api/v1/as/repair-presets/{id}/parts — 프리셋 부품 목록 조회
- **API:** GET /api/v1/as/repair-presets/search — 프리셋 이름 검색

Repair Work API

- **API:** GET /api/v1/as/repair-works/{id} — 수리 작업 내역 단건 조회
- **API:** PUT /api/v1/as/repair-works/{id} — 수리 작업 내역 수정
- **API:** DELETE /api/v1/as/repair-works/{id} — 수리 작업 내역 삭제
- **API:** GET /api/v1/as/repair-works — 수리 작업 내역 전체 조회
- **API:** POST /api/v1/as/repair-works — 수리 작업 내역 등록
- **API:** GET /api/v1/as/repair-works/search — 작업명 검색
- **API:** GET /api/v1/as/repair-works/repair/{repairId} — 특정 수리 건의 작업 내역 조회

Shipment API

- **API:** POST /api/v1/shipments — 택배 배송 등록
- **API:** POST /api/v1/shipments/{shipmentId}/tracking/refresh — 배송 추적 갱신
- **API:** POST /api/v1/shipments/pickups — 방문 수령 등록
- **API:** PATCH /api/v1/shipments/{shipmentId}/tracking-info — 운송장 정보 수정
- **API:** PATCH /api/v1/shipments/{shipmentId}/shipping — 배송 시작
- **API:** PATCH /api/v1/shipments/{shipmentId}/pickup — 방문 수령 완료
- **API:** PATCH /api/v1/shipments/{shipmentId}/delivered — 배송 완료
- **API:** GET /api/v1/shipments/{shipmentId} — 배송 단건 조회
- **API:** GET /api/v1/shipments/{shipmentId}/details — 배송 상세 조회
- **API:** GET /api/v1/shipments/receptions/{asReceptionId} — 접수별 배송 조회
- **API:** GET /api/v1/shipments/list — 배송 관리 목록 조회

Shipment Statistics API

- **API:** GET /api/v1/admin/statistics/shipments — 배송 현황 통계 조회
- **API:** GET /api/v1/admin/statistics/shipments/history — 기간 배송 내역 조회

Symptom Preset API

- **API:** GET /api/v1/as/symptom-presets — 고장 증상 프리셋 목록 조회
- **API:** GET /api/v1/as/symptom-presets/{symptomId}/repair-presets — 증상에 연결된 수리 프리셋 조회
- **API:** GET /api/v1/as/symptom-presets/search — 고장 증상 프리셋 검색

Technician Statistics API

- **API:** GET /api/v1/admin/statistics/technicians — 기사별 통계 조회

## 1.3 A/S Reception API

### 1.3.1 접수 목록 조회

- 엔드포인트: `GET /api/v1/as-receptions`
- 설명: 검색 조건에 맞는 A/S 접수 목록을 페이징하여 반환한다.
- 접근 권한: 로그인 사용자
- Query Parameter: keyword (String, 선택) / status (AsReceptionStatus, 선택) / technicianId (Long, 선택) / customerId (Long, 선택) / page (int, 선택, 예: 0) — 페이지 번호. 0부터 시작한다. / size (int, 선택, 예: 20) — 페이지 크기 / sort (string, 선택) — 정렬. 예: createdAt,desc
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "content": [
    {
      "id": 1,
      "receptionNumber": "AS-251111-abc",
      "createdAt": "2025-11-11T09:00:00",
      "customerName": "홍길동",
      "customerPhone": "010-1234-5678",
      "modelName": "FixGo Washer X1",
      "receptionStatus": "RECEIVED",
      "repairStatus": "DIAGNOSING",
      "technicianName": "김기사"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false
}
```

### 1.3.2 접수 등록

- 엔드포인트: `POST /api/v1/as-receptions`
- 설명: 새로운 A/S 접수를 등록한다.
- 접근 권한: 로그인 사용자
- 필수 필드: customerId, productId, technicianId, symptom, symptomDetail
- Request Body:

```json
{
  "customerId": 1,
  "productId": 1,
  "technicianId": 3,
  "symptom": "전원 불량",
  "symptomDetail": "전원 버튼을 눌러도 켜지지 않음",
  "memo": "고객 방문 예정",
  "returnMethod": "DELIVERY",
  "fileUrl": "<https://cdn.fixgo.com/files/as-001.jpg>"
}
```

- Response Body (HTTP 201):

```json
{
  "id": 1,
  "receptionNumber": "AS-251111-abc",
  "customerId": 1,
  "productId": 1,
  "receptionistId": 2,
  "technicianId": 3,
  "status": "RECEIVED",
  "createdAt": "2025-11-11T09:00:00"
}
```

### 1.3.3 점검 시작

- 엔드포인트: `POST /api/v1/as-receptions/my/{id}/inspection/start`
- 설명: 담당 기사가 접수 건의 점검을 시작한다.
- 접근 권한: 수리기사
- Path Variable: id (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "asReceptionId": 1,
  "technicianId": 3,
  "diagnosis": "메인보드 전원부 고장",
  "workNote": "메인보드 교체",
  "repairDecision": "REPAIRABLE",
  "warrantyResult": "IN_WARRANTY",
  "reason": "부품 노후",
  "laborRate": 30000,
  "inspectionCost": 10000,
  "finalAmount": 85000,
  "status": "WAITING",
  "reasonForRejection": "견적 미승인",
  "confirmedAt": "2025-11-11T12:00:00",
  "createdAt": "2025-11-11T09:00:00",
  "updatedAt": "2025-11-11T10:00:00",
  "createdBy": 2,
  "updatedBy": 3
}
```

### 1.3.4 기사 재배정

- 엔드포인트: `PATCH /api/v1/as-receptions/{receptionId}/technician`
- 설명: 접수원/관리자가 A/S 접수의 담당 기사를 재배정한다.
- 접근 권한: 접수담당자, 관리자
- Path Variable: receptionId (Long, 필수)
- 필수 필드: technicianId, reason
- Request Body:

```json
{
  "technicianId": 5,
  "reason": "담당 기사 일정 변경"
}
```

- Response Body: 없음 (HTTP 204)

### 1.3.5 접수 상태 수동 변경

- 엔드포인트: `PATCH /api/v1/as-receptions/{receptionId}/status`
- 설명: 관리자/접수원이 A/S 접수 상태를 수동으로 변경한다.
- 접근 권한: 관리자, 접수담당자
- Path Variable: receptionId (Long, 필수)
- Query Parameter: newStatus (AsReceptionStatus, 필수) / reason (String, 선택)
- Request Body: 없음
- Response Body: 없음 (HTTP 200)

### 1.3.6 접수 상세 조회

- 엔드포인트: `GET /api/v1/as-receptions/{id}`
- 설명: 접수 ID로 A/S 접수 상세 정보를 조회한다.
- 접근 권한: 로그인 사용자
- Path Variable: id (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "receptionNumber": "AS-251111-abc",
  "createdAt": "2025-11-11T09:00:00",
  "customer": {
    "id": 1,
    "name": "",
    "phone": "",
    "email": "",
    "zipcode": "",
    "basicAddress": "",
    "detailAddress": "",
    "createdAt": "2026-11-13T10:00:00",
    "updatedAt": "2026-11-13T10:00:00",
    "createdBy": 1,
    "updatedBy": 1
  },
  "product": {
    "id": 1,
    "productModelId": 1,
    "customerId": 1,
    "serialNumber": "",
    "manufacturedAt": "2026-11-13T10:00:00",
    "purchasedAt": "2026-11-13T10:00:00"
  },
  "modelName": "FixGo Washer X1",
  "receptionist": {
    "id": 1,
    "name": "김기사"
  },
  "technician": {
    "id": 1,
    "name": "김기사"
  },
  "status": "RECEIVED",
  "symptom": "전원 불량",
  "symptomDetail": "전원 버튼을 눌러도 켜지지 않음",
  "memo": "고객 방문 예정",
  "returnMethod": "DELIVERY",
  "fileUrl": "<https://cdn.fixgo.com/files/as-001.jpg>"
}
```

### 1.3.7 접수 수정

- 엔드포인트: `PATCH /api/v1/as-receptions/{id}`
- 설명: A/S 접수 정보를 수정한다.
- 접근 권한: 로그인 사용자
- Path Variable: id (Long, 필수)
- Request Body:

```json
{
  "symptom": "전원 불량",
  "symptomDetail": "전원 버튼을 눌러도 켜지지 않음",
  "memo": "고객 방문 예정",
  "returnMethod": "DELIVERY"
}
```

- Response Body (HTTP 200):

```json
{
  "id": 1,
  "receptionNumber": "AS-251111-abc",
  "createdAt": "2025-11-11T09:00:00",
  "customer": {
    "id": 1,
    "name": "",
    "phone": "",
    "email": "",
    "zipcode": "",
    "basicAddress": "",
    "detailAddress": "",
    "createdAt": "2026-11-13T10:00:00",
    "updatedAt": "2026-11-13T10:00:00",
    "createdBy": 1,
    "updatedBy": 1
  },
  "product": {
    "id": 1,
    "productModelId": 1,
    "customerId": 1,
    "serialNumber": "",
    "manufacturedAt": "2026-11-13T10:00:00",
    "purchasedAt": "2026-11-13T10:00:00"
  },
  "modelName": "FixGo Washer X1",
  "receptionist": {
    "id": 1,
    "name": "김기사"
  },
  "technician": {
    "id": 1,
    "name": "김기사"
  },
  "status": "RECEIVED",
  "symptom": "전원 불량",
  "symptomDetail": "전원 버튼을 눌러도 켜지지 않음",
  "memo": "고객 방문 예정",
  "returnMethod": "DELIVERY",
  "fileUrl": "<https://cdn.fixgo.com/files/as-001.jpg>"
}
```

### 1.3.8 기사 배정 이력 조회

- 엔드포인트: `GET /api/v1/as-receptions/{receptionId}/technician-history`
- 설명: A/S 접수의 기사 배정/재배정 이력을 조회한다.
- 접근 권한: 접수담당자, 관리자
- Path Variable: receptionId (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "receptionId": 1,
  "previousTechnician": {
    "id": 3,
    "name": "김기사"
  },
  "newTechnician": {
    "id": 3,
    "name": "김기사"
  },
  "reason": "담당 기사 일정 변경",
  "createdAt": "2025-11-11T10:00:00",
  "changedBy": {
    "id": 3,
    "name": "김기사"
  }
}
```

### 1.3.9 고객 안내 링크 조회

- 엔드포인트: `GET /api/v1/as-receptions/{id}/portal-link`
- 설명: 고객 포탈 안내 링크를 조회한다.
- 접근 권한: 로그인 사용자
- Path Variable: id (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "token": "",
  "expiresAt": "2026-11-13T10:00:00"
}
```

### 1.3.10 접수 상태 변경 이력 조회

- 엔드포인트: `GET /api/v1/as-receptions/{id}/history`
- 설명: A/S 접수의 상태 변경 이력을 조회한다.
- 접근 권한: 로그인 사용자
- Path Variable: id (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "fromStatus": "",
  "toStatus": "",
  "reason": "",
  "changedBy": "",
  "createdAt": "2026-11-13T10:00:00"
}
```

### 1.3.11 수리내역서 조회

- 엔드포인트: `GET /api/v1/as-receptions/{id}/documents/repair-statement`
- 설명: A/S 접수에 대한 수리내역서를 조회한다.
- 접근 권한: 접수담당자, 관리자
- Path Variable: id (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "receptionNumber": "",
  "customerName": "",
  "issuedAt": "2026-11-13T10:00:00",
  "technicianName": "",
  "workNote": "",
  "repairWorks": [
    {
      "repairWorkId": 1,
      "repairPresetName": "",
      "workName": "",
      "usedParts": [
        {
          "id": 1,
          "repairWorkId": 1,
          "partId": 1,
          "partName": "",
          "quantity": 1,
          "unitPrice": 1,
          "totalPrice": 1,
          "createdAt": "2026-11-13T10:00:00"
        }
      ]
    }
  ],
  "laborCost": 1,
  "finalAmount": 1
}
```

### 1.3.12 접수증 조회

- 엔드포인트: `GET /api/v1/as-receptions/{id}/documents/reception-receipt`
- 설명: A/S 접수증 문서를 조회한다.
- 접근 권한: 접수담당자, 관리자
- Path Variable: id (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "receptionNumber": "AS-251111-abc",
  "createdAt": "2025-11-11T09:00:00",
  "customerName": "홍길동",
  "customerPhone": "010-1234-5678",
  "customerEmail": "customer@example.com",
  "zipcode": "06236",
  "basicAddress": "서울시 강남구 테헤란로",
  "detailAddress": "101동 202호",
  "modelName": "FixGo Washer X1",
  "serialNumber": "SN-123456",
  "symptom": "전원 불량",
  "returnMethod": "DELIVERY",
  "receptionistName": "이접수",
  "technicianName": "김기사"
}
```

### 1.3.13 견적서 조회

- 엔드포인트: `GET /api/v1/as-receptions/{id}/documents/estimate`
- 설명: A/S 접수에 대한 견적서를 조회한다.
- 접근 권한: 접수담당자, 관리자
- Path Variable: id (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "receptionId": 1,
  "receptionNumber": "",
  "confirmedAt": "2026-11-13T10:00:00",
  "laborRate": 1,
  "inspectionCost": 1,
  "totalAmount": 1,
  "items": [
    {
      "category": "",
      "name": "",
      "quantity": 1,
      "amount": 1
    }
  ]
}
```

### 1.3.14 /api/v1/as-receptions/technician-histories

- 엔드포인트: `GET /api/v1/as-receptions/technician-histories`
- 접근 권한: 관리자
- Query Parameter: keyword (String, 선택) / page (int, 선택, 예: 0) — 페이지 번호. 0부터 시작한다. / size (int, 선택, 예: 20) — 페이지 크기 / sort (string, 선택) — 정렬. 예: createdAt,desc
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "content": [
    {
      "id": 1,
      "receptionId": 1,
      "receptionNumber": "",
      "customerName": "",
      "modelName": "",
      "previousTechnicianName": "",
      "newTechnicianName": "",
      "reason": "",
      "changedBy": "",
      "createdAt": "2026-11-13T10:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false
}
```

### 1.3.15 /api/v1/as-receptions/status-histories

- 엔드포인트: `GET /api/v1/as-receptions/status-histories`
- 접근 권한: 관리자
- Query Parameter: keyword (String, 선택) / toStatus (String, 선택) / page (int, 선택, 예: 0) — 페이지 번호. 0부터 시작한다. / size (int, 선택, 예: 20) — 페이지 크기 / sort (string, 선택) — 정렬. 예: createdAt,desc
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "content": [
    {
      "id": 1,
      "receptionId": 1,
      "receptionNumber": "",
      "customerName": "",
      "modelName": "",
      "fromStatus": "",
      "toStatus": "",
      "reason": "",
      "changedBy": "",
      "createdAt": "2026-11-13T10:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false
}
```

### 1.3.16 기사 전용 접수 목록 조회

- 엔드포인트: `GET /api/v1/as-receptions/my`
- 설명: 담당 기사에게 배정된 A/S 접수 목록을 페이징하여 조회한다.
- 접근 권한: 수리기사
- Query Parameter: keyword (String, 선택) / status (AsReceptionStatus, 선택) / page (int, 선택, 예: 0) — 페이지 번호. 0부터 시작한다. / size (int, 선택, 예: 20) — 페이지 크기 / sort (string, 선택) — 정렬. 예: createdAt,desc
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "content": [
    {
      "id": 1,
      "receptionNumber": "AS-251111-abc",
      "createdAt": "2025-11-11T09:00:00",
      "customerName": "홍길동",
      "customerPhone": "010-1234-5678",
      "modelName": "FixGo Washer X1",
      "receptionStatus": "RECEIVED",
      "repairStatus": "DIAGNOSING",
      "technicianName": "김기사"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false
}
```

### 1.3.17 기사 전용 접수 상세 조회

- 엔드포인트: `GET /api/v1/as-receptions/my/{id}`
- 설명: 담당 기사에게 배정된 A/S 접수 상세 정보를 조회한다.
- 접근 권한: 수리기사
- Path Variable: id (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "reception": {
    "id": 1,
    "receptionNumber": "AS-251111-abc",
    "createdAt": "2025-11-11T09:00:00",
    "customer": {
      "id": 1,
      "name": "",
      "phone": "",
      "email": "",
      "zipcode": "",
      "basicAddress": "",
      "detailAddress": "",
      "createdAt": "2026-11-13T10:00:00",
      "updatedAt": "2026-11-13T10:00:00",
      "createdBy": 1,
      "updatedBy": 1
    },
    "product": {
      "id": 1,
      "productModelId": 1,
      "customerId": 1,
      "serialNumber": "",
      "manufacturedAt": "2026-11-13T10:00:00",
      "purchasedAt": "2026-11-13T10:00:00"
    },
    "modelName": "FixGo Washer X1",
    "receptionist": {
      "id": 1,
      "name": "김기사"
    },
    "technician": {
      "id": 1,
      "name": "김기사"
    },
    "status": "RECEIVED",
    "symptom": "전원 불량",
    "symptomDetail": "전원 버튼을 눌러도 켜지지 않음",
    "memo": "고객 방문 예정",
    "returnMethod": "DELIVERY",
    "fileUrl": "<https://cdn.fixgo.com/files/as-001.jpg>"
  },
  "repair": {
    "id": 1,
    "asReceptionId": 1,
    "technicianId": 3,
    "diagnosis": "메인보드 전원부 고장",
    "workNote": "메인보드 교체",
    "repairDecision": "REPAIRABLE",
    "warrantyResult": "IN_WARRANTY",
    "reason": "부품 노후",
    "laborRate": 30000,
    "inspectionCost": 10000,
    "finalAmount": 85000,
    "status": "WAITING",
    "reasonForRejection": "견적 미승인",
    "confirmedAt": "2025-11-11T12:00:00",
    "createdAt": "2025-11-11T09:00:00",
    "updatedAt": "2025-11-11T10:00:00",
    "createdBy": 2,
    "updatedBy": 3
  }
}
```

## 1.4 Admin Member API

### 1.4.1 회원 생성

- 엔드포인트: `POST /api/v1/admin/member`
- 설명: 관리자가 신규 회원을 생성
- 접근 권한: 관리자
- 필수 필드: email, password, name, phone, address, role
- Request Body:

```json
{
  "email": "user@fixgo.com",
  "password": "password123!",
  "name": "김병호",
  "phone": "010-1234-5678",
  "address": "서울특별시 강남구 테헤란로 123",
  "role": "ROLE_ADMIN"
}
```

- Response Body (HTTP 201):

```json
{
  "id": 1,
  "email": "user@fixgo.com",
  "name": "김병호",
  "phone": "010-1234-5678",
  "address": "서울특별시 강남구 테헤란로 123",
  "role": "ROLE_ADMIN",
  "status": "PENDING"
}
```

### 1.3.2 회원 삭제

- 엔드포인트: `DELETE /api/v1/admin/member`
- 설명: 관리자가 회원을 삭제(소프트 삭제)
- 접근 권한: 관리자
- Query Parameter: memberId (Long, 필수)
- Request Body: 없음
- Response Body: 없음 (HTTP 200)

### 1.3.3 회원 상태 변경

- 엔드포인트: `PATCH /api/v1/admin/member/status`
- 설명: 관리자가 회원의 상태를 변경
- 접근 권한: 관리자
- 필수 필드: email, status
- Request Body:

```json
{
  "email": "user@fixgo.com",
  "status": "PENDING"
}
```

- Response Body (HTTP 200):

```json
{
  "id": 1,
  "email": "user@fixgo.com",
  "name": "김병호",
  "role": "ROLE_ADMIN",
  "status": "PENDING"
}
```

### 1.3.4 회원 역할 변경

- 엔드포인트: `PATCH /api/v1/admin/member/role`
- 설명: 관리자가 회원의 역할을 변경
- 접근 권한: 관리자
- 필수 필드: email, role
- Request Body:

```json
{
  "email": "user@fixgo.com",
  "role": "ROLE_ADMIN"
}
```

- Response Body (HTTP 200):

```json
{
  "id": 1,
  "email": "user@fixgo.com",
  "name": "김병호",
  "role": "ROLE_ADMIN",
  "status": "PENDING"
}
```

### 1.3.5 회원 프로필 수정

- 엔드포인트: `PATCH /api/v1/admin/member/profile`
- 설명: 관리자가 회원 프로필 정보를 수정
- 접근 권한: 관리자
- 필수 필드: email, name, phone, address
- Request Body:

```json
{
  "email": "user@fixgo.com",
  "name": "김병호",
  "phone": "010-9876-5432",
  "address": "서울특별시 서초구 반포대로 45"
}
```

- Response Body (HTTP 200):

```json
{
  "id": 1,
  "email": "user@fixgo.com",
  "name": "김병호",
  "phone": "010-1234-5678",
  "address": "서울특별시 강남구 테헤란로 123",
  "role": "ROLE_ADMIN",
  "status": "PENDING"
}
```

## **1.4 Admin Member API**

### **1.4.1 직원 생성**

- **엔드포인트:** POST /api/v1/admin/member
- **설명:** 관리자가 신규 직원을 생성
- **접근 권한:** 관리자
- **필수 필드:** email, password, name, phone, address, role

Request Body:

```json
{
	"email": "user@fixgo.com",
	"password": "password123!",
	"name": "김병호",
	"phone": "010-1234-5678",
	"address": "서울특별시 강남구 테헤란로 123",
	"role": "ROLE_ADMIN"
}
```

Response Body (HTTP 201):

```json
{
	"id": 1,
	"email": "user@fixgo.com",
	"name": "김병호",
	"phone": "010-1234-5678",
	"address": "서울특별시 강남구 테헤란로 123",
	"role": "ROLE_ADMIN",
	"status": "PENDING"
}
```

실패 시 응답은 1.1.2 공통 에러 형식을 따른다.

### **1.4.2 직원 삭제**

- **엔드포인트:** DELETE /api/v1/admin/member
- **설명:** 관리자가 직원을 삭제(소프트 삭제)
- **접근 권한:** 관리자
- **Query Parameter:** memberId (Long, 필수)
- **Request Body:** 없음
- **Response Body:** 없음 (HTTP 200)

실패 시 응답은 1.1.2 공통 에러 형식을 따른다.

### **1.4.3 직원 상태 변경**

- **엔드포인트:** PATCH /api/v1/admin/member/status
- **설명:** 관리자가 직원의 상태를 변경
- **접근 권한:** 관리자
- **필수 필드:** email, status

Request Body:

```json
{
	"email": "user@fixgo.com",
	"status": "PENDING"
}
```

Response Body (HTTP 200):

```json
{
	"id": 1,
	"email": "user@fixgo.com",
	"name": "김병호",
	"role": "ROLE_ADMIN",
	"status": "PENDING"
}
```

실패 시 응답은 1.1.2 공통 에러 형식을 따른다.

### **1.4.4 직원 역할 변경**

- **엔드포인트:** PATCH /api/v1/admin/member/role
- **설명:** 관리자가 직원의 역할을 변경
- **접근 권한:** 관리자
- **필수 필드:** email, role

Request Body:

```json
{
	"email": "user@fixgo.com",
	"role": "ROLE_ADMIN"
}
```

Response Body (HTTP 200):

```json
{
	"id": 1,
	"email": "user@fixgo.com",
	"name": "김병호",
	"role": "ROLE_ADMIN",
	"status": "PENDING"
}
```

실패 시 응답은 1.1.2 공통 에러 형식을 따른다.

### **1.4.5 직원 프로필 수정**

- **엔드포인트:** PATCH /api/v1/admin/member/profile
- **설명:** 관리자가 직원 프로필 정보를 수정
- **접근 권한:** 관리자
- **필수 필드:** email, name, phone, address

Request Body:

```json
{
	"email": "user@fixgo.com",
	"name": "김병호",
	"phone": "010-9876-5432",
	"address": "서울특별시 서초구 반포대로 45"
}
```

Response Body (HTTP 200):

```json
{
	"id": 1,
	"email": "user@fixgo.com",
	"name": "김병호",
	"phone": "010-1234-5678",
	"address": "서울특별시 강남구 테헤란로 123",
	"role": "ROLE_ADMIN",
	"status": "PENDING"
}
```

실패 시 응답은 1.1.2 공통 에러 형식을 따른다.

## 1.5 AI API

### 1.5.1 BrunoTest용

- 엔드포인트: `POST /api/v1/ai/chat/test`
- 설명: 사용자의 채팅을 입력받아 그에 맞는 답을 반환
- 접근 권한: 관리자
- Request Body:

```json
{
  "text": "안녕"
}
```

- Response Body: 텍스트 (HTTP 200)

### 1.5.2 AI 스트리밍 채팅

- 엔드포인트: `POST /api/v1/ai/chat/stream`
- 설명: 사용자 입력을 받아 AI 응답을 스트리밍으로 반환한다.
- 접근 권한: 관리자
- Request Body:

```json
{
  "text": "안녕"
}
```

- Response Body: 스트리밍 텍스트 (HTTP 200)

### 1.5.3 채팅 이력 조회

- 엔드포인트: `GET /api/v1/ai`
- 설명: 로그인한 관리자의 AI 채팅 이력을 조회한다.
- 접근 권한: 관리자
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "memberId": "",
  "content": "",
  "type": "",
  "createdAt": "2026-11-13T10:00:00"
}
```

## 1.6 Auth API

### 1.6.1 토큰 재발급

- 엔드포인트: `POST /api/v1/auth/refresh`
- 설명: Refresh Token으로 Access Token을 재발급한다.
- 접근 권한: 전체 (비인증)
- Request Body:

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

- Response Body (HTTP 200):

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### 1.6.2 로그아웃

- 엔드포인트: `POST /api/v1/auth/logout`
- 설명: 현재 로그인 사용자의 Refresh Token을 무효화한다.
- 접근 권한: 로그인 사용자
- Request Body: 없음
- Response Body: 없음 (HTTP 200)

### 1.6.3 로그인

- 엔드포인트: `POST /api/v1/auth/login`
- 설명: 이메일과 비밀번호로 로그인하여 Access/Refresh 토큰을 발급한다.
- 접근 권한: 전체 (비인증)
- 필수 필드: email, password
- Request Body:

```json
{
  "email": "admin@fixgo.com",
  "password": "password123!"
}
```

- Response Body (HTTP 200):

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

## 1.7 Customer API

### 1.7.1 고객 목록/검색

- 엔드포인트: `GET /api/v1/customers`
- 설명: 조건이 없으면 전체 조회하며, id 내림차순으로 페이징한다.
- 접근 권한: 로그인 사용자
- Query Parameter: name (String, 선택, 예: 홍길동) — 검색할 고객 이름 / phone (String, 선택, 예: 010-1234-5678) — 검색할 고객 연락처 / email (String, 선택, 예: hong@example.com) — 검색할 고객 이메일 / page (int, 선택, 예: 0) — 페이지 번호. 0부터 시작한다. / size (int, 선택, 예: 20) — 페이지 크기 / sort (string, 선택) — 정렬. 예: createdAt,desc
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "content": [
    {
      "id": 1,
      "name": "홍길동",
      "phone": "010-1234-5678",
      "email": "hong@example.com",
      "zipcode": "06236",
      "basicAddress": "서울특별시 강남구 테헤란로 123",
      "detailAddress": "101동 202호",
      "createdAt": "2026-01-01T10:00:00",
      "updatedAt": "2026-01-02T10:00:00",
      "createdBy": 1,
      "updatedBy": 1
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false
}
```

### 1.7.2 고객 등록

- 엔드포인트: `POST /api/v1/customers`
- 설명: 고객 정보를 등록한다.
- 접근 권한: 로그인 사용자
- 필수 필드: name, phone, email
- Request Body:

```json
{
  "name": "홍길동",
  "phone": "010-1234-5678",
  "email": "hong@example.com",
  "zipcode": "06236",
  "basicAddress": "서울특별시 강남구 테헤란로 123",
  "detailAddress": "101동 202호"
}
```

- Response Body (HTTP 201):

```json
{
  "id": 1,
  "name": "홍길동",
  "phone": "010-1234-5678",
  "email": "hong@example.com",
  "zipcode": "06236",
  "basicAddress": "서울특별시 강남구 테헤란로 123",
  "detailAddress": "101동 202호",
  "createdAt": "2026-01-01T10:00:00",
  "updatedAt": "2026-01-02T10:00:00",
  "createdBy": 1,
  "updatedBy": 1
}
```

### 1.7.3 고객 일괄 등록

- 엔드포인트: `POST /api/v1/customers/bulk`
- 설명: 검증된 고객 정보를 일괄 등록한다.
- 접근 권한: 로그인 사용자
- Request Body:

```json
{
  "customers": [
    {
      "rowNumber": 2,
      "name": "홍길동",
      "phone": "010-1234-5678",
      "email": "hong@example.com",
      "zipcode": "06236",
      "basicAddress": "서울특별시 강남구 테헤란로 123",
      "detailAddress": "101동 202호"
    }
  ]
}
```

- Response Body (HTTP 200):

```json
{
  "successCount": 8,
  "failCount": 2,
  "failures": [
    {
      "rowNumber": 3,
      "reason": "연락처가 중복되었습니다."
    }
  ]
}
```

### 1.7.4 고객 일괄 등록 전 검증

- 엔드포인트: `POST /api/v1/customers/bulk/validate`
- 설명: 엑셀 파일의 고객 정보를 등록 전에 검증한다.
- 접근 권한: 로그인 사용자
- Request Body: multipart/form-data, file (필수)
- Response Body (HTTP 200):

```json
{
  "fileName": "customers.xlsx",
  "totalCount": 10,
  "validCount": 8,
  "invalidCount": 2,
  "rows": [
    {
      "rowNumber": 2,
      "name": "홍길동",
      "phone": "010-1234-5678",
      "email": "hong@example.com",
      "zipcode": "06236",
      "basicAddress": "서울특별시 강남구 테헤란로 123",
      "detailAddress": "101동 202호",
      "registrable": true,
      "reason": "연락처가 중복되었습니다."
    }
  ]
}
```

### 1.7.5 고객 단건 조회

- 엔드포인트: `GET /api/v1/customers/{id}`
- 설명: id로 CustomerDto.Response를 반환한다.
- 접근 권한: 로그인 사용자
- Path Variable: id (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "name": "홍길동",
  "phone": "010-1234-5678",
  "email": "hong@example.com",
  "zipcode": "06236",
  "basicAddress": "서울특별시 강남구 테헤란로 123",
  "detailAddress": "101동 202호",
  "createdAt": "2026-01-01T10:00:00",
  "updatedAt": "2026-01-02T10:00:00",
  "createdBy": 1,
  "updatedBy": 1
}
```

### 1.7.6 고객 정보 수정

- 엔드포인트: `PATCH /api/v1/customers/{id}`
- 설명: 고객 정보를 수정한다.
- 접근 권한: 로그인 사용자
- Path Variable: id (Long, 필수)
- 필수 필드: name, phone, email
- Request Body:

```json
{
  "name": "홍길동",
  "phone": "010-1234-5678",
  "email": "hong@example.com",
  "zipcode": "06236",
  "basicAddress": "서울특별시 강남구 테헤란로 123",
  "detailAddress": "101동 202호"
}
```

- Response Body (HTTP 200):

```json
{
  "id": 1,
  "name": "홍길동",
  "phone": "010-1234-5678",
  "email": "hong@example.com",
  "zipcode": "06236",
  "basicAddress": "서울특별시 강남구 테헤란로 123",
  "detailAddress": "101동 202호",
  "createdAt": "2026-01-01T10:00:00",
  "updatedAt": "2026-01-02T10:00:00",
  "createdBy": 1,
  "updatedBy": 1
}
```

### 1.7.7 연락처 중복 체크

- 엔드포인트: `GET /api/v1/customers/check-duplicate`
- 설명: 연락처 중복 여부를 확인한다.
- 접근 권한: 로그인 사용자
- Query Parameter: phone (String, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "duplicated": true
}
```

### 1.7.8 엑셀 양식 다운로드

- 엔드포인트: `GET /api/v1/customers/bulk/template`
- 설명: 고객 일괄 등록용 엑셀 파일 양식을 다운로드한다.
- 접근 권한: 로그인 사용자
- Request Body: 없음
- Response Body: 파일 (HTTP 200)

## 1.8 Email Preset API

### 1.8.1 이메일 프리셋 수정

- 엔드포인트: `PUT /api/v1/email-presets/{id}`
- 설명: 이메일 프리셋을 수정한다.
- 접근 권한: 관리자, 접수담당자
- Path Variable: id (Long, 필수)
- 필수 필드: name, messageTitle, message
- Request Body:

```json
{
  "name": "수리 완료 안내",
  "messageTitle": "수리가 완료되었습니다",
  "message": "고객님의 제품 수리가 완료되었습니다."
}
```

- Response Body (HTTP 200):

```json
{
  "id": 1,
  "name": "수리 완료 안내",
  "messageTitle": "수리가 완료되었습니다",
  "message": "고객님의 제품 수리가 완료되었습니다."
}
```

### 1.8.2 이메일 프리셋 삭제

- 엔드포인트: `DELETE /api/v1/email-presets/{id}`
- 설명: 이메일 프리셋을 삭제한다.
- 접근 권한: 관리자, 접수담당자
- Path Variable: id (Long, 필수)
- Request Body: 없음
- Response Body: 없음 (HTTP 200)

### 1.8.3 이메일 프리셋 목록 조회

- 엔드포인트: `GET /api/v1/email-presets`
- 설명: 이메일 프리셋 목록을 조회한다.
- 접근 권한: 관리자, 접수담당자
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "name": "수리 완료 안내",
  "messageTitle": "수리가 완료되었습니다",
  "message": "고객님의 제품 수리가 완료되었습니다."
}
```

### 1.8.4 이메일 프리셋 등록

- 엔드포인트: `POST /api/v1/email-presets`
- 설명: 이메일 프리셋을 등록한다.
- 접근 권한: 관리자, 접수담당자
- 필수 필드: name, messageTitle, message
- Request Body:

```json
{
  "name": "수리 완료 안내",
  "messageTitle": "수리가 완료되었습니다",
  "message": "고객님의 제품 수리가 완료되었습니다."
}
```

- Response Body (HTTP 201):

```json
{
  "id": 1,
  "name": "수리 완료 안내",
  "messageTitle": "수리가 완료되었습니다",
  "message": "고객님의 제품 수리가 완료되었습니다."
}
```

## 1.9 Labor Rate API

### 1.9.1 공임비·점검비 전체 목록 조회

- 엔드포인트: `GET /api/v1/labor-rates`
- 설명: 등록된 공임비·점검비 전체 목록을 한 번에 조회한다.
- 접근 권한: 로그인 사용자
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "productCategory": "LAPTOP",
  "cost": 1,
  "rateType": "INSPECTION_COST",
  "effectiveDate": "2026-11-13",
  "createdAt": "2026-11-13T10:00:00",
  "updatedAt": "2026-11-13T10:00:00",
  "createdBy": 1,
  "updatedBy": 1
}
```

### 1.9.2 공임비·점검비 수정

- 엔드포인트: `PATCH /api/v1/labor-rates`
- 설명: 관리자가 기존 공임비 또는 점검비 금액을 수정한다. 적용일은 수정 당일로 자동 반영된다.
- 접근 권한: 관리자
- 필수 필드: id, cost
- Request Body:

```json
{
  "id": 1,
  "cost": 1
}
```

- Response Body (HTTP 200):

```json
{
  "id": 1,
  "productCategory": "LAPTOP",
  "cost": 1,
  "rateType": "INSPECTION_COST",
  "effectiveDate": "2026-11-13",
  "createdAt": "2026-11-13T10:00:00",
  "updatedAt": "2026-11-13T10:00:00",
  "createdBy": 1,
  "updatedBy": 1
}
```

### 1.9.3 공임비·점검비 상세 조회

- 엔드포인트: `GET /api/v1/labor-rates/{id}`
- 설명: 요금 ID로 상세 정보를 조회한다.
- 접근 권한: 로그인 사용자
- Path Variable: id (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "productCategory": "LAPTOP",
  "cost": 1,
  "rateType": "INSPECTION_COST",
  "effectiveDate": "2026-11-13",
  "createdAt": "2026-11-13T10:00:00",
  "updatedAt": "2026-11-13T10:00:00",
  "createdBy": 1,
  "updatedBy": 1
}
```

## 1.10 Long Pending Statistics API

### 1.10.1 장기 미처리 접수건 조회

- 엔드포인트: `GET /api/v1/admin/statistics/long_pending`
- 설명: 상태 변경 후 14일이 경과한 장기 미처리 접수건을 조회한다.
- 접근 권한: 관리자
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "receptionId": 1,
  "receptionNumber": "",
  "customerName": "",
  "modelName": "",
  "status": "RECEIVED",
  "technicianName": "",
  "elapsedDays": 1,
  "lastActivityAt": "2026-11-13T10:00:00"
}
```

## 1.11 Member API

### 1.11.1 비밀번호 변경

- 엔드포인트: `PATCH /api/v1/member/me/password`
- 설명: 로그인한 직원의 비밀번호를 변경
- 접근 권한: 로그인 사용자
- 필수 필드: email, password
- Request Body:

```json
{
  "email": "user@fixgo.com",
  "password": "newPassword123!"
}
```

- Response Body: 없음 (HTTP 200)

### 1.11.2 직원 목록 조회

- 엔드포인트: `GET /api/v1/member`
- 설명: 검색 조건에 맞는 직원 목록을 페이징하여 반환
- 접근 권한: 로그인 사용자
- Query Parameter: keyword (String, 선택, 예: 김병호) — 검색 키워드(이름/이메일) / role (Role, 선택, 예: TECHNICIAN) — 역할 필터 / status (Status, 선택, 예: ACTIVE) — 상태 필터 / page (int, 선택, 예: 0) — 페이지 번호. 0부터 시작한다. / size (int, 선택, 예: 20) — 페이지 크기 / sort (string, 선택) — 정렬. 예: createdAt,desc
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "content": [
    {
      "id": 1,
      "email": "user@fixgo.com",
      "name": "김병호",
      "role": "ROLE_ADMIN",
      "status": "PENDING"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false
}
```

### 1.11.3 직원 상세 조회

- 엔드포인트: `GET /api/v1/member/{memberId}`
- 설명: 회원 ID로 상세 프로필 정보를 조회
- 접근 권한: 로그인 사용자
- Path Variable: memberId (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "email": "user@fixgo.com",
  "name": "김병호",
  "phone": "010-1234-5678",
  "address": "서울특별시 강남구 테헤란로 123",
  "role": "ROLE_ADMIN",
  "status": "PENDING"
}
```

### 1.11.4 배정 가능 기술자 목록 조회

- 엔드포인트: `GET /api/v1/member/technicians`
- 설명: 접수원/관리자가 배정 가능한 기술자 목록을 조회
- 접근 권한: 접수담당자, 관리자
- Query Parameter: keyword (String, 선택) / sortBy (MemberDto.TechnicianSort, 선택, 예: NAME) / direction (Sort.Direction, 선택, 예: ASC) / page (int, 선택, 예: 0) — 페이지 번호. 0부터 시작한다. / size (int, 선택, 예: 20) — 페이지 크기 / sort (string, 선택) — 정렬. 예: createdAt,desc
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "content": [
    {
      "technicianId": 3,
      "name": "이수리",
      "email": "tech@fixgo.com",
      "status": "PENDING",
      "inspectionWaitingCount": 2,
      "repairInProgressCount": 1,
      "estimatedDurationMinutes": 90
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false
}
```

### 1.11.5 내 프로필 조회

- 엔드포인트: `GET /api/v1/member/me`
- 설명: 로그인한 회원의 상세 프로필 정보를 반환
- 접근 권한: 로그인 사용자
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "email": "user@fixgo.com",
  "name": "김병호",
  "phone": "010-1234-5678",
  "address": "서울특별시 강남구 테헤란로 123",
  "role": "ROLE_ADMIN",
  "status": "PENDING"
}
```

### 1.11.6 삭제된 회원 목록 조회

- 엔드포인트: `GET /api/v1/member/deleted_member`
- 설명: 관리자가 삭제된 회원 목록을 페이징하여 조회
- 접근 권한: 관리자
- Query Parameter: page (int, 선택, 예: 0) — 페이지 번호. 0부터 시작한다. / size (int, 선택, 예: 20) — 페이지 크기 / sort (string, 선택) — 정렬. 예: createdAt,desc
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "content": [
    {
      "id": 1,
      "email": "user@fixgo.com",
      "name": "김병호",
      "role": "ROLE_ADMIN",
      "status": "PENDING"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false
}
```

## 1.12 Model Statistics API

### 1.12.1 모델별 수리 통계 조회

- 엔드포인트: `GET /api/v1/admin/statistics/models`
- 설명: 기간별 모델별 수리 통계를 조회한다.
- 접근 권한: 관리자
- Query Parameter: startDate (LocalDate, 필수) / endDate (LocalDate, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "modelId": 1,
  "modelName": "",
  "productGroup": "",
  "receptionCount": 1,
  "completedCount": 1,
  "inProgressCount": 1,
  "mainSymptom": ""
}
```

### 1.12.2 모델 증상별 통계 조회

- 엔드포인트: `GET /api/v1/admin/statistics/models/{modelId}/symptoms`
- 설명: 특정 모델의 증상별 통계를 조회한다.
- 접근 권한: 관리자
- Path Variable: modelId (Long, 필수)
- Query Parameter: startDate (LocalDate, 필수) / endDate (LocalDate, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "symptomName": "",
  "count": 1,
  "percentage": 1
}
```

## 1.13 Notification API

### 1.13.1 이메일 발송 내역 조회

- 엔드포인트: `GET /api/v1/notifications/emails`
- 설명: 이메일 발송 내역을 조건으로 페이징 조회한다.
- 접근 권한: 관리자, 접수담당자
- Query Parameter: keyword (String, 선택) / startDate (LocalDate, 선택) / endDate (LocalDate, 선택) / page (int, 선택, 예: 0) — 페이지 번호. 0부터 시작한다. / size (int, 선택, 예: 20) — 페이지 크기 / sort (string, 선택) — 정렬. 예: createdAt,desc
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "content": [
    {
      "id": 1,
      "customerName": "홍길동",
      "recipientEmail": "customer@example.com",
      "notificationType": "RECEIVED",
      "messageTitle": "수리 완료 안내",
      "status": "PENDING",
      "sentAt": "2024-01-01T12:00:00",
      "createdAt": "2024-01-01T11:59:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false
}
```

### 1.13.2 이메일 발송

- 엔드포인트: `POST /api/v1/notifications/emails`
- 설명: 이메일을 발송한다.
- 접근 권한: 관리자, 접수담당자
- Request Body:

```json
{
  "asReceptionId": 1,
  "recipientEmail": "customer@example.com",
  "messageTitle": "수리 완료 안내",
  "message": "수리가 완료되었습니다.",
  "notificationType": "RECEIVED"
}
```

- Response Body (HTTP 200):

```json
{
  "id": 1,
  "customerName": "홍길동",
  "recipientEmail": "customer@example.com",
  "notificationType": "RECEIVED",
  "messageTitle": "수리 완료 안내",
  "status": "PENDING",
  "sentAt": "2024-01-01T12:00:00",
  "createdAt": "2024-01-01T11:59:00"
}
```

### 1.13.3 실패 이메일 재발송

- 엔드포인트: `POST /api/v1/notifications/emails/{id}/retry`
- 설명: 실패한 이메일을 재발송한다.
- 접근 권한: 관리자, 접수담당자
- Path Variable: id (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "customerName": "홍길동",
  "recipientEmail": "customer@example.com",
  "notificationType": "RECEIVED",
  "messageTitle": "수리 완료 안내",
  "status": "PENDING",
  "sentAt": "2024-01-01T12:00:00",
  "createdAt": "2024-01-01T11:59:00"
}
```

### 1.13.4 이메일 발송 통계 조회

- 엔드포인트: `GET /api/v1/notifications/statistics`
- 설명: 이메일 발송 통계를 조회한다.
- 접근 권한: 관리자, 접수담당자
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "total": 100,
  "success": 95,
  "failed": 5
}
```

### 1.13.5 이메일 발송 상세 조회

- 엔드포인트: `GET /api/v1/notifications/emails/{id}`
- 설명: 이메일 발송 상세 정보를 조회한다.
- 접근 권한: 관리자, 접수담당자
- Path Variable: id (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "asReceptionId": 1,
  "receptionNumber": "AS-20240101-001",
  "customerName": "홍길동",
  "eventKey": "repair-complete-1",
  "notificationType": "RECEIVED",
  "recipientEmail": "customer@example.com",
  "messageTitle": "수리 완료 안내",
  "message": "수리가 완료되었습니다.",
  "notificationStatus": "PENDING",
  "retryCount": 0,
  "errorMessage": "null",
  "sentAt": "2024-01-01T12:00:00",
  "createdAt": "2024-01-01T11:59:00"
}
```

## 1.14 Part API

### 1.14.1 부품 단가 수정

- 엔드포인트: `PATCH /api/v1/part/unit_price`
- 설명: 관리자가 부품 단가를 수정한다.
- 접근 권한: 관리자
- 필수 필드: unitPrice
- Request Body:

```json
{
  "id": 1,
  "unitPrice": 55000
}
```

- Response Body: 없음 (HTTP 200)

### 1.14.2 재고 수량 다건 수정

- 엔드포인트: `PATCH /api/v1/part/stocks`
- 설명: 수리 ID에 연결된 사용 부품들의 재고 수량을 일괄 반영한다.
- 접근 권한: 로그인 사용자
- Query Parameter: repairId (Long, 필수)
- Request Body: 없음
- Response Body: 없음 (HTTP 200)

### 1.14.3 재고 수량 단건 수정

- 엔드포인트: `PATCH /api/v1/part/stock`
- 설명: 단일 부품의 재고 수량을 입출고 처리한다.
- 접근 권한: 로그인 사용자
- 필수 필드: quantity
- Request Body:

```json
{
  "id": 1,
  "movementType": "OUT",
  "quantity": 2,
  "reason": "수리 사용"
}
```

- Response Body: 없음 (HTTP 200)

### 1.14.4 표준 작업시간 수정

- 엔드포인트: `PATCH /api/v1/part/standard_minutes`
- 설명: 관리자가 부품 표준 작업시간(분)을 수정한다.
- 접근 권한: 관리자
- 필수 필드: standardMinutes
- Request Body:

```json
{
  "id": 1,
  "standardMinutes": 90
}
```

- Response Body: 없음 (HTTP 200)

### 1.14.5 부품 목록 조회

- 엔드포인트: `GET /api/v1/part`
- 설명: 검색 조건에 맞는 부품 목록을 페이징하여 반환한다.
- 접근 권한: 로그인 사용자
- Query Parameter: partName (String, 선택, 예: 메인보드) — 부품명 검색어 / status (PartStatus, 선택, 예: ACTIVE) — 부품 상태 / category (Category, 선택, 예: LAPTOP) — 호환 제품군 / productModelId (Long, 선택, 예: 1) — 호환 제품 모델 ID / page (int, 선택, 예: 0) — 페이지 번호. 0부터 시작한다. / size (int, 선택, 예: 20) — 페이지 크기 / sort (string, 선택) — 정렬. 예: createdAt,desc
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "content": [
    {
      "id": 1,
      "name": "메인보드",
      "unitPrice": 50000,
      "stockQuantity": 20
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false
}
```

### 1.14.6 부품 상세 조회

- 엔드포인트: `GET /api/v1/part/{partId}`
- 설명: 부품 ID로 상세 정보를 조회한다.
- 접근 권한: 로그인 사용자
- Path Variable: partId (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "name": "메인보드",
  "unitPrice": 50000,
  "standardMinutes": 60,
  "stockQuantity": 20,
  "status": "ACTIVE",
  "version": 1
}
```

## 1.15 Payment API

### 1.15.1 결제 환불

- 엔드포인트: `POST /api/v1/payments/{billId}/refund`
- 설명: 결제 완료된 청구서를 전액 환불한다.
- 접근 권한: 접수담당자, 관리자
- Path Variable: billId (Long, 필수)
- 필수 필드: cancelReason
- Request Body:

```json
{
  "cancelReason": "고객 요청",
  "refundAccount": {
    "bank": "88",
    "accountNumber": "110123456789",
    "holderName": "홍길동"
  }
}
```

- Response Body: 없음 (HTTP 200)

### 1.15.2 현금 결제

- 엔드포인트: `POST /api/v1/payments/{billId}/cash`
- 설명: 현장 현금 결제를 처리한다. (직원 전용)
- 접근 권한: 접수담당자, 관리자
- Path Variable: billId (Long, 필수)
- Request Body: 없음
- Response Body: 없음 (HTTP 204)

### 1.15.3 토스 웹훅 처리

- 엔드포인트: `POST /api/v1/payments/webhook`
- 설명: 토스에서 보내는 웹훅을 처리한다.
- 접근 권한: 전체 (비인증)
- Request Body:

```json
{
  "createdAt": "2026-11-13T10:00:00",
  "secret": "4_SSzEfbIoCrMGsTwve2t",
  "status": "DONE",
  "orderId": "PAY-asdw125-sad1w5-dsadqs21"
}
```

- Response Body: 없음 (HTTP 200)

### 1.15.4 결제 승인

- 엔드포인트: `POST /api/v1/payments/confirm`
- 설명: 토스 결제창 성공 후 전달된 값으로 결제를 승인한다.
- 접근 권한: 전체 (비인증)
- 필수 필드: orderId, paymentKey, amount
- Request Body:

```json
{
  "orderId": "ORDER-20240101-001",
  "paymentKey": "tgen_xxxxxxxx",
  "amount": 50000
}
```

- Response Body: 없음 (HTTP 204)

### 1.15.5 결제 목록 조회

- 엔드포인트: `GET /api/v1/payments`
- 설명: 키워드·상태 조건으로 결제 목록을 페이징 조회한다.
- 접근 권한: 접수담당자, 관리자, 수리기사
- Query Parameter: keyword (String, 선택) / status (PaymentBillStatus, 선택) / page (int, 선택, 예: 0) — 페이지 번호. 0부터 시작한다. / size (int, 선택, 예: 20) — 페이지 크기 / sort (string, 선택) — 정렬. 예: createdAt,desc
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "content": [
    {
      "billId": 1,
      "asReceptionNumber": "AS-20240101-001",
      "customerName": "홍길동",
      "amount": 50000,
      "status": "PAID",
      "paymentMethod": "CARD",
      "approvedAt": "2024-01-01T12:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false
}
```

### 1.15.6 결제 상세 조회

- 엔드포인트: `GET /api/v1/payments/{billId}`
- 설명: 청구서 ID로 결제 상세 정보를 조회한다.
- 접근 권한: 접수담당자, 관리자, 수리기사
- Path Variable: billId (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "billId": 1,
  "reception": {
    "asReceptionNumber": "AS-20240101-001",
    "customerName": "홍길동",
    "productName": "갤럭시 S24",
    "symptom": "액정 파손",
    "symptomDetail": "화면 좌측 하단 깨짐"
  },
  "amount": 50000,
  "status": "PAID",
  "paymentStatus": "DONE",
  "paymentMethod": "CARD",
  "virtualAccount": {
    "bankCode": "88",
    "accountNumber": "56201234567890",
    "dueAt": "2024-01-02T23:59:59"
  },
  "paymentHistory": {
    "attemptId": 1,
    "orderId": "ORDER-20240101-001",
    "amount": 50000,
    "paymentMethod": "CARD",
    "approvedAt": "2024-01-01T12:00:00"
  },
  "refundHistory": {
    "refundId": 1,
    "amount": 50000,
    "cancelReason": "고객 요청",
    "status": "DONE",
    "canceledAt": "2024-01-03T15:00:00"
  },
  "refundable": true
}
```

## 1.16 Payment Statistics API

### 1.16.1 미입금 청구서 조회

- 엔드포인트: `GET /api/v1/admin/statistics/unpaid-bills`
- 설명: 미입금 청구서 목록을 조회한다.
- 접근 권한: 관리자
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "billId": 1,
  "receptionNumber": "",
  "customerName": "",
  "modelName": "",
  "amount": 1,
  "billedAt": "2026-11-13T10:00:00",
  "status": "UNPAID"
}
```

### 1.16.2 결제 통계 조회

- 엔드포인트: `GET /api/v1/admin/statistics/payments`
- 설명: 기간별 결제 통계를 조회한다.
- 접근 권한: 관리자
- Query Parameter: startDate (LocalDate, 필수) / endDate (LocalDate, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "startDate": "2026-11-13",
  "endDate": "2026-11-13",
  "totalPaidAmount": 1,
  "totalRefundAmount": 1,
  "netCollectedAmount": 1,
  "unpaidCount": 1
}
```

### 1.16.3 기간 결제 내역 조회

- 엔드포인트: `GET /api/v1/admin/statistics/payments/history`
- 설명: 조회 기간의 입금, 미입금, 환불 내역을 조회한다.
- 접근 권한: 관리자
- Query Parameter: startDate (LocalDate, 필수) / endDate (LocalDate, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "billId": 1,
  "receptionNumber": "",
  "customerName": "",
  "amount": 1,
  "status": "UNPAID",
  "occurredAt": "2026-11-13T10:00:00"
}
```

## 1.17 Portal API

### 1.17.1 견적 거부

- 엔드포인트: `POST /api/v1/portal/{token}/quote/reject`
- 설명: 견적을 거부한다. 점검비 청구서가 발행되고 결제 대기 상태가 된다.
- 접근 권한: 전체 (비인증)
- Path Variable: token (String, 필수)
- Request Body: 없음
- Response Body: 없음 (HTTP 204)

### 1.17.2 견적 승인

- 엔드포인트: `POST /api/v1/portal/{token}/quote/approve`
- 설명: 견적을 승인한다. 이후 수리 대기 상태가 된다.
- 접근 권한: 전체 (비인증)
- Path Variable: token (String, 필수)
- Request Body: 없음
- Response Body: 없음 (HTTP 204)

### 1.17.3 결제 시작

- 엔드포인트: `POST /api/v1/portal/{token}/payments/start`
- 설명: 결제를 시작하고, 프론트가 토스 결제창을 띄우는 데 필요한 값을 반환한다.
- 접근 권한: 전체 (비인증)
- Path Variable: token (String, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "orderId": "ORDER-20240101-001",
  "amount": 50000,
  "orderName": "수리비"
}
```

### 1.17.4 가상계좌 취소

- 엔드포인트: `POST /api/v1/portal/{token}/payments/cancel`
- 설명: 발급된 가상계좌를 취소한다.
- 접근 권한: 전체 (비인증)
- Path Variable: token (String, 필수)
- Request Body: 없음
- Response Body: 없음 (HTTP 204)

### 1.17.5 포탈 조회

- 엔드포인트: `GET /api/v1/portal/{token}`
- 설명: 고객 포탈 화면 데이터(접수, 견적, 결제)를 조회한다.
- 접근 권한: 전체 (비인증)
- Path Variable: token (String, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "reception": {
    "receptionNumber": "AS-251111-abc",
    "receivedAt": "2025-11-11T09:00:00",
    "customerName": "홍길동",
    "customerPhone": "010-1234-5678",
    "modelName": "FixGo Washer X1",
    "serialNumber": "SN-123456",
    "symptom": "전원 불량",
    "symptomDetail": "전원 버튼을 눌러도 켜지지 않음",
    "status": "RECEIVED",
    "histories": [
      {
        "status": "RECEIVED",
        "changedAt": "2025-11-11T09:00:00"
      }
    ]
  },
  "quote": {
    "status": "PENDING",
    "diagnosis": "",
    "items": [
      {
        "name": "",
        "quantity": 1,
        "unitPrice": 1,
        "amount": 1
      }
    ],
    "laborCost": 1,
    "inspectionCost": 1,
    "totalAmount": 1,
    "issuedAt": "2026-11-13T10:00:00"
  },
  "payment": {
    "status": "PENDING",
    "orderName": "수리비",
    "amount": 50000,
    "paymentMethod": "CARD",
    "paidAt": "2024-01-01T12:00:00",
    "virtualAccount": {
      "bankName": "신한",
      "accountNumber": "56201234567890",
      "dueAt": "2024-01-02T23:59:59"
    }
  }
}
```

## 1.18 Product API

### 1.18.1 시리얼번호 검색

- 엔드포인트: `GET /api/v1/products`
- 설명: 시리얼번호로 수리 대상 제품을 검색한다.
- 접근 권한: 로그인 사용자
- Query Parameter: serialNumber (String, 선택)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "serialNumber": "SN-ABC-12345",
  "manufacturedAt": "2025-01-01T00:00:00",
  "purchasedAt": "2025-06-01T00:00:00",
  "customerId": 1,
  "productModelId": 1,
  "name": "갤럭시북",
  "modelName": "NT750XDA",
  "category": "LAPTOP",
  "warrantyMonths": 24,
  "createdAt": "2026-01-01T10:00:00",
  "updatedAt": "2026-01-02T10:00:00",
  "createdBy": 1,
  "updatedBy": 1
}
```

### 1.18.2 수리 대상 제품 정보 등록

- 엔드포인트: `POST /api/v1/products`
- 설명: 수리 대상 제품 정보를 등록한다.
- 접근 권한: 로그인 사용자
- 필수 필드: productModelId, serialNumber, manufacturedAt
- Request Body:

```json
{
  "productModelId": 1,
  "customerId": 1,
  "serialNumber": "SN-ABC-12345",
  "manufacturedAt": "2025-01-01T00:00:00",
  "purchasedAt": "2025-06-01T00:00:00"
}
```

- Response Body (HTTP 201):

```json
{
  "id": 1,
  "serialNumber": "SN-ABC-12345",
  "manufacturedAt": "2025-01-01T00:00:00",
  "purchasedAt": "2025-06-01T00:00:00",
  "customerId": 1,
  "productModelId": 1,
  "name": "갤럭시북",
  "modelName": "NT750XDA",
  "category": "LAPTOP",
  "warrantyMonths": 24,
  "createdAt": "2026-01-01T10:00:00",
  "updatedAt": "2026-01-02T10:00:00",
  "createdBy": 1,
  "updatedBy": 1
}
```

### 1.18.3 수리 대상 제품 정보 수정

- 엔드포인트: `PATCH /api/v1/products/{id}/purchased_at`
- 설명: 수리 대상 제품 정보를 수정한다.
- 접근 권한: 로그인 사용자
- Path Variable: id (Long, 필수)
- 필수 필드: productModelId
- Request Body:

```json
{
  "productModelId": 1,
  "purchasedAt": "2025-06-01T00:00:00"
}
```

- Response Body (HTTP 200):

```json
{
  "id": 1,
  "serialNumber": "SN-ABC-12345",
  "manufacturedAt": "2025-01-01T00:00:00",
  "purchasedAt": "2025-06-01T00:00:00",
  "customerId": 1,
  "productModelId": 1,
  "name": "갤럭시북",
  "modelName": "NT750XDA",
  "category": "LAPTOP",
  "warrantyMonths": 24,
  "createdAt": "2026-01-01T10:00:00",
  "updatedAt": "2026-01-02T10:00:00",
  "createdBy": 1,
  "updatedBy": 1
}
```

### 1.18.4 수리 대상 제품 정보 조회

- 엔드포인트: `GET /api/v1/products/{id}`
- 설명: 제품 ID로 수리 대상 제품 정보를 조회한다.
- 접근 권한: 로그인 사용자
- Path Variable: id (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "serialNumber": "SN-ABC-12345",
  "manufacturedAt": "2025-01-01T00:00:00",
  "purchasedAt": "2025-06-01T00:00:00",
  "customerId": 1,
  "productModelId": 1,
  "name": "갤럭시북",
  "modelName": "NT750XDA",
  "category": "LAPTOP",
  "warrantyMonths": 24,
  "createdAt": "2026-01-01T10:00:00",
  "updatedAt": "2026-01-02T10:00:00",
  "createdBy": 1,
  "updatedBy": 1
}
```

### 1.18.5 제품 페이징 검색

- 엔드포인트: `GET /api/v1/products/search`
- 설명: 시리얼번호 조건으로 제품을 페이징 검색한다. 시리얼번호가 없으면 전체를 페이징한다.
- 접근 권한: 로그인 사용자
- Query Parameter: serialNumber (String, 선택, 예: SN-ABC-12345) — 검색할 시리얼 번호 / page (int, 선택, 예: 0) — 페이지 번호. 0부터 시작한다. / size (int, 선택, 예: 20) — 페이지 크기 / sort (string, 선택) — 정렬. 예: createdAt,desc
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "content": [
    {
      "id": 1,
      "serialNumber": "SN-ABC-12345",
      "manufacturedAt": "2025-01-01T00:00:00",
      "purchasedAt": "2025-06-01T00:00:00",
      "customerId": 1,
      "productModelId": 1,
      "name": "갤럭시북",
      "modelName": "NT750XDA",
      "category": "LAPTOP",
      "warrantyMonths": 24,
      "createdAt": "2026-01-01T10:00:00",
      "updatedAt": "2026-01-02T10:00:00",
      "createdBy": 1,
      "updatedBy": 1
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false
}
```

## 1.19 Product Model API

### 1.19.1 제품 모델 등록

- 엔드포인트: `POST /api/v1/product-models`
- 설명: 제품 모델을 등록한다.
- 접근 권한: 로그인 사용자
- 필수 필드: name, modelName, category, warrantyMonths
- Request Body:

```json
{
  "name": "갤럭시북",
  "modelName": "NT750XDA",
  "category": "LAPTOP",
  "warrantyMonths": 24
}
```

- Response Body (HTTP 201):

```json
{
  "id": 1,
  "name": "갤럭시북",
  "modelName": "NT750XDA",
  "category": "LAPTOP",
  "warrantyMonths": 24,
  "isActive": true,
  "createdAt": "2026-01-01T10:00:00",
  "updatedAt": "2026-01-02T10:00:00",
  "createdBy": 1,
  "updatedBy": 1
}
```

### 1.19.2 제품 모델 단건 조회

- 엔드포인트: `GET /api/v1/product-models/{id}`
- 설명: id로 ProductModelDto.Response를 반환한다.
- 접근 권한: 로그인 사용자
- Path Variable: id (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "name": "갤럭시북",
  "modelName": "NT750XDA",
  "category": "LAPTOP",
  "warrantyMonths": 24,
  "isActive": true,
  "createdAt": "2026-01-01T10:00:00",
  "updatedAt": "2026-01-02T10:00:00",
  "createdBy": 1,
  "updatedBy": 1
}
```

### 1.19.3 제품 모델 수정

- 엔드포인트: `PATCH /api/v1/product-models/{id}`
- 설명: 제품 모델 정보를 수정한다.
- 접근 권한: 로그인 사용자
- Path Variable: id (Long, 필수)
- 필수 필드: name, modelName, category, warrantyMonths, isActive
- Request Body:

```json
{
  "name": "갤럭시북",
  "modelName": "NT750XDA",
  "category": "LAPTOP",
  "warrantyMonths": 24,
  "isActive": true
}
```

- Response Body (HTTP 200):

```json
{
  "id": 1,
  "name": "갤럭시북",
  "modelName": "NT750XDA",
  "category": "LAPTOP",
  "warrantyMonths": 24,
  "isActive": true,
  "createdAt": "2026-01-01T10:00:00",
  "updatedAt": "2026-01-02T10:00:00",
  "createdBy": 1,
  "updatedBy": 1
}
```

### 1.19.4 제품 모델 목록 검색

- 엔드포인트: `GET /api/v1/product-models/list`
- 설명: 키워드로 제품 모델을 검색하고 페이징한다.
- 접근 권한: 로그인 사용자
- Query Parameter: keyword (String, 선택) / page (int, 선택, 예: 0) — 페이지 번호. 0부터 시작한다. / size (int, 선택, 예: 20) — 페이지 크기 / sort (string, 선택) — 정렬. 예: createdAt,desc
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "content": [
    {
      "id": 1,
      "name": "갤럭시북",
      "modelName": "NT750XDA",
      "category": "LAPTOP",
      "warrantyMonths": 24,
      "isActive": true,
      "createdAt": "2026-01-01T10:00:00",
      "updatedAt": "2026-01-02T10:00:00",
      "createdBy": 1,
      "updatedBy": 1
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false
}
```

## 1.20 Reception Statistics API

### 1.20.1 접수 통계 조회

- 엔드포인트: `GET /api/v1/admin/statistics/receptions`
- 설명: 기간별 접수·완료 건수와 완료율을 조회한다.
- 접근 권한: 관리자
- Query Parameter: startDate (LocalDate, 필수) / endDate (LocalDate, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "startDate": "2026-11-13",
  "endDate": "2026-11-13",
  "receivedCount": 1,
  "completedCount": 1,
  "completionRate": 1
}
```

## 1.21 Repair API

### 1.21.1 수리 상세 조회

- 엔드포인트: `GET /api/v1/as/repairs/{id}`
- 설명: 수리 ID로 수리 상세 정보를 조회한다.
- 접근 권한: 로그인 사용자
- Path Variable: id (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "asReceptionId": 1,
  "technicianId": 3,
  "diagnosis": "메인보드 전원부 고장",
  "workNote": "메인보드 교체",
  "repairDecision": "REPAIRABLE",
  "warrantyResult": "IN_WARRANTY",
  "reason": "부품 노후",
  "laborRate": 30000,
  "inspectionCost": 10000,
  "finalAmount": 85000,
  "status": "WAITING",
  "reasonForRejection": "견적 미승인",
  "confirmedAt": "2025-11-11T12:00:00",
  "createdAt": "2025-11-11T09:00:00",
  "updatedAt": "2025-11-11T10:00:00",
  "createdBy": 2,
  "updatedBy": 3
}
```

### 1.21.2 수리 정보 수정

- 엔드포인트: `PUT /api/v1/as/repairs/{id}`
- 설명: 수리 정보를 수정한다.
- 접근 권한: 로그인 사용자
- Path Variable: id (Long, 필수)
- 필수 필드: asReceptionId, technicianId, warrantyResult, laborRate, inspectionCost, finalAmount, status
- Request Body:

```json
{
  "asReceptionId": 1,
  "technicianId": 3,
  "diagnosis": "메인보드 전원부 고장",
  "workNote": "메인보드 교체",
  "repairDecision": "REPAIRABLE",
  "warrantyResult": "IN_WARRANTY",
  "reason": "부품 노후",
  "laborRate": 30000,
  "inspectionCost": 10000,
  "finalAmount": 85000,
  "status": "WAITING",
  "reasonForRejection": "견적 미승인"
}
```

- Response Body (HTTP 200):

```json
{
  "id": 1,
  "asReceptionId": 1,
  "technicianId": 3,
  "diagnosis": "메인보드 전원부 고장",
  "workNote": "메인보드 교체",
  "repairDecision": "REPAIRABLE",
  "warrantyResult": "IN_WARRANTY",
  "reason": "부품 노후",
  "laborRate": 30000,
  "inspectionCost": 10000,
  "finalAmount": 85000,
  "status": "WAITING",
  "reasonForRejection": "견적 미승인",
  "confirmedAt": "2025-11-11T12:00:00",
  "createdAt": "2025-11-11T09:00:00",
  "updatedAt": "2025-11-11T10:00:00",
  "createdBy": 2,
  "updatedBy": 3
}
```

### 1.21.3 수리 삭제

- 엔드포인트: `DELETE /api/v1/as/repairs/{id}`
- 설명: 수리 정보를 삭제한다.
- 접근 권한: 로그인 사용자
- Path Variable: id (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "message": "수리가 시작되었습니다."
}
```

### 1.21.4 전체 수리 목록 조회

- 엔드포인트: `GET /api/v1/as/repairs`
- 설명: 등록된 전체 수리 목록을 조회한다.
- 접근 권한: 로그인 사용자
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "asReceptionId": 1,
  "technicianId": 3,
  "diagnosis": "메인보드 전원부 고장",
  "workNote": "메인보드 교체",
  "repairDecision": "REPAIRABLE",
  "warrantyResult": "IN_WARRANTY",
  "reason": "부품 노후",
  "laborRate": 30000,
  "inspectionCost": 10000,
  "finalAmount": 85000,
  "status": "WAITING",
  "reasonForRejection": "견적 미승인",
  "confirmedAt": "2025-11-11T12:00:00",
  "createdAt": "2025-11-11T09:00:00",
  "updatedAt": "2025-11-11T10:00:00",
  "createdBy": 2,
  "updatedBy": 3
}
```

### 1.21.5 수리 등록

- 엔드포인트: `POST /api/v1/as/repairs`
- 설명: 새로운 수리 정보를 등록한다.
- 접근 권한: 로그인 사용자
- 필수 필드: asReceptionId, technicianId, warrantyResult, laborRate, inspectionCost, finalAmount, status
- Request Body:

```json
{
  "asReceptionId": 1,
  "technicianId": 3,
  "diagnosis": "메인보드 전원부 고장",
  "workNote": "메인보드 교체",
  "repairDecision": "REPAIRABLE",
  "warrantyResult": "IN_WARRANTY",
  "reason": "부품 노후",
  "laborRate": 30000,
  "inspectionCost": 10000,
  "finalAmount": 85000,
  "status": "WAITING",
  "reasonForRejection": "견적 미승인"
}
```

- Response Body (HTTP 201):

```json
{
  "message": "수리가 시작되었습니다."
}
```

### 1.21.6 A/S 중단

- 엔드포인트: `POST /api/v1/as/repairs/{id}/abort`
- 설명: 담당 기사가 점검 중인 A/S를 중단하고 반환 대기로 넘긴다.
- 접근 권한: 수리기사
- Path Variable: id (Long, 필수)
- 필수 필드: category, reason
- Request Body:

```json
{
  "category": "REPAIR_IMPOSSIBLE",
  "reason": "부품 단종"
}
```

- Response Body (HTTP 200):

```json
{
  "message": "수리가 시작되었습니다."
}
```

### 1.21.7 수리 상태 변경

- 엔드포인트: `PATCH /api/v1/as/repairs/{id}/status`
- 설명: 관리자/매니저가 수리 상태를 변경한다.
- 접근 권한: 관리자, 매니저
- Path Variable: id (Long, 필수)
- Request Body:

```json
{
  "status": "DIAGNOSING"
}
```

- Response Body (HTTP 200):

```json
{
  "message": "수리가 시작되었습니다."
}
```

### 1.21.8 수리 시작

- 엔드포인트: `PATCH /api/v1/as/repairs/{id}/start`
- 설명: 담당 기사가 수리를 시작한다.
- 접근 권한: 수리기사
- Path Variable: id (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "message": "수리가 시작되었습니다."
}
```

### 1.21.9 수리 완료

- 엔드포인트: `PATCH /api/v1/as/repairs/{id}/complete`
- 설명: 담당 기사가 본인에게 배정된 수리를 완료한다.
- 접근 권한: 수리기사
- Path Variable: id (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "message": "수리가 시작되었습니다."
}
```

### 1.21.10 접수번호로 수리 상세 조회

- 엔드포인트: `GET /api/v1/as/repairs/reception/{receptionNumber}`
- 설명: A/S 접수번호로 수리 상세 정보를 조회한다.
- 접근 권한: 로그인 사용자
- Path Variable: receptionNumber (String, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "asReceptionId": 1,
  "technicianId": 3,
  "diagnosis": "메인보드 전원부 고장",
  "workNote": "메인보드 교체",
  "repairDecision": "REPAIRABLE",
  "warrantyResult": "IN_WARRANTY",
  "reason": "부품 노후",
  "laborRate": 30000,
  "inspectionCost": 10000,
  "finalAmount": 85000,
  "status": "WAITING",
  "reasonForRejection": "견적 미승인",
  "confirmedAt": "2025-11-11T12:00:00",
  "createdAt": "2025-11-11T09:00:00",
  "updatedAt": "2025-11-11T10:00:00",
  "createdBy": 2,
  "updatedBy": 3
}
```

### 1.21.11 본인에게 배정된 A/S 목록 조회

- 엔드포인트: `GET /api/v1/as/repairs/my`
- 설명: 담당 기사에게 배정된 수리 목록을 조회한다.
- 접근 권한: 수리기사
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "asReceptionId": 1,
  "technicianId": 3,
  "diagnosis": "메인보드 전원부 고장",
  "workNote": "메인보드 교체",
  "repairDecision": "REPAIRABLE",
  "warrantyResult": "IN_WARRANTY",
  "reason": "부품 노후",
  "laborRate": 30000,
  "inspectionCost": 10000,
  "finalAmount": 85000,
  "status": "WAITING",
  "reasonForRejection": "견적 미승인",
  "confirmedAt": "2025-11-11T12:00:00",
  "createdAt": "2025-11-11T09:00:00",
  "updatedAt": "2025-11-11T10:00:00",
  "createdBy": 2,
  "updatedBy": 3
}
```

### 1.21.12 본인에게 배정된 A/S 상세 조회

- 엔드포인트: `GET /api/v1/as/repairs/my/{id}`
- 설명: 담당 기사에게 배정된 수리 상세 정보를 조회한다.
- 접근 권한: 수리기사
- Path Variable: id (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "asReceptionId": 1,
  "technicianId": 3,
  "diagnosis": "메인보드 전원부 고장",
  "workNote": "메인보드 교체",
  "repairDecision": "REPAIRABLE",
  "warrantyResult": "IN_WARRANTY",
  "reason": "부품 노후",
  "laborRate": 30000,
  "inspectionCost": 10000,
  "finalAmount": 85000,
  "status": "WAITING",
  "reasonForRejection": "견적 미승인",
  "confirmedAt": "2025-11-11T12:00:00",
  "createdAt": "2025-11-11T09:00:00",
  "updatedAt": "2025-11-11T10:00:00",
  "createdBy": 2,
  "updatedBy": 3
}
```

## 1.22 Repair Cost API

### 1.22.1 수리비 계산

- 엔드포인트: `POST /api/v1/as/repairs/{repairId}/cost`
- 설명: 수리 ID로 수리비를 계산한다.
- 접근 권한: 로그인 사용자
- Path Variable: repairId (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "laborRate": 1,
  "inspectionCost": 1,
  "finalAmount": 1
}
```

### 1.22.2 수리비 확정

- 엔드포인트: `POST /api/v1/as/repairs/{repairId}/cost/confirm`
- 설명: 계산된 수리비를 확정한다.
- 접근 권한: 로그인 사용자
- Path Variable: repairId (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "message": "수리가 시작되었습니다."
}
```

## 1.23 Repair History API

### 1.23.1 A/S 수리 이력 조회

- 엔드포인트: `GET /api/v1/as/repair-histories/{asReceptionId}`
- 설명: A/S 접수 ID로 수리 이력을 조회한다.
- 접근 권한: 로그인 사용자
- Path Variable: asReceptionId (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "repairId": 1,
  "asReceptionId": 1,
  "diagnosis": "",
  "workNote": "",
  "repairDecision": "",
  "warrantyResult": "",
  "finalAmount": 1,
  "status": "",
  "createdAt": "2026-11-13T10:00:00",
  "updatedAt": "2026-11-13T10:00:00",
  "repairWorks": [
    {
      "repairWorkId": 1,
      "repairPresetName": "",
      "workName": "",
      "usedParts": [
        {
          "id": 1,
          "repairWorkId": 1,
          "partId": 1,
          "partName": "",
          "quantity": 1,
          "unitPrice": 1,
          "totalPrice": 1,
          "createdAt": "2026-11-13T10:00:00"
        }
      ]
    }
  ]
}
```

## 1.24 Repair Part Usage API

### 1.24.1 실제 수리 부품 사용 등록

- 엔드포인트: `POST /api/v1/as/repair-part-usages`
- 설명: 수리에 사용된 부품을 등록한다.
- 접근 권한: 로그인 사용자
- 필수 필드: repairWorkId, partId, quantity
- Request Body:

```json
{
  "repairWorkId": 1,
  "partId": 1,
  "quantity": 1
}
```

- Response Body (HTTP 201):

```json
{
  "id": 1,
  "repairWorkId": 1,
  "partId": 1,
  "partName": "",
  "quantity": 1,
  "unitPrice": 1,
  "totalPrice": 1,
  "createdAt": "2026-11-13T10:00:00"
}
```

## 1.25 Repair Preset API

### 1.25.1 단일 프리셋 조회

- 엔드포인트: `GET /api/v1/as/repair-presets/{id}`
- 설명: 프리셋 ID로 수리 프리셋 상세 정보를 조회한다.
- 접근 권한: 로그인 사용자
- Path Variable: id (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "name": "",
  "description": "",
  "createdAt": "2026-11-13T10:00:00",
  "updatedAt": "2026-11-13T10:00:00",
  "createdBy": 1,
  "updatedBy": 1,
  "basePrice": 1,
  "estimatedMinutes": 1
}
```

### 1.25.2 프리셋 수정

- 엔드포인트: `PUT /api/v1/as/repair-presets/{id}`
- 설명: 수리 프리셋 정보를 수정한다.
- 접근 권한: 로그인 사용자
- Path Variable: id (Long, 필수)
- Request Body:

```json
{
  "name": "메인보드 교체",
  "description": "메인보드 불량 시 교체 작업",
  "parts": [
    {
      "partId": 1,
      "quantity": 1
    }
  ]
}
```

- Response Body (HTTP 200):

```json
{
  "id": 1,
  "name": "",
  "description": "",
  "createdAt": "2026-11-13T10:00:00",
  "updatedAt": "2026-11-13T10:00:00",
  "createdBy": 1,
  "updatedBy": 1,
  "basePrice": 1,
  "estimatedMinutes": 1
}
```

### 1.25.3 프리셋 삭제

- 엔드포인트: `DELETE /api/v1/as/repair-presets/{id}`
- 설명: 수리 프리셋을 삭제한다.
- 접근 권한: 로그인 사용자
- Path Variable: id (Long, 필수)
- Request Body: 없음
- Response Body: 없음 (HTTP 204)

### 1.25.4 전체 프리셋 조회

- 엔드포인트: `GET /api/v1/as/repair-presets`
- 설명: 등록된 전체 수리 프리셋 목록을 조회한다.
- 접근 권한: 로그인 사용자
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "name": "",
  "description": "",
  "createdAt": "2026-11-13T10:00:00",
  "updatedAt": "2026-11-13T10:00:00",
  "createdBy": 1,
  "updatedBy": 1,
  "basePrice": 1,
  "estimatedMinutes": 1
}
```

### 1.25.5 프리셋 등록

- 엔드포인트: `POST /api/v1/as/repair-presets`
- 설명: 새로운 수리 프리셋을 등록한다.
- 접근 권한: 로그인 사용자
- 필수 필드: name
- Request Body:

```json
{
  "name": "메인보드 교체",
  "description": "메인보드 불량 시 교체 작업",
  "parts": [
    {
      "partId": 1,
      "quantity": 1
    }
  ]
}
```

- Response Body (HTTP 201):

```json
{
  "id": 1,
  "name": "",
  "description": "",
  "createdAt": "2026-11-13T10:00:00",
  "updatedAt": "2026-11-13T10:00:00",
  "createdBy": 1,
  "updatedBy": 1,
  "basePrice": 1,
  "estimatedMinutes": 1
}
```

### 1.25.6 프리셋 부품 목록 조회

- 엔드포인트: `GET /api/v1/as/repair-presets/{id}/parts`
- 설명: 수리 프리셋에 연결된 부품 목록을 조회한다.
- 접근 권한: 로그인 사용자
- Path Variable: id (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
	"partId": 1,
	"partName": "",
	"quantiㅂty": 1,
	"stockQuantity": 1,
	"standardMinutes": 1,
	"unitPrice": 1
}
```

### 1.25.7 프리셋 이름 검색

- 엔드포인트: `GET /api/v1/as/repair-presets/search`
- 설명: 이름으로 수리 프리셋을 검색한다.
- 접근 권한: 로그인 사용자
- Query Parameter: name (String, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
	"id": 1,
	"name": "",
	"description": "",
	"createdAt": "2026-11-13T10:00:00",
	"updatedAt": "2026-11-13T10:00:00",
	"createdBy": 1,
	"updatedBy": 1,
	"basePrice": 1,
	"estimatedMinutes": 1
}
```

## 1.26 Repair Work API

### 1.26.1 수리 작업 내역 단건 조회

- 엔드포인트: `GET /api/v1/as/repair-works/{id}`
- 설명: 수리 작업 내역 ID로 단건을 조회한다.
- 접근 권한: 로그인 사용자
- Path Variable: id (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "repairId": 1,
  "repairPresetName": "",
  "workName": "",
  "createdAt": "2026-11-13T10:00:00",
  "updatedAt": "2026-11-13T10:00:00",
  "createdBy": 1,
  "updatedBy": 1
}
```

### 1.26.2 수리 작업 내역 수정

- 엔드포인트: `PUT /api/v1/as/repair-works/{id}`
- 설명: 수리 작업 내역을 수정한다.
- 접근 권한: 로그인 사용자
- Path Variable: id (Long, 필수)
- Request Body:

```json
{
  "presetId": 10,
  "workName": "메인보드 교체"
}
```

- Response Body (HTTP 200):

```json
{
  "id": 1,
  "repairId": 1,
  "repairPresetName": "",
  "workName": "",
  "createdAt": "2026-11-13T10:00:00",
  "updatedAt": "2026-11-13T10:00:00",
  "createdBy": 1,
  "updatedBy": 1
}
```

### 1.26.3 수리 작업 내역 삭제

- 엔드포인트: `DELETE /api/v1/as/repair-works/{id}`
- 설명: 수리 작업 내역을 삭제한다.
- 접근 권한: 로그인 사용자
- Path Variable: id (Long, 필수)
- Request Body: 없음
- Response Body: 없음 (HTTP 204)

### 1.26.4 수리 작업 내역 전체 조회

- 엔드포인트: `GET /api/v1/as/repair-works`
- 설명: 등록된 전체 수리 작업 내역을 조회한다.
- 접근 권한: 로그인 사용자
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
	"id": 1,
	"repairId": 1,
	"repairPresetName": "",
	"workName": "",
	"createdAt": "2026-11-13T10:00:00",
	"updatedAt": "2026-11-13T10:00:00",
	"createdBy": 1,
	"updatedBy": 1
}
```

### 1.26.5 수리 작업 내역 등록

- 엔드포인트: `POST /api/v1/as/repair-works`
- 설명: 새로운 수리 작업 내역을 등록한다.
- 접근 권한: 로그인 사용자
- Request Body:

```json
{
  "repairId": 1,
  "presetId": 10,
  "workName": "메인보드 교체"
}
```

- Response Body (HTTP 201):

```json
{
  "id": 1,
  "repairId": 1,
  "repairPresetName": "",
  "workName": "",
  "createdAt": "2026-11-13T10:00:00",
  "updatedAt": "2026-11-13T10:00:00",
  "createdBy": 1,
  "updatedBy": 1
}
```

### 1.26.6 작업명 검색

- 엔드포인트: `GET /api/v1/as/repair-works/search`
- 설명: 작업명으로 수리 작업 내역을 검색한다.
- 접근 권한: 로그인 사용자
- Query Parameter: workName (String, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "repairId": 1,
  "repairPresetName": "",
  "workName": "",
  "createdAt": "2026-11-13T10:00:00",
  "updatedAt": "2026-11-13T10:00:00",
  "createdBy": 1,
  "updatedBy": 1
}
```

### 1.26.7 특정 수리 건의 작업 내역 조회

- 엔드포인트: `GET /api/v1/as/repair-works/repair/{repairId}`
- 설명: 수리 ID에 연결된 작업 내역 목록을 조회한다.
- 접근 권한: 로그인 사용자
- Path Variable: repairId (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "repairId": 1,
  "repairPresetName": "",
  "workName": "",
  "createdAt": "2026-11-13T10:00:00",
  "updatedAt": "2026-11-13T10:00:00",
  "createdBy": 1,
  "updatedBy": 1
}
```

## 1.27 Shipment API

### 1.27.1 택배 배송 등록

- 엔드포인트: `POST /api/v1/shipments`
- 설명: 택배 배송을 등록한다.
- 접근 권한: 관리자, 접수담당자
- 필수 필드: asReceptionId, courierCode, trackingNumber
- Request Body:

```json
{
  "asReceptionId": 1,
  "courierCode": "04",
  "trackingNumber": "123456789012"
}
```

- Response Body (HTTP 201):

```json
{
  "id": 1,
  "asReceptionId": 1,
  "receptionNumber": "AS-20240101-001",
  "recipientName": "홍길동",
  "recipientPhone": "01012345678",
  "returnMethod": "DELIVERY",
  "courier": "CJ대한통운",
  "courierCode": "04",
  "trackingNumber": "123456789012",
  "status": "SHIPPING",
  "shippedAt": "2024-01-01T10:00:00",
  "completedAt": "2024-01-03T15:00:00"
}
```

### 1.27.2 배송 추적 갱신

- 엔드포인트: `POST /api/v1/shipments/{shipmentId}/tracking/refresh`
- 설명: SweetTracker로 배송을 조회하고 이력을 갱신한다.
- 접근 권한: 관리자, 접수담당자
- Path Variable: shipmentId (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "invoiceNo": "",
  "itemName": "",
  "result": "",
  "level": 1,
  "complete": true,
  "completeYN": "",
  "estimate": "",
  "trackingDetails": [
    {
      "code": "",
      "kind": "",
      "level": 1,
      "time": 1,
      "timeString": "",
      "where": "",
      "remark": ""
    }
  ],
  "status": true,
  "msg": ""
}
```

### 1.27.3 방문 수령 등록

- 엔드포인트: `POST /api/v1/shipments/pickups`
- 설명: 방문 수령을 등록한다.
- 접근 권한: 관리자, 접수담당자
- 필수 필드: asReceptionId
- Request Body:

```json
{
  "asReceptionId": 1
}
```

- Response Body (HTTP 201):

```json
{
  "id": 1,
  "asReceptionId": 1,
  "receptionNumber": "AS-20240101-001",
  "recipientName": "홍길동",
  "recipientPhone": "01012345678",
  "returnMethod": "DELIVERY",
  "courier": "CJ대한통운",
  "courierCode": "04",
  "trackingNumber": "123456789012",
  "status": "SHIPPING",
  "shippedAt": "2024-01-01T10:00:00",
  "completedAt": "2024-01-03T15:00:00"
}
```

### 1.27.4 운송장 정보 수정

- 엔드포인트: `PATCH /api/v1/shipments/{shipmentId}/tracking-info`
- 설명: 택배사 및 운송장번호를 수정한다.
- 접근 권한: 관리자, 접수담당자
- Path Variable: shipmentId (Long, 필수)
- 필수 필드: courierCode, trackingNumber
- Request Body:

```json
{
  "courierCode": "04",
  "trackingNumber": "123456789012"
}
```

- Response Body (HTTP 200):

```json
{
  "id": 1,
  "asReceptionId": 1,
  "receptionNumber": "AS-20240101-001",
  "recipientName": "홍길동",
  "recipientPhone": "01012345678",
  "returnMethod": "DELIVERY",
  "courier": "CJ대한통운",
  "courierCode": "04",
  "trackingNumber": "123456789012",
  "status": "SHIPPING",
  "shippedAt": "2024-01-01T10:00:00",
  "completedAt": "2024-01-03T15:00:00"
}
```

### 1.27.5 배송 시작

- 엔드포인트: `PATCH /api/v1/shipments/{shipmentId}/shipping`
- 설명: 배송을 시작하고 이메일 알림을 발송한다.
- 접근 권한: 관리자, 접수담당자
- Path Variable: shipmentId (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "asReceptionId": 1,
  "receptionNumber": "AS-20240101-001",
  "recipientName": "홍길동",
  "recipientPhone": "01012345678",
  "returnMethod": "DELIVERY",
  "courier": "CJ대한통운",
  "courierCode": "04",
  "trackingNumber": "123456789012",
  "status": "SHIPPING",
  "shippedAt": "2024-01-01T10:00:00",
  "completedAt": "2024-01-03T15:00:00"
}
```

### 1.27.6 방문 수령 완료

- 엔드포인트: `PATCH /api/v1/shipments/{shipmentId}/pickup`
- 설명: 방문 수령 완료를 처리한다.
- 접근 권한: 관리자, 접수담당자
- Path Variable: shipmentId (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "asReceptionId": 1,
  "receptionNumber": "AS-20240101-001",
  "recipientName": "홍길동",
  "recipientPhone": "01012345678",
  "returnMethod": "DELIVERY",
  "courier": "CJ대한통운",
  "courierCode": "04",
  "trackingNumber": "123456789012",
  "status": "SHIPPING",
  "shippedAt": "2024-01-01T10:00:00",
  "completedAt": "2024-01-03T15:00:00"
}
```

### 1.27.7 배송 완료

- 엔드포인트: `PATCH /api/v1/shipments/{shipmentId}/delivered`
- 설명: 배송 완료를 수동 처리한다.
- 접근 권한: 관리자, 접수담당자
- Path Variable: shipmentId (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "asReceptionId": 1,
  "receptionNumber": "AS-20240101-001",
  "recipientName": "홍길동",
  "recipientPhone": "01012345678",
  "returnMethod": "DELIVERY",
  "courier": "CJ대한통운",
  "courierCode": "04",
  "trackingNumber": "123456789012",
  "status": "SHIPPING",
  "shippedAt": "2024-01-01T10:00:00",
  "completedAt": "2024-01-03T15:00:00"
}
```

### 1.27.8 배송 단건 조회

- 엔드포인트: `GET /api/v1/shipments/{shipmentId}`
- 설명: 배송 ID로 배송 정보를 조회한다.
- 접근 권한: 관리자, 접수담당자, 수리기사
- Path Variable: shipmentId (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "asReceptionId": 1,
  "receptionNumber": "AS-20240101-001",
  "recipientName": "홍길동",
  "recipientPhone": "01012345678",
  "returnMethod": "DELIVERY",
  "courier": "CJ대한통운",
  "courierCode": "04",
  "trackingNumber": "123456789012",
  "status": "SHIPPING",
  "shippedAt": "2024-01-01T10:00:00",
  "completedAt": "2024-01-03T15:00:00"
}
```

### 1.27.9 배송 상세 조회

- 엔드포인트: `GET /api/v1/shipments/{shipmentId}/details`
- 설명: 배송 상세 및 이벤트 이력을 조회한다.
- 접근 권한: 관리자, 접수담당자, 수리기사
- Path Variable: shipmentId (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "asReceptionId": 1,
  "receptionNumber": "AS-20240101-001",
  "recipientName": "홍길동",
  "recipientPhone": "01012345678",
  "returnMethod": "DELIVERY",
  "address": "서울시 강남구",
  "addressDetail": "테헤란로 123",
  "courier": "CJ대한통운",
  "courierCode": "04",
  "trackingNumber": "123456789012",
  "status": "SHIPPING",
  "shippedAt": "2024-01-01T10:00:00",
  "completedAt": "2024-01-03T15:00:00",
  "lastCheckedAt": "2024-01-02T12:00:00",
  "lastTrackingStatus": "배송중",
  "events": [
    {
      "id": 1,
      "status": "IN_TRANSIT",
      "description": "배송중",
      "occurredAt": "2024-01-02T09:00:00"
    }
  ]
}
```

### 1.27.10 접수별 배송 조회

- 엔드포인트: `GET /api/v1/shipments/receptions/{asReceptionId}`
- 설명: A/S 접수별 배송 정보를 조회한다.
- 접근 권한: 관리자, 접수담당자, 수리기사
- Path Variable: asReceptionId (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "asReceptionId": 1,
  "receptionNumber": "AS-20240101-001",
  "recipientName": "홍길동",
  "recipientPhone": "01012345678",
  "returnMethod": "DELIVERY",
  "courier": "CJ대한통운",
  "courierCode": "04",
  "trackingNumber": "123456789012",
  "status": "SHIPPING",
  "shippedAt": "2024-01-01T10:00:00",
  "completedAt": "2024-01-03T15:00:00"
}
```

### 1.27.11 배송 관리 목록 조회

- 엔드포인트: `GET /api/v1/shipments/list`
- 설명: 배송 및 방문 수령 통합 목록을 조회한다.
- 접근 권한: 관리자, 접수담당자, 수리기사
- Query Parameter: keyword (String, 선택) / status (String, 선택, 예: ALL) / returnMethod (String, 선택, 예: ALL) / page (int, 선택, 예: 0) / size (int, 선택, 예: 20)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "content": [
    {
      "asReceptionId": 1,
      "shipmentId": 1,
      "receptionNumber": "AS-20240101-001",
      "customerName": "홍길동",
      "recipientPhone": "01012345678",
      "returnMethod": "DELIVERY",
      "asReceptionStatus": "RECEIVED",
      "shipmentStatus": "SHIPPING",
      "courier": "CJ대한통운",
      "trackingNumber": "123456789012",
      "shippedAt": "2024-01-01T10:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false
}
```

## 1.28 Shipment Statistics API

### 1.28.1 배송 현황 통계 조회

- 엔드포인트: `GET /api/v1/admin/statistics/shipments`
- 설명: 기간별 배송 현황 통계를 조회한다.
- 접근 권한: 관리자
- Query Parameter: startDate (LocalDate, 필수) / endDate (LocalDate, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "startDate": "2026-11-13",
  "endDate": "2026-11-13",
  "readyCount": 1,
  "shippingCount": 1,
  "completedCount": 1
}
```

### 1.28.2 기간 배송 내역 조회

- 엔드포인트: `GET /api/v1/admin/statistics/shipments/history`
- 설명: 조회 기간에 등록된 배송 내역을 조회한다.
- 접근 권한: 관리자
- Query Parameter: startDate (LocalDate, 필수) / endDate (LocalDate, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
	"shipmentId": 1,
	"receptionId": 1,
	"receptionNumber": "",
	"customerName": "",
	"returnMethod": "DELIVERY",
	"status": "READY",
	"createdAt": "2026-11-13T10:00:00"
}
```

## 1.29 Symptom Preset API

### 1.29.1 고장 증상 프리셋 목록 조회

- 엔드포인트: `GET /api/v1/as/symptom-presets`
- 설명: 등록된 고장 증상 프리셋 목록을 조회한다.
- 접근 권한: 로그인 사용자
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
  "id": 1,
  "productCategory": "",
  "name": ""
}
```

### 1.29.2 증상에 연결된 수리 프리셋 조회

- 엔드포인트: `GET /api/v1/as/symptom-presets/{symptomId}/repair-presets`
- 설명: 고장 증상 ID에 연결된 수리 프리셋 목록을 조회한다.
- 접근 권한: 로그인 사용자
- Path Variable: symptomId (Long, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
	"id": 1,
	"name": "",
	"description": "",
	"createdAt": "2026-11-13T10:00:00",
	"updatedAt": "2026-11-13T10:00:00",
	"createdBy": 1,
	"updatedBy": 1,
	"basePrice": 1,
	"estimatedMinutes": 1
}
```

### 1.29.3 고장 증상 프리셋 검색

- 엔드포인트: `GET /api/v1/as/symptom-presets/search`
- 설명: 이름으로 고장 증상 프리셋을 검색한다.
- 접근 권한: 로그인 사용자
- Query Parameter: name (String, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
	"id": 1,
	"productCategory": "",
	"name": ""
}
```

## 1.30 Technician Statistics API

### 1.30.1 기사별 통계 조회

- 엔드포인트: `GET /api/v1/admin/statistics/technicians`
- 설명: 기간별 기사별 통계를 조회한다.
- 접근 권한: 관리자
- Query Parameter: startDate (LocalDate, 필수) / endDate (LocalDate, 필수)
- Request Body: 없음
- Response Body (HTTP 200):

```json
{
	 "technicianId": 1,
	 "technicianName": "",
	 "assignedCount": 1,
	 "completedCount": 1,
	 "inProgressCount": 1,
	 "averageDurationMinutes": 1,
	 "revenue": 1
}
```

실패 시 응답은 1.1.2 공통 에러 형식을 따른다.
