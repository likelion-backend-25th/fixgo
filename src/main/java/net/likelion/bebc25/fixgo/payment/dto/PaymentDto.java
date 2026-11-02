package net.likelion.bebc25.fixgo.payment.dto;

import net.likelion.bebc25.fixgo.payment.entity.PaymentAttempt;

import java.time.LocalDateTime;

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

    /**
     * 결제 승인 요청. 프론트는 토스 결제창의 successUrl로 돌아올 때
     * 쿼리 파라미터로 받은 세 값을 그대로 보낸다.
     * <p>
     * 서버는 이 값을 신뢰하지 않는다.
     * paymentKey는 토스 승인 응답의 값을 저장한다.
     */
    public record ConfirmPaymentRequest(
            String orderId,
            String paymentKey,
            Long amount
    ) {}

    /**
     * 결제 승인 응답. 프론트는 결제 완료 화면에 이 값을 보여준다.
     * <p>
     * 토스 응답이 아니라 DB에 저장된 attempt로 만든다.
     */
    public record ConfirmPaymentResponse(
            String orderName,
            Long amount,
            LocalDateTime approvedAt
    ) {
        public static ConfirmPaymentResponse from(PaymentAttempt attempt) {
            return new ConfirmPaymentResponse(
                    attempt.getPaymentBill().getType().getOrderName(),
                    attempt.getAmount(),
                    attempt.getApprovedAt()
            );
        }
    }
}
