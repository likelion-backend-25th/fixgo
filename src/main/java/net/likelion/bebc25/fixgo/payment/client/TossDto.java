package net.likelion.bebc25.fixgo.payment.client;

import java.time.OffsetDateTime;

public class TossDto {

    public record ConfirmRequest(String paymentKey,
                                 String orderId,
                                 Long amount) {}

    public record ConfirmResponse(
            String paymentKey,
            String orderId,
            String status,
            OffsetDateTime approvedAt) {}

    public record ErrorResponse(String code, String message) {}
}

