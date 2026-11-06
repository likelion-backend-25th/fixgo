package net.likelion.bebc25.fixgo.statistics.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.statistics.dto.LongPendingStatisticsDto;
import net.likelion.bebc25.fixgo.statistics.service.LongPendingStatisticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/statistics")
@PreAuthorize("hasRole('ADMIN')")
public class LongPendingStatisticsController {

    private final LongPendingStatisticsService longPendingStatisticsService;

    // 장기 미처리 접수건 조회 (상태 변경 후 14일 경과)
    @GetMapping("/long_pending")
    public List<LongPendingStatisticsDto.LongPendingResponse> getLongPendingReceptions() {
        return longPendingStatisticsService.getLongPendingReceptions();
    }
}