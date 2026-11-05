package net.likelion.bebc25.fixgo.repair.mapper;

import net.likelion.bebc25.fixgo.repair.dto.preset.RepairPresetResponse;
import net.likelion.bebc25.fixgo.repair.entity.RepairPreset;
import org.springframework.stereotype.Component;

@Component
public class RepairPresetMapper {

    // Entity -> Response DTO
    public RepairPresetResponse toResponse(RepairPreset preset, Long basePrice) {
        return new RepairPresetResponse(preset.getId(), preset.getName(), preset.getDescription(), preset.getCreatedAt(), preset.getUpdatedAt(), preset.getCreatedBy(), preset.getUpdatedBy(), basePrice);
    }
}