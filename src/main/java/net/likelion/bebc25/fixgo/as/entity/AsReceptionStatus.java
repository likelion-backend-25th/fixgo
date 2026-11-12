package net.likelion.bebc25.fixgo.as.entity;

public enum AsReceptionStatus {
    RECEIVED,                 // 접수
    INSPECTION,               // 점검
    AWAITING_QUOTE_APPROVAL,  // 견적 승인 대기 (고객 동의)
    AWAITING_REPAIR,          // 수리 대기
    REPAIR_IN_PROGRESS,       // 수리 중
    AWAITING_PAYMENT,         // 결제 대기
    AWAITING_SHIPMENT,        // 배송 대기
    AWAITING_PICKUP,          // 수령 대기
    COMPLETED                // 완료
}