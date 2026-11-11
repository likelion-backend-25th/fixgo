package net.likelion.bebc25.fixgo.statistics.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.statistics.dto.PaymentStatisticsDto;
import net.likelion.bebc25.fixgo.statistics.service.PaymentStatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Payment Statistics API", description = "결제/미수금 통계 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/statistics")
@PreAuthorize("hasRole('ADMIN')")
public class PaymentStatisticsController {

    private final PaymentStatisticsService paymentStatisticsService;

    @GetMapping("/payments")
    @Operation(summary = "결제 통계 조회", description = "기간별 결제 통계를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결제 통계 조회 성공"),
    })
    public PaymentStatisticsDto.PaymentStatisticsResponse getPaymentStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return paymentStatisticsService.getPaymentStatistics(startDate, endDate);
    }

    @GetMapping("/unpaid-bills")
    @Operation(summary = "미수금 청구서 조회", description = "미수금 청구서 목록을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "미수금 청구서 조회 성공"),
    })
    public List<PaymentStatisticsDto.UnpaidBillResponse> getUnpaidBills() {
        return paymentStatisticsService.getUnpaidBills();
    }
}
