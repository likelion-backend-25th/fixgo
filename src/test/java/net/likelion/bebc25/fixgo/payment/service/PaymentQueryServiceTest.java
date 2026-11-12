package net.likelion.bebc25.fixgo.payment.service;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.as.service.AsStatusHistoryService;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.support.AsReceptionFixture;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.payment.client.TossDto;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttempt;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBill;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBillType;
import net.likelion.bebc25.fixgo.payment.entity.PaymentMethod;
import net.likelion.bebc25.fixgo.payment.repository.PaymentAttemptRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import net.likelion.bebc25.fixgo.payment.repository.VirtualAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({JpaConfig.class, AsReceptionFixture.class, AsStatusHistoryService.class, PaymentQueryService.class, PaymentAttemptService.class})
class PaymentQueryServiceTest {
    @Autowired
    AsReceptionFixture asReceptionFixture;

    private AsReception reception;
    private Long asReceptionId;

    @BeforeEach
    void setUpReception() {
        reception = asReceptionFixture.save();
        asReceptionId = reception.getId();
    }

    private static final LocalDateTime APPROVED_AT =
            LocalDateTime.of(2026, 11, 2, 15, 30);
    private static final LocalDateTime DUE_AT = LocalDateTime.of(2026, 11, 9, 23, 59, 59);

    @Autowired
    PaymentQueryService paymentQueryService;
    @Autowired
    PaymentAttemptService paymentAttemptService;
    @Autowired
    PaymentBillRepository paymentBillRepository;
    @Autowired
    VirtualAccountRepository virtualAccountRepository;
    @Autowired
    PaymentAttemptRepository paymentAttemptRepository;
    @Autowired
    EntityManager em;

    @Test
    void detailRejectsMissingBill() {
        assertThatThrownBy(() -> paymentQueryService.getDetail(Long.MAX_VALUE))
                .isInstanceOf(net.likelion.bebc25.fixgo.payment.exception.PaymentException.class);
    }

    private PaymentBill issueBill() {
        return paymentBillRepository.save(PaymentBill.create(reception, 30_000L, PaymentBillType.REPAIR));
    }

    @Test
    @DisplayName("청구서가 없으면 결제 영역이 비어 있다.")
    void notIssued() {
        assertThat(paymentQueryService.findCustomerView(asReceptionId)).isEmpty();
    }

    @Test
    @DisplayName("청구서가 발행되고 결제 전이면 UNPAID이고 청구 항목과 금액이 담긴다.")
    void unpaid() {
        issueBill();

        PaymentDto.CustomerPaymentView summary = paymentQueryService.findCustomerView(asReceptionId).orElseThrow();

        assertThat(summary.status()).isEqualTo(PaymentDto.CustomerPaymentView.Status.UNPAID);
        assertThat(summary.orderName()).isEqualTo("A/S 수리비");
        assertThat(summary.amount()).isEqualTo(30_000L);
        assertThat(summary.paymentMethod()).isNull();
        assertThat(summary.paidAt()).isNull();
    }

    @Test
    @DisplayName("결제 시작만 하고 승인 전이면 여전히 UNPAID다.")
    void startedButNotPaid() {
        PaymentBill bill = issueBill();
        paymentAttemptService.startPayment(asReceptionId);

        PaymentDto.CustomerPaymentView summary = paymentQueryService.findCustomerView(asReceptionId).orElseThrow();

        assertThat(summary.status()).isEqualTo(PaymentDto.CustomerPaymentView.Status.UNPAID);
    }

    @Test
    @DisplayName("결제가 완료되면 PAID이고 결제 수단과 결제 시각이 담긴다.")
    void paid() {
        // given: 실제 결제 흐름을 거쳐 결제 완료 상태를 만든다 (토스 호출 단계는 제외)
        PaymentBill bill = issueBill();
        PaymentDto.StartPaymentResponse started = paymentAttemptService.startPayment(asReceptionId);
        paymentAttemptService.prepareConfirm(started.orderId(), started.amount());
        paymentAttemptService.reconcile(new TossDto.ConfirmResponse(
                "test_payment_key", started.orderId(), "DONE", "카드", APPROVED_AT.atOffset(ZoneOffset.ofHours(9)), null, null, null));
        em.flush();
        em.clear();

        // when
        PaymentDto.CustomerPaymentView summary = paymentQueryService.findCustomerView(asReceptionId).orElseThrow();

        // then
        assertThat(summary.status()).isEqualTo(PaymentDto.CustomerPaymentView.Status.PAID);
        assertThat(summary.amount()).isEqualTo(30_000L);
        assertThat(summary.paymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(summary.paidAt()).isEqualTo(LocalDateTime.of(2026, 11, 2, 15, 30));
    }

    @Test
    @DisplayName("청구서는 PAID인데 완료된 결제 시도가 없으면 데이터 이상으로 예외가 난다.")
    void paidWithoutDoneAttempt() {
        // given: 결제 흐름을 거치지 않고 bill만 PAID로 바꿔 깨진 데이터를 만든다
        PaymentBill bill = issueBill();
        bill.markPaid(APPROVED_AT);
        em.flush();
        em.clear();

        // when & then
        assertThatThrownBy(() -> paymentQueryService.findCustomerView(asReceptionId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("가상계좌가 발급되고 입금 전이면 WAITING_FOR_DEPOSIT이고 계좌 정보가 담긴다.")
    void waitingForDeposit() {
        // given: 실제 흐름으로 가상계좌 발급까지 진행 (토스 호출 단계는 제외)
        PaymentBill bill = issueBill();
        PaymentDto.StartPaymentResponse started = paymentAttemptService.startPayment(asReceptionId);
        paymentAttemptService.prepareConfirm(started.orderId(), started.amount());
        paymentAttemptService.reconcile(new TossDto.ConfirmResponse(
                "test_payment_key", started.orderId(), "WAITING_FOR_DEPOSIT", "가상계좌", null, "ps_test_secret",
                new TossDto.ConfirmResponse.VirtualAccount("12345678901234", "20", DUE_AT.atOffset(ZoneOffset.ofHours(9))), null));
        em.flush();
        em.clear();

        // when
        PaymentDto.CustomerPaymentView summary = paymentQueryService.findCustomerView(asReceptionId).orElseThrow();

        // then
        assertThat(summary.status()).isEqualTo(PaymentDto.CustomerPaymentView.Status.WAITING_FOR_DEPOSIT);
        assertThat(summary.amount()).isEqualTo(30_000L);
        assertThat(summary.paymentMethod()).isEqualTo(PaymentMethod.VIRTUAL_ACCOUNT);
        assertThat(summary.paidAt()).isNull();
        assertThat(summary.virtualAccount().accountNumber()).isEqualTo("12345678901234");
        assertThat(summary.virtualAccount().dueAt()).isEqualTo(DUE_AT);
        // TODO: "우리은행" 등 한글코드로 들어오면 거기에 맞추기
        assertThat(summary.virtualAccount().bankName()).isEqualTo("20");
    }

    @Test
    @DisplayName("입금 대기 중인 시도에 가상계좌가 없으면 데이터 이상으로 예외가 난다.")
    void waitingWithoutVirtualAccount() {
        // given: 계좌 저장 없이 attempt만 입금 대기로 바꿔 깨진 데이터를 만든다
        PaymentBill bill = issueBill();
        PaymentDto.StartPaymentResponse started = paymentAttemptService.startPayment(asReceptionId);
        paymentAttemptService.prepareConfirm(started.orderId(), started.amount());
        PaymentAttempt attempt = paymentAttemptRepository.findByOrderId(started.orderId()).orElseThrow();
        attempt.markWaitingForDeposit("test_payment_key");
        em.flush();
        em.clear();

        // when & then
        assertThatThrownBy(() -> paymentQueryService.findCustomerView(asReceptionId))
                .isInstanceOf(IllegalStateException.class);
    }
}