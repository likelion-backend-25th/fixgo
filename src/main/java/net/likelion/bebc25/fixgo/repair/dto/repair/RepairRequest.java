package net.likelion.bebc25.fixgo.repair.dto.repair;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairDecision;
import net.likelion.bebc25.fixgo.repair.entity.repair.WarrantyResult;

public record RepairRequest(

        // A/S 접수 ID
        @NotNull(message = "A/S 접수 ID는 필수입니다.")
        Long asReceptionId,

        // 담당 기사 ID
        @NotNull(message = "담당 기사 ID는 필수입니다.")
        Long technicianId,

        // 진단 내용
        String diagnosis,

        // 작업 내용
        String workNote,

        // 수리 판정
        RepairDecision repairDecision,

        // 보증 여부
        @NotBlank(message = "보증 여부는 필수입니다.")
        String warrantyResult,

        // 수리 사유
        String reason,

        // 시간당 공임
        @Min(value = 0, message = "시간당 공임은 0 이상이어야 합니다.")
        int laborRate,

        // 점검 비용
        @Min(value = 0, message = "점검 비용은 0 이상이어야 합니다.")
        int inspectionCost,

        // 최종 수리 금액
        @Min(value = 0, message = "최종 수리 금액은 0 이상이어야 합니다.")
        int finalAmount,

        // 수리 상태
        @NotBlank(message = "수리 상태는 필수입니다.")
        String status,

        // 반려 사유
        String reasonForRejection

) {

    // RepairRequest -> 기존 Repair Entity 수정
    public static Repair toEntity(
            Repair repair,
            RepairRequest dto,
            AsReception asReception,
            Member technician
    ) {
        repair.updateRepairInfo(
                asReception,
                technician,
                dto.diagnosis(),
                dto.workNote(),
                dto.repairDecision(),
                WarrantyResult.valueOf(dto.warrantyResult()),
                dto.reason(),
                dto.laborRate(),
                dto.inspectionCost(),
                dto.finalAmount(),
                dto.reasonForRejection()
        );

        return repair;
    }
}
