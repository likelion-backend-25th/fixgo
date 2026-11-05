package net.likelion.bebc25.fixgo.statistics.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.statistics.dto.ShipmentStatisticsDto;
import net.likelion.bebc25.fixgo.statistics.service.ShipmentStatisticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/statistics")
@PreAuthorize("hasRole('ADMIN')")
public class ShipmentStatisticsController {

    private final ShipmentStatisticsService shipmentStatisticsService;

    // 배송 현황 통계 조회
    @GetMapping("/shipments")
    public ShipmentStatisticsDto getShipmentStatistics() {
        return shipmentStatisticsService.getShipmentStatistics();
    }
}