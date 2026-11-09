package net.likelion.bebc25.fixgo.repair.dto.repair;

import net.likelion.bebc25.fixgo.repair.entity.RepairWork;
import net.likelion.bebc25.fixgo.repair.entity.part.RepairPartUsage;

// 견적서 한 줄. 작업은 금액이 없고, 부품·공임·점검만 금액이 있다.
public record EstimateItemResponse(
        String category,
        String name,
        Integer quantity,
        Integer amount
) {
    public static EstimateItemResponse work(RepairWork work) {
        String preset = work.getRepairPresetName();
        String name = preset == null || preset.isBlank()
                ? work.getWorkName()
                : work.getWorkName() + " (" + preset + ")";
        return new EstimateItemResponse("작업", name, null, null);
    }

    public static EstimateItemResponse part(RepairPartUsage usage) {
        return new EstimateItemResponse(
                "부품",
                usage.getPart().getName(),
                usage.getQuantity(),
                Math.multiplyExact(usage.getUnitPrice(), usage.getQuantity())
        );
    }

    public static EstimateItemResponse labor(int amount) {
        return new EstimateItemResponse("공임", "공임", null, amount);
    }

    public static EstimateItemResponse inspection(int amount) {
        return new EstimateItemResponse("점검", "점검비", null, amount);
    }
}
