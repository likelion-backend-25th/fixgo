package net.likelion.bebc25.fixgo.payment.dto;

import net.likelion.bebc25.fixgo.payment.entity.PaymentAttempt;

public class PaymentDto {

    /**
     * 결제 시작 응답.
     * orderId와 amount는 승인 시 서버가 DB 값과 대조해 검증한다.
     */
    public record StartPaymentResponse(
            String orderId,
            Long amount,
            String orderName
    ) {
        public static StartPaymentResponse from(PaymentAttempt attempt) {
            return new StartPaymentResponse(
                    attempt.getOrderId(),
                    attempt.getAmount(),
                    attempt.getPaymentBill().getType().getOrderName()
            );
        }
    }

    public record ConfirmPaymentRequest(
            String orderId,
            String paymentKey,
            Long amount
    ) {}

    public record ConfirmPaymentResponse() {}
}
