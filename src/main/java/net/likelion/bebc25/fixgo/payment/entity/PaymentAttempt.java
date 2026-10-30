package net.likelion.bebc25.fixgo.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @Column(unique = true)
    private String orderId;

    @Column(unique = true)
    private String paymentKey;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentAttemptStatus status;

    private String failureCode;

    private String failureMessage;

    private LocalDateTime approvedAt;

    @Column(nullable = false, updatable = false)
    private String createdBy;

    public static PaymentAttempt createOnline(PaymentBill paymentBill, String orderId) {
        if (paymentBill == null) {
            throw new IllegalArgumentException("청구서는 필수입니다.");
        }

        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("주문번호는 필수입니다.");
        }

        PaymentAttempt attempt = new PaymentAttempt();

        attempt.paymentBill = paymentBill;
        attempt.orderId = orderId;
        attempt.amount = paymentBill.getAmount();
        attempt.channel = PaymentChannel.ONLINE;
        attempt.status = PaymentAttemptStatus.READY;

        return attempt;
    }

    public static PaymentAttempt createOnsite(PaymentBill paymentBill, PaymentMethod method, String handledBy, LocalDateTime approvedAt) {
        if (paymentBill == null) {
            throw new IllegalArgumentException("청구서는 필수입니다.");
        }

        if (method != PaymentMethod.CARD && method != PaymentMethod.CASH) {
            throw new IllegalArgumentException("현장 수납은 카드 또는 현금만 가능합니다.");
        }

        if (handledBy == null) {
            throw new IllegalArgumentException("수납 담당 직원은 필수입니다.");
        }

        if (approvedAt == null) {
            throw new IllegalArgumentException("수납 일시는 필수입니다.");
        }

        PaymentAttempt attempt = new PaymentAttempt();

        attempt.paymentBill = paymentBill;
        attempt.amount = paymentBill.getAmount();
        attempt.method = method;
        attempt.channel = PaymentChannel.ONSITE;
        attempt.status = PaymentAttemptStatus.DONE;
        attempt.createdBy = handledBy;
        attempt.approvedAt = approvedAt;

        return attempt;
    }
}
