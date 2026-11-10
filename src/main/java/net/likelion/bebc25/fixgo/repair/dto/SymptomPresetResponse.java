package net.likelion.bebc25.fixgo.repair.dto;

import net.likelion.bebc25.fixgo.repair.entity.SymptomPreset;

public record SymptomPresetResponse(
        Long id,
        String productCategory,
        String name
) {
    public static SymptomPresetResponse from(SymptomPreset preset) {
        return new SymptomPresetResponse(
                preset.getId(),
                preset.getProductCategory(),
                preset.getName()
        );
    }
}