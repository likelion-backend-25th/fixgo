package net.likelion.bebc25.fixgo.repair.dto.repair;

import net.likelion.bebc25.fixgo.repair.entity.RepairWork;

import java.util.List;

public record RepairWorkHistoryResponse(
        Long repairWorkId,
        String repairPresetName,
        String workName,
        List<RepairPartUsageResponse> usedParts
) {
    public static RepairWorkHistoryResponse from(
            RepairWork repairWork,
            List<RepairPartUsageResponse> usedParts
    ) {
        return new RepairWorkHistoryResponse(
                repairWork.getId(),
                repairWork.getRepairPresetName(),
                repairWork.getWorkName(),
                usedParts
        );
    }
}