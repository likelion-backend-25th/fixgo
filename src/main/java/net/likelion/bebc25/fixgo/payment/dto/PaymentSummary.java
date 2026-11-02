package net.likelion.bebc25.fixgo.payment.dto;

import net.likelion.bebc25.fixgo.payment.entity.PaymentAttempt;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBill;
import net.likelion.bebc25.fixgo.payment.entity.PaymentMethod;

import java.time.LocalDateTime;

public record PaymentSummary(
        PaymentSummaryStatus status,
        String orderName,
        Long amount,
        PaymentMethod paymentMethod,
        LocalDateTime paidAt
) {
    public static PaymentSummary notIssued() {
        return new PaymentSummary(
                PaymentSummaryStatus.NOT_ISSUED,
                null,
                null,
                null,
                null
        );
    }

    public static PaymentSummary unpaid(PaymentBill bill) {
        return new PaymentSummary(
                PaymentSummaryStatus.UNPAID,
                bill.getType().getOrderName(),
                bill.getAmount(),
                null,
                null
        );
    }

    public static PaymentSummary paid(PaymentBill bill, PaymentAttempt doneAttempt) {
        return new PaymentSummary(
                PaymentSummaryStatus.PAID,
                bill.getType().getOrderName(),
                bill.getAmount(),
                doneAttempt.getMethod(),
                bill.getPaidAt()
        );
    }

    // TODO: 가상계좌
//    public static PaymentSummary waitingForDeposit

    /**
     * 가상계좌 입금 안내 정보. 고객이 입금할 때 필요한 값만 담는다.
     */
    public record VirtualAccountInfo(
            String bankName,
            String accountNumber,
            LocalDateTime dueAt
    ) {}
}