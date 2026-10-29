package net.likelion.bebc25.fixgo.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VirtualAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_attempt_id", nullable = false, unique = true)
    private PaymentAttempt paymentAttempt;

    @Column(nullable = false)
    private String bankCode;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String webhookSecret;

    @Column(nullable = false)
    private LocalDateTime dueAt;

    public VirtualAccount(PaymentAttempt paymentAttempt, String bankCode, String accountNumber, String webhookSecret, LocalDateTime dueAt) {
        if (paymentAttempt == null) {
            throw new IllegalArgumentException("결제 시도는 필수입니다.");
        }
        if (bankCode == null || bankCode.isBlank()) {
            throw new IllegalArgumentException("은행 코드는 필수입니다.");
        }
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("계좌번호는 필수입니다.");
        }
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new IllegalArgumentException("웹훅 검증 값은 필수입니다.");
        }
        if (dueAt == null) {
            throw new IllegalArgumentException("입금 기한은 필수입니다.");
        }
        
        this.paymentAttempt = paymentAttempt;
        this.bankCode = bankCode;
        this.accountNumber = accountNumber;
        this.webhookSecret = webhookSecret;
        this.dueAt = dueAt;
    }
}
