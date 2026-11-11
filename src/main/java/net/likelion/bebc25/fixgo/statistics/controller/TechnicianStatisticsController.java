package net.likelion.bebc25.fixgo.statistics.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.statistics.dto.TechnicianStatisticsDto;
import net.likelion.bebc25.fixgo.statistics.service.TechnicianStatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Technician Statistics API", description = "기사별 통계 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/statistics")
@PreAuthorize("hasRole('ADMIN')")
public class TechnicianStatisticsController {

    private final TechnicianStatisticsService technicianStatisticsService;

    // 기사별 통계 조회
    @GetMapping("/technicians")
    @Operation(summary = "기사별 통계 조회", description = "기간별 기사별 통계를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "기사별 통계 조회 성공"),
    })
    public List<TechnicianStatisticsDto> getTechnicianStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return technicianStatisticsService.getTechnicianStatistics(startDate, endDate);
    }
}
