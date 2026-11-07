package net.likelion.bebc25.fixgo.repair.dto.repair;

import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;

import java.time.LocalDateTime;
import java.util.List;

public record RepairHistoryResponse(
        Long repairId,
        Long asReceptionId,
        String diagnosis,
        String workNote,
        String repairDecision,
        String warrantyResult,
        Integer finalAmount,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<RepairWorkHistoryResponse> repairWorks
) {
    public static RepairHistoryResponse from(
            Repair repair,
            List<RepairWorkHistoryResponse> repairWorks
    ) {
        return new RepairHistoryResponse(
                repair.getId(),
                repair.getAsReception().getId(),
                repair.getDiagnosis(),
                repair.getWorkNote(),
                repair.getRepairDecision() == null
                        ? null : repair.getRepairDecision().name(),
                repair.getWarrantyResult() == null
                        ? null : repair.getWarrantyResult().name(),
                repair.getFinalAmount(),
                repair.getStatus() == null
                        ? null : repair.getStatus().name(),
                repair.getCreatedAt(),
                repair.getUpdatedAt(),
                repairWorks
        );
    }
}