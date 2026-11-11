package net.likelion.bebc25.fixgo.as.dto;

import java.time.LocalDateTime;

public class AsHistoryListDto {

    public record StatusItem(
            Long id,
            Long receptionId,
            String receptionNumber,
            String customerName,
            String modelName,
            String fromStatus,
            String toStatus,
            String reason,
            String changedBy,
            LocalDateTime createdAt
    ) {
    }

    public record TechnicianItem(
            Long id,
            Long receptionId,
            String receptionNumber,
            String customerName,
            String modelName,
            String previousTechnicianName,
            String newTechnicianName,
            String reason,
            String changedBy,
            LocalDateTime createdAt
    ) {
    }
}
