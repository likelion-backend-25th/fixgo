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
        int finalAmount
) {

    public static RepairStatementResponse from(
            Repair repair,
            List<RepairWorkHistoryResponse> repairWorks,
            LocalDateTime issuedAt
    ) {
        AsReception reception = repair.getAsReception();

        return new RepairStatementResponse(
                reception.getReceptionNumber(),
                reception.getCustomer().getName(),
                issuedAt,
                repair.getTechnician().getName(),
                repair.getWorkNote(),
                repairWorks,
                repair.getFinalAmount()
        );
    }

}
