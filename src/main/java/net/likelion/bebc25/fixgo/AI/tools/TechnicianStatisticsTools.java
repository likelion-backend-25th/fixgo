package net.likelion.bebc25.fixgo.AI.tools;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.statistics.dto.TechnicianStatisticsDto;
import net.likelion.bebc25.fixgo.statistics.service.TechnicianStatisticsService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TechnicianStatisticsTools {

    private final TechnicianStatisticsService technicianStatisticsService;

    @Tool(description = """
            기간별 기사(테크니션) 실적 통계를 조회합니다.
            기사별 배정/완료/진행 건수, 평균 소요 시간(분), 매출을 반환합니다.
            """)
    public List<TechnicianStatisticsDto> getTechnicianStatistics(
            @ToolParam(description = "조회 시작일 (yyyy-MM-dd)") String startDate,
            @ToolParam(description = "조회 종료일 (yyyy-MM-dd)") String endDate
    ) {
        return technicianStatisticsService.getTechnicianStatistics(
                LocalDate.parse(startDate),
                LocalDate.parse(endDate)
        );
    }
}
