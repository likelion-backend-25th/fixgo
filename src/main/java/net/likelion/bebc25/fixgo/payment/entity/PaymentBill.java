package net.likelion.bebc25.fixgo.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.common.entity.BaseEntity;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * A/S 접수 한 건에 대한 청구서
 * 접수당 하나만 존재하며(asReceptionId) 결제 완료/환불 상태를 관리한다.
 */
@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentBill extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "as_reception_id", nullable = false, unique = true)
    private AsReception asReception;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentBillStatus status = PaymentBillStatus.UNPAID;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentBillType type;

    private LocalDateTime paidAt;

    // 청구서 생성 UNPAID 상태로 생성된다.
    public static PaymentBill create(AsReception asReception, Long amount, PaymentBillType paymentBillType) {
        if (asReception == null) {
            throw new IllegalArgumentException("A/S 접수는 필수입니다.");
        }

        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("청구 금액은 0보다 커야 합니다.");
        }

        if (paymentBillType == null) {
            throw new IllegalArgumentException("청구 유형은 필수입니다.");
        }

        PaymentBill paymentBill = new PaymentBill();
        paymentBill.asReception = asReception;
        paymentBill.amount = amount;
        paymentBill.type = paymentBillType;

        return paymentBill;
    }

    // 상태 변경 검증 메서드
    private void changeStatus(PaymentBillStatus next) {
        if (!status.canTransitTo(next)) {
            throw new IllegalStateException(status + " -> " + next + " 불가");
        }
        this.status = next;
    }

    // Paid로 상태를 변경한다.
    public void markPaid(LocalDateTime paidAt) {
        if (paidAt == null) {
            throw new IllegalArgumentException("승인시각은 필수입니다.");
        }
        changeStatus(PaymentBillStatus.PAID);
        this.paidAt = paidAt;
    }

    // Refunded로 상태를 변경한다.
    public void markRefunded() {
        changeStatus(PaymentBillStatus.REFUNDED);
    }
}
