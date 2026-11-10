package net.likelion.bebc25.fixgo.repair.dto;

import net.likelion.bebc25.fixgo.repair.entity.RepairPresetPart;
import net.likelion.bebc25.fixgo.repair.entity.part.Part;

public record RepairPresetPartResponse(
        Long partId,
        String partName,
        int quantity,
        int stockQuantity,
        int standardMinutes,
        int unitPrice
) {
    public static RepairPresetPartResponse from(RepairPresetPart presetPart) {
        Part part = presetPart.getPart();

        return new RepairPresetPartResponse(
                part.getId(),
                part.getName(),
                presetPart.getQuantity(),
                part.getStockQuantity(),
                part.getStandardMinutes(),
                part.getUnitPrice()
        );
    }
}