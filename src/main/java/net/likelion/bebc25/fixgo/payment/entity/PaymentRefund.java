package net.likelion.bebc25.fixgo.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.common.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentRefund extends BaseEntity {
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
        refund.cancelAmount = paymentAttempt.getAmount();
        refund.status = PaymentRefundStatus.IN_PROGRESS;

        return refund;
    }

    public void markDone(String transactionKey, LocalDateTime canceledAt) {
        changeStatus(PaymentRefundStatus.DONE);
        this.transactionKey = transactionKey;
        this.canceledAt = canceledAt;
    }

    public void markFailed(String errorCode, String errorMessage) {
        changeStatus(PaymentRefundStatus.FAILED);
        this.failureCode = errorCode;
        this.failureMessage = errorMessage;
    }

    private void changeStatus(PaymentRefundStatus next) {
        if (!status.canTransitTo(next)) {
            throw new IllegalStateException(status + " -> " + next + " 불가");
        }
        this.status = next;
    }
}
