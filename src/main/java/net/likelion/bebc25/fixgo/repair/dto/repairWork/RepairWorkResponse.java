package net.likelion.bebc25.fixgo.repair.dto.repairWork;

import net.likelion.bebc25.fixgo.repair.entity.RepairWork;

import java.time.LocalDateTime;

public record RepairWorkResponse(
        Long id,
        Long repairId,
        String repairPresetName,
        String workName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy
) {

    public static RepairWorkResponse from(RepairWork repairWork) {
        return new RepairWorkResponse(
                repairWork.getId(),
                repairWork.getRepair().getId(),
                repairWork.getRepairPresetName(),
                repairWork.getWorkName(),
                repairWork.getCreatedAt(),
                repairWork.getUpdatedAt(),
                repairWork.getCreatedBy(),
                repairWork.getUpdatedBy()
        );
    }
}