package net.likelion.bebc25.fixgo.as.dto;

import net.likelion.bebc25.fixgo.as.entity.AsStatusHistory;

import java.time.LocalDateTime;

public record AsStatusHistoryDto(Long id, String fromStatus, String toStatus, String reason, String changedBy,
                                 LocalDateTime createdAt) {
    public static AsStatusHistoryDto from(AsStatusHistory history) {
        return new AsStatusHistoryDto(history.getId(), history.getFromStatus(), history.getToStatus(), history.getReason(), history.getChangedBy() != null ? history.getChangedBy().getName() : null, history.getCreatedAt());
    }
}
