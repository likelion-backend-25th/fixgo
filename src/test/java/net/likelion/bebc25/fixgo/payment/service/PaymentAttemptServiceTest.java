package net.likelion.bebc25.fixgo.payment.service;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.payment.entity.*;
import net.likelion.bebc25.fixgo.payment.exception.PaymentErrorCode;
import net.likelion.bebc25.fixgo.payment.exception.PaymentException;
import net.likelion.bebc25.fixgo.payment.repository.PaymentAttemptRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
@Import(PaymentAttemptService.class)
public class PaymentAttemptServiceTest {
    private static final Long AS_REQUEST_ID = 1L;
    @Autowired
    PaymentAttemptService paymentAttemptService;
    @Autowired
    PaymentAttemptRepository paymentAttemptRepository;
    @Autowired
    PaymentBillRepository paymentBillRepository;
    @Autowired
    EntityManager em;

    private PaymentAttempt startAndFindAttempt(Long asRequestId) {
        PaymentDto.StartPaymentResponse startPaymentResponse = paymentAttemptService.startPayment(asRequestId);
        return paymentAttemptRepository.findByOrderId(startPaymentResponse.orderId()).orElseThrow();
    }

    private PaymentBill issueBill() {
        return paymentBillRepository.save(PaymentBill.issue(AS_REQUEST_ID, 30_000L, PaymentBillType.REPAIR));
    }

    @Nested
    @DisplayName("startPayment")
    class StartPayment {
        @Test
        @DisplayName("결제를 시작하면 READY 상태의 결제 시도가 만들어진다.")
        void startPayment_createsReadyAttempt() {
            // given: UNPAID bill 저장
            PaymentBill bill = paymentBillRepository.save(PaymentBill.issue(AS_REQUEST_ID, 30_000L, PaymentBillType.REPAIR));

            // when
            paymentAttemptService.startPayment(AS_REQUEST_ID);
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
            PaymentAttempt attempt1 = startAndFindAttempt(AS_REQUEST_ID);
            em.flush();
            em.clear();
            PaymentAttempt attempt2 = startAndFindAttempt(AS_REQUEST_ID);

            // then: attempt는 여전히 1개이고, 두 호출이 같은 orderId를 가리킨다
            assertThat(paymentAttemptRepository.findAllByPaymentBillId(bill.getId()).size()).isEqualTo(1);
            assertThat(attempt1.getOrderId()).isEqualTo(attempt2.getOrderId());

        }

        @Test
        @DisplayName("이전 결제 시도가 실패했으면 새 결제 시도가 만들어진다.")
        void startPayment_afterFailed() {
            // given: bill + FAILED 상태의 attempt
            PaymentBill bill = paymentBillRepository.save(PaymentBill.issue(1L, 30_000L, PaymentBillType.REPAIR));
            PaymentAttempt attempt1 = startAndFindAttempt(AS_REQUEST_ID);
            attempt1.markFailed("PAY_PROCESS_CANCELED", "사용자가 결제를 취소했습니다.");
            em.flush();
            em.clear();

            // when: startPayment
            PaymentAttempt attempt2 = startAndFindAttempt(AS_REQUEST_ID);

            // then: 새 attempt가 READY로 만들어지고, 기존 FAILED attempt는 그대로 남아 있다
            //       (실패 이력을 지우지 않는다는 점도 확인한다)
            PaymentAttempt reloadedAttempt1 = paymentAttemptRepository.findById(attempt1.getId()).orElseThrow();
            assertThat(reloadedAttempt1.getStatus()).isEqualTo(PaymentAttemptStatus.FAILED);
            assertThat(attempt2.getStatus()).isEqualTo(PaymentAttemptStatus.READY);
            assertThat(attempt2.getOrderId()).isNotEqualTo(attempt1.getOrderId());
            assertThat(paymentAttemptRepository.findAllByPaymentBillId(bill.getId()).size()).isEqualTo(2);
        }

        @Test
        @DisplayName("존재하지 않는 청구서로는 결제를 시작할 수 없다.")
        void startPayment_billNotFound() {
            // given: 저장하지 않은 id
            Long billId = 999L;

            // when & then: startPayment 호출 시 예외 (어떤 예외 타입을 던질지 정한다)
            assertThatThrownBy(() -> paymentAttemptService.startPayment(AS_REQUEST_ID))
                    .isInstanceOf(PaymentException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.BILL_NOT_FOUND);
        }

        @Test
        @DisplayName("이미 결제 완료된 청구서로는 결제를 시작할 수 없고 결제 시도도 만들어지지 않는다.")
        void startPayment_alreadyPaid() {
            // given: bill을 만들고 markPaid()로 PAID 상태로 만든 뒤 저장
            PaymentBill bill = paymentBillRepository.save(PaymentBill.issue(1L, 30_000L, PaymentBillType.REPAIR));
            bill.markPaid(OffsetDateTime.now());
            em.flush();
            em.clear();

            // when & then: startPayment 호출 시 예외
            assertThatThrownBy(() -> paymentAttemptService.startPayment(AS_REQUEST_ID))
                    .isInstanceOf(IllegalStateException.class);

            // 실패한 호출이 attempt를 남기지 않았는지도 확인한다
            assertThat(paymentAttemptRepository.findAllByPaymentBillId(bill.getId()).size()).isEqualTo(0);

        }

        @Test
        @DisplayName("승인 진행 중인 결제 시도가 있으면 새 결제를 시작할 수 없다.")
        void startPayment_alreadyInProgress() {
            // given: bill + IN_PROGRESS 상태의 attempt
            PaymentBill bill = paymentBillRepository.save(PaymentBill.issue(1L, 30_000L, PaymentBillType.REPAIR));
            PaymentAttempt attempt = startAndFindAttempt(AS_REQUEST_ID);
            attempt.markInProgress();
            em.flush();
            em.clear();

            // when & then: startPayment 호출 시 예외
            assertThatThrownBy(() -> paymentAttemptService.startPayment(AS_REQUEST_ID))
                    .isInstanceOf(PaymentException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_IN_PROGRESS);

            // 새 attempt가 만들어지지 않았다
            assertThat(paymentAttemptRepository.findAllByPaymentBillId(bill.getId())).hasSize(1);
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

    @Nested
    @DisplayName("prepareConfirm")
    class PrepareConfirm {
        @Test
        @DisplayName("READY 결제 시도의 금액이 일치하면 IN_PROGRESS로 변경된다.")
        void prepareConfirm_success() {
            // given: bill + READY attempt
            PaymentBill bill = paymentBillRepository.save(PaymentBill.issue(1L, 30_000L, PaymentBillType.REPAIR));
            PaymentAttempt attempt1 = startAndFindAttempt(AS_REQUEST_ID);

            // when: 저장된 orderId와 amount로 prepareConfirm
            paymentAttemptService.prepareConfirm(attempt1.getOrderId(), attempt1.getAmount());
            em.flush();
            em.clear();

            // then: attempt가 IN_PROGRESS로 바뀌어 있다 (DB에서 다시 조회해서 확인)
            PaymentAttempt reloaded = paymentAttemptRepository.findById(attempt1.getId()).orElseThrow();
            assertThat(reloaded.getStatus()).isEqualTo(PaymentAttemptStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("존재하지 않는 orderId로는 승인을 준비할 수 없다.")
        void prepareConfirm_orderNotFound() {
            // when & then
            assertThatThrownBy(() -> paymentAttemptService.prepareConfirm("NOT_EXIST_ORDER", 30_000L))
                    .isInstanceOf(PaymentException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.ATTEMPT_NOT_FOUND);
        }

        @Test
        @DisplayName("요청 금액이 저장된 금액과 다르면 승인을 준비할 수 없고 상태도 바뀌지 않는다.")
        void prepareConfirm_amountMismatch() {
            // given
            PaymentBill bill = paymentBillRepository.save(PaymentBill.issue(1L, 30_000L, PaymentBillType.REPAIR));
            PaymentAttempt attempt = startAndFindAttempt(AS_REQUEST_ID);
            em.flush();
            em.clear();

            // when & then
            assertThatThrownBy(() -> paymentAttemptService.prepareConfirm(attempt.getOrderId(), 1_000L))
                    .isInstanceOf(PaymentException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.AMOUNT_MISMATCH);

            // then: prepareConfirm은 상태를 바꾸지 않는다 (FAILED 기록은 호출하는 쪽 책임)
            em.clear();
            PaymentAttempt reloaded = paymentAttemptRepository.findById(attempt.getId()).orElseThrow();
            assertThat(reloaded.getStatus()).isEqualTo(PaymentAttemptStatus.READY);
        }

        @Test
        @DisplayName("이미 승인 진행 중인 결제 시도는 다시 승인을 준비할 수 없다. (중복 승인 방지)")
        void prepareConfirm_alreadyInProgress() {
            // given: 한 번 승인 준비해서 IN_PROGRESS로 만든다
            PaymentBill bill = paymentBillRepository.save(PaymentBill.issue(1L, 30_000L, PaymentBillType.REPAIR));
            PaymentAttempt attempt = startAndFindAttempt(AS_REQUEST_ID);
            paymentAttemptService.prepareConfirm(attempt.getOrderId(), attempt.getAmount());
            em.flush();
            em.clear();

            // when & then: 같은 요청이 한 번 더 오면 거절
            assertThatThrownBy(() -> paymentAttemptService.prepareConfirm(attempt.getOrderId(), attempt.getAmount()))
                    .isInstanceOf(PaymentException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.ATTEMPT_NOT_READY);
        }

        @Test
        @DisplayName("청구서가 이미 결제 완료되었으면 승인을 준비할 수 없다.")
        void prepareConfirm_billAlreadyPaid() {
            // given: READY attempt가 있는 상태에서 bill이 PAID가 된다
            //        (현실에서는 카드 결제창을 띄워 둔 사이 현장에서 현금으로 결제된 경우)
            PaymentBill bill = paymentBillRepository.save(PaymentBill.issue(1L, 30_000L, PaymentBillType.REPAIR));
            PaymentAttempt attempt = startAndFindAttempt(AS_REQUEST_ID);
            bill.markPaid(OffsetDateTime.now());
            em.flush();
            em.clear();

            // when & then
            assertThatThrownBy(() -> paymentAttemptService.prepareConfirm(attempt.getOrderId(), attempt.getAmount()))
                    .isInstanceOf(PaymentException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.BILL_NOT_PAYABLE);

            // then
            em.clear();
            PaymentAttempt reloaded = paymentAttemptRepository.findById(attempt.getId()).orElseThrow();
            assertThat(reloaded.getStatus()).isEqualTo(PaymentAttemptStatus.READY);
        }

        @Test
        @Disabled("동시성 테스트: @SpringBootTest + 스레드로 별도 작성")
        @DisplayName("같은 결제 시도에 승인 준비가 동시에 들어와도 하나만 통과한다.")
        void prepareConfirm_concurrent() {
        }
    }

    @Nested
    @DisplayName("prepareConfirm")
    class CompleteConfirm {
        private final OffsetDateTime approvedAt =
                OffsetDateTime.of(2026, 10, 31, 15, 30, 0, 0, ZoneOffset.ofHours(9));

        @Test
        @DisplayName("승인 진행 중인 결제 시도를 완료하면 attempt는 DONE, bill은 PAID가 된다.")
        void success() {
            // given: IN_PROGRESS attempt
            PaymentBill bill = issueBill();
            PaymentAttempt attempt = startAndFindAttempt(AS_REQUEST_ID);
            paymentAttemptService.prepareConfirm(attempt.getOrderId(), attempt.getAmount());
            em.flush();
            em.clear();

            // when
            paymentAttemptService.completeConfirm(attempt.getOrderId(), "test_payment_key", approvedAt);
            em.flush();
            em.clear();

            // then: attempt
            PaymentAttempt reloadedAttempt = paymentAttemptRepository.findById(attempt.getId()).orElseThrow();
            assertThat(reloadedAttempt.getStatus()).isEqualTo(PaymentAttemptStatus.DONE);
            assertThat(reloadedAttempt.getPaymentKey()).isEqualTo("test_payment_key");
            assertThat(reloadedAttempt.getMethod()).isEqualTo(PaymentMethod.CARD);
            assertThat(reloadedAttempt.getApprovedAt()).isEqualTo(LocalDateTime.of(2026, 10, 31, 15, 30));

            // then: bill
            PaymentBill reloadedBill = paymentBillRepository.findById(bill.getId()).orElseThrow();
            assertThat(reloadedBill.getStatus()).isEqualTo(PaymentBillStatus.PAID);
            assertThat(reloadedBill.getPaidAt()).isEqualTo(LocalDateTime.of(2026, 10, 31, 15, 30));
        }

        @Test
        @DisplayName("승인 준비를 거치지 않은 결제 시도는 완료할 수 없고 상태도 바뀌지 않는다.")
        void notInProgress() {
            // given: READY attempt (prepareConfirm을 거치지 않음)
            PaymentBill bill = issueBill();
            PaymentAttempt attempt = startAndFindAttempt(AS_REQUEST_ID);
            em.flush();
            em.clear();

            // when & then
            assertThatThrownBy(() -> paymentAttemptService.completeConfirm(attempt.getOrderId(), "test_payment_key", approvedAt))
                    .isInstanceOf(IllegalStateException.class);

            // then: attempt와 bill 모두 그대로
            em.clear();
            assertThat(paymentAttemptRepository.findById(attempt.getId()).orElseThrow().getStatus())
                    .isEqualTo(PaymentAttemptStatus.READY);
            assertThat(paymentBillRepository.findById(bill.getId()).orElseThrow().getStatus())
                    .isEqualTo(PaymentBillStatus.UNPAID);
        }
    }
}

