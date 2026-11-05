package net.likelion.bebc25.fixgo.payment.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static net.likelion.bebc25.fixgo.support.AsReceptionFixture.transientReception;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentAttemptTest {

    private static final String PAYMENT_KEY = "test_payment_key";
    private static final LocalDateTime APPROVED_AT = LocalDateTime.of(2026, 11, 3, 15, 30);

    private PaymentBill bill() {
        return PaymentBill.create(transientReception(), 30_000L, PaymentBillType.REPAIR);
    }

    private PaymentAttempt ready() {
        return PaymentAttempt.create(bill());
    }

    private PaymentAttempt inProgress() {
        PaymentAttempt attempt = ready();
        attempt.markInProgress();
        return attempt;
    }

    private PaymentAttempt waitingForDeposit() {
        PaymentAttempt attempt = inProgress();
        attempt.markWaitingForDeposit(PAYMENT_KEY);
        return attempt;
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("청구서로 결제 시도를 만들면 READY이고 금액은 청구서 금액과 같다.")
        void success() {
            PaymentBill bill = bill();

            PaymentAttempt attempt = PaymentAttempt.create(bill);

            assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.READY);
            assertThat(attempt.getPaymentBill()).isSameAs(bill);
            assertThat(attempt.getAmount()).isEqualTo(30_000L);
            assertThat(attempt.getOrderId()).startsWith("PAY-");
            assertThat(attempt.getPaymentKey()).isNull();
            assertThat(attempt.getMethod()).isNull();
        }

        @Test
        @DisplayName("결제 시도마다 orderId가 다르다.")
        void uniqueOrderId() {
            assertThat(ready().getOrderId()).isNotEqualTo(ready().getOrderId());
        }

        @Test
        @DisplayName("청구서 없이는 만들 수 없다.")
        void nullBill() {
            assertThatThrownBy(() -> PaymentAttempt.create(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("markInProgress")
    class MarkInProgress {

        @Test
        @DisplayName("READY면 IN_PROGRESS가 된다.")
        void success() {
            PaymentAttempt attempt = ready();

            attempt.markInProgress();

            assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("이미 IN_PROGRESS면 다시 바꿀 수 없다. (중복 승인 방지)")
        void alreadyInProgress() {
            PaymentAttempt attempt = inProgress();

            assertThatThrownBy(attempt::markInProgress)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("markCardDone")
    class MarkCardDone {

        @Test
        @DisplayName("IN_PROGRESS면 DONE이 되고 paymentKey, 카드 수단, 승인 시각이 기록된다.")
        void success() {
            PaymentAttempt attempt = inProgress();

            attempt.markCardDone(PAYMENT_KEY, APPROVED_AT);

            assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.DONE);
            assertThat(attempt.getPaymentKey()).isEqualTo(PAYMENT_KEY);
            assertThat(attempt.getMethod()).isEqualTo(PaymentMethod.CARD);
            assertThat(attempt.getApprovedAt()).isEqualTo(APPROVED_AT);
        }

        @Test
        @DisplayName("승인 준비를 거치지 않은 READY는 완료할 수 없고 값도 기록되지 않는다.")
        void fromReady() {
            PaymentAttempt attempt = ready();

            assertThatThrownBy(() -> attempt.markCardDone(PAYMENT_KEY, APPROVED_AT))
                    .isInstanceOf(IllegalStateException.class);

            assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.READY);
            assertThat(attempt.getPaymentKey()).isNull();
        }

        @Test
        @DisplayName("입금 대기 중인 가상계좌 결제를 카드 완료로 바꿀 수 없다.")
        void fromWaitingForDeposit() {
            PaymentAttempt attempt = waitingForDeposit();

            assertThatThrownBy(() -> attempt.markCardDone(PAYMENT_KEY, APPROVED_AT))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("markWaitingForDeposit")
    class MarkWaitingForDeposit {

        @Test
        @DisplayName("IN_PROGRESS면 입금 대기가 되고 paymentKey와 가상계좌 수단이 기록된다. 승인 시각은 없다.")
        void success() {
            PaymentAttempt attempt = inProgress();

            attempt.markWaitingForDeposit(PAYMENT_KEY);

            assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.WAITING_FOR_DEPOSIT);
            assertThat(attempt.getPaymentKey()).isEqualTo(PAYMENT_KEY);
            assertThat(attempt.getMethod()).isEqualTo(PaymentMethod.VIRTUAL_ACCOUNT);
            assertThat(attempt.getApprovedAt()).isNull();
        }

        @Test
        @DisplayName("승인 준비를 거치지 않은 READY는 입금 대기로 바꿀 수 없다.")
        void fromReady() {
            PaymentAttempt attempt = ready();

            assertThatThrownBy(() -> attempt.markWaitingForDeposit(PAYMENT_KEY))
                    .isInstanceOf(IllegalStateException.class);

            assertThat(attempt.getMethod()).isNull();
        }
    }

    @Nested
    @DisplayName("markVirtualAccountDone")
    class MarkVirtualAccountDone {

        @Test
        @DisplayName("입금 대기면 DONE이 되고 승인 시각이 기록된다. paymentKey와 수단은 유지된다.")
        void success() {
            PaymentAttempt attempt = waitingForDeposit();

            attempt.markVirtualAccountDone(APPROVED_AT);

            assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.DONE);
            assertThat(attempt.getApprovedAt()).isEqualTo(APPROVED_AT);
            assertThat(attempt.getPaymentKey()).isEqualTo(PAYMENT_KEY);
            assertThat(attempt.getMethod()).isEqualTo(PaymentMethod.VIRTUAL_ACCOUNT);
        }

        @Test
        @DisplayName("계좌가 발급되지 않은 IN_PROGRESS는 입금 완료로 바꿀 수 없고 승인 시각도 기록되지 않는다.")
        void fromInProgress() {
            PaymentAttempt attempt = inProgress();

            assertThatThrownBy(() -> attempt.markVirtualAccountDone(APPROVED_AT))
                    .isInstanceOf(IllegalStateException.class);

            assertThat(attempt.getApprovedAt()).isNull();
        }

        @Test
        @DisplayName("이미 완료된 결제에 입금 완료가 다시 와도 바꿀 수 없다.")
        void alreadyDone() {
            PaymentAttempt attempt = waitingForDeposit();
            attempt.markVirtualAccountDone(APPROVED_AT);

            assertThatThrownBy(() -> attempt.markVirtualAccountDone(APPROVED_AT.plusHours(1)))
                    .isInstanceOf(IllegalStateException.class);

            assertThat(attempt.getApprovedAt()).isEqualTo(APPROVED_AT);
        }
    }

    @Nested
    @DisplayName("markCashDone")
    class MarkCashDone {

        @Test
        @DisplayName("READY면 DONE이 되고 현금 수단과 처리한 직원이 기록된다.")
        void success() {
            PaymentAttempt attempt = ready();

            attempt.markCashDone(100L);

            assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.DONE);
            assertThat(attempt.getMethod()).isEqualTo(PaymentMethod.CASH);
            assertThat(attempt.getCreatedBy()).isEqualTo(100L);
            assertThat(attempt.getApprovedAt()).isNotNull();
        }

        @Test
        @DisplayName("카드 승인 진행 중인 결제를 현금 완료로 바꿀 수 없다. (이중 결제 방지)")
        void cashDoneFromInProgress() {
            PaymentAttempt attempt = inProgress();

            assertThatThrownBy(() -> attempt.markCashDone(100L))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("markFailed")
    class MarkFailed {

        @Test
        @DisplayName("진행 중인 결제는 FAILED가 되고 실패 사유가 기록된다.")
        void success() {
            PaymentAttempt attempt = inProgress();

            attempt.markFailed("REJECT_CARD_PAYMENT", "한도 초과");

            assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.FAILED);
            assertThat(attempt.getFailureCode()).isEqualTo("REJECT_CARD_PAYMENT");
            assertThat(attempt.getFailureMessage()).isEqualTo("한도 초과");
        }

        @Test
        @DisplayName("입금 대기 중인 결제도 실패 처리할 수 있다. (입금 전 취소, 기한 만료)")
        void fromWaitingForDeposit() {
            PaymentAttempt attempt = waitingForDeposit();

            attempt.markFailed("CANCELED", "입금 전 취소");

            assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.FAILED);
        }

        @Test
        @DisplayName("완료된 결제는 실패로 바꿀 수 없다.")
        void fromDone() {
            PaymentAttempt attempt = inProgress();
            attempt.markCardDone(PAYMENT_KEY, APPROVED_AT);

            assertThatThrownBy(() -> attempt.markFailed("ANY", "any"))
                    .isInstanceOf(IllegalStateException.class);

            assertThat(attempt.getFailureCode()).isNull();
        }

        @Test
        @DisplayName("이미 실패한 결제는 다시 실패 처리할 수 없다.")
        void fromFailed() {
            PaymentAttempt attempt = ready();
            attempt.markFailed("FIRST", "first");

            assertThatThrownBy(() -> attempt.markFailed("SECOND", "second"))
                    .isInstanceOf(IllegalStateException.class);

            assertThat(attempt.getFailureCode()).isEqualTo("FIRST");
        }
    }
}
