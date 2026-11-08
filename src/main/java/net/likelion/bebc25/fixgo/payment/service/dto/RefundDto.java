package net.likelion.bebc25.fixgo.payment.service.dto;

public class RefundDto {
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
    
    public record StuckRefund(
            Long refundId,
            String paymentKey
    ) {}
}
