package net.likelion.bebc25.fixgo.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * A/S 접수 한 건에 대한 청구서
 * 접수당 하나만 존재하며(asReceptionId) 결제 완료/환불 상태를 관리한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentBill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long asReceptionId;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentBillStatus status = PaymentBillStatus.UNPAID;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentBillType type;

    private LocalDateTime paidAt;


    public static PaymentBill issue(Long asReceptionId, Long amount, PaymentBillType paymentBillType) {
        if (asReceptionId == null) {
            throw new IllegalArgumentException("A/S 접수 ID는 필수입니다.");
        }

        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("청구 금액은 0보다 커야 합니다.");
        }

        PaymentBill paymentBill = new PaymentBill();
        paymentBill.asReceptionId = asReceptionId;
        paymentBill.amount = amount;
        paymentBill.type = paymentBillType;

        return paymentBill;
    }

    private void changeStatus(PaymentBillStatus next) {
        if (!status.canTransitTo(next)) {
            throw new IllegalStateException(status + " -> " + next + " 불가");
        }
        this.status = next;
    }

    public void markPaid(LocalDateTime paidAt) {
        changeStatus(PaymentBillStatus.PAID);
        this.paidAt = paidAt;
    }

    public void markRefunded() {
        changeStatus(PaymentBillStatus.REFUNDED);
    }
}
