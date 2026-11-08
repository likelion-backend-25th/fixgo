package net.likelion.bebc25.fixgo.repair.dto.repair;

import net.likelion.bebc25.fixgo.repair.entity.part.RepairPartUsage;

import java.time.LocalDateTime;

public record RepairPartUsageResponse(
        Long id,
        Long repairWorkId,
        Long partId,
        String partName,
        int quantity,
        int unitPrice,
        int totalPrice,
        LocalDateTime createdAt
) {

    public static RepairPartUsageResponse from(RepairPartUsage usage) {
        return new RepairPartUsageResponse(
                usage.getId(),
                usage.getRepairWork().getId(),
                usage.getPart().getId(),
                usage.getPart().getName(),
                usage.getQuantity(),
                usage.getUnitPrice(),
                usage.getQuantity() * usage.getUnitPrice(),
                usage.getCreatedAt()
        );
    }
}