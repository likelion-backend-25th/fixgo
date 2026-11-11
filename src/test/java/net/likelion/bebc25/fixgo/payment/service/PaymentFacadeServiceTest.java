package net.likelion.bebc25.fixgo.payment.service;

import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.payment.client.TossDto;
import net.likelion.bebc25.fixgo.payment.client.TossPaymentsClient;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.payment.entity.*;
import net.likelion.bebc25.fixgo.payment.exception.PaymentErrorCode;
import net.likelion.bebc25.fixgo.payment.exception.PaymentException;
import net.likelion.bebc25.fixgo.payment.repository.PaymentAttemptRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import net.likelion.bebc25.fixgo.payment.repository.VirtualAccountRepository;
import net.likelion.bebc25.fixgo.support.AsReceptionFixture;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@DataJpaTest
@Import({JpaConfig.class, AsReceptionFixture.class, PaymentFacadeService.class, PaymentAttemptService.class, PaymentRefundService.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED) // 테스트 전체를 감싸는 트랜잭션을 끈다 
class PaymentFacadeServiceTest {
    private static final String PAYMENT_KEY = "test_payment_key";
    private static final OffsetDateTime APPROVED_AT =
            OffsetDateTime.of(2026, 11, 1, 15, 30, 0, 0, ZoneOffset.ofHours(9));
    @Autowired
    AsReceptionFixture asReceptionFixture;
    @Autowired
    PaymentFacadeService paymentFacadeService;
    @Autowired
    PaymentAttemptService paymentAttemptService;
    @Autowired
    PaymentAttemptRepository paymentAttemptRepository;
    @Autowired
    PaymentBillRepository paymentBillRepository;
    @Autowired
    VirtualAccountRepository virtualAccountRepository;
    @MockitoBean
    TossPaymentsClient tossPaymentsClient; // 진짜 토스 대신 가짜
    private AsReception reception;
    private Long asReceptionId;

    @BeforeEach
    void setUpReception() {
        reception = asReceptionFixture.save();
        asReceptionId = reception.getId();
    }

    // 테스트 트랜잭션이 없어서 데이터가 실제로 커밋된다. 테스트끼리 섞이지 않게 매번 지운다. (FK 때문에 가상계좌, attempt 순서로)
    @AfterEach
    void cleanUp() {
        virtualAccountRepository.deleteAll();
        paymentAttemptRepository.deleteAll();
        paymentBillRepository.deleteAll();
        asReceptionFixture.deleteAll();
    }

    private PaymentBill issueBill() {
        return paymentBillRepository.save(PaymentBill.create(reception, 30_000L, PaymentBillType.REPAIR));
    }

    private PaymentAttempt findAttempt(String orderId) {
        return paymentAttemptRepository.findByOrderId(orderId).orElseThrow();
    }

    @Test
    @DisplayName("토스 승인이 성공하면 attempt는 DONE, bill은 PAID가 되고 승인 정보가 저장된다.")
    void success() {
        // given
        PaymentBill bill = issueBill();
        PaymentDto.StartPaymentResponse started = paymentAttemptService.startPayment(asReceptionId);
        given(tossPaymentsClient.confirm(PAYMENT_KEY, started.orderId(), started.amount()))
                .willReturn(new TossDto.ConfirmResponse(PAYMENT_KEY, started.orderId(), "DONE", "카드", APPROVED_AT, null, null, null));

        // when
        paymentFacadeService.confirmPayment(PAYMENT_KEY, started.orderId(), started.amount());

        // then: attempt
        PaymentAttempt attempt = findAttempt(started.orderId());
        assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.DONE);
        assertThat(attempt.getPaymentKey()).isEqualTo(PAYMENT_KEY);
        assertThat(attempt.getMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(attempt.getApprovedAt()).isEqualTo(LocalDateTime.of(2026, 11, 1, 15, 30));

        // then: bill
        PaymentBill reloadedBill = paymentBillRepository.findById(bill.getId()).orElseThrow();
        assertThat(reloadedBill.getStatus()).isEqualTo(PaymentBillStatus.PAID);
        assertThat(reloadedBill.getPaidAt()).isEqualTo(LocalDateTime.of(2026, 11, 1, 15, 30));
    }

    @Test
    @DisplayName("금액이 조작되면 attempt는 FAILED로 기록되고 토스는 호출되지 않는다.")
    void amountMismatch() {
        // given
        PaymentBill bill = issueBill();
        PaymentDto.StartPaymentResponse started = paymentAttemptService.startPayment(asReceptionId);

        // when & then
        assertThatThrownBy(() -> paymentFacadeService.confirmPayment(PAYMENT_KEY, started.orderId(), 1_000L))
                .isInstanceOf(PaymentException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.AMOUNT_MISMATCH);

        // then: prepareConfirm이 잠금을 쥔 채 FAILED를 기록하고 커밋했다
        PaymentAttempt attempt = findAttempt(started.orderId());
        assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.FAILED);
        assertThat(attempt.getFailureCode()).isEqualTo("AMOUNT_MISMATCH");

        // then: 토스 승인은 한 번도 호출되지 않았다
        then(tossPaymentsClient).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("이미 승인 진행 중인 결제에 요청이 또 오면 거절하고, 진행 중인 attempt는 건드리지 않는다.")
    void alreadyInProgress() {
        // given: 첫 번째 요청이 승인 준비까지 마친 상태
        PaymentBill bill = issueBill();
        PaymentDto.StartPaymentResponse started = paymentAttemptService.startPayment(asReceptionId);
        paymentAttemptService.prepareConfirm(started.orderId(), started.amount());

        // when & then: 두 번째 요청
        assertThatThrownBy(() -> paymentFacadeService.confirmPayment(PAYMENT_KEY, started.orderId(), started.amount()))
                .isInstanceOf(PaymentException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.ATTEMPT_NOT_READY);

        // then: FAILED로 바뀌지 않고 IN_PROGRESS 그대로
        assertThat(findAttempt(started.orderId()).getStatus()).isEqualTo(PaymentAttemptStatus.IN_PROGRESS);
        then(tossPaymentsClient).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("토스가 승인을 거절하면 attempt는 토스 에러 코드와 함께 FAILED가 되고 bill은 UNPAID로 남는다.")
    void tossRejected() {
        // given
        PaymentBill bill = issueBill();
        PaymentDto.StartPaymentResponse started = paymentAttemptService.startPayment(asReceptionId);

        HttpClientErrorException rejected = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8);
        rejected.setBodyConvertFunction(type ->
                                                new TossDto.ErrorResponse("REJECT_CARD_PAYMENT", "한도초과 혹은 잔액부족으로 결제에 실패했습니다."));
        given(tossPaymentsClient.confirm(any(), any(), any())).willThrow(rejected);

        // when & then
        assertThatThrownBy(() -> paymentFacadeService.confirmPayment(PAYMENT_KEY, started.orderId(), started.amount()))
                .isInstanceOf(PaymentException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.TOSS_REJECTED);

        // then
        PaymentAttempt attempt = findAttempt(started.orderId());
        assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.FAILED);
        assertThat(attempt.getFailureCode()).isEqualTo("REJECT_CARD_PAYMENT");
        assertThat(paymentBillRepository.findById(bill.getId()).orElseThrow().getStatus())
                .isEqualTo(PaymentBillStatus.UNPAID);
    }

    @Test
    @DisplayName("토스 응답을 받지 못하면 승인 여부를 알 수 없으므로 attempt를 IN_PROGRESS로 남긴다.")
    void tossTimeout() {
        // given
        PaymentBill bill = issueBill();
        PaymentDto.StartPaymentResponse started = paymentAttemptService.startPayment(asReceptionId);
        given(tossPaymentsClient.confirm(any(), any(), any()))
                .willThrow(new ResourceAccessException("Read timed out"));

        // when & then: 사용자에게는 실패가 아니라 "확인 중"으로 응답한다
        assertThatThrownBy(() -> paymentFacadeService.confirmPayment(PAYMENT_KEY, started.orderId(), started.amount()))
                .isInstanceOf(PaymentException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_PENDING);

        // then
        assertThat(findAttempt(started.orderId()).getStatus()).isEqualTo(PaymentAttemptStatus.IN_PROGRESS);
        assertThat(paymentBillRepository.findById(bill.getId()).orElseThrow().getStatus())
                .isEqualTo(PaymentBillStatus.UNPAID);
    }

    @Test
    @DisplayName("토스 서버 오류(5xx)는 승인 여부를 알 수 없으므로 FAILED로 바꾸지 않고 IN_PROGRESS로 남긴다.")
    void tossServerError() {
        // given
        PaymentBill bill = issueBill();
        PaymentDto.StartPaymentResponse started = paymentAttemptService.startPayment(asReceptionId);
        given(tossPaymentsClient.confirm(any(), any(), any()))
                .willThrow(HttpServerErrorException.create(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8));

        // when & then
        assertThatThrownBy(() -> paymentFacadeService.confirmPayment(PAYMENT_KEY, started.orderId(), started.amount()))
                .isInstanceOf(PaymentException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_PENDING);

        // then
        assertThat(findAttempt(started.orderId()).getStatus()).isEqualTo(PaymentAttemptStatus.IN_PROGRESS);
        assertThat(paymentBillRepository.findById(bill.getId()).orElseThrow().getStatus())
                .isEqualTo(PaymentBillStatus.UNPAID);
    }

    @Test
    @DisplayName("토스 승인 응답이 확정 상태가 아니면(IN_PROGRESS) 성공으로 응답하지 않고 '확인 중'으로 응답한다.")
    void tossResponseNotFinal() {
        // given
        issueBill();
        PaymentDto.StartPaymentResponse started = paymentAttemptService.startPayment(asReceptionId);
        given(tossPaymentsClient.confirm(any(), any(), any()))
                .willReturn(tossPayment(started.orderId(), "IN_PROGRESS"));

        // when & then
        assertThatThrownBy(() -> paymentFacadeService.confirmPayment(PAYMENT_KEY, started.orderId(), started.amount()))
                .isInstanceOf(PaymentException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_PENDING);

        assertThat(findAttempt(started.orderId()).getStatus()).isEqualTo(PaymentAttemptStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("승인 응답을 반영하기 전에 스케줄러가 먼저 DONE으로 반영했어도 예외 없이 성공한다.")
    void alreadyReconciledByScheduler() {
        // given: 토스 승인 응답이 도착하기 직전에 스케줄러가 같은 결제를 먼저 반영한 상황
        PaymentBill bill = issueBill();
        PaymentDto.StartPaymentResponse started = paymentAttemptService.startPayment(asReceptionId);
        TossDto.ConfirmResponse done = tossPayment(started.orderId(), "DONE");
        given(tossPaymentsClient.confirm(any(), any(), any())).willAnswer(invocation -> {
            paymentAttemptService.reconcile(done); // 스케줄러가 먼저 반영
            return done;
        });

        // when
        paymentFacadeService.confirmPayment(PAYMENT_KEY, started.orderId(), started.amount());

        // then
        assertThat(findAttempt(started.orderId()).getStatus()).isEqualTo(PaymentAttemptStatus.DONE);
        assertThat(paymentBillRepository.findById(bill.getId()).orElseThrow().getStatus())
                .isEqualTo(PaymentBillStatus.PAID);
    }

    private TossDto.ConfirmResponse tossPayment(String orderId, String status) {
        return new TossDto.ConfirmResponse(PAYMENT_KEY, orderId, status, "카드", APPROVED_AT, null, null, null);
    }

    /**
     * 스케줄러가 호출하는 대사 로직.
     * cutoff보다 먼저 IN_PROGRESS가 된 attempt만 대상이다. 테스트에서는 방금 만든 attempt도 대상이 되도록 cutoff를 1분 뒤로 준다.
     */
    @Nested
    @DisplayName("reconcileStuckAttempts")
    class ReconcileStuckAttempts {
        private final LocalDateTime cutoffIncludingNow = LocalDateTime.now().plusMinutes(1);

        /**
         * 토스 응답을 받지 못해 IN_PROGRESS로 남은 attempt를 만들고 orderId를 돌려준다.
         */
        private String stuckOrderId(AsReception target) {
            paymentBillRepository.save(PaymentBill.create(target, 30_000L, PaymentBillType.REPAIR));
            PaymentDto.StartPaymentResponse started = paymentAttemptService.startPayment(target.getId());
            paymentAttemptService.prepareConfirm(started.orderId(), started.amount());
            return started.orderId();
        }

        private HttpClientErrorException notFound() {
            return HttpClientErrorException.create(
                    HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8);
        }

        @Test
        @DisplayName("토스 조회 결과가 DONE이면 attempt는 DONE, bill은 PAID가 된다.")
        void done() {
            // given
            String orderId = stuckOrderId(reception);
            given(tossPaymentsClient.getByOrderId(orderId)).willReturn(tossPayment(orderId, "DONE"));

            // when
            paymentFacadeService.reconcileStuckAttempts(cutoffIncludingNow);

            // then
            PaymentAttempt attempt = findAttempt(orderId);
            assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.DONE);
            assertThat(attempt.getPaymentKey()).isEqualTo(PAYMENT_KEY);
            assertThat(paymentBillRepository.findById(attempt.getPaymentBill().getId()).orElseThrow().getStatus())
                    .isEqualTo(PaymentBillStatus.PAID);
        }

        @Test
        @DisplayName("토스 조회 결과가 ABORTED면 attempt는 FAILED가 된다.")
        void aborted() {
            // given
            String orderId = stuckOrderId(reception);
            given(tossPaymentsClient.getByOrderId(orderId)).willReturn(tossPayment(orderId, "ABORTED"));

            // when
            paymentFacadeService.reconcileStuckAttempts(cutoffIncludingNow);

            // then
            assertThat(findAttempt(orderId).getStatus()).isEqualTo(PaymentAttemptStatus.FAILED);
        }

        @Test
        @DisplayName("토스에서도 아직 IN_PROGRESS면 그대로 둔다.")
        void tossStillInProgress() {
            // given
            String orderId = stuckOrderId(reception);
            given(tossPaymentsClient.getByOrderId(orderId)).willReturn(tossPayment(orderId, "IN_PROGRESS"));

            // when
            paymentFacadeService.reconcileStuckAttempts(cutoffIncludingNow);

            // then
            assertThat(findAttempt(orderId).getStatus()).isEqualTo(PaymentAttemptStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("토스 조회가 타임아웃이면 예외를 밖으로 던지지 않고 IN_PROGRESS로 둔다. (다음 주기에 다시 시도)")
        void lookupTimeout() {
            // given
            String orderId = stuckOrderId(reception);
            given(tossPaymentsClient.getByOrderId(orderId)).willThrow(new ResourceAccessException("Read timed out"));

            // when & then
            assertThatCode(() -> paymentFacadeService.reconcileStuckAttempts(cutoffIncludingNow))
                    .doesNotThrowAnyException();
            assertThat(findAttempt(orderId).getStatus()).isEqualTo(PaymentAttemptStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("한 건의 조회가 실패해도 나머지 건은 정상 처리된다.")
        void oneFailureDoesNotStopOthers() {
            // given: 멈춘 결제 2건. 첫 건은 조회 실패, 둘째 건은 DONE
            String failingOrderId = stuckOrderId(reception);
            String okOrderId = stuckOrderId(asReceptionFixture.save());
            given(tossPaymentsClient.getByOrderId(failingOrderId)).willThrow(new ResourceAccessException("Read timed out"));
            given(tossPaymentsClient.getByOrderId(okOrderId)).willReturn(tossPayment(okOrderId, "DONE"));

            // when
            paymentFacadeService.reconcileStuckAttempts(cutoffIncludingNow);

            // then
            assertThat(findAttempt(failingOrderId).getStatus()).isEqualTo(PaymentAttemptStatus.IN_PROGRESS);
            assertThat(findAttempt(okOrderId).getStatus()).isEqualTo(PaymentAttemptStatus.DONE);
        }

        @Test
        @DisplayName("cutoff 이후에 IN_PROGRESS가 된 결제(아직 승인 처리 중일 수 있음)는 조회하지 않는다.")
        void recentAttemptIsSkipped() {
            // given: 방금 IN_PROGRESS가 됨. 스케줄러처럼 cutoff를 2분 전으로 준다
            String orderId = stuckOrderId(reception);

            // when
            paymentFacadeService.reconcileStuckAttempts(LocalDateTime.now().minusMinutes(2));

            // then
            then(tossPaymentsClient).should(never()).getByOrderId(any());
            assertThat(findAttempt(orderId).getStatus()).isEqualTo(PaymentAttemptStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("IN_PROGRESS가 아닌 결제(READY)는 조회하지 않는다.")
        void notInProgressIsSkipped() {
            // given: 결제 시작만 하고 승인 요청 전인 attempt
            issueBill();
            paymentAttemptService.startPayment(asReceptionId);

            // when
            paymentFacadeService.reconcileStuckAttempts(cutoffIncludingNow);

            // then
            then(tossPaymentsClient).should(never()).getByOrderId(any());
        }
    }

    @Nested
    @DisplayName("cancelVirtualAccountBeforeDeposit")
    class CancelVirtualAccountBeforeDeposit {
        private static final OffsetDateTime DUE_AT =
                OffsetDateTime.of(2026, 11, 10, 23, 59, 59, 0, ZoneOffset.ofHours(9));

        /**
         * 실제 흐름으로 가상계좌 발급(입금 대기)까지 진행하고 orderId를 돌려준다.
         */
        private String issueVirtualAccount() {
            issueBill();
            PaymentDto.StartPaymentResponse started = paymentAttemptService.startPayment(asReceptionId);
            paymentAttemptService.prepareConfirm(started.orderId(), started.amount());
            paymentAttemptService.reconcile(new TossDto.ConfirmResponse(
                    PAYMENT_KEY, started.orderId(), "WAITING_FOR_DEPOSIT", "가상계좌", null, "ps_test_secret",
                    new TossDto.ConfirmResponse.VirtualAccount("12345678901234", "20", DUE_AT), null));
            return started.orderId();
        }

        /**
         * 토스가 에러 코드와 함께 4xx로 거절한 응답
         */
        private HttpClientErrorException tossClientError(String code, String message) {
            HttpClientErrorException e = HttpClientErrorException.create(
                    HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8);
            e.setBodyConvertFunction(type -> new TossDto.ErrorResponse(code, message));
            return e;
        }

        @Test
        @DisplayName("취소에 성공하면 attempt는 FAILED, bill은 UNPAID로 남고 가상계좌 기록은 지우지 않는다.")
        void success() {
            // given
            String orderId = issueVirtualAccount();

            // when
            paymentFacadeService.cancelVirtualAccountBeforeDeposit(asReceptionId);

            // then: 토스에 그 결제의 paymentKey로, 환불 계좌 없이 취소를 요청했다
            then(tossPaymentsClient).should().cancel(eq(PAYMENT_KEY), anyString(), isNull());

            // then: attempt
            PaymentAttempt attempt = findAttempt(orderId);
            assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.FAILED);
            assertThat(attempt.getFailureCode()).isEqualTo("VIRTUAL_ACCOUNT_CANCELED");

            // then: 돈이 오간 적 없으므로 청구서는 그대로
            assertThat(paymentBillRepository.findByAsReceptionId(asReceptionId).orElseThrow().getStatus())
                    .isEqualTo(PaymentBillStatus.UNPAID);

            // then: 가상계좌 기록은 남긴다 (웹훅 검증, 추적용)
            assertThat(virtualAccountRepository.findByPaymentAttemptId(attempt.getId())).isPresent();
        }

        @Test
        @DisplayName("취소 후에는 같은 청구서로 다시 결제를 시작할 수 있고, 새 결제 시도가 만들어진다.")
        void canStartAgainAfterCancel() {
            // given
            String canceledOrderId = issueVirtualAccount();
            paymentFacadeService.cancelVirtualAccountBeforeDeposit(asReceptionId);

            // when: 다시 결제 시작 (예: 카드로 바꿔서)
            PaymentDto.StartPaymentResponse restarted = paymentAttemptService.startPayment(asReceptionId);

            // then
            assertThat(restarted.orderId()).isNotEqualTo(canceledOrderId);
            assertThat(findAttempt(restarted.orderId()).getStatus()).isEqualTo(PaymentAttemptStatus.READY);
        }

        @Test
        @DisplayName("토스가 취소를 거절하면(4xx) TOSS_REJECTED로 응답하고 attempt는 입금 대기 그대로 둔다.")
        void tossRejected() {
            // given
            String orderId = issueVirtualAccount();
            given(tossPaymentsClient.cancel(any(), any(), any()))
                    .willThrow(tossClientError("NOT_CANCELABLE_PAYMENT", "취소 할 수 없는 결제 입니다."));

            // when & then
            assertThatThrownBy(() -> paymentFacadeService.cancelVirtualAccountBeforeDeposit(asReceptionId))
                    .isInstanceOf(PaymentException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.TOSS_REJECTED);

            assertThat(findAttempt(orderId).getStatus()).isEqualTo(PaymentAttemptStatus.WAITING_FOR_DEPOSIT);
        }

        @Test
        @DisplayName("토스에서 이미 취소된 결제(ALREADY_CANCELED_PAYMENT)면 성공으로 보고 FAILED로 반영한다.")
        void alreadyCanceledInToss() {
            // given: 이전 취소 요청이 타임아웃됐지만 토스에서는 취소된 상황
            String orderId = issueVirtualAccount();
            given(tossPaymentsClient.cancel(any(), any(), any()))
                    .willThrow(tossClientError("ALREADY_CANCELED_PAYMENT", "이미 취소된 결제 입니다."));

            // when & then
            assertThatCode(() -> paymentFacadeService.cancelVirtualAccountBeforeDeposit(asReceptionId))
                    .doesNotThrowAnyException();

            PaymentAttempt attempt = findAttempt(orderId);
            assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.FAILED);
            assertThat(attempt.getFailureCode()).isEqualTo("VIRTUAL_ACCOUNT_CANCELED");
        }

        @Test
        @DisplayName("토스 응답을 받지 못하면 취소 여부를 알 수 없으므로 PAYMENT_PENDING으로 응답하고 입금 대기 그대로 둔다.")
        void tossTimeout() {
            // given
            String orderId = issueVirtualAccount();
            given(tossPaymentsClient.cancel(any(), any(), any()))
                    .willThrow(new ResourceAccessException("Read timed out"));

            // when & then
            assertThatThrownBy(() -> paymentFacadeService.cancelVirtualAccountBeforeDeposit(asReceptionId))
                    .isInstanceOf(PaymentException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_PENDING);

            assertThat(findAttempt(orderId).getStatus()).isEqualTo(PaymentAttemptStatus.WAITING_FOR_DEPOSIT);
        }

        @Test
        @DisplayName("토스 서버 오류(5xx)는 취소 여부를 알 수 없으므로 PAYMENT_PENDING으로 응답하고 입금 대기 그대로 둔다.")
        void tossServerError() {
            // given
            String orderId = issueVirtualAccount();
            given(tossPaymentsClient.cancel(any(), any(), any()))
                    .willThrow(HttpServerErrorException.create(
                            HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8));

            // when & then
            assertThatThrownBy(() -> paymentFacadeService.cancelVirtualAccountBeforeDeposit(asReceptionId))
                    .isInstanceOf(PaymentException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_PENDING);

            assertThat(findAttempt(orderId).getStatus()).isEqualTo(PaymentAttemptStatus.WAITING_FOR_DEPOSIT);
        }
    }
}