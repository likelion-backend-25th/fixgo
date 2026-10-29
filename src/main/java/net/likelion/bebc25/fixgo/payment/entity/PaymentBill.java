package net.likelion.bebc25.fixgo.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentBill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private Long asRequestId;

    @Column(nullable = false, updatable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentBillStatus status;

    private Long successfulAttemptId;

    private LocalDateTime paidAt;

    public PaymentBill(Long asRequestId, Long amount) {
        if (asRequestId == null) {
            throw new IllegalArgumentException("A/S 접수 ID는 필수입니다.");
        }

        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("청구 금액은 0보다 커야 합니다.");
        }

        this.asRequestId = asRequestId;
        this.amount = amount;
        this.status = PaymentBillStatus.UNPAID;
    }
}
