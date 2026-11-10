package net.likelion.bebc25.fixgo.repair.dto.preset;

import net.likelion.bebc25.fixgo.repair.entity.RepairPreset;

import java.time.LocalDateTime;

public record RepairPresetResponse(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy,
        Long basePrice,
        Long estimatedMinutes
) {
    public static RepairPresetResponse from(
            RepairPreset preset,
            Long basePrice,
            Long estimatedMinutes
    ) {
        return new RepairPresetResponse(
                preset.getId(),
                preset.getName(),
                preset.getDescription(),
                preset.getCreatedAt(),
                preset.getUpdatedAt(),
                preset.getCreatedBy(),
                preset.getUpdatedBy(),
                basePrice,
                estimatedMinutes
        );
    }
}