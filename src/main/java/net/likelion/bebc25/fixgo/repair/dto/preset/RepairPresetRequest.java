package net.likelion.bebc25.fixgo.repair.dto.preset;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairPresetPartRequest;

import java.util.List;

@Getter
@NoArgsConstructor
public class RepairPresetRequest {

    @NotBlank(message = "수리 프리셋 이름은 필수입니다.")
    private String name;

    private String description;

    private List<RepairPresetPartRequest> parts;
}