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
            throw new IllegalArgumentException("청구서가 존재하지 않습니다.");
        }

        PaymentAttempt paymentAttempt = new PaymentAttempt();

        paymentAttempt.paymentBill = paymentBill;
        paymentAttempt.orderId = "PAY-" + UUID.randomUUID().toString();
        paymentAttempt.amount = paymentBill.getAmount();

        return paymentAttempt;
    }

    /**
     * 상태 전이 규칙을 검증하고 paymentAttempt를 FAILED로 바꾼다.
     */
    public void markFailed(String code, String message) {
        changeStatus(PaymentAttemptStatus.FAILED);
        this.failureCode = code;
        this.failureMessage = message;
    }

    /**
     * 상태 전이 규칙을 검증하고 paymentAttempt를 IN_PROGRESS로 바꾼다.
     */
    public void markInProgress() {
        changeStatus(PaymentAttemptStatus.IN_PROGRESS);
    }

    /**
     * 가상계좌 발급 완료 처리. 계좌가 발급되었고 고객의 입금을 기다리는 상태로 바꾼다.
     * 반드시 승인 준비(IN_PROGRESS)를 거친 시도만 처리할 수 있다.
     * 계좌 정보(은행, 계좌번호, 입금 기한, 웹훅 검증 값)는 VirtualAccount에 따로 저장한다.
     * 아직 입금 전이므로 승인 시각은 기록하지 않는다.
     *
     * @param paymentKey 토스 승인 응답의 paymentKey (결제 조회, 취소 시 필요)
     */
    public void markWaitingForDeposit(String paymentKey) {
        if (this.getStatus() != PaymentAttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("가상계좌 발급 완료는 IN_PROGRESS 상태에서만 가능합니다. 현재: " + status);
        }
        changeStatus(PaymentAttemptStatus.WAITING_FOR_DEPOSIT);
        this.paymentKey = paymentKey;
        this.method = PaymentMethod.VIRTUAL_ACCOUNT;
    }

    /**
     * 온라인(카드) 결제 승인 완료 처리
     * 반드시 승인 준비(IN_PROGRESS)를 거친 시도만 완료할 수 있다.
     *
     * @param paymentKey 토스 승인 응답의 paymentKey (환불 시 필요)
     * @param approvedAt 토스 승인 시각
     */
    public void markCardDone(String paymentKey, LocalDateTime approvedAt) {
        if (this.getStatus() != PaymentAttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("카드 결제 완료는 IN_PROGRESS 상태에서만 가능합니다. 현재: " + status);
        }
        changeStatus(PaymentAttemptStatus.DONE);
        this.paymentKey = paymentKey;
        this.method = PaymentMethod.CARD;
        this.approvedAt = approvedAt;
    }

    /**
     * 온라인(가상계좌) 결제 승인 완료 처리
     * 반드시 입금 대기(WAITING_FOR_DEPOSIT)를 거친 시도만 완료할 수 있다.
     */
    public void markVirtualAccountDone(LocalDateTime approvedAt) {
        if (this.getStatus() != PaymentAttemptStatus.WAITING_FOR_DEPOSIT) {
            throw new IllegalStateException("가상 계좌 결제 완료는 WAITING_FOR_DEPOSIT 상태에서만 가능합니다. 현재: " + status);
        }
        this.approvedAt = approvedAt;
        changeStatus(PaymentAttemptStatus.DONE);
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
