package net.likelion.bebc25.fixgo.repair.dto.preset;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairPresetPartRequest;

import java.util.List;

@Getter
@NoArgsConstructor
public class RepairPresetRequest {

    @Schema(description = "수리 프리셋 이름", example = "메인보드 교체")
    @NotBlank(message = "수리 프리셋 이름은 필수입니다.")
    private String name;

    @Schema(description = "수리 프리셋 설명", example = "메인보드 불량 시 교체 작업")
    private String description;

    @Schema(description = "프리셋에 포함된 부품 목록")
    private List<RepairPresetPartRequest> parts;
}
