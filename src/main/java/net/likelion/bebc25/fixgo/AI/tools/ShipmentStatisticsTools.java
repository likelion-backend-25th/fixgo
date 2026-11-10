package net.likelion.bebc25.fixgo.AI.tools;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.statistics.dto.ShipmentStatisticsDto;
import net.likelion.bebc25.fixgo.statistics.service.ShipmentStatisticsService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ShipmentStatisticsTools {

    private final ShipmentStatisticsService shipmentStatisticsService;

    @Tool(description = """
            기간별 출고(배송) 통계를 조회합니다.
            배송대기, 배송중, 완료 건수를 반환합니다.
            """)
    public ShipmentStatisticsDto getShipmentStatistics(
            @ToolParam(description = "조회 시작일 (yyyy-MM-dd)") String startDate,
            @ToolParam(description = "조회 종료일 (yyyy-MM-dd)") String endDate
    ) {
        return shipmentStatisticsService.getShipmentStatistics(
                LocalDate.parse(startDate),
                LocalDate.parse(endDate)
        );
    }
}
