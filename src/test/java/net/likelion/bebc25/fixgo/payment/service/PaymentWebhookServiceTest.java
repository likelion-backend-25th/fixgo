package net.likelion.bebc25.fixgo.payment.service;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.payment.client.TossDto;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.payment.entity.*;
import net.likelion.bebc25.fixgo.payment.exception.PaymentErrorCode;
import net.likelion.bebc25.fixgo.payment.exception.PaymentException;
import net.likelion.bebc25.fixgo.payment.repository.PaymentAttemptRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({PaymentWebhookService.class, PaymentAttemptService.class})
class PaymentWebhookServiceTest {

    private static final Long AS_RECEPTION_ID = 1L;
    private static final String PAYMENT_KEY = "test_payment_key";
    private static final String SECRET = "ps_test_secret";
    private static final LocalDateTime DUE_AT = LocalDateTime.of(2026, 11, 10, 23, 59, 59);
    private static final LocalDateTime DEPOSITED_AT = LocalDateTime.of(2026, 11, 3, 15, 30);

    @Autowired
    PaymentWebhookService paymentWebhookService;
    @Autowired
    PaymentAttemptService paymentAttemptService;
    @Autowired
    PaymentAttemptRepository paymentAttemptRepository;
    @Autowired
    PaymentBillRepository paymentBillRepository;
    @Autowired
    EntityManager em;

    /**
     * 실제 흐름으로 가상계좌 발급(입금 대기)까지 진행하고 orderId를 돌려준다
     */
    private String issueVirtualAccount() {
        paymentBillRepository.save(PaymentBill.issue(AS_RECEPTION_ID, 30_000L, PaymentBillType.REPAIR));
        PaymentDto.StartPaymentResponse started = paymentAttemptService.startPayment(AS_RECEPTION_ID);
        paymentAttemptService.prepareConfirm(started.orderId(), started.amount());
        paymentAttemptService.issueVirtualAccount(
                started.orderId(), PAYMENT_KEY, "20", "12345678901234", SECRET, DUE_AT);
        em.flush();
        em.clear();
        return started.orderId();
    }

    /**
     * 토스가 보내는 웹훅 본문. 필드 순서는 DepositCallbackWebhook 선언 순서에 맞춘다
     */
    private TossDto.DepositCallbackWebhook webhook(String orderId, String status, String secret, LocalDateTime createdAt) {
        return new TossDto.DepositCallbackWebhook(createdAt, secret, status, orderId);
    }

    private PaymentAttempt findAttempt(String orderId) {
        return paymentAttemptRepository.findByOrderId(orderId).orElseThrow();
    }

    private PaymentBill findBill() {
        return paymentBillRepository.findByAsReceptionId(AS_RECEPTION_ID).orElseThrow();
    }

    @Test
    @DisplayName("입금 완료 웹훅이 오면 attempt는 DONE, bill은 PAID가 되고 두 곳에 같은 시각이 기록된다.")
    void depositDone() {
        // given
        String orderId = issueVirtualAccount();

        // when
        paymentWebhookService.handleDepositCallback(webhook(orderId, "DONE", SECRET, DEPOSITED_AT));
        em.flush();
        em.clear();

        // then
        PaymentAttempt attempt = findAttempt(orderId);
        assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.DONE);
        assertThat(attempt.getApprovedAt()).isEqualTo(DEPOSITED_AT);

        PaymentBill bill = findBill();
        assertThat(bill.getStatus()).isEqualTo(PaymentBillStatus.PAID);
        assertThat(bill.getPaidAt()).isEqualTo(DEPOSITED_AT);
    }

    @Test
    @DisplayName("같은 입금 완료 웹훅이 다시 와도 예외 없이 무시되고 기존 기록은 바뀌지 않는다. (재전송 대비)")
    void depositDoneTwice() {
        // given: 한 번 입금 완료 처리
        String orderId = issueVirtualAccount();
        paymentWebhookService.handleDepositCallback(webhook(orderId, "DONE", SECRET, DEPOSITED_AT));
        em.flush();
        em.clear();

        // when: 같은 웹훅이 다시 옴 (시각을 다르게 넣어 덮어쓰지 않는지 확인)
        paymentWebhookService.handleDepositCallback(webhook(orderId, "DONE", SECRET, DEPOSITED_AT.plusMinutes(5)));
        em.flush();
        em.clear();

        // then
        assertThat(findAttempt(orderId).getApprovedAt()).isEqualTo(DEPOSITED_AT);
        assertThat(findBill().getPaidAt()).isEqualTo(DEPOSITED_AT);
    }

    @Test
    @DisplayName("secret이 다르면 거절하고 상태를 바꾸지 않는다.")
    void secretMismatch() {
        // given
        String orderId = issueVirtualAccount();

        // when & then
        assertThatThrownBy(() -> paymentWebhookService.handleDepositCallback(
                webhook(orderId, "DONE", "wrong_secret", DEPOSITED_AT)))
                .isInstanceOf(PaymentException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.SECRET_MISMATCH);

        // then
        em.clear();
        assertThat(findAttempt(orderId).getStatus()).isEqualTo(PaymentAttemptStatus.WAITING_FOR_DEPOSIT);
        assertThat(findBill().getStatus()).isEqualTo(PaymentBillStatus.UNPAID);
    }

    @Test
    @DisplayName("존재하지 않는 orderId면 거절한다.")
    void attemptNotFound() {
        assertThatThrownBy(() -> paymentWebhookService.handleDepositCallback(
                webhook("NOT_EXIST_ORDER", "DONE", SECRET, DEPOSITED_AT)))
                .isInstanceOf(PaymentException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.ATTEMPT_NOT_FOUND);
    }

    @Test
    @DisplayName("가상계좌가 아닌 결제(카드)의 orderId로 오면 거절한다.")
    void notVirtualAccount() {
        // given: 카드 결제 완료된 attempt
        paymentBillRepository.save(PaymentBill.issue(AS_RECEPTION_ID, 30_000L, PaymentBillType.REPAIR));
        PaymentDto.StartPaymentResponse started = paymentAttemptService.startPayment(AS_RECEPTION_ID);
        paymentAttemptService.prepareConfirm(started.orderId(), started.amount());
        paymentAttemptService.completeConfirm(started.orderId(), PAYMENT_KEY, DEPOSITED_AT);
        em.flush();
        em.clear();

        // when & then
        assertThatThrownBy(() -> paymentWebhookService.handleDepositCallback(
                webhook(started.orderId(), "DONE", SECRET, DEPOSITED_AT)))
                .isInstanceOf(PaymentException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.VIRTUAL_ACCOUNT_NOT_FOUND);
    }

    @Test
    @DisplayName("이미 실패 처리된 결제에 입금 완료가 오면 예외 없이 기록만 하고 상태는 바꾸지 않는다.")
    void depositDoneOnFailed() {
        // given: 입금 대기 후 실패 처리 (기한 만료 등)
        String orderId = issueVirtualAccount();
        paymentAttemptService.failConfirm(orderId, "EXPIRED", "입금 기한 만료");
        em.flush();
        em.clear();

        // when: 늦게 입금 완료 웹훅이 옴
        paymentWebhookService.handleDepositCallback(webhook(orderId, "DONE", SECRET, DEPOSITED_AT));
        em.flush();
        em.clear();

        // then: 자동으로 완료 처리하지 않는다 (이중 결제 여부는 사람이 확인)
        assertThat(findAttempt(orderId).getStatus()).isEqualTo(PaymentAttemptStatus.FAILED);
        assertThat(findBill().getStatus()).isEqualTo(PaymentBillStatus.UNPAID);
    }

    @Test
    @DisplayName("처리 대상이 아닌 상태(발급 알림 등)의 웹훅은 예외 없이 무시한다.")
    void otherStatus() {
        // given
        String orderId = issueVirtualAccount();

        // when
        paymentWebhookService.handleDepositCallback(webhook(orderId, "WAITING_FOR_DEPOSIT", SECRET, DEPOSITED_AT));
        em.flush();
        em.clear();

        // then
        assertThat(findAttempt(orderId).getStatus()).isEqualTo(PaymentAttemptStatus.WAITING_FOR_DEPOSIT);
        assertThat(findBill().getStatus()).isEqualTo(PaymentBillStatus.UNPAID);
    }
}