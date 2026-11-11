package net.likelion.bebc25.fixgo.statistics.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.statistics.dto.LongPendingStatisticsDto;
import net.likelion.bebc25.fixgo.statistics.service.LongPendingStatisticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Long Pending Statistics API", description = "장기 미처리 접수건 통계 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/statistics")
@PreAuthorize("hasRole('ADMIN')")
public class LongPendingStatisticsController {

    private final LongPendingStatisticsService longPendingStatisticsService;

    // 장기 미처리 접수건 조회 (상태 변경 후 14일 경과)
    @GetMapping("/long_pending")
    @Operation(summary = "장기 미처리 접수건 조회", description = "상태 변경 후 14일이 경과한 장기 미처리 접수건을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "장기 미처리 접수건 조회 성공"),
    })
    public List<LongPendingStatisticsDto.LongPendingResponse> getLongPendingReceptions() {
        return longPendingStatisticsService.getLongPendingReceptions();
    }
}
