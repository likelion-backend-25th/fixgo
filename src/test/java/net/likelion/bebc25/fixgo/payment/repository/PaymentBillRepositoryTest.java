package net.likelion.bebc25.fixgo.payment.repository;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.support.AsReceptionFixture;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBill;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBillStatus;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBillType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({JpaConfig.class, AsReceptionFixture.class})
class PaymentBillRepositoryTest {

    private static final Long NOT_EXISTING_RECEPTION_ID = -1L;

    @Autowired
    private PaymentBillRepository paymentBillRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private AsReceptionFixture asReceptionFixture;

    private AsReception reception;

    @BeforeEach
    void setUpReception() {
        reception = asReceptionFixture.save();
    }

    @Test
    @DisplayName("청구서를 저장하고 DB에서 다시 읽으면 값이 그대로이고 UNPAID 상태이다")
    void save_and_find() {
        // given
        PaymentBill saved = paymentBillRepository.saveAndFlush(PaymentBill.create(reception, 30_000L, PaymentBillType.REPAIR));

        // flush: SQL을 DB로 보냄 / clear: 1차 캐시를 비움
        // clear를 안 하면 findById가 DB가 아니라 메모리(1차 캐시)에서 꺼내와서, 매핑이 틀려도 통과해 버린다.
        em.flush();
        em.clear();

        // when
        PaymentBill found = paymentBillRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getAsReception().getId()).isEqualTo(reception.getId());
        assertThat(found.getAmount()).isEqualTo(30_000L);
        assertThat(found.getStatus()).isEqualTo(PaymentBillStatus.UNPAID);
        assertThat(found.getPaidAt()).isNull();
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("같은 asReceptionId로 두 번 저장하면 unique 제약 위반 예외가 난다")
    void save_duplicateAsReceptionId() {
        paymentBillRepository.saveAndFlush(PaymentBill.create(reception, 30_000L, PaymentBillType.REPAIR));

        // 서비스의 existsBy 체크를 거치지 않고 DB에 직접 넣어서, 유니크 제약조건이 동작하는지 확인한다.
        assertThatThrownBy(() -> paymentBillRepository.saveAndFlush(PaymentBill.create(reception, 10_000L, PaymentBillType.REPAIR))).isInstanceOf(
                DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("asReceptionId로 청구서를 조회할 수 있다")
    void findByAsReceptionId() {
        paymentBillRepository.saveAndFlush(PaymentBill.create(reception, 50_000L, PaymentBillType.REPAIR));

        assertThat(paymentBillRepository.findByAsReceptionId(reception.getId())).isPresent();
        assertThat(paymentBillRepository.findByAsReceptionId(NOT_EXISTING_RECEPTION_ID)).isEmpty();
    }

    @Test
    @DisplayName("asReceptionId로 청구서 존재 여부를 확인할 수 있다")
    void existsByAsReceptionId() {
        paymentBillRepository.saveAndFlush(PaymentBill.create(reception, 50_000L, PaymentBillType.REPAIR));

        assertThat(paymentBillRepository.existsByAsReceptionId(reception.getId())).isTrue();
        assertThat(paymentBillRepository.existsByAsReceptionId(NOT_EXISTING_RECEPTION_ID)).isFalse();
    }
}
