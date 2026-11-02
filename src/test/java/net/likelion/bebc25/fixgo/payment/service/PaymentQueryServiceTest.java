package net.likelion.bebc25.fixgo.payment.service;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.payment.dto.PaymentSummary;
import net.likelion.bebc25.fixgo.payment.dto.PaymentSummaryStatus;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBill;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBillType;
import net.likelion.bebc25.fixgo.payment.entity.PaymentMethod;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({PaymentQueryService.class, PaymentAttemptService.class})
class PaymentQueryServiceTest {

    private static final Long AS_REQUEST_ID = 1L;
    private static final OffsetDateTime APPROVED_AT =
            OffsetDateTime.of(2026, 11, 2, 15, 30, 0, 0, ZoneOffset.ofHours(9));

    @Autowired
    PaymentQueryService paymentQueryService;
    @Autowired
    PaymentAttemptService paymentAttemptService;
    @Autowired
    PaymentBillRepository paymentBillRepository;
    @Autowired
    EntityManager em;

    private PaymentBill issueBill() {
        return paymentBillRepository.save(PaymentBill.issue(AS_REQUEST_ID, 30_000L, PaymentBillType.REPAIR));
    }

    @Test
    @DisplayName("청구서가 없으면 NOT_ISSUED이고 나머지 값은 모두 비어 있다.")
    void notIssued() {
        PaymentSummary summary = paymentQueryService.getSummary(AS_REQUEST_ID);

        assertThat(summary.status()).isEqualTo(PaymentSummaryStatus.NOT_ISSUED);
        assertThat(summary.amount()).isNull();
        assertThat(summary.paymentMethod()).isNull();
        assertThat(summary.paidAt()).isNull();
    }

    @Test
    @DisplayName("청구서가 발행되고 결제 전이면 UNPAID이고 청구 항목과 금액이 담긴다.")
    void unpaid() {
        issueBill();

        PaymentSummary summary = paymentQueryService.getSummary(AS_REQUEST_ID);

        assertThat(summary.status()).isEqualTo(PaymentSummaryStatus.UNPAID);
        assertThat(summary.orderName()).isEqualTo("A/S 수리비");
        assertThat(summary.amount()).isEqualTo(30_000L);
        assertThat(summary.paymentMethod()).isNull();
        assertThat(summary.paidAt()).isNull();
    }

    @Test
    @DisplayName("결제 시작만 하고 승인 전이면 여전히 UNPAID다.")
    void startedButNotPaid() {
        PaymentBill bill = issueBill();
        paymentAttemptService.startPayment(AS_REQUEST_ID);

        PaymentSummary summary = paymentQueryService.getSummary(AS_REQUEST_ID);

        assertThat(summary.status()).isEqualTo(PaymentSummaryStatus.UNPAID);
    }

    @Test
    @DisplayName("결제가 완료되면 PAID이고 결제 수단과 결제 시각이 담긴다.")
    void paid() {
        // given: 실제 결제 흐름을 거쳐 결제 완료 상태를 만든다 (토스 호출 단계는 제외)
        PaymentBill bill = issueBill();
        PaymentDto.StartPaymentResponse started = paymentAttemptService.startPayment(AS_REQUEST_ID);
        paymentAttemptService.prepareConfirm(started.orderId(), started.amount());
        paymentAttemptService.completeConfirm(started.orderId(), "test_payment_key", APPROVED_AT);
        em.flush();
        em.clear();

        // when
        PaymentSummary summary = paymentQueryService.getSummary(AS_REQUEST_ID);

        // then
        assertThat(summary.status()).isEqualTo(PaymentSummaryStatus.PAID);
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
        assertThatThrownBy(() -> paymentQueryService.getSummary(AS_REQUEST_ID))
                .isInstanceOf(IllegalStateException.class);
    }
}