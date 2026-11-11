package net.likelion.bebc25.fixgo.repair.dto.repair;

import jakarta.validation.constraints.NotNull;

public record RepairPartUsageRequest(
        @NotNull Long repairWorkId,
        @NotNull Long partId,
        @NotNull int quantity
) {
}