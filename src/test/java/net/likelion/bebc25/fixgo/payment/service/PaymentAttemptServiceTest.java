package net.likelion.bebc25.fixgo.payment.service;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.support.AsReceptionFixture;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.payment.entity.*;
import net.likelion.bebc25.fixgo.payment.exception.PaymentErrorCode;
import net.likelion.bebc25.fixgo.payment.exception.PaymentException;
import net.likelion.bebc25.fixgo.payment.repository.PaymentAttemptRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import net.likelion.bebc25.fixgo.payment.repository.VirtualAccountRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({JpaConfig.class, AsReceptionFixture.class, PaymentAttemptService.class})
public class PaymentAttemptServiceTest {
    @Autowired
    AsReceptionFixture asReceptionFixture;

    private AsReception reception;
    private Long asReceptionId;

    @BeforeEach
    void setUpReception() {
        reception = asReceptionFixture.save();
        asReceptionId = reception.getId();
    }

    @Autowired
    PaymentAttemptService paymentAttemptService;
    @Autowired
    PaymentAttemptRepository paymentAttemptRepository;
    @Autowired
    PaymentBillRepository paymentBillRepository;
    @Autowired
    VirtualAccountRepository virtualAccountRepository;
    @Autowired
    EntityManager em;


    private PaymentAttempt startAndFindAttempt(Long asReceptionId) {
        PaymentDto.StartPaymentResponse startPaymentResponse = paymentAttemptService.startPayment(asReceptionId);
        return paymentAttemptRepository.findByOrderId(startPaymentResponse.orderId()).orElseThrow();
    }

    private PaymentBill issueBill() {
        return paymentBillRepository.save(PaymentBill.create(reception, 30_000L, PaymentBillType.REPAIR));
    }

    @Nested
    @DisplayName("startPayment")
    class StartPayment {
        @Test
        @DisplayName("결제를 시작하면 READY 상태의 결제 시도가 만들어진다.")
        void startPayment_createsReadyAttempt() {
            // given: UNPAID bill 저장
            PaymentBill bill = paymentBillRepository.save(PaymentBill.create(reception, 30_000L, PaymentBillType.REPAIR));

            // when
            PaymentDto.StartPaymentResponse response = paymentAttemptService.startPayment(asReceptionId);
            em.flush();
            em.clear();

            // then: attempt가 1개 저장되어 있고, status가 READY이며, amount가 bill의 금액과 같다
            assertThat(paymentAttemptRepository.findAllByPaymentBillId(bill.getId()).size()).isEqualTo(1);
            PaymentAttempt attempt = paymentAttemptRepository.findAllByPaymentBillId(bill.getId()).getFirst();
            assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.READY);
            assertThat(attempt.getAmount()).isEqualTo(bill.getAmount());

            // 응답은 저장된 attempt를 가리킨다
            assertThat(response.orderId()).isEqualTo(attempt.getOrderId());
            assertThat(response.amount()).isEqualTo(bill.getAmount());
            assertThat(response.orderName()).isEqualTo(PaymentBillType.REPAIR.getOrderName());
        }

        @Test
        @DisplayName("같은 청구서에 READY인 결제 시도가 있으면 새로 만들지 않고 재사용한다.")
        void startPayment_reusesReadyAttempt() {
            // given: UNPAID bill 저장
            PaymentBill bill = paymentBillRepository.save(PaymentBill.create(reception, 30_000L, PaymentBillType.REPAIR));

            // when: startPayment를 같은 billId로 두 번 호출
            PaymentAttempt attempt1 = startAndFindAttempt(asReceptionId);
            em.flush();
            em.clear();
            PaymentAttempt attempt2 = startAndFindAttempt(asReceptionId);

            // then: attempt는 여전히 1개이고, 두 호출이 같은 orderId를 가리킨다
            assertThat(paymentAttemptRepository.findAllByPaymentBillId(bill.getId()).size()).isEqualTo(1);
            assertThat(attempt1.getOrderId()).isEqualTo(attempt2.getOrderId());

        }

        @Test
        @DisplayName("이전 결제 시도가 실패했으면 새 결제 시도가 만들어진다.")
        void startPayment_afterFailed() {
            // given: bill + FAILED 상태의 attempt
            PaymentBill bill = paymentBillRepository.save(PaymentBill.create(reception, 30_000L, PaymentBillType.REPAIR));
            PaymentAttempt attempt1 = startAndFindAttempt(asReceptionId);
            attempt1.markFailed("PAY_PROCESS_CANCELED", "사용자가 결제를 취소했습니다.");
            em.flush();
            em.clear();

            // when: startPayment
            PaymentAttempt attempt2 = startAndFindAttempt(asReceptionId);

            // then: 새 attempt가 READY로 만들어지고, 기존 FAILED attempt는 그대로 남아 있다
            //       (실패 이력을 지우지 않는다는 점도 확인한다)
            PaymentAttempt reloadedAttempt1 = paymentAttemptRepository.findById(attempt1.getId()).orElseThrow();
            assertThat(reloadedAttempt1.getStatus()).isEqualTo(PaymentAttemptStatus.FAILED);
            assertThat(attempt2.getStatus()).isEqualTo(PaymentAttemptStatus.READY);
            assertThat(attempt2.getOrderId()).isNotEqualTo(attempt1.getOrderId());
            assertThat(paymentAttemptRepository.findAllByPaymentBillId(bill.getId()).size()).isEqualTo(2);
        }

        @Test
        @DisplayName("청구서가 없는 접수로는 결제를 시작할 수 없다.")
        void startPayment_billNotFound() {
            // given: 접수만 있고 청구서는 없다

            // when & then
            assertThatThrownBy(() -> paymentAttemptService.startPayment(asReceptionId))
                    .isInstanceOf(PaymentException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.BILL_NOT_FOUND);
        }

        @Test
        @DisplayName("이미 결제 완료된 청구서로는 결제를 시작할 수 없고 결제 시도도 만들어지지 않는다.")
        void startPayment_alreadyPaid() {
            // given: bill을 만들고 markPaid()로 PAID 상태로 만든 뒤 저장
            PaymentBill bill = paymentBillRepository.save(PaymentBill.create(reception, 30_000L, PaymentBillType.REPAIR));
            bill.markPaid(LocalDateTime.now());
            em.flush();
            em.clear();

            // when & then: startPayment 호출 시 예외
            assertThatThrownBy(() -> paymentAttemptService.startPayment(asReceptionId))
                    .isInstanceOf(PaymentException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.BILL_NOT_PAYABLE);

            // 실패한 호출이 attempt를 남기지 않았는지도 확인한다
            assertThat(paymentAttemptRepository.findAllByPaymentBillId(bill.getId()).size()).isEqualTo(0);

        }

        @Test
        @DisplayName("승인 진행 중인 결제 시도가 있으면 새 결제를 시작할 수 없다.")
        void startPayment_alreadyInProgress() {
            // given: bill + IN_PROGRESS 상태의 attempt
            PaymentBill bill = paymentBillRepository.save(PaymentBill.create(reception, 30_000L, PaymentBillType.REPAIR));
            PaymentAttempt attempt = startAndFindAttempt(asReceptionId);
            attempt.markInProgress();
            em.flush();
            em.clear();

            // when & then: startPayment 호출 시 예외
            assertThatThrownBy(() -> paymentAttemptService.startPayment(asReceptionId))
                    .isInstanceOf(PaymentException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_IN_PROGRESS);

            // 새 attempt가 만들어지지 않았다
            assertThat(paymentAttemptRepository.findAllByPaymentBillId(bill.getId())).hasSize(1);
        }

        @Test
        @DisplayName("입금 대기 중인 결제 시도가 있으면 새로 결제를 시작할 수 없다. (이중 결제 방지)")
        void startPayment_waitingForDeposit() {
            // given: bill + WAITING_FOR_DEPOSIT 상태의 attempt
            PaymentBill bill = issueBill();
            PaymentAttempt attempt = startAndFindAttempt(asReceptionId);
            attempt.markInProgress();
            attempt.markWaitingForDeposit("pk-waiting");
            em.flush();
            em.clear();

            // when & then
            assertThatThrownBy(() -> paymentAttemptService.startPayment(asReceptionId))
                    .isInstanceOf(PaymentException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_IN_PROGRESS);

            assertThat(paymentAttemptRepository.findAllByPaymentBillId(bill.getId())).hasSize(1);
        }

        @Test
        @DisplayName("bill이 UNPAID여도 DONE인 결제 시도가 있으면 새로 결제를 시작할 수 없다. (방어 로직)")
        void startPayment_doneAttemptWithUnpaidBill() {
            // given: 정상 흐름에서는 생기지 않는 불일치 상태. attempt만 DONE이고 bill은 UNPAID
            PaymentBill bill = issueBill();
            PaymentAttempt attempt = startAndFindAttempt(asReceptionId);
            attempt.markInProgress();
            attempt.markCardDone("pk-done", LocalDateTime.now());
            em.flush();
            em.clear();

            // when & then
            assertThatThrownBy(() -> paymentAttemptService.startPayment(asReceptionId))
                    .isInstanceOf(PaymentException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_IN_PROGRESS);

            assertThat(paymentAttemptRepository.findAllByPaymentBillId(bill.getId())).hasSize(1);
        }

        @Test
        @DisplayName("환불된 청구서로는 결제를 시작할 수 없다.")
        void startPayment_refunded() {
            // given: PAID → REFUNDED
            PaymentBill bill = issueBill();
            bill.markPaid(LocalDateTime.now());
            bill.markRefunded();
            em.flush();
            em.clear();

            // when & then
            assertThatThrownBy(() -> paymentAttemptService.startPayment(asReceptionId))
                    .isInstanceOf(PaymentException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.BILL_NOT_PAYABLE);

            assertThat(paymentAttemptRepository.findAllByPaymentBillId(bill.getId())).isEmpty();
        }

        // 동시 요청 테스트는 실제 커밋이 필요해서 PaymentAttemptServiceConcurrencyTest에 따로 둔다.
    }

    @Nested
    @DisplayName("prepareConfirm")
    class PrepareConfirm {
        @Test
        @DisplayName("READY 결제 시도의 금액이 일치하면 IN_PROGRESS로 변경된다.")
        void prepareConfirm_success() {
            // given: bill + READY attempt
            PaymentBill bill = paymentBillRepository.save(PaymentBill.create(reception, 30_000L, PaymentBillType.REPAIR));
            PaymentAttempt attempt1 = startAndFindAttempt(asReceptionId);

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
            PaymentBill bill = paymentBillRepository.save(PaymentBill.create(reception, 30_000L, PaymentBillType.REPAIR));
            PaymentAttempt attempt = startAndFindAttempt(asReceptionId);
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
            PaymentBill bill = paymentBillRepository.save(PaymentBill.create(reception, 30_000L, PaymentBillType.REPAIR));
            PaymentAttempt attempt = startAndFindAttempt(asReceptionId);
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
            PaymentBill bill = paymentBillRepository.save(PaymentBill.create(reception, 30_000L, PaymentBillType.REPAIR));
            PaymentAttempt attempt = startAndFindAttempt(asReceptionId);
            bill.markPaid(LocalDateTime.now());
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
        private final LocalDateTime approvedAt =
                LocalDateTime.of(2026, 10, 31, 15, 30);

        @Test
        @DisplayName("승인 진행 중인 결제 시도를 완료하면 attempt는 DONE, bill은 PAID가 된다.")
        void success() {
            // given: IN_PROGRESS attempt
            PaymentBill bill = issueBill();
            PaymentAttempt attempt = startAndFindAttempt(asReceptionId);
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
            PaymentAttempt attempt = startAndFindAttempt(asReceptionId);
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

    @Nested
    @DisplayName("issueVirtualAccount")
    class IssueVirtualAccount {

        private final LocalDateTime dueAt = LocalDateTime.of(2026, 11, 9, 23, 59, 59);

        @Test
        @DisplayName("승인 진행 중인 시도에 가상계좌가 발급되면 입금 대기가 되고 계좌 정보가 저장된다. bill은 UNPAID 그대로다.")
        void success() {
            // given: IN_PROGRESS attempt
            PaymentBill bill = issueBill();
            PaymentAttempt attempt = startAndFindAttempt(asReceptionId);
            paymentAttemptService.prepareConfirm(attempt.getOrderId(), attempt.getAmount());

            // when
            paymentAttemptService.issueVirtualAccount(
                    attempt.getOrderId(), "test_payment_key", "20", "12345678901234", "ps_test_secret", dueAt);
            em.flush();
            em.clear();

            // then: attempt
            PaymentAttempt reloaded = paymentAttemptRepository.findById(attempt.getId()).orElseThrow();
            assertThat(reloaded.getStatus()).isEqualTo(PaymentAttemptStatus.WAITING_FOR_DEPOSIT);
            assertThat(reloaded.getPaymentKey()).isEqualTo("test_payment_key");
            assertThat(reloaded.getMethod()).isEqualTo(PaymentMethod.VIRTUAL_ACCOUNT);
            assertThat(reloaded.getApprovedAt()).isNull();

            // then: 계좌 정보. 매개변수 순서가 바뀌는 실수를 잡기 위해 필드를 하나씩 확인한다
            VirtualAccount virtualAccount = virtualAccountRepository.findByPaymentAttemptId(attempt.getId()).orElseThrow();
            assertThat(virtualAccount.getBankCode()).isEqualTo("20");
            assertThat(virtualAccount.getAccountNumber()).isEqualTo("12345678901234");
            assertThat(virtualAccount.getWebhookSecret()).isEqualTo("ps_test_secret");
            assertThat(virtualAccount.getDueAt()).isEqualTo(dueAt);

            // then: 입금 전이므로 청구서는 그대로
            assertThat(paymentBillRepository.findById(bill.getId()).orElseThrow().getStatus())
                    .isEqualTo(PaymentBillStatus.UNPAID);
        }

        @Test
        @DisplayName("승인 준비를 거치지 않은 시도에는 가상계좌를 발급할 수 없고 계좌도 저장되지 않는다.")
        void notInProgress() {
            // given: READY attempt
            PaymentBill bill = issueBill();
            PaymentAttempt attempt = startAndFindAttempt(asReceptionId);
            em.flush();
            em.clear();

            // when & then
            assertThatThrownBy(() -> paymentAttemptService.issueVirtualAccount(
                    attempt.getOrderId(), "test_payment_key", "20", "12345678901234", "ps_test_secret", dueAt))
                    .isInstanceOf(IllegalStateException.class);

            em.clear();
            assertThat(virtualAccountRepository.findByPaymentAttemptId(attempt.getId())).isEmpty();
            assertThat(paymentAttemptRepository.findById(attempt.getId()).orElseThrow().getStatus())
                    .isEqualTo(PaymentAttemptStatus.READY);
        }
    }
}

