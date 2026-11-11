package net.likelion.bebc25.fixgo.repair.dto.repair;

import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;

import java.time.LocalDateTime;
import java.util.List;

// 수리내역서 응답 DTO
public record RepairStatementResponse(
        String receptionNumber,
        String customerName,
        LocalDateTime issuedAt,
        String technicianName,
        String workNote,
        List<RepairWorkHistoryResponse> repairWorks,
        int laborCost,
        int finalAmount
) {

    public static RepairStatementResponse from(
            Repair repair,
            List<RepairWorkHistoryResponse> repairWorks,
            LocalDateTime issuedAt
    ) {
        AsReception reception = repair.getAsReception();
        var technician = reception.getTechnician();

        return new RepairStatementResponse(
                reception.getReceptionNumber(),
                reception.getCustomer().getName(),
                issuedAt,
                technician == null ? repair.getTechnician().getName() : technician.getName(),
                repair.getWorkNote(),
                repairWorks,
                // laborRate 컬럼에는 확정된 공임비 합계가 저장된다.
                repair.getLaborRate(),
                repair.getFinalAmount()
        );
    }

}
