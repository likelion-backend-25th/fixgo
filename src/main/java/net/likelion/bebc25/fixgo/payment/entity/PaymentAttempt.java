package net.likelion.bebc25.fixgo.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_bill_id", nullable = false)
    private PaymentBill paymentBill;

    @Column(nullable = false, unique = true)
    private String orderId;

    @Column(unique = true)
    private String paymentKey;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentAttemptStatus status = PaymentAttemptStatus.READY;

    private String failureCode;

    private String failureMessage;

    private LocalDateTime approvedAt;

    private Long createdBy;

    public static PaymentAttempt create(PaymentBill paymentBill) {
        if (paymentBill == null) {
            throw new IllegalArgumentException("paymentBill is null");
        }

        PaymentAttempt paymentAttempt = new PaymentAttempt();

        paymentAttempt.paymentBill = paymentBill;
        paymentAttempt.orderId = "PAY" + UUID.randomUUID().toString();
        paymentAttempt.amount = paymentBill.getAmount();

        return paymentAttempt;
    }

    public void markFailed(String code, String message) {
        changeStatus(PaymentAttemptStatus.FAILED);
        this.failureCode = code;
        this.failureMessage = message;
    }

    public void markCardDone(String paymentKey) {
        changeStatus(PaymentAttemptStatus.DONE);
        this.paymentKey = paymentKey;
        this.method = PaymentMethod.CARD;
        this.approvedAt = LocalDateTime.now();
    }

    public void markCashDone(Long createdBy) {
        changeStatus(PaymentAttemptStatus.DONE);
        this.method = PaymentMethod.CASH;
        this.createdBy = createdBy;
        this.approvedAt = LocalDateTime.now();
    }

    private void changeStatus(PaymentAttemptStatus next) {
        if (!status.canTransitTo(next)) {
            throw new IllegalStateException(status + " -> " + next + " 불가");
        }
        this.status = next;
    }
}
