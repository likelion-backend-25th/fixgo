package net.likelion.bebc25.fixgo.payment.service;

import net.likelion.bebc25.fixgo.payment.entity.PaymentBill;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBillStatus;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(PaymentBillService.class)
class PaymentBillServiceTest {

    @Autowired
    private PaymentBillService paymentBillService;

    @Autowired
    private PaymentBillRepository paymentBillRepository;

    @Test
    @DisplayName("청구서를 발행하면 UNPAID 상태로 저장되고 id를 돌려준다")
    void issueBill_success() {
        // when
        Long billId = paymentBillService.issueBill(1L, 30_000L);

        // then
        PaymentBill saved = paymentBillRepository.findById(billId).orElseThrow();
        assertThat(saved.getAsRequestId()).isEqualTo(1L);
        assertThat(saved.getAmount()).isEqualTo(30_000L);
        assertThat(saved.getStatus()).isEqualTo(PaymentBillStatus.UNPAID);
    }

    @Test
    @DisplayName("같은 접수에 청구서를 두 번 발행하면 예외가 나고 기존 청구서는 그대로다")
    void issueBill_duplicate() {
        // given
        paymentBillService.issueBill(1L, 30_000L);

        // when & then
        assertThatThrownBy(() -> paymentBillService.issueBill(1L, 10_000L)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("이미 청구서");

        // 실패한 두 번째 호출이 기존 데이터를 건드리지 않았는지도 확인한다.
        PaymentBill existing = paymentBillRepository.findByAsRequestId(1L).orElseThrow();
        assertThat(existing.getAmount()).isEqualTo(30_000L);
    }

    @Test
    @DisplayName("금액이 0 이하이면 발행할 수 없고 아무것도 저장되지 않는다")
    void issueBill_invalidAmount() {
        assertThatThrownBy(() -> paymentBillService.issueBill(1L, 0L)).isInstanceOf(IllegalArgumentException.class);

        assertThat(paymentBillRepository.existsByAsRequestId(1L)).isFalse();
    }
}