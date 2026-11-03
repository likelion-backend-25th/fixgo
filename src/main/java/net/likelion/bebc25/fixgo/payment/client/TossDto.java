package net.likelion.bebc25.fixgo.payment.client;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class TossDto {

    public record ConfirmRequest(String paymentKey,
                                 String orderId,
                                 Long amount) {}

    /**
     * 토스 승인 응답(Payment 객체). 카드와 가상계좌가 같은 모양으로 온다.
     * 카드: virtualAccount가 null. 가상계좌: approvedAt이 null(아직 입금 전).
     */
    public record ConfirmResponse(
            String paymentKey,
            String orderId,
            String status,
            OffsetDateTime approvedAt,
            String secret,                    // 가상계좌 웹훅 검증 값. virtualAccount 안이 아니라 최상위에 있음
            VirtualAccount virtualAccount     // 가상계좌 결제일 때만 값이 있음
    ) {
        /**
         * 토스 응답 안의 가상계좌 정보. 저장할 필드만 선언한다
         */
        public record VirtualAccount(
                String accountNumber,
                String bankCode,
                OffsetDateTime dueDate
        ) {}
    }

    public record ErrorResponse(String code, String message) {}

    /**
     * 토스 가상계좌 웹훅
     */
    public record DepositCallbackWebhook(
            LocalDateTime createdAt,
            String secret,
            String status,
            String orderId
    ) {}
}

