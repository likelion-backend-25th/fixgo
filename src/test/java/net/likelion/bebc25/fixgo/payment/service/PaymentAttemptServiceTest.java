package net.likelion.bebc25.fixgo.payment.service;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttempt;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttemptStatus;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBill;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBillType;
import net.likelion.bebc25.fixgo.payment.repository.PaymentAttemptRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(PaymentAttemptService.class)
public class PaymentAttemptServiceTest {

    @Autowired
    PaymentAttemptService paymentAttemptService;
    @Autowired
    PaymentAttemptRepository paymentAttemptRepository;
    @Autowired
    PaymentBillRepository paymentBillRepository;
    @Autowired
    EntityManager em;

    @Test
    @DisplayName("결제를 시작하면 READY 상태의 결제 시도가 만들어진다.")
    void startPayment_createsReadyAttempt() {
        // given: UNPAID bill 저장
        PaymentBill bill = paymentBillRepository.save(PaymentBill.issue(1L, 30_000L, PaymentBillType.REPAIR));

        // when
        paymentAttemptService.startPayment(bill.getId());
        em.flush();
        em.clear();

        // then: attempt가 1개 저장되어 있고, status가 READY이며, amount가 bill의 금액과 같다
        assertThat(paymentAttemptRepository.findAllByPaymentBillId(bill.getId()).size()).isEqualTo(1);
        PaymentAttempt attempt = paymentAttemptRepository.findAllByPaymentBillId(bill.getId()).getFirst();
        assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.READY);
        assertThat(attempt.getAmount()).isEqualTo(bill.getAmount());
    }

    @Test
    @DisplayName("같은 청구서에 READY인 결제 시도가 있으면 새로 만들지 않고 재사용한다.")
    void startPayment_reusesReadyAttempt() {
        // given: UNPAID bill 저장
        PaymentBill bill = paymentBillRepository.save(PaymentBill.issue(1L, 30_000L, PaymentBillType.REPAIR));

        // when: startPayment를 같은 billId로 두 번 호출
        PaymentAttempt attempt1 = startAndFindAttempt(bill);
        em.flush();
        em.clear();
        PaymentAttempt attempt2 = startAndFindAttempt(bill);

        // then: attempt는 여전히 1개이고, 두 호출이 같은 orderId를 가리킨다
        assertThat(paymentAttemptRepository.findAllByPaymentBillId(bill.getId()).size()).isEqualTo(1);
        assertThat(attempt1.getOrderId()).isEqualTo(attempt2.getOrderId());

    }

    private PaymentAttempt startAndFindAttempt(PaymentBill bill) {
        PaymentDto.StartPaymentResponse startPaymentResponse = paymentAttemptService.startPayment(bill.getId());
        return paymentAttemptRepository.findByOrderId(startPaymentResponse.orderId()).orElseThrow();
    }


    @Test
    @DisplayName("이전 결제 시도가 실패했으면 새 결제 시도가 만들어진다.")
    void startPayment_afterFailed() {
        // given: bill + FAILED 상태의 attempt
        PaymentBill bill = paymentBillRepository.save(PaymentBill.issue(1L, 30_000L, PaymentBillType.REPAIR));
        PaymentAttempt attempt1 = startAndFindAttempt(bill);
        attempt1.markFailed("PAY_PROCESS_CANCELED", "사용자가 결제를 취소했습니다.");
        em.flush();
        em.clear();

        // when: startPayment
        PaymentAttempt attempt2 = startAndFindAttempt(bill);

        // then: 새 attempt가 READY로 만들어지고, 기존 FAILED attempt는 그대로 남아 있다
        //       (실패 이력을 지우지 않는다는 점도 확인한다)
        PaymentAttempt reloadedAttempt1 = paymentAttemptRepository.findById(attempt1.getId()).orElseThrow();
        assertThat(reloadedAttempt1.getStatus()).isEqualTo(PaymentAttemptStatus.FAILED);
        assertThat(attempt2.getStatus()).isEqualTo(PaymentAttemptStatus.READY);
        assertThat(attempt2.getOrderId()).isNotEqualTo(attempt1.getOrderId());
        assertThat(paymentAttemptRepository.findAllByPaymentBillId(bill.getId()).size()).isEqualTo(2);
    }

    // ---------- 실패 흐름 ----------

    @Test
    @DisplayName("존재하지 않는 청구서로는 결제를 시작할 수 없다.")
    void startPayment_billNotFound() {
        // given: 저장하지 않은 id
        Long billId = 999L;

        // when & then: startPayment 호출 시 예외 (어떤 예외 타입을 던질지 정한다)
        assertThatThrownBy(() -> paymentAttemptService.startPayment(billId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("이미 결제 완료된 청구서로는 결제를 시작할 수 없고 결제 시도도 만들어지지 않는다.")
    void startPayment_alreadyPaid() {
        // given: bill을 만들고 markPaid()로 PAID 상태로 만든 뒤 저장
        PaymentBill bill = paymentBillRepository.save(PaymentBill.issue(1L, 30_000L, PaymentBillType.REPAIR));
        bill.markPaid();
        em.flush();
        em.clear();

        // when & then: startPayment 호출 시 예외
        assertThatThrownBy(() -> paymentAttemptService.startPayment(bill.getId()))
                .isInstanceOf(IllegalStateException.class);

        // 실패한 호출이 attempt를 남기지 않았는지도 확인한다
        assertThat(paymentAttemptRepository.findAllByPaymentBillId(bill.getId()).size()).isEqualTo(0);

    }

    @Test
    @Disabled("선행 작업: 가상계좌 단계에서 WAITING_FOR_DEPOSIT으로 만드는 엔티티 메서드 구현 후")
    @DisplayName("입금 대기 중인 결제 시도가 있으면 새로 결제를 시작할 수 없다. (이중 결제 방지)")
    void startPayment_waitingForDeposit() {
        // given: bill + WAITING_FOR_DEPOSIT 상태의 attempt

        // when & then: startPayment 호출 시 예외
    }

    @Test
    @Disabled("동시성 테스트: @DataJpaTest는 테스트 하나가 트랜잭션 하나라서 불가능. @SpringBootTest + 스레드로 별도 작성")
    @DisplayName("같은 청구서에 동시에 결제를 시작해도 결제 시도는 하나만 만들어진다.")
    void startPayment_concurrent() {
        // 이 테스트가 증명하는 것: bill 잠금이 중복 생성을 막는다.
    }
}
