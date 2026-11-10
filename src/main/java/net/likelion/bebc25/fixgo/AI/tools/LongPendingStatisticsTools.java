package net.likelion.bebc25.fixgo.AI.tools;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.statistics.dto.LongPendingStatisticsDto;
import net.likelion.bebc25.fixgo.statistics.service.LongPendingStatisticsService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LongPendingStatisticsTools {

    private final LongPendingStatisticsService longPendingStatisticsService;

    @Tool(description = """
            장기 미처리 A/S 접수 목록을 조회합니다.
            14일 이상 완료되지 않은 접수와 경과 일수를 반환합니다.
            날짜 파라미터 없이 현재 기준 장기 미처리 건이 필요할 때 사용하세요.
            """)
    public List<LongPendingStatisticsDto.LongPendingResponse> getLongPendingReceptions() {
        return longPendingStatisticsService.getLongPendingReceptions();
    }
}
