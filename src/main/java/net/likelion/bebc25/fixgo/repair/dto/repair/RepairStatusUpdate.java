package net.likelion.bebc25.fixgo.repair.dto.repair;

import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;

// 수리 상태 변경 요청 DTO
public record RepairStatusUpdate(

        // 변경할 수리 상태
        RepairStatus status,

        // 반려 사유
        String reasonForRejection

) {
}