package net.likelion.bebc25.fixgo.repair.dto.repairWork;

public record RepairWorkRequest(
        Long repairId,
        Long presetId,
        String workName
) {
}