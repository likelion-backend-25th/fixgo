package net.likelion.bebc25.fixgo.repair.dto.repair;

public record RepairPartUsageRequest(
        Long repairWorkId,
        Long partId,
        int quantity
) {
}