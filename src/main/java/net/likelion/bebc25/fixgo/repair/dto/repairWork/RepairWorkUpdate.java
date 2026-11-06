package net.likelion.bebc25.fixgo.repair.dto.repairWork;

public record RepairWorkUpdate(

        // 수리 프리셋 이름
        String repairPresetName,

        // 작업 내용
        String workName

) {
}