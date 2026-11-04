package net.likelion.bebc25.fixgo.statistics.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.statistics.dto.TechnicianStatisticsDto;
import net.likelion.bebc25.fixgo.statistics.service.TechnicianStatisticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/statistics")
@PreAuthorize("hasRole('ADMIN')")
public class TechnicianStatisticsController {

    private final TechnicianStatisticsService technicianStatisticsService;

    // 기사별 통계 조회
    @GetMapping("/technicians")
    public List<TechnicianStatisticsDto> getTechnicianStatistics() {
        return technicianStatisticsService.getTechnicianStatistics();
    }
}
