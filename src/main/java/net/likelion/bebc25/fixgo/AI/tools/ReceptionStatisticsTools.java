package net.likelion.bebc25.fixgo.AI.tools;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.statistics.dto.ReceptionStatisticsDto;
import net.likelion.bebc25.fixgo.statistics.service.ReceptionStatisticsService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ReceptionStatisticsTools {

    private final ReceptionStatisticsService receptionStatisticsService;

    @Tool(description = """
            기간별 A/S 접수 통계를 조회합니다.
            접수 건수, 완료 건수, 완료율(%)을 반환합니다.
            사용자가 '지난달 접수 현황', '이번 주 완료율'처럼 물을 때 사용하세요.
            """)
    public ReceptionStatisticsDto getReceptionStatistics(
            @ToolParam(description = "조회 시작일 (yyyy-MM-dd)") String startDate,
            @ToolParam(description = "조회 종료일 (yyyy-MM-dd)") String endDate
    ) {
        return receptionStatisticsService.getReceptionStatistics(
                LocalDate.parse(startDate),
                LocalDate.parse(endDate)
        );
    }
}
