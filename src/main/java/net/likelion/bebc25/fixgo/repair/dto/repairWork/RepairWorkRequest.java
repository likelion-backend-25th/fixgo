package net.likelion.bebc25.fixgo.repair.dto.repairWork;

import io.swagger.v3.oas.annotations.media.Schema;

public record RepairWorkRequest(
        @Schema(description = "수리 ID", example = "1")
        Long repairId,
        @Schema(description = "수리 프리셋 ID", example = "10")
        Long presetId,
        @Schema(description = "작업명", example = "메인보드 교체")
        String workName
) {
}
