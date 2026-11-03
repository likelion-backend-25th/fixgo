package net.likelion.bebc25.fixgo.payment.service;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttempt;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBill;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBillType;
import net.likelion.bebc25.fixgo.payment.entity.PaymentMethod;
import net.likelion.bebc25.fixgo.payment.repository.PaymentAttemptRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import net.likelion.bebc25.fixgo.payment.repository.VirtualAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({PaymentQueryService.class, PaymentAttemptService.class})
class PaymentQueryServiceTest {

    private static final Long AS_RECEPTION_ID = 1L;
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

    private PaymentBill issueBill() {
        return paymentBillRepository.save(PaymentBill.issue(AS_RECEPTION_ID, 30_000L, PaymentBillType.REPAIR));
    }

    @Test
    @DisplayName("청구서가 없으면 NOT_ISSUED이고 나머지 값은 모두 비어 있다.")
    void notIssued() {
        PaymentDto.PaymentSummaryResponse summary = paymentQueryService.getSummary(AS_RECEPTION_ID);

        assertThat(summary.status()).isEqualTo(PaymentDto.PaymentSummaryResponse.Status.NOT_ISSUED);
        assertThat(summary.amount()).isNull();
        assertThat(summary.paymentMethod()).isNull();
        assertThat(summary.paidAt()).isNull();
    }

    @Test
    @DisplayName("청구서가 발행되고 결제 전이면 UNPAID이고 청구 항목과 금액이 담긴다.")
    void unpaid() {
        issueBill();

        PaymentDto.PaymentSummaryResponse summary = paymentQueryService.getSummary(AS_RECEPTION_ID);

        assertThat(summary.status()).isEqualTo(PaymentDto.PaymentSummaryResponse.Status.UNPAID);
        assertThat(summary.orderName()).isEqualTo("A/S 수리비");
        assertThat(summary.amount()).isEqualTo(30_000L);
        assertThat(summary.paymentMethod()).isNull();
        assertThat(summary.paidAt()).isNull();
    }

    @Test
    @DisplayName("결제 시작만 하고 승인 전이면 여전히 UNPAID다.")
    void startedButNotPaid() {
        PaymentBill bill = issueBill();
        paymentAttemptService.startPayment(AS_RECEPTION_ID);

        PaymentDto.PaymentSummaryResponse summary = paymentQueryService.getSummary(AS_RECEPTION_ID);

        assertThat(summary.status()).isEqualTo(PaymentDto.PaymentSummaryResponse.Status.UNPAID);
    }

    @Test
    @DisplayName("결제가 완료되면 PAID이고 결제 수단과 결제 시각이 담긴다.")
    void paid() {
        // given: 실제 결제 흐름을 거쳐 결제 완료 상태를 만든다 (토스 호출 단계는 제외)
        PaymentBill bill = issueBill();
        PaymentDto.StartPaymentResponse started = paymentAttemptService.startPayment(AS_RECEPTION_ID);
        paymentAttemptService.prepareConfirm(started.orderId(), started.amount());
        paymentAttemptService.completeConfirm(started.orderId(), "test_payment_key", APPROVED_AT);
        em.flush();
        em.clear();

        // when
        PaymentDto.PaymentSummaryResponse summary = paymentQueryService.getSummary(AS_RECEPTION_ID);

        // then
        assertThat(summary.status()).isEqualTo(PaymentDto.PaymentSummaryResponse.Status.PAID);
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
        assertThatThrownBy(() -> paymentQueryService.getSummary(AS_RECEPTION_ID))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("가상계좌가 발급되고 입금 전이면 WAITING_FOR_DEPOSIT이고 계좌 정보가 담긴다.")
    void waitingForDeposit() {
        // given: 실제 흐름으로 가상계좌 발급까지 진행 (토스 호출 단계는 제외)
        PaymentBill bill = issueBill();
        PaymentDto.StartPaymentResponse started = paymentAttemptService.startPayment(AS_RECEPTION_ID);
        paymentAttemptService.prepareConfirm(started.orderId(), started.amount());
        paymentAttemptService.issueVirtualAccount(
                started.orderId(), "test_payment_key", "20", "12345678901234", "ps_test_secret", DUE_AT);
        em.flush();
        em.clear();

        // when
        PaymentDto.PaymentSummaryResponse summary = paymentQueryService.getSummary(AS_RECEPTION_ID);

        // then
        assertThat(summary.status()).isEqualTo(PaymentDto.PaymentSummaryResponse.Status.WAITING_FOR_DEPOSIT);
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
        PaymentDto.StartPaymentResponse started = paymentAttemptService.startPayment(AS_RECEPTION_ID);
        paymentAttemptService.prepareConfirm(started.orderId(), started.amount());
        PaymentAttempt attempt = paymentAttemptRepository.findByOrderId(started.orderId()).orElseThrow();
        attempt.markWaitingForDeposit("test_payment_key");
        em.flush();
        em.clear();

        // when & then
        assertThatThrownBy(() -> paymentQueryService.getSummary(AS_RECEPTION_ID))
                .isInstanceOf(IllegalStateException.class);
    }
}