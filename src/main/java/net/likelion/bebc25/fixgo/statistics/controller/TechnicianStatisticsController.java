package net.likelion.bebc25.fixgo.statistics.controller;

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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/statistics")
@PreAuthorize("hasRole('ADMIN')")
public class TechnicianStatisticsController {

    private final TechnicianStatisticsService technicianStatisticsService;

    // 기사별 통계 조회
    @GetMapping("/technicians")
    public List<TechnicianStatisticsDto> getTechnicianStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return technicianStatisticsService.getTechnicianStatistics(startDate, endDate);
    }
}
