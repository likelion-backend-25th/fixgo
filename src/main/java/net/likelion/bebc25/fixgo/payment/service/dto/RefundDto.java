package net.likelion.bebc25.fixgo.payment.service.dto;

public class RefundDto {
    /**
     * @param completed true면 토스 호출 없이 이미 환불이 끝난 것이다(현금 환불).
     */
    public record PreparedRefund(
            Long refundId,
            String paymentKey,
            RefundAccount refundAccount,
            boolean completed
    ) {
        public static PreparedRefund completed(Long refundId) {
            return new PreparedRefund(refundId, null, null, true);
        }
    }

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
