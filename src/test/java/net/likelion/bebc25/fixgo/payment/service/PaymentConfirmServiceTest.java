package net.likelion.bebc25.fixgo.payment.service;

import net.likelion.bebc25.fixgo.payment.client.TossDto;
import net.likelion.bebc25.fixgo.payment.client.TossPaymentsClient;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.payment.entity.*;
import net.likelion.bebc25.fixgo.payment.exception.PaymentErrorCode;
import net.likelion.bebc25.fixgo.payment.exception.PaymentException;
import net.likelion.bebc25.fixgo.payment.repository.PaymentAttemptRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DataJpaTest
@Import({PaymentConfirmService.class, PaymentAttemptService.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED) // 테스트 전체를 감싸는 트랜잭션을 끈다 
class PaymentConfirmServiceTest {
    private static final Long AS_RECEPTION_ID = 1L;
    private static final String PAYMENT_KEY = "test_payment_key";
    private static final OffsetDateTime APPROVED_AT =
            OffsetDateTime.of(2026, 11, 1, 15, 30, 0, 0, ZoneOffset.ofHours(9));
    @Autowired
    PaymentConfirmService paymentConfirmService;
    @Autowired
    PaymentAttemptService paymentAttemptService;
    @Autowired
    PaymentAttemptRepository paymentAttemptRepository;
    @Autowired
    PaymentBillRepository paymentBillRepository;
    @MockitoBean
    TossPaymentsClient tossPaymentsClient; // 진짜 토스 대신 가짜

    // 테스트 트랜잭션이 없어서 데이터가 실제로 커밋된다. 테스트끼리 섞이지 않게 매번 지운다. (FK 때문에 attempt 먼저)
    @AfterEach
    void cleanUp() {
        paymentAttemptRepository.deleteAll();
        paymentBillRepository.deleteAll();
    }

    private PaymentBill issueBill() {
        return paymentBillRepository.save(PaymentBill.issue(AS_RECEPTION_ID, 30_000L, PaymentBillType.REPAIR));
    }

    private PaymentAttempt findAttempt(String orderId) {
        return paymentAttemptRepository.findByOrderId(orderId).orElseThrow();
    }

    @Test
    @DisplayName("토스 승인이 성공하면 attempt는 DONE, bill은 PAID가 되고 승인 정보가 저장된다.")
    void success() {
        // given
        PaymentBill bill = issueBill();
        PaymentDto.StartPaymentResponse started = paymentAttemptService.startPayment(AS_RECEPTION_ID);
        given(tossPaymentsClient.confirm(PAYMENT_KEY, started.orderId(), started.amount()))
                .willReturn(new TossDto.ConfirmResponse(PAYMENT_KEY, started.orderId(), "DONE", APPROVED_AT, null, null));

        // when
        paymentConfirmService.confirmPayment(PAYMENT_KEY, started.orderId(), started.amount());

        // then: attempt
        PaymentAttempt attempt = findAttempt(started.orderId());
        assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.DONE);
        assertThat(attempt.getPaymentKey()).isEqualTo(PAYMENT_KEY);
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
        PaymentDto.StartPaymentResponse started = paymentAttemptService.startPayment(AS_RECEPTION_ID);

        // when & then
        assertThatThrownBy(() -> paymentConfirmService.confirmPayment(PAYMENT_KEY, started.orderId(), 1_000L))
                .isInstanceOf(PaymentException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.AMOUNT_MISMATCH);

        // then: prepareConfirm은 롤백됐지만 failConfirm의 기록은 남아 있다
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
        PaymentDto.StartPaymentResponse started = paymentAttemptService.startPayment(AS_RECEPTION_ID);
        paymentAttemptService.prepareConfirm(started.orderId(), started.amount());

        // when & then: 두 번째 요청
        assertThatThrownBy(() -> paymentConfirmService.confirmPayment(PAYMENT_KEY, started.orderId(), started.amount()))
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
        PaymentDto.StartPaymentResponse started = paymentAttemptService.startPayment(AS_RECEPTION_ID);

        HttpClientErrorException rejected = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8);
        rejected.setBodyConvertFunction(type ->
                                                new TossDto.ErrorResponse("REJECT_CARD_PAYMENT", "한도초과 혹은 잔액부족으로 결제에 실패했습니다."));
        given(tossPaymentsClient.confirm(any(), any(), any())).willThrow(rejected);

        // when & then
        assertThatThrownBy(() -> paymentConfirmService.confirmPayment(PAYMENT_KEY, started.orderId(), started.amount()))
                .isInstanceOf(RestClientResponseException.class);

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
        PaymentDto.StartPaymentResponse started = paymentAttemptService.startPayment(AS_RECEPTION_ID);
        given(tossPaymentsClient.confirm(any(), any(), any()))
                .willThrow(new ResourceAccessException("Read timed out"));

        // when & then
        assertThatThrownBy(() -> paymentConfirmService.confirmPayment(PAYMENT_KEY, started.orderId(), started.amount()))
                .isInstanceOf(ResourceAccessException.class);

        // then
        assertThat(findAttempt(started.orderId()).getStatus()).isEqualTo(PaymentAttemptStatus.IN_PROGRESS);
        assertThat(paymentBillRepository.findById(bill.getId()).orElseThrow().getStatus())
                .isEqualTo(PaymentBillStatus.UNPAID);
    }
}