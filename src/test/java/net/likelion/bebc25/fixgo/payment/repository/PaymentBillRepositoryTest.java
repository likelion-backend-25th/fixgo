package net.likelion.bebc25.fixgo.payment.repository;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBill;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBillStatus;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBillType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class PaymentBillRepositoryTest {

    @Autowired
    private PaymentBillRepository paymentBillRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("청구서를 저장하고 DB에서 다시 읽으면 값이 그대로이고 UNPAID 상태이다")
    void save_and_find() {
        // given
        PaymentBill saved = paymentBillRepository.saveAndFlush(PaymentBill.issue(1L, 30_000L, PaymentBillType.REPAIR));

        // flush: SQL을 DB로 보냄 / clear: 1차 캐시를 비움
        // clear를 안 하면 findById가 DB가 아니라 메모리(1차 캐시)에서 꺼내와서, 매핑이 틀려도 통과해 버린다.
        em.flush();
        em.clear();

        // when
        PaymentBill found = paymentBillRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getAsRequestId()).isEqualTo(1L);
        assertThat(found.getAmount()).isEqualTo(30_000L);
        assertThat(found.getStatus()).isEqualTo(PaymentBillStatus.UNPAID);
        assertThat(found.getPaidAt()).isNull();
    }

    @Test
    @DisplayName("같은 asRequestId로 두 번 저장하면 unique 제약 위반 예외가 난다")
    void save_duplicateAsRequestId() {
        paymentBillRepository.saveAndFlush(PaymentBill.issue(1L, 30_000L, PaymentBillType.REPAIR));

        // 서비스의 existsBy 체크를 거치지 않고 DB에 직접 넣어서, 유니크 제약조건이 동작하는지 확인한다.
        assertThatThrownBy(() -> paymentBillRepository.saveAndFlush(PaymentBill.issue(1L, 10_000L, PaymentBillType.REPAIR))).isInstanceOf(
                DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("asRequestId로 청구서를 조회할 수 있다")
    void findByAsRequestId() {
        paymentBillRepository.saveAndFlush(PaymentBill.issue(7L, 50_000L, PaymentBillType.REPAIR));

        assertThat(paymentBillRepository.findByAsRequestId(7L)).isPresent();
        assertThat(paymentBillRepository.findByAsRequestId(999L)).isEmpty();
    }

    @Test
    @DisplayName("asRequestId로 청구서 존재 여부를 확인할 수 있다")
    void existsByAsRequestId() {
        paymentBillRepository.saveAndFlush(PaymentBill.issue(7L, 50_000L, PaymentBillType.REPAIR));

        assertThat(paymentBillRepository.existsByAsRequestId(7L)).isTrue();
        assertThat(paymentBillRepository.existsByAsRequestId(999L)).isFalse();
    }
}
