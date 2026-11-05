package net.likelion.bebc25.fixgo.statistics.service;

import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.support.AsReceptionFixture;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttempt;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBill;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBillType;
import net.likelion.bebc25.fixgo.payment.repository.PaymentAttemptRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import net.likelion.bebc25.fixgo.statistics.dto.PaymentStatisticsDto;
import net.likelion.bebc25.fixgo.statistics.repository.PaymentStatisticsRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({JpaConfig.class, AsReceptionFixture.class, PaymentStatisticsService.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PaymentStatisticsServiceTest {
    @Autowired
    AsReceptionFixture asReceptionFixture;

    @Autowired
    PaymentStatisticsService paymentStatisticsService;

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
        asReceptionFixture.deleteAll();
    }

    // 접수마다 청구서는 하나뿐이므로 청구서마다 새 접수를 만든다.
    private PaymentBill issueBill(Long amount) {
        return paymentBillRepository.save(PaymentBill.create(asReceptionFixture.save(), amount, PaymentBillType.REPAIR));
    }

    private void createDoneAttempt(PaymentBill bill, String paymentKey, LocalDateTime approvedAt) {
        PaymentAttempt attempt = PaymentAttempt.create(bill);
        attempt.markInProgress();
        attempt.markCardDone(paymentKey, approvedAt);

        paymentAttemptRepository.save(attempt);
    }

    @Test
    @DisplayName("결제 통계를 정상적으로 조회한다.")
    void success() {
        // given
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 26);

        PaymentBill bill1 = issueBill(18_400_000L);
        PaymentBill bill2 = issueBill(500_000L);

        createDoneAttempt(bill1, "payment_key_1", LocalDateTime.of(2026, 10, 10, 15, 30));

        createDoneAttempt(bill2, "payment_key_2", LocalDateTime.of(2026, 10, 20, 15, 30));

        // when
        PaymentStatisticsDto response = paymentStatisticsService.getPaymentStatistics(startDate, endDate);

        // then
        assertThat(response.startDate()).isEqualTo(startDate);
        assertThat(response.endDate()).isEqualTo(endDate);
        assertThat(response.totalPaidAmount()).isEqualTo(18_900_000L);
        assertThat(response.totalRefundAmount()).isZero();
        assertThat(response.netCollectedAmount()).isEqualTo(18_900_000L);
        assertThat(response.unpaidCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("조회 시작일이 null이면 예외가 발생한다.")
    void nullStartDate() {
        // when & then
        assertThatThrownBy(
                () -> paymentStatisticsService.getPaymentStatistics(null, LocalDate.of(2026, 10, 26))).isInstanceOf(
                IllegalArgumentException.class);
    }

    @Test
    @DisplayName("조회 종료일이 null이면 예외가 발생한다.")
    void nullEndDate() {
        // when & then
        assertThatThrownBy(
                () -> paymentStatisticsService.getPaymentStatistics(LocalDate.of(2026, 10, 1), null)).isInstanceOf(
                IllegalArgumentException.class);
    }

    @Test
    @DisplayName("시작일이 종료일보다 늦으면 예외가 발생한다.")
    void invalidDateRange() {
        // given
        LocalDate startDate = LocalDate.of(2026, 10, 27);
        LocalDate endDate = LocalDate.of(2026, 10, 26);

        // when & then
        assertThatThrownBy(() -> paymentStatisticsService.getPaymentStatistics(startDate, endDate)).isInstanceOf(
                IllegalArgumentException.class);
    }
}