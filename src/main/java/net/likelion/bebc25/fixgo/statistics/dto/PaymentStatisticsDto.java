package net.likelion.bebc25.fixgo.statistics.dto;

import net.likelion.bebc25.fixgo.payment.entity.PaymentBillStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PaymentStatisticsDto {
    public record PaymentStatisticsResponse(LocalDate startDate, LocalDate endDate, Long totalPaidAmount,
                                            Long totalRefundAmount, Long netCollectedAmount, Long unpaidCount) {}

    public record UnpaidBillResponse(Long billId, String receptionNumber, String customerName, String modelName,
                                     Long amount, LocalDateTime billedAt, PaymentBillStatus status) {}

    public record PaymentHistoryResponse(Long billId, String receptionNumber, String customerName, Long amount,
                                         PaymentBillStatus status, LocalDateTime occurredAt) {}
}
