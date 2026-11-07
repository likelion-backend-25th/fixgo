package net.likelion.bebc25.fixgo.repair.dto.repairWork;

import net.likelion.bebc25.fixgo.repair.entity.RepairWork;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;

public record RepairWorkRequest(

        // 수리 ID
        Long repairId,

        // 수리 프리셋 이름
        String repairPresetName,

        // 작업 내용
        String workName

) {

    public static RepairWork toEntity(RepairWorkRequest dto, Repair repair) {
        return new RepairWork(
                repair,
                dto.repairPresetName(),
                dto.workName()
        );
    }
}