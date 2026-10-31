package net.likelion.bebc25.fixgo.repair.dto.repair;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RepairResponseDto {

    // 수리 ID
    private Long id;

    // A/S 접수 ID
    private Long asRequestId;

    // 담당 기사 ID
    private Long technicianId;

    // 진단 내용
    private String diagnosis;

    // 작업 내용
    private String workNote;

    // 수리 판정
    private String repairDecision;

    // 보증 여부
    private String warrantyResult;

    // 수리 사유
    private String reason;

    // 시간당 공임
    private int laborRate;

    // 점검 비용
    private int inspectionCost;

    // 최종 수리 금액
    private int finalAmount;

    // 수리 상태
    private String status;

    // 반려 사유
    private String reasonForRejection;

    // 승인 확정 일시
    private LocalDateTime confirmedAt;

    // 생성 일시
    private LocalDateTime createdAt;

    // 수정 일시
    private LocalDateTime updatedAt;

    // 생성자
    private String createdBy;

    // 수정자
    private String updatedBy;
}