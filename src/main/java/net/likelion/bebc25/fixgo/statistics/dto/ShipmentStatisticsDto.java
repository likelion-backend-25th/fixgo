package net.likelion.bebc25.fixgo.statistics.dto;

import net.likelion.bebc25.fixgo.shipment.entity.ReturnMethod;
import net.likelion.bebc25.fixgo.shipment.entity.ShipmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ShipmentStatisticsDto(LocalDate startDate, LocalDate endDate, Long readyCount, Long shippingCount,
                                    Long completedCount) {
    public record ShipmentHistoryResponse(Long shipmentId, Long receptionId, String receptionNumber,
                                          String customerName, ReturnMethod returnMethod, ShipmentStatus status,
                                          LocalDateTime createdAt) {}
}