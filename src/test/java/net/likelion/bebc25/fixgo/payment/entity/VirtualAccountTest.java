package net.likelion.bebc25.fixgo.payment.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static net.likelion.bebc25.fixgo.support.AsReceptionFixture.transientReception;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VirtualAccountTest {

    private static final String BANK_CODE = "20";
    private static final String ACCOUNT_NUMBER = "12345678901234";
    private static final String WEBHOOK_SECRET = "ps_test_secret";
    private static final LocalDateTime DUE_AT = LocalDateTime.of(2026, 11, 10, 23, 59, 59);

    private PaymentAttempt attempt() {
        return PaymentAttempt.create(PaymentBill.create(transientReception(), 30_000L, PaymentBillType.REPAIR));
    }

    @Test
    @DisplayName("올바른 값으로 발급하면 값이 그대로 담긴다.")
    void success() {
        PaymentAttempt attempt = attempt();

        VirtualAccount virtualAccount =
                VirtualAccount.issue(attempt, BANK_CODE, ACCOUNT_NUMBER, WEBHOOK_SECRET, DUE_AT);

        // 매개변수 순서가 바뀌는 실수를 잡기 위해 필드를 하나씩 확인한다
        assertThat(virtualAccount.getPaymentAttempt()).isSameAs(attempt);
        assertThat(virtualAccount.getBankCode()).isEqualTo(BANK_CODE);
        assertThat(virtualAccount.getAccountNumber()).isEqualTo(ACCOUNT_NUMBER);
        assertThat(virtualAccount.getWebhookSecret()).isEqualTo(WEBHOOK_SECRET);
        assertThat(virtualAccount.getDueAt()).isEqualTo(DUE_AT);
    }

    @Test
    @DisplayName("결제 시도 없이는 발급할 수 없다.")
    void nullAttempt() {
        assertThatThrownBy(() -> VirtualAccount.issue(null, BANK_CODE, ACCOUNT_NUMBER, WEBHOOK_SECRET, DUE_AT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("은행 코드가 비어 있으면 발급할 수 없다.")
    void blankBankCode() {
        assertThatThrownBy(() -> VirtualAccount.issue(attempt(), null, ACCOUNT_NUMBER, WEBHOOK_SECRET, DUE_AT))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VirtualAccount.issue(attempt(), "", ACCOUNT_NUMBER, WEBHOOK_SECRET, DUE_AT))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VirtualAccount.issue(attempt(), " ", ACCOUNT_NUMBER, WEBHOOK_SECRET, DUE_AT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("계좌번호가 비어 있으면 발급할 수 없다.")
    void blankAccountNumber() {
        assertThatThrownBy(() -> VirtualAccount.issue(attempt(), BANK_CODE, null, WEBHOOK_SECRET, DUE_AT))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VirtualAccount.issue(attempt(), BANK_CODE, "", WEBHOOK_SECRET, DUE_AT))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VirtualAccount.issue(attempt(), BANK_CODE, " ", WEBHOOK_SECRET, DUE_AT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("웹훅 검증 값이 비어 있으면 발급할 수 없다. (입금 웹훅을 검증할 수 없게 되므로)")
    void blankWebhookSecret() {
        assertThatThrownBy(() -> VirtualAccount.issue(attempt(), BANK_CODE, ACCOUNT_NUMBER, null, DUE_AT))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VirtualAccount.issue(attempt(), BANK_CODE, ACCOUNT_NUMBER, "", DUE_AT))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VirtualAccount.issue(attempt(), BANK_CODE, ACCOUNT_NUMBER, " ", DUE_AT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("입금 기한이 없으면 발급할 수 없다.")
    void nullDueAt() {
        assertThatThrownBy(() -> VirtualAccount.issue(attempt(), BANK_CODE, ACCOUNT_NUMBER, WEBHOOK_SECRET, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}