package net.likelion.bebc25.fixgo.statistics.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttemptStatus;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBillStatus;
import net.likelion.bebc25.fixgo.payment.entity.PaymentRefundStatus;
import net.likelion.bebc25.fixgo.statistics.dto.PaymentStatisticsDto;
import net.likelion.bebc25.fixgo.statistics.repository.PaymentStatisticsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentStatisticsService {

    private final PaymentStatisticsRepository paymentStatisticsRepository;

    // 결제 통계 조회
    public PaymentStatisticsDto getPaymentStatistics(LocalDate startDate, LocalDate endDate) {
        // 날짜 유효성 검사
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("조회 시작일과 종료일은 필수입니다.");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작일은 종료일보다 늦을 수 없습니다.");
        }

        // 조회 기간 설정
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay(); // 10월 31일까지 조회라면 11월 1일 00시까지 조회이므로 +1

        // 승인 완료된 결제 금액
        Long totalPaidAmount = paymentStatisticsRepository.sumApprovedAmount(PaymentAttemptStatus.DONE, startDateTime,
                                                                             endDateTime);

        // 환불 완료 금액
        Long totalRefundAmount = paymentStatisticsRepository.sumRefundAmount(PaymentRefundStatus.DONE, startDateTime,
                                                                             endDateTime);

        // 현재 미입금 건수
        Long unpaidCount = paymentStatisticsRepository.countUnpaidBills(PaymentBillStatus.UNPAID);

        // 순수 수납액 계산 (결제와 환불이 각각 발생한 시점을 기준으로 계산)
        Long netCollectedAmount = totalPaidAmount - totalRefundAmount;

        return new PaymentStatisticsDto(startDate, endDate, totalPaidAmount, totalRefundAmount, netCollectedAmount,
                                        unpaidCount);
    }
}