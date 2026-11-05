package net.likelion.bebc25.fixgo.payment.service;

import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBill;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBillStatus;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBillType;
import net.likelion.bebc25.fixgo.payment.exception.PaymentErrorCode;
import net.likelion.bebc25.fixgo.payment.exception.PaymentException;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import net.likelion.bebc25.fixgo.support.AsReceptionFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({JpaConfig.class, AsReceptionFixture.class, PaymentBillService.class})
class PaymentBillServiceTest {

    @Autowired
    private PaymentBillService paymentBillService;

    @Autowired
    private PaymentBillRepository paymentBillRepository;

    @Autowired
    private AsReceptionFixture asReceptionFixture;

    private Long asReceptionId;

    @BeforeEach
    void setUpReception() {
        asReceptionId = asReceptionFixture.save().getId();
    }

    @Test
    @DisplayName("청구서를 발행하면 UNPAID 상태로 저장되고 id를 돌려준다")
    void createBill_success() {
        // when
        Long billId = paymentBillService.createBill(asReceptionId, 30_000L, PaymentBillType.REPAIR);

        // then
        PaymentBill saved = paymentBillRepository.findById(billId).orElseThrow();
        assertThat(saved.getAsReception().getId()).isEqualTo(asReceptionId);
        assertThat(saved.getAmount()).isEqualTo(30_000L);
        assertThat(saved.getStatus()).isEqualTo(PaymentBillStatus.UNPAID);
    }

    @Test
    @DisplayName("같은 접수에 청구서를 두 번 발행하면 예외가 나고 기존 청구서는 그대로다")
    void createBill_duplicate() {
        // given
        paymentBillService.createBill(asReceptionId, 30_000L, PaymentBillType.REPAIR);

        // when & then
        assertThatThrownBy(() -> paymentBillService.createBill(asReceptionId, 10_000L, PaymentBillType.REPAIR))
                .isInstanceOf(PaymentException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.BILL_ALREADY_EXISTS);

        // 실패한 두 번째 호출이 기존 데이터를 건드리지 않았는지도 확인한다.
        PaymentBill existing = paymentBillRepository.findByAsReceptionId(asReceptionId).orElseThrow();
        assertThat(existing.getAmount()).isEqualTo(30_000L);
    }

    @Test
    @DisplayName("금액이 0 이하이면 발행할 수 없고 아무것도 저장되지 않는다")
    void createBill_invalidAmount() {
        assertThatThrownBy(() -> paymentBillService.createBill(asReceptionId, 0L, PaymentBillType.REPAIR))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(paymentBillRepository.existsByAsReceptionId(asReceptionId)).isFalse();
    }
}