package net.likelion.bebc25.fixgo.repair.dto.repair;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepairRequestDto {

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
}