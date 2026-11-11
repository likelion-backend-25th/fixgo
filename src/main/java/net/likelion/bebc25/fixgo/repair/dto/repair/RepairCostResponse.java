package net.likelion.bebc25.fixgo.repair.dto.repair;

import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;

public record RepairCostResponse(
        // 시간당 공임
        int laborRate,

        // 점검 비용
        int inspectionCost,

        // 최종 수리 금액
        int finalAmount
) {

    public static RepairCostResponse from(Repair repair) {
        return new RepairCostResponse(
                repair.getLaborRate(),
                repair.getInspectionCost(),
                repair.getFinalAmount()
        );
    }
}