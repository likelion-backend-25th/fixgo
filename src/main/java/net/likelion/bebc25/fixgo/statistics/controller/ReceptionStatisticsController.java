package net.likelion.bebc25.fixgo.statistics.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.statistics.dto.ReceptionStatisticsDto;
import net.likelion.bebc25.fixgo.statistics.service.ReceptionStatisticsService;
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
public class ReceptionStatisticsController {

    private final ReceptionStatisticsService receptionStatisticsService;

    // 기간별 접수·완료 건수와 완료율 조회
    @GetMapping("/receptions")
    public ReceptionStatisticsDto getReceptionStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return receptionStatisticsService.getReceptionStatistics(startDate, endDate);
    }
}