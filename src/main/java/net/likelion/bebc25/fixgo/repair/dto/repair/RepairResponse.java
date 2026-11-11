package net.likelion.bebc25.fixgo.repair.dto.repair;

import io.swagger.v3.oas.annotations.media.Schema;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairDecision;

import java.time.LocalDateTime;

public record RepairResponse(

        // 수리 ID
        @Schema(description = "수리 ID", example = "1")
        Long id,

        // A/S 접수 ID
        @Schema(description = "A/S 접수 ID", example = "1")
        Long asReceptionId,

        // 담당 기사 ID
        @Schema(description = "담당 기사 ID", example = "3")
        Long technicianId,

        // 진단 내용
        @Schema(description = "진단 내용", example = "메인보드 전원부 고장")
        String diagnosis,

        // 작업 내용
        @Schema(description = "작업 내용", example = "메인보드 교체")
        String workNote,

        // 수리 판정
        @Schema(description = "수리 판정", example = "REPAIRABLE")
        RepairDecision repairDecision,

        // 보증 여부
        @Schema(description = "보증 여부", example = "IN_WARRANTY")
        String warrantyResult,

        // 수리 사유
        @Schema(description = "수리 사유", example = "부품 노후")
        String reason,

        // 시간당 공임
        @Schema(description = "시간당 공임", example = "30000")
        int laborRate,

        // 점검 비용
        @Schema(description = "점검 비용", example = "10000")
        int inspectionCost,

        // 최종 수리 금액
        @Schema(description = "최종 수리 금액", example = "85000")
        int finalAmount,

        // 수리 상태
        @Schema(description = "수리 상태", example = "WAITING")
        String status,

        // 반려 사유
        @Schema(description = "반려 사유", example = "견적 미승인")
        String reasonForRejection,

        // 승인 확정 일시
        @Schema(description = "승인 확정 일시", example = "2025-11-11T12:00:00")
        LocalDateTime confirmedAt,

        // 생성 일시
        @Schema(description = "생성 일시", example = "2025-11-11T09:00:00")
        LocalDateTime createdAt,

        // 수정 일시
        @Schema(description = "수정 일시", example = "2025-11-11T10:00:00")
        LocalDateTime updatedAt,

        // 생성자 ID
        @Schema(description = "생성자 ID", example = "2")
        Long createdBy,

        // 수정자 ID
        @Schema(description = "수정자 ID", example = "3")
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
