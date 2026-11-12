package net.likelion.bebc25.fixgo.repair.dto.repair;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RepairAbortRequest(
        @Schema(description = "중단 카테고리", example = "REPAIR_IMPOSSIBLE")
        @NotBlank(message = "중단 카테고리를 선택해 주세요.")
        String category,
        @Schema(description = "중단 사유", example = "부품 단종")
        @NotBlank(message = "중단 사유를 입력해 주세요.")
        String reason
) {
}
