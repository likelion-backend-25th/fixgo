package net.likelion.bebc25.fixgo.repair.dto.repair;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepairCostResponseDto {

    // 시간당 공임
    private int laborRate;

    // 점검 비용
    private int inspectionCost;

    // 최종 수리 금액
    private int finalAmount;
}