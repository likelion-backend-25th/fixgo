package net.likelion.bebc25.fixgo.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentRefund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_attempt_id", nullable = false)
    private PaymentAttempt paymentAttempt;

    @Column(unique = true)
    private String idempotencyKey;

    @Column(unique = true)
    private String transactionKey;

    @Column(nullable = false)
    private Long cancelAmount;

    @Column(nullable = false)
    private String cancelReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentRefundStatus status;

    private String refundBankCode;

    private String refundAccountNumber;

    private String refundHolderName;

    private String failureCode;

    private String failureMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentChannel refundChannel;

    private LocalDateTime canceledAt;

    @Column(nullable = false)
    private String createdBy;

    public PaymentRefund(PaymentAttempt paymentAttempt, Long cancelAmount, String cancelReason, String requestedBy) {
        if (paymentAttempt == null) {
            throw new IllegalArgumentException("결제 시도는 필수입니다.");
        }
        if (cancelAmount == null || cancelAmount <= 0) {
            throw new IllegalArgumentException("환불 금액은 0보다 커야 합니다.");
        }
        if (cancelReason == null || cancelReason.isBlank()) {
            throw new IllegalArgumentException("환불 사유는 필수입니다.");
        }
        if (requestedBy == null || requestedBy.isBlank()) {
            throw new IllegalArgumentException("처리 직원은 필수입니다.");
        }

        this.paymentAttempt = paymentAttempt;
        this.cancelAmount = cancelAmount;
        this.cancelReason = cancelReason;
        this.createdBy = requestedBy;
        this.status = PaymentRefundStatus.REQUESTED;
    }
}
