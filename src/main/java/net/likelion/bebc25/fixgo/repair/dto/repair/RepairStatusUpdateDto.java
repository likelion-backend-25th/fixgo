package net.likelion.bebc25.fixgo.repair.dto.repair;

import lombok.Getter;
import lombok.Setter;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;

@Getter
@Setter
public class RepairStatusUpdateDto {

    // 변경할 수리 상태
    private RepairStatus status;

    // 반려 사유
    private String reasonForRejection;
}