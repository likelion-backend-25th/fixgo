package net.likelion.bebc25.fixgo.repair.dto.repair;

import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairDecision;

import java.time.LocalDateTime;

public record RepairResponse(

        // 수리 ID
        Long id,

        // A/S 접수 ID
        Long asReceptionId,

        // 담당 기사 ID
        Long technicianId,

        // 진단 내용
        String diagnosis,

        // 작업 내용
        String workNote,

        // 수리 판정
        RepairDecision repairDecision,

        // 보증 여부
        String warrantyResult,

        // 수리 사유
        String reason,

        // 시간당 공임
        int laborRate,

        // 점검 비용
        int inspectionCost,

        // 최종 수리 금액
        int finalAmount,

        // 수리 상태
        String status,

        // 반려 사유
        String reasonForRejection,

        // 승인 확정 일시
        LocalDateTime confirmedAt,

        // 생성 일시
        LocalDateTime createdAt,

        // 수정 일시
        LocalDateTime updatedAt,

        // 생성자 ID
        Long createdBy,

        // 수정자 ID
        Long updatedBy

) {

    // Repair Entity -> RepairResponse
    public static RepairResponse from(Repair repair) {
        var reception = repair.getAsReception();
        var technician = reception.getTechnician();
        return new RepairResponse(
                repair.getId(),
                reception.getId(),
                technician == null ? repair.getTechnician().getId() : technician.getId(),
                repair.getDiagnosis(),
                repair.getWorkNote(),
                repair.getRepairDecision(),
                repair.getWarrantyResult().name(),
                repair.getReason(),
                repair.getLaborRate(),
                repair.getInspectionCost(),
                repair.getFinalAmount(),
                repair.getStatus().name(),
                repair.getReasonForRejection(),
                repair.getConfirmedAt(),
                repair.getCreatedAt(),
                repair.getUpdatedAt(),
                repair.getCreatedBy(),
                repair.getUpdatedBy()
        );
    }
}
