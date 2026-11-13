package net.likelion.bebc25.fixgo.statistics.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.List;

@Tag(name = "Shipment Statistics API", description = "배송 현황 통계 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/statistics")
@PreAuthorize("hasRole('ADMIN')")
public class ShipmentStatisticsController {

    private final ShipmentStatisticsService shipmentStatisticsService;

    // 배송 현황 통계 조회
    @GetMapping("/shipments")
    @Operation(summary = "배송 현황 통계 조회", description = "기간별 배송 현황 통계를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배송 현황 통계 조회 성공"),
    })
    public ShipmentStatisticsDto getShipmentStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return shipmentStatisticsService.getShipmentStatistics(startDate, endDate);
    }

    @GetMapping("/shipments/history")
    @Operation(summary = "기간 배송 내역 조회", description = "조회 기간에 등록된 배송 내역을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "기간 배송 내역 조회 성공"),
    })
    public List<ShipmentStatisticsDto.ShipmentHistoryResponse> getShipmentHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return shipmentStatisticsService.getShipmentHistory(startDate, endDate);
    }
}
