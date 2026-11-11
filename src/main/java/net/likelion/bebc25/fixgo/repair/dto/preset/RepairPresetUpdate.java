package net.likelion.bebc25.fixgo.repair.dto.preset;

import io.swagger.v3.oas.annotations.media.Schema;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairPresetPartRequest;

import java.util.List;

public record RepairPresetUpdate(
        @Schema(description = "수리 프리셋 이름", example = "메인보드 교체")
        String name,
        @Schema(description = "수리 프리셋 설명", example = "메인보드 불량 시 교체 작업")
        String description,
        @Schema(description = "프리셋에 포함된 부품 목록")
        List<RepairPresetPartRequest> parts
) {

    // 기존 2개 인자 생성자 호환
    public RepairPresetUpdate(String name, String description) {
        this(name, description, null);
    }
}
