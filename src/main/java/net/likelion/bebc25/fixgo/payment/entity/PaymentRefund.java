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
    private String transactionKey;

    private Long cancelAmount;

    @Column(nullable = false)
    private String cancelReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentRefundStatus status;

    private String failureCode;

    private String failureMessage;

    private LocalDateTime canceledAt;

    @Column(nullable = false)
    private Long createdBy;

    public static PaymentRefund create(PaymentAttempt paymentAttempt, String cancelReason, Long requestedBy) {
        if (paymentAttempt == null) {
            throw new IllegalArgumentException("결제 시도는 필수입니다.");
        }
        if (cancelReason == null || cancelReason.isBlank()) {
            throw new IllegalArgumentException("환불 사유는 필수입니다.");
        }
        if (requestedBy == null) {
            throw new IllegalArgumentException("처리 직원은 필수입니다.");
        }
        PaymentRefund refund = new PaymentRefund();

        refund.paymentAttempt = paymentAttempt;
        refund.cancelReason = cancelReason;
        refund.createdBy = requestedBy;
        refund.status = PaymentRefundStatus.IN_PROGRESS;

        return refund;
    }

    public void markDone(String transactionKey, LocalDateTime canceledAt) {
        this.transactionKey = transactionKey;
        this.status = PaymentRefundStatus.DONE;
        this.canceledAt = canceledAt;
    }

    public void markFailed(String errorCode, String errorMessage) {
        this.status = PaymentRefundStatus.FAILED;
        this.failureCode = errorCode;
        this.failureMessage = errorMessage;
    }
}
