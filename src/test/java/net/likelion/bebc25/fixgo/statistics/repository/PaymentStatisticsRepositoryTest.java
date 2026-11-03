package net.likelion.bebc25.fixgo.statistics.repository;

import net.likelion.bebc25.fixgo.payment.entity.*;
import net.likelion.bebc25.fixgo.payment.repository.PaymentAttemptRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PaymentStatisticsRepositoryTest {

    @Autowired
    PaymentStatisticsRepository paymentStatisticsRepository;

    @Autowired
    PaymentAttemptRepository paymentAttemptRepository;

    @Autowired
    PaymentBillRepository paymentBillRepository;

    @AfterEach
    void cleanUp() {
        paymentAttemptRepository.deleteAll();
        paymentBillRepository.deleteAll();
    }

    private PaymentBill issueBill(Long asRequestId, Long amount) {
        return paymentBillRepository.save(PaymentBill.issue(asRequestId, amount, PaymentBillType.REPAIR));
    }

    private PaymentAttempt createDoneAttempt(PaymentBill bill, String paymentKey, LocalDateTime approvedAt) {
        PaymentAttempt attempt = PaymentAttempt.create(bill);
        attempt.markInProgress();
        attempt.markCardDone(paymentKey, approvedAt);

        return paymentAttemptRepository.save(attempt);
    }

    private PaymentAttempt createReadyAttempt(PaymentBill bill) {
        return paymentAttemptRepository.save(PaymentAttempt.create(bill));
    }

    @Test
    @DisplayName("조회 기간 내 승인 완료된 결제 금액만 합산한다.")
    void sumApprovedAmount() {
        // given
        PaymentBill bill1 = issueBill(1L, 30_000L);
        PaymentBill bill2 = issueBill(2L, 50_000L);
        PaymentBill bill3 = issueBill(3L, 70_000L);

        createDoneAttempt(bill1, "payment_key_1", LocalDateTime.of(2026, 10, 10, 15, 30));

        createDoneAttempt(bill2, "payment_key_2", LocalDateTime.of(2026, 10, 20, 15, 30));

        // READY 상태이므로 합산되지 않아야 한다.
        createReadyAttempt(bill3);

        LocalDateTime startDateTime = LocalDateTime.of(2026, 10, 1, 0, 0);

        LocalDateTime endDateTime = LocalDateTime.of(2026, 11, 1, 0, 0);

        // when
        Long result = paymentStatisticsRepository.sumApprovedAmount(PaymentAttemptStatus.DONE, startDateTime,
                                                                    endDateTime);

        // then
        assertThat(result).isEqualTo(80_000L);
    }

    @Test
    @DisplayName("현재 UNPAID 상태인 청구서의 전체 건수를 조회한다.")
    void countUnpaidBills() {
        // given
        PaymentBill bill1 = issueBill(1L, 30_000L);
        PaymentBill bill2 = issueBill(2L, 50_000L);
        issueBill(3L, 70_000L);

        // bill1은 결제 완료 처리
        bill1.markPaid(LocalDateTime.of(2026, 10, 10, 15, 30));

        paymentBillRepository.save(bill1);

        // when
        Long result = paymentStatisticsRepository.countUnpaidBills(PaymentBillStatus.UNPAID);

        // then
        assertThat(result).isEqualTo(2L);
    }
}
