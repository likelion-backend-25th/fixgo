package net.likelion.bebc25.fixgo.statistics.controller;

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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/statistics")
@PreAuthorize("hasRole('ADMIN')")
public class PaymentStatisticsController {

    private final PaymentStatisticsService paymentStatisticsService;

    @GetMapping("/payments")
    public PaymentStatisticsDto.PaymentStatisticsResponse getPaymentStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return paymentStatisticsService.getPaymentStatistics(startDate, endDate);
    }

    @GetMapping("/unpaid-bills")
    public List<PaymentStatisticsDto.UnpaidBillResponse> getUnpaidBills() {
        return paymentStatisticsService.getUnpaidBills();
    }
}