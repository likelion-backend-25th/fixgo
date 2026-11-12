package net.likelion.bebc25.fixgo.payment.service;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.service.AsStatusHistoryService;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.payment.client.TossDto;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.payment.entity.*;
import net.likelion.bebc25.fixgo.payment.exception.PaymentErrorCode;
import net.likelion.bebc25.fixgo.payment.exception.PaymentException;
import net.likelion.bebc25.fixgo.payment.repository.PaymentAttemptRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import net.likelion.bebc25.fixgo.payment.repository.VirtualAccountRepository;
import net.likelion.bebc25.fixgo.support.AsReceptionFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({JpaConfig.class, AsReceptionFixture.class, AsStatusHistoryService.class, PaymentAttemptService.class})
public class PaymentAttemptServiceTest {
    @Autowired
    AsReceptionFixture asReceptionFixture;
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
    private AsReception reception;
    private Long asReceptionId;

    @BeforeEach
    void setUpReception() {
        reception = asReceptionFixture.save();
        asReceptionId = reception.getId();
    }

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
            attempt.markOnlineDone("pk-done", PaymentMethod.CARD, LocalDateTime.now());
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
            PaymentErrorCode failure = paymentAttemptService.prepareConfirm(attempt1.getOrderId(), attempt1.getAmount());
            em.flush();
            em.clear();

            // then: 실패 코드 없이 통과하고, attempt가 IN_PROGRESS로 바뀌어 있다 (DB에서 다시 조회해서 확인)
            assertThat(failure).isNull();
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
        @DisplayName("요청 금액이 저장된 금액과 다르면 AMOUNT_MISMATCH를 반환하고 attempt를 FAILED로 기록한다.")
        void prepareConfirm_amountMismatch() {
            // given
            PaymentBill bill = paymentBillRepository.save(PaymentBill.create(reception, 30_000L, PaymentBillType.REPAIR));
            PaymentAttempt attempt = startAndFindAttempt(asReceptionId);
            em.flush();
            em.clear();

            // when
            PaymentErrorCode failure = paymentAttemptService.prepareConfirm(attempt.getOrderId(), 1_000L);
            em.flush();
            em.clear();

            // then: 실패 코드를 돌려주고, 같은 트랜잭션에서 FAILED가 기록된다
            assertThat(failure).isEqualTo(PaymentErrorCode.AMOUNT_MISMATCH);
            PaymentAttempt reloaded = paymentAttemptRepository.findById(attempt.getId()).orElseThrow();
            assertThat(reloaded.getStatus()).isEqualTo(PaymentAttemptStatus.FAILED);
            assertThat(reloaded.getFailureCode()).isEqualTo(PaymentErrorCode.AMOUNT_MISMATCH.name());
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

            // then: 진행 중인 attempt는 건드리지 않는다
            em.clear();
            PaymentAttempt reloaded = paymentAttemptRepository.findById(attempt.getId()).orElseThrow();
            assertThat(reloaded.getStatus()).isEqualTo(PaymentAttemptStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("READY가 아니면 금액이 달라도 FAILED로 바꾸지 않는다. (READY 검사가 먼저)")
        void prepareConfirm_notReadyWithWrongAmount() {
            // given: IN_PROGRESS attempt
            PaymentBill bill = issueBill();
            PaymentAttempt attempt = startAndFindAttempt(asReceptionId);
            paymentAttemptService.prepareConfirm(attempt.getOrderId(), attempt.getAmount());
            em.flush();
            em.clear();

            // when & then: 금액을 조작한 요청이 와도 ATTEMPT_NOT_READY로만 거절한다
            assertThatThrownBy(() -> paymentAttemptService.prepareConfirm(attempt.getOrderId(), 1_000L))
                    .isInstanceOf(PaymentException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.ATTEMPT_NOT_READY);

            // then: 진행 중인 결제가 FAILED로 덮이지 않는다
            em.clear();
            PaymentAttempt reloaded = paymentAttemptRepository.findById(attempt.getId()).orElseThrow();
            assertThat(reloaded.getStatus()).isEqualTo(PaymentAttemptStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("청구서가 이미 결제 완료되었으면 BILL_NOT_PAYABLE을 반환하고 attempt를 FAILED로 기록한다.")
        void prepareConfirm_billAlreadyPaid() {
            // given: READY attempt가 있는 상태에서 bill이 PAID가 된다
            //        (현실에서는 카드 결제창을 띄워 둔 사이 현장에서 현금으로 결제된 경우)
            PaymentBill bill = paymentBillRepository.save(PaymentBill.create(reception, 30_000L, PaymentBillType.REPAIR));
            PaymentAttempt attempt = startAndFindAttempt(asReceptionId);
            bill.markPaid(LocalDateTime.now());
            em.flush();
            em.clear();

            // when
            PaymentErrorCode failure = paymentAttemptService.prepareConfirm(attempt.getOrderId(), attempt.getAmount());
            em.flush();
            em.clear();

            // then
            assertThat(failure).isEqualTo(PaymentErrorCode.BILL_NOT_PAYABLE);
            PaymentAttempt reloaded = paymentAttemptRepository.findById(attempt.getId()).orElseThrow();
            assertThat(reloaded.getStatus()).isEqualTo(PaymentAttemptStatus.FAILED);
            assertThat(reloaded.getFailureCode()).isEqualTo(PaymentErrorCode.BILL_NOT_PAYABLE.name());
        }

        // 동시 요청 테스트는 실제 커밋이 필요해서 PaymentAttemptServiceConcurrencyTest에 따로 둔다.
    }

    /**
     * 토스 결제 상태(승인 응답 또는 결제 조회 응답)를 받아 attempt를 맞춘다.
     * 스케줄러와 승인 스레드가 둘 다 호출할 수 있으므로, 누가 먼저 호출하든 결과가 같고 두 번 반영되지 않아야 한다.
     */
    @Nested
    @DisplayName("reconcile")
    class Reconcile {
        private static final String PAYMENT_KEY = "test_payment_key";
        private final OffsetDateTime tossApprovedAt =
                OffsetDateTime.of(2026, 10, 31, 15, 30, 0, 0, ZoneOffset.ofHours(9));
        private final LocalDateTime approvedAt = LocalDateTime.of(2026, 10, 31, 15, 30);

        private TossDto.ConfirmResponse tossPayment(String orderId, String status, String method) {
            return new TossDto.ConfirmResponse(PAYMENT_KEY, orderId, status, method, tossApprovedAt, null, null, null);
        }

        /**
         * 토스 응답을 받지 못해 IN_PROGRESS로 남은 attempt
         */
        private PaymentAttempt inProgressAttempt() {
            PaymentAttempt attempt = startAndFindAttempt(asReceptionId);
            paymentAttemptService.prepareConfirm(attempt.getOrderId(), attempt.getAmount());
            em.flush();
            em.clear();
            return attempt;
        }

        private PaymentAttempt reload(PaymentAttempt attempt) {
            return paymentAttemptRepository.findById(attempt.getId()).orElseThrow();
        }

        private PaymentBill reload(PaymentBill bill) {
            return paymentBillRepository.findById(bill.getId()).orElseThrow();
        }

        @Test
        @DisplayName("토스 결제가 DONE이면 attempt는 DONE, bill은 PAID가 되고 승인 정보가 기록된다.")
        void done() {
            // given
            PaymentBill bill = issueBill();
            PaymentAttempt attempt = inProgressAttempt();

            // when
            paymentAttemptService.reconcile(tossPayment(attempt.getOrderId(), "DONE", "카드"));
            em.flush();
            em.clear();

            // then: attempt
            PaymentAttempt reloadedAttempt = reload(attempt);
            assertThat(reloadedAttempt.getStatus()).isEqualTo(PaymentAttemptStatus.DONE);
            assertThat(reloadedAttempt.getPaymentKey()).isEqualTo(PAYMENT_KEY);
            assertThat(reloadedAttempt.getMethod()).isEqualTo(PaymentMethod.CARD);
            assertThat(reloadedAttempt.getApprovedAt()).isEqualTo(approvedAt);

            // then: bill
            PaymentBill reloadedBill = reload(bill);
            assertThat(reloadedBill.getStatus()).isEqualTo(PaymentBillStatus.PAID);
            assertThat(reloadedBill.getPaidAt()).isEqualTo(approvedAt);
        }

        @Test
        @DisplayName("간편결제로 승인되면 결제수단이 EASY_PAY로 저장된다.")
        void doneByEasyPay() {
            // given
            issueBill();
            PaymentAttempt attempt = inProgressAttempt();

            // when
            paymentAttemptService.reconcile(tossPayment(attempt.getOrderId(), "DONE", "간편결제"));
            em.flush();
            em.clear();

            // then
            PaymentAttempt reloadedAttempt = reload(attempt);
            assertThat(reloadedAttempt.getStatus()).isEqualTo(PaymentAttemptStatus.DONE);
            assertThat(reloadedAttempt.getMethod()).isEqualTo(PaymentMethod.EASY_PAY);
        }

        @Test
        @DisplayName("승인 준비를 거치지 않은 READY 결제에는 토스 결과를 반영하지 않는다.")
        void readyIsNotReconciled() {
            // given: 승인 요청 전인 attempt
            PaymentBill bill = issueBill();
            PaymentAttempt attempt = startAndFindAttempt(asReceptionId);
            em.flush();
            em.clear();

            // when
            PaymentAttemptStatus result = paymentAttemptService.reconcile(tossPayment(attempt.getOrderId(), "DONE", "카드"));
            em.flush();
            em.clear();

            // then
            assertThat(result).isEqualTo(PaymentAttemptStatus.READY);
            assertThat(reload(attempt).getStatus()).isEqualTo(PaymentAttemptStatus.READY);
            assertThat(reload(bill).getStatus()).isEqualTo(PaymentBillStatus.UNPAID);
        }

        @Test
        @DisplayName("토스 결제가 ABORTED(승인 실패)면 attempt는 FAILED가 되고 bill은 UNPAID 그대로다.")
        void aborted() {
            // given
            PaymentBill bill = issueBill();
            PaymentAttempt attempt = inProgressAttempt();

            // when
            paymentAttemptService.reconcile(tossPayment(attempt.getOrderId(), "ABORTED", "카드"));
            em.flush();
            em.clear();

            // then
            PaymentAttempt reloadedAttempt = reload(attempt);
            assertThat(reloadedAttempt.getStatus()).isEqualTo(PaymentAttemptStatus.FAILED);
            assertThat(reloadedAttempt.getFailureCode()).isNotNull();
            assertThat(reload(bill).getStatus()).isEqualTo(PaymentBillStatus.UNPAID);
        }

        @Test
        @DisplayName("토스 결제가 EXPIRED(유효 시간 만료)면 attempt는 FAILED가 된다.")
        void expired() {
            // given
            PaymentBill bill = issueBill();
            PaymentAttempt attempt = inProgressAttempt();

            // when
            paymentAttemptService.reconcile(tossPayment(attempt.getOrderId(), "EXPIRED", "카드"));
            em.flush();
            em.clear();

            // then
            PaymentAttempt reloadedAttempt = reload(attempt);
            assertThat(reloadedAttempt.getStatus()).isEqualTo(PaymentAttemptStatus.FAILED);
            assertThat(reloadedAttempt.getFailureCode()).isNotNull();
            assertThat(reload(bill).getStatus()).isEqualTo(PaymentBillStatus.UNPAID);
        }

        @Test
        @DisplayName("토스 응답에 실패 정보(failure)가 있으면 그 코드와 메시지로 FAILED를 기록한다.")
        void abortedWithFailureInfo() {
            // given
            issueBill();
            PaymentAttempt attempt = inProgressAttempt();
            TossDto.ConfirmResponse payment = new TossDto.ConfirmResponse(
                    PAYMENT_KEY, attempt.getOrderId(), "ABORTED", "카드", null, null, null,
                    new TossDto.ConfirmResponse.Failure("REJECT_CARD_COMPANY", "카드사에서 거절했습니다."));

            // when
            paymentAttemptService.reconcile(payment);
            em.flush();
            em.clear();

            // then
            PaymentAttempt reloadedAttempt = reload(attempt);
            assertThat(reloadedAttempt.getFailureCode()).isEqualTo("REJECT_CARD_COMPANY");
            assertThat(reloadedAttempt.getFailureMessage()).isEqualTo("카드사에서 거절했습니다.");
        }

        @Test
        @DisplayName("토스 응답에 실패 정보가 없으면(만료 등) 토스 상태로 만든 기본 코드를 기록한다.")
        void expiredWithoutFailureInfo() {
            // given
            issueBill();
            PaymentAttempt attempt = inProgressAttempt();

            // when
            paymentAttemptService.reconcile(tossPayment(attempt.getOrderId(), "EXPIRED", "카드"));
            em.flush();
            em.clear();

            // then
            assertThat(reload(attempt).getFailureCode()).isEqualTo("TOSS_EXPIRED");
        }

        @Test
        @DisplayName("반영 결과로 바뀐 attempt 상태를 돌려준다.")
        void returnsResultStatus() {
            // given
            issueBill();
            PaymentAttempt attempt = inProgressAttempt();

            // when
            PaymentAttemptStatus result = paymentAttemptService.reconcile(tossPayment(attempt.getOrderId(), "DONE", "카드"));

            // then
            assertThat(result).isEqualTo(PaymentAttemptStatus.DONE);
        }

        @Test
        @DisplayName("토스 결제가 아직 IN_PROGRESS면 확정 전이므로 아무것도 바꾸지 않는다.")
        void tossStillInProgress() {
            // given
            PaymentBill bill = issueBill();
            PaymentAttempt attempt = inProgressAttempt();

            // when
            paymentAttemptService.reconcile(tossPayment(attempt.getOrderId(), "IN_PROGRESS", "카드"));
            em.flush();
            em.clear();

            // then
            PaymentAttempt reloadedAttempt = reload(attempt);
            assertThat(reloadedAttempt.getStatus()).isEqualTo(PaymentAttemptStatus.IN_PROGRESS);
            assertThat(reloadedAttempt.getPaymentKey()).isNull();
            assertThat(reload(bill).getStatus()).isEqualTo(PaymentBillStatus.UNPAID);
        }

        @Test
        @DisplayName("토스 결제가 WAITING_FOR_DEPOSIT이면 attempt는 입금 대기가 되고 가상계좌 정보가 저장된다.")
        void waitingForDeposit() {
            // given
            PaymentBill bill = issueBill();
            PaymentAttempt attempt = inProgressAttempt();
            OffsetDateTime dueDate = OffsetDateTime.of(2026, 11, 9, 23, 59, 59, 0, ZoneOffset.ofHours(9));
            TossDto.ConfirmResponse payment = new TossDto.ConfirmResponse(
                    PAYMENT_KEY, attempt.getOrderId(), "WAITING_FOR_DEPOSIT", "가상계좌", null, "ps_test_secret",
                    new TossDto.ConfirmResponse.VirtualAccount("12345678901234", "20", dueDate), null);

            // when
            paymentAttemptService.reconcile(payment);
            em.flush();
            em.clear();

            // then: attempt
            PaymentAttempt reloadedAttempt = reload(attempt);
            assertThat(reloadedAttempt.getStatus()).isEqualTo(PaymentAttemptStatus.WAITING_FOR_DEPOSIT);
            assertThat(reloadedAttempt.getPaymentKey()).isEqualTo(PAYMENT_KEY);
            assertThat(reloadedAttempt.getMethod()).isEqualTo(PaymentMethod.VIRTUAL_ACCOUNT);

            // then: 계좌 정보
            VirtualAccount virtualAccount = virtualAccountRepository.findByPaymentAttemptId(attempt.getId()).orElseThrow();
            assertThat(virtualAccount.getBankCode()).isEqualTo("20");
            assertThat(virtualAccount.getAccountNumber()).isEqualTo("12345678901234");
            assertThat(virtualAccount.getWebhookSecret()).isEqualTo("ps_test_secret");
            assertThat(virtualAccount.getDueAt()).isEqualTo(LocalDateTime.of(2026, 11, 9, 23, 59, 59));

            // then: 입금 전이므로 bill은 그대로
            assertThat(reload(bill).getStatus()).isEqualTo(PaymentBillStatus.UNPAID);
        }

        @Test
        @DisplayName("이미 DONE으로 반영된 결제에 같은 DONE 결과가 다시 오면 예외 없이 무시한다.")
        void alreadyDone() {
            // given: 다른 쪽(승인 스레드 또는 스케줄러)이 먼저 같은 결과로 DONE을 반영함
            PaymentBill bill = issueBill();
            PaymentAttempt attempt = inProgressAttempt();
            paymentAttemptService.reconcile(tossPayment(attempt.getOrderId(), "DONE", "카드"));
            em.flush();
            em.clear();

            // when & then: bill.markPaid가 두 번 불리면 PAID → PAID 전이로 예외가 난다
            assertThatCode(() -> paymentAttemptService.reconcile(tossPayment(attempt.getOrderId(), "DONE", "카드")))
                    .doesNotThrowAnyException();
            em.flush();
            em.clear();

            // then: 그대로
            assertThat(reload(attempt).getStatus()).isEqualTo(PaymentAttemptStatus.DONE);
            PaymentBill reloadedBill = reload(bill);
            assertThat(reloadedBill.getStatus()).isEqualTo(PaymentBillStatus.PAID);
            assertThat(reloadedBill.getPaidAt()).isEqualTo(approvedAt);
        }

        @Test
        @DisplayName("이미 FAILED로 반영된 결제에 토스 DONE이 오면 예외 없이 상태를 바꾸지 않는다. (수동 확인 대상)")
        void alreadyFailedButTossDone() {
            // given: 거절로 FAILED 처리된 attempt
            PaymentBill bill = issueBill();
            PaymentAttempt attempt = inProgressAttempt();
            paymentAttemptService.failConfirm(attempt.getOrderId(), "REJECT_CARD_COMPANY", "카드사 거절");
            em.flush();
            em.clear();

            // when & then
            assertThatCode(() -> paymentAttemptService.reconcile(tossPayment(attempt.getOrderId(), "DONE", "카드")))
                    .doesNotThrowAnyException();
            em.flush();
            em.clear();

            // then: 자동으로 되돌리지 않는다
            assertThat(reload(attempt).getStatus()).isEqualTo(PaymentAttemptStatus.FAILED);
            assertThat(reload(bill).getStatus()).isEqualTo(PaymentBillStatus.UNPAID);
        }

        @Test
        @DisplayName("없는 orderId면 예외가 발생한다.")
        void orderNotFound() {
            assertThatThrownBy(() -> paymentAttemptService.reconcile(tossPayment("not-exist-order-id", "DONE", "카드")))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("payByCash")
    class PayByCash {
        @Test
        @DisplayName("현금 결제하면 결제 시도는 DONE(CASH), 청구서는 PAID가 된다.")
        void payByCash_marksDoneAndPaid() {
            PaymentBill bill = issueBill();

            paymentAttemptService.payByCash(bill.getId(), 1L);
            em.flush();
            em.clear();

            PaymentAttempt attempt = paymentAttemptRepository.findAllByPaymentBillId(bill.getId()).getFirst();
            assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.DONE);
            assertThat(attempt.getMethod()).isEqualTo(PaymentMethod.CASH);
            assertThat(paymentBillRepository.findById(bill.getId()).orElseThrow().getStatus())
                    .isEqualTo(PaymentBillStatus.PAID);
        }

        @Test
        @DisplayName("고객이 열어둔 READY 시도가 있으면 새로 만들지 않고 재사용한다.")
        void payByCash_reusesReadyAttempt() {
            PaymentBill bill = issueBill();
            PaymentAttempt ready = startAndFindAttempt(asReceptionId);

            paymentAttemptService.payByCash(bill.getId(), 1L);
            em.flush();
            em.clear();

            assertThat(paymentAttemptRepository.findAllByPaymentBillId(bill.getId()))
                    .singleElement()
                    .satisfies(a -> {
                        assertThat(a.getId()).isEqualTo(ready.getId());
                        assertThat(a.getStatus()).isEqualTo(PaymentAttemptStatus.DONE);
                    });
        }

        @Test
        @DisplayName("가상계좌 입금 대기 중이면 현금 결제할 수 없다.")
        void payByCash_rejectsWaitingForDeposit() {
            PaymentBill bill = issueBill();
            PaymentAttempt attempt = startAndFindAttempt(asReceptionId);
            attempt.markInProgress();
            attempt.markWaitingForDeposit("payment-key");

            assertThatThrownBy(() -> paymentAttemptService.payByCash(bill.getId(), 1L))
                    .isInstanceOf(PaymentException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_IN_PROGRESS);
            assertThat(bill.getStatus()).isEqualTo(PaymentBillStatus.UNPAID);
        }

        @Test
        @DisplayName("이미 결제된 청구서는 현금 결제할 수 없다.")
        void payByCash_rejectsPaidBill() {
            PaymentBill bill = issueBill();
            paymentAttemptService.payByCash(bill.getId(), 1L);

            assertThatThrownBy(() -> paymentAttemptService.payByCash(bill.getId(), 1L))
                    .isInstanceOf(PaymentException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.BILL_NOT_PAYABLE);
        }
    }
}
