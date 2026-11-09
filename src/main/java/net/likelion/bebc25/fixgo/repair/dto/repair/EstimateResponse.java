package net.likelion.bebc25.fixgo.repair.dto.repair;

import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;

import java.time.LocalDateTime;
import java.util.List;

// 견적서 조회 응답 DTO
public record EstimateResponse(
        Long receptionId,
        String receptionNumber,
        LocalDateTime confirmedAt,
        int laborRate,
        int inspectionCost,
        int totalAmount,
        List<EstimateItemResponse> items
) {
    public static EstimateResponse from(Repair repair, List<EstimateItemResponse> items) {
        AsReception reception = repair.getAsReception();

        return new EstimateResponse(
                reception.getId(),
                reception.getReceptionNumber(),
                repair.getConfirmedAt(),
                repair.getLaborRate(),
                repair.getInspectionCost(),
                repair.getFinalAmount(),
                items
        );
    }
}
