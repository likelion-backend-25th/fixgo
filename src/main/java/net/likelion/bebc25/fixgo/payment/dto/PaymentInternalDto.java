package net.likelion.bebc25.fixgo.payment.dto;

public class PaymentInternalDto {
    public record PreparedRefund(
            Long refundId,
            String paymentKey,
            RefundAccount refundAccount
    ) {}

    public record RefundAccount(
            String bank,
            String accountNumber,
            String holderName
    ) {}
}
