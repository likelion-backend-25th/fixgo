package net.likelion.bebc25.fixgo.repair.dto.preset;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RepairPresetRequest {

    @NotBlank(message = "수리 프리셋 이름은 필수입니다.")
    private String name;

    private String description;

    @NotBlank(message = "생성자 정보는 필수입니다.")
    private String createdBy;
}