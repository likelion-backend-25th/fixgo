package net.likelion.bebc25.fixgo.repair.entity.repair;


public enum RepairStatus {
    DIAGNOSING, // 고장 원인 진단 중
    REQUESTED,  // 수리 승인 대기
    APPROVED,   // 수리 승인
    REJECTED,   // 수리 거절
    REPAIRING,  // 수리 중
    COMPLETED   // 수리 완료
}
