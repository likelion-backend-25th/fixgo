package net.likelion.bebc25.fixgo.statistics.dto;

import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;

import java.time.LocalDateTime;

public class LongPendingStatisticsDto {

    public record LongPendingResponse(String receptionNumber, String customerName, String modelName,
                                      AsReceptionStatus status, String technicianName, long elapsedDays,
                                      LocalDateTime lastActivityAt) {}
}