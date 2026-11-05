package net.likelion.bebc25.fixgo.statistics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReceptionStatisticsDto(LocalDate startDate, LocalDate endDate, Long receivedCount, Long completedCount,
                                     BigDecimal completionRate) {}