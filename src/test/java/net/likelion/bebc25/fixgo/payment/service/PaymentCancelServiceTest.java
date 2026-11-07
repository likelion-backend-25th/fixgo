package net.likelion.bebc25.fixgo.payment.service;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.support.AsReceptionFixture;
import net.likelion.bebc25.fixgo.payment.client.TossDto;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.payment.client.TossPaymentsClient;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.payment.entity.*;
import net.likelion.bebc25.fixgo.payment.repository.PaymentAttemptRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import net.likelion.bebc25.fixgo.payment.repository.VirtualAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.then;

@DataJpaTest
@Import({JpaConfig.class, AsReceptionFixture.class, PaymentFacadeService.class, PaymentAttemptService.class, PaymentRefundService.class})
class PaymentCancelServiceTest {
    @Autowired
    AsReceptionFixture asReceptionFixture;

    private AsReception reception;
    private Long asReceptionId;

    @BeforeEach
    void setUpReception() {
        reception = asReceptionFixture.save();
        asReceptionId = reception.getId();
    }

    private static final String PAYMENT_KEY = "test_payment_key";
    private static final LocalDateTime DUE_AT = LocalDateTime.of(2026, 11, 10, 23, 59, 59);

    @Autowired
    PaymentFacadeService paymentFacadeService;
    @Autowired
    PaymentAttemptService paymentAttemptService;
    @Autowired
    PaymentAttemptRepository paymentAttemptRepository;
    @Autowired
    PaymentBillRepository paymentBillRepository;
    @Autowired
    VirtualAccountRepository virtualAccountRepository;
    @Autowired
    EntityManager em;

    @MockitoBean
    TossPaymentsClient tossPaymentsClient;

    /**
     * 실제 흐름으로 가상계좌 발급(입금 대기)까지 진행하고 orderId를 돌려준다
     */
    private String issueVirtualAccount() {
        paymentBillRepository.save(PaymentBill.create(reception, 30_000L, PaymentBillType.REPAIR));
        PaymentDto.StartPaymentResponse started = paymentAttemptService.startPayment(asReceptionId);
        paymentAttemptService.prepareConfirm(started.orderId(), started.amount());
        paymentAttemptService.reconcile(new TossDto.ConfirmResponse(
                PAYMENT_KEY, started.orderId(), "WAITING_FOR_DEPOSIT", "가상계좌", null, "ps_test_secret",
                new TossDto.ConfirmResponse.VirtualAccount("12345678901234", "20", DUE_AT.atOffset(ZoneOffset.ofHours(9))), null));
        em.flush();
        em.clear();
        return started.orderId();
    }

    @Test
    @DisplayName("입금 대기 중인 가상계좌를 취소하면 attempt는 FAILED가 되고, bill은 UNPAID, 가상계좌 기록은 남는다.")
    void cancel() {
        // given
        String orderId = issueVirtualAccount();

        // when
        paymentFacadeService.cancelVirtualAccountBeforeDeposit(asReceptionId);
        em.flush();
        em.clear();

        // then: 토스에 그 결제의 paymentKey로, 환불 계좌 없이 취소를 요청했다
        then(tossPaymentsClient).should().cancel(eq(PAYMENT_KEY), anyString(), isNull());

        // then: attempt
        PaymentAttempt attempt = paymentAttemptRepository.findByOrderId(orderId).orElseThrow();
        assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.FAILED);
        assertThat(attempt.getFailureCode()).isEqualTo("VIRTUAL_ACCOUNT_CANCELED");

        // then: 돈이 오간 적 없으므로 청구서는 그대로
        assertThat(paymentBillRepository.findByAsReceptionId(asReceptionId).orElseThrow().getStatus())
                .isEqualTo(PaymentBillStatus.UNPAID);

        // then: 가상계좌 기록은 지우지 않는다 (웹훅 검증, 추적용)
        assertThat(virtualAccountRepository.findByPaymentAttemptId(attempt.getId())).isPresent();
    }

    @Test
    @DisplayName("가상계좌를 취소하면 다시 결제를 시작할 수 있고, 새 결제 시도가 만들어진다.")
    void canStartAgainAfterCancel() {
        // given: 가상계좌 발급 후 취소
        String canceledOrderId = issueVirtualAccount();
        paymentFacadeService.cancelVirtualAccountBeforeDeposit(asReceptionId);
        em.flush();
        em.clear();

        // when: 다시 결제 시작 (예: 카드로 바꿔서)
        PaymentDto.StartPaymentResponse restarted = paymentAttemptService.startPayment(asReceptionId);

        // then: 새 orderId의 READY attempt가 생긴다
        assertThat(restarted.orderId()).isNotEqualTo(canceledOrderId);
        PaymentAttempt newAttempt = paymentAttemptRepository.findByOrderId(restarted.orderId()).orElseThrow();
        assertThat(newAttempt.getStatus()).isEqualTo(PaymentAttemptStatus.READY);
    }
}