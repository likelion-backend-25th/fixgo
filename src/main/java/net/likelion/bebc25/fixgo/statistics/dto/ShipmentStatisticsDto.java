package net.likelion.bebc25.fixgo.statistics.dto;

import java.time.LocalDate;

public record ShipmentStatisticsDto(LocalDate startDate, LocalDate endDate, Long readyCount, Long shippingCount,
                                    Long completedCount) {}