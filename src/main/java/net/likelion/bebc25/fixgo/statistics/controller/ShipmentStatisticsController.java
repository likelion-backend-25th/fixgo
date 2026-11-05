package net.likelion.bebc25.fixgo.statistics.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.statistics.dto.ShipmentStatisticsDto;
import net.likelion.bebc25.fixgo.statistics.service.ShipmentStatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/statistics")
@PreAuthorize("hasRole('ADMIN')")
public class ShipmentStatisticsController {

    private final ShipmentStatisticsService shipmentStatisticsService;

    // 배송 현황 통계 조회
    @GetMapping("/shipments")
    public ShipmentStatisticsDto getShipmentStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return shipmentStatisticsService.getShipmentStatistics(startDate, endDate);
    }
}