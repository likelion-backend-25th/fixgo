package net.likelion.bebc25.fixgo.repair.dto.repair;

import net.likelion.bebc25.fixgo.repair.entity.part.RepairPartUsage;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 고객 포탈 견적 영역. 수리비가 확정된 뒤부터 보인다.
 * 담당 기사, 내부 작업 메모는 담지 않는다.
 */
public record CustomerQuoteView(
        Status status,
        String diagnosis,
        List<Item> items,
        int laborCost,
        int inspectionCost,
        int totalAmount,
        LocalDateTime issuedAt
) {
    /**
     * 고객 입장의 견적 상태. 승인 이후 수리가 진행되거나 끝나도 견적은 승인된 상태로 본다.
     */
    public enum Status {
        PENDING, APPROVED, REJECTED;

        static Status from(RepairStatus repairStatus) {
            return switch (repairStatus) {
                case REQUESTED -> PENDING;
                case APPROVED, REPAIRING, COMPLETED -> APPROVED;
                case REJECTED -> REJECTED;
                case DIAGNOSING -> throw new IllegalStateException("수리비 확정 전에는 견적이 없습니다.");
            };
        }
    }

    public record Item(
            String name,
            int quantity,
            int unitPrice,
            int amount
    ) {
        public static Item from(RepairPartUsage usage) {
            return new Item(
                    usage.getPart().getName(),
                    usage.getQuantity(),
                    usage.getUnitPrice(),
                    usage.getUnitPrice() * usage.getQuantity()
            );
        }
    }

    public static CustomerQuoteView of(Repair repair, List<Item> items) {
        return new CustomerQuoteView(
                Status.from(repair.getStatus()),
                repair.getDiagnosis(),
                items,
                repair.getLaborRate(),
                repair.getInspectionCost(),
                repair.getFinalAmount(),
                repair.getConfirmedAt()
        );
    }
}
