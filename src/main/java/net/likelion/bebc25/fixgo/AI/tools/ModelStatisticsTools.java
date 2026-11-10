package net.likelion.bebc25.fixgo.AI.tools;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.statistics.dto.ModelStatisticsDto;
import net.likelion.bebc25.fixgo.statistics.service.ModelStatisticsService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ModelStatisticsTools {

    private final ModelStatisticsService modelStatisticsService;

    @Tool(description = """
            기간별 모델(제품) 수리 통계를 조회합니다.
            모델별 접수/완료/진행 건수와 주요 증상을 반환합니다.
            """)
    public List<ModelStatisticsDto.ModelStatisticsResponse> getModelStatistics(
            @ToolParam(description = "조회 시작일 (yyyy-MM-dd)") String startDate,
            @ToolParam(description = "조회 종료일 (yyyy-MM-dd)") String endDate
    ) {
        return modelStatisticsService.getModelStatistics(
                LocalDate.parse(startDate),
                LocalDate.parse(endDate)
        );
    }

    @Tool(description = """
            특정 모델의 기간별 증상 통계를 조회합니다.
            증상명, 건수, 비율(%)을 반환합니다.
            """)
    public List<ModelStatisticsDto.ModelSymptomStatisticsResponse> getModelSymptomStatistics(
            @ToolParam(description = "모델 ID") Long modelId,
            @ToolParam(description = "조회 시작일 (yyyy-MM-dd)") String startDate,
            @ToolParam(description = "조회 종료일 (yyyy-MM-dd)") String endDate
    ) {
        return modelStatisticsService.getModelSymptomStatistics(
                modelId,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate)
        );
    }
}
