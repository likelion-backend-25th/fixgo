package net.likelion.bebc25.fixgo.repair.dto.repairWork;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepairWorkUpdateDto {

    // 수리 프리셋 이름
    private String repairPresetName;

    // 작업 내용
    private String workName;
}