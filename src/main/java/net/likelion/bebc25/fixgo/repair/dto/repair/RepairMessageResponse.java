package net.likelion.bebc25.fixgo.repair.dto.repair;

import io.swagger.v3.oas.annotations.media.Schema;

public record RepairMessageResponse(
        @Schema(description = "처리 결과 메시지", example = "수리가 시작되었습니다.")
        String message
) {
}
