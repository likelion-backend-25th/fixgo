package net.likelion.bebc25.fixgo.repair.dto.repair;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RepairPresetPartRequest(

        @NotNull(message = "부품 ID는 필수입니다.")
        Long partId,

        @Positive(message = "기본 부품 수량은 1개 이상이어야 합니다.")
        int quantity

) {
}