package net.likelion.bebc25.fixgo.payment.service;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.support.AsReceptionFixture;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.payment.client.TossDto;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.payment.dto.PaymentInternalDto;
import net.likelion.bebc25.fixgo.payment.entity.*;
import net.likelion.bebc25.fixgo.payment.repository.PaymentAttemptRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentRefundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaConfig.class, AsReceptionFixture.class, PaymentRefundService.class, PaymentAttemptService.class})
class PaymentRefundServiceTest {
    @Autowired
    AsReceptionFixture asReceptionFixture;

    private AsReception reception;
    private Long asReceptionId;

    @BeforeEach
    void setUpReception() {
        reception = asReceptionFixture.save();
        asReceptionId = reception.getId();
    }

    private static final Long MEMBER_ID = 100L;
    private static final String PAYMENT_KEY = "test_payment_key";
    private static final String CANCEL_REASON = "수리 불가로 환불";
    private static final String TRANSACTION_KEY = "test_cancel_transaction_key";
    private static final LocalDateTime CANCELED_AT = LocalDateTime.of(2026, 11, 4, 16, 0);

    @Autowired
    PaymentRefundService paymentRefundService;
    @Autowired
    PaymentAttemptService paymentAttemptService;
    @Autowired
    PaymentBillRepository paymentBillRepository;
    @Autowired
    PaymentAttemptRepository paymentAttemptRepository;
    @Autowired
    PaymentRefundRepository paymentRefundRepository;
    @Autowired
    EntityManager em;

    /**
     * 실제 흐름으로 카드 결제 완료(bill PAID)까지 진행하고 billId를 돌려준다
     */
    private Long paidByCard() {
        PaymentBill bill = paymentBillRepository.save(PaymentBill.create(reception, 30_000L, PaymentBillType.REPAIR));
        PaymentDto.StartPaymentResponse started = paymentAttemptService.startPayment(asReceptionId);
        paymentAttemptService.prepareConfirm(started.orderId(), started.amount());
        paymentAttemptService.reconcile(new TossDto.ConfirmResponse(
                PAYMENT_KEY, started.orderId(), "DONE", "카드", LocalDateTime.of(2026, 11, 4, 10, 0).atOffset(ZoneOffset.ofHours(9)), null, null, null));
        em.flush();
        em.clear();
        return bill.getId();
    }

    @Test
    @DisplayName("환불 준비: 결제 완료된 청구서면 진행 중인 환불 기록을 만들고, 토스 취소에 필요한 값을 돌려준다.")
    void prepareRefund() {
        // given
        Long billId = paidByCard();

        // when
        PaymentInternalDto.PreparedRefund prepared =
                paymentRefundService.prepareRefund(billId, CANCEL_REASON, MEMBER_ID, null);
        em.flush();
        em.clear();

        // then: 퍼사드가 토스를 부를 때 쓸 값
        assertThat(prepared.refundId()).isNotNull();
        assertThat(prepared.paymentKey()).isEqualTo(PAYMENT_KEY);

        // then: 환불 기록은 진행 중이고, 토스 결과(거래 키, 취소 시각)는 아직 없다
        PaymentRefund refund = paymentRefundRepository.findById(prepared.refundId()).orElseThrow();
        assertThat(refund.getStatus()).isEqualTo(PaymentRefundStatus.IN_PROGRESS);
        assertThat(refund.getCancelReason()).isEqualTo(CANCEL_REASON);
        assertThat(refund.getCreatedBy()).isEqualTo(MEMBER_ID);
        assertThat(refund.getTransactionKey()).isNull();
        assertThat(refund.getCanceledAt()).isNull();

        // then: 아직 토스 취소 전이므로 청구서는 그대로 PAID
        assertThat(paymentBillRepository.findById(billId).orElseThrow().getStatus())
                .isEqualTo(PaymentBillStatus.PAID);
    }

    @Test
    @DisplayName("환불 반영: 환불 기록이 DONE이 되고 거래 키와 취소 시각이 남으며, 청구서는 REFUNDED, 결제 시도는 DONE 그대로다.")
    void completeRefund() {
        // given: 환불 준비까지 마친 상태
        Long billId = paidByCard();
        PaymentInternalDto.PreparedRefund prepared =
                paymentRefundService.prepareRefund(billId, CANCEL_REASON, MEMBER_ID, null);
        em.flush();
        em.clear();

        // when
        paymentRefundService.completeRefund(prepared.refundId(), TRANSACTION_KEY, CANCELED_AT);
        em.flush();
        em.clear();

        // then: 환불 기록
        PaymentRefund refund = paymentRefundRepository.findById(prepared.refundId()).orElseThrow();
        assertThat(refund.getStatus()).isEqualTo(PaymentRefundStatus.DONE);
        assertThat(refund.getTransactionKey()).isEqualTo(TRANSACTION_KEY);
        assertThat(refund.getCanceledAt()).isEqualTo(CANCELED_AT);

        // then: 청구서는 환불됨
        assertThat(paymentBillRepository.findById(billId).orElseThrow().getStatus())
                .isEqualTo(PaymentBillStatus.REFUNDED);

        // then: 결제 시도는 DONE 그대로 (환불은 청구서와 환불 기록으로 관리한다)
        PaymentAttempt attempt = paymentAttemptRepository.findById(refund.getPaymentAttempt().getId()).orElseThrow();
        assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.DONE);
    }
}