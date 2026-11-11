package net.likelion.bebc25.fixgo.AI.tools;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.statistics.dto.PaymentStatisticsDto;
import net.likelion.bebc25.fixgo.statistics.service.PaymentStatisticsService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentStatisticsTools {

    private final PaymentStatisticsService paymentStatisticsService;

    @Tool(description = """
            기간별 결제 통계를 조회합니다.
            승인 금액, 환불 금액, 순수 수납액, 현재 미입금 건수를 반환합니다.
            """)
    public PaymentStatisticsDto.PaymentStatisticsResponse getPaymentStatistics(
            @ToolParam(description = "조회 시작일 (yyyy-MM-dd)") String startDate,
            @ToolParam(description = "조회 종료일 (yyyy-MM-dd)") String endDate
    ) {
        return paymentStatisticsService.getPaymentStatistics(
                LocalDate.parse(startDate),
                LocalDate.parse(endDate)
        );
    }

    @Tool(description = """
            현재 미입금(미수금) 청구서 목록을 조회합니다.
            날짜 조건 없이 현재 미입금 건만 필요할 때 사용하세요.
            """)
    public List<PaymentStatisticsDto.UnpaidBillResponse> getUnpaidBills() {
        return paymentStatisticsService.getUnpaidBills();
    }
}
