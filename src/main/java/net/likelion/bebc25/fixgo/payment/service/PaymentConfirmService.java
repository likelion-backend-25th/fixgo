package net.likelion.bebc25.fixgo.payment.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.payment.client.TossDto;
import net.likelion.bebc25.fixgo.payment.client.TossPaymentsClient;
import net.likelion.bebc25.fixgo.payment.exception.PaymentException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

@Service
@RequiredArgsConstructor
public class PaymentConfirmService {
    private final PaymentAttemptService paymentAttemptService;

    private final TossPaymentsClient tossPaymentsClient;

    public void confirmPayment(String paymentKey, String orderId, Long amount) {
        try {
            paymentAttemptService.prepareConfirm(orderId, amount);
        } catch (PaymentException e) {
            // 금액 불일치, 청구서 결제 불가처럼 기록해야하는 실패만 FAILED로 남긴다.
            // prepareConfirm은 롤백됐으므로 별도 트랜잭션인 failConfirm으로 기록한다.
            if (e.getErrorCode().shouldRecordAsFailure()) {
                paymentAttemptService.failConfirm(orderId, e.getErrorCode().name(), e.getMessage());
            }
            // 검증에 실패했으므로 토스 승인으로 넘어가지 않고 멈춘다.
            throw e;
        }

        TossDto.ConfirmResponse response;
        try {
            response = tossPaymentsClient.confirm(paymentKey, orderId, amount);
        } catch (RestClientResponseException e) {
            // 토스가 명확히 거절했다(한도 초과 등). 결제되지 않았으므로 FAILED로 기록한다.
            TossDto.ErrorResponse error = e.getResponseBodyAs(TossDto.ErrorResponse.class);
            String code = (error != null) ? error.code() : "TOSS_HTTP_" + e.getStatusCode().value();
            String message = (error != null) ? error.message() : e.getMessage();
            paymentAttemptService.failConfirm(orderId, code, message);
            throw e;
        } catch (ResourceAccessException e) {
            // 응답을 받지 못했다(타임아웃, 연결 실패). 승인됐는지 알 수 없으므로 FAILED로 바꾸지 않고 IN_PROGRESS로 둔다.
            // TODO: 토스 결제 조회 API로 실제 상태를 확인해 정리
            throw e;
        }

        // 카드는 DONE, 가상계좌는 WAITING_FOR_DEPOSIT으로 온다. 그 외 상태는 attempt가 IN_PROGRESS로 남으므로 결제 조회로 정리가 필요하다(TODO)
        switch (response.status()) {
            case "DONE" ->
                    paymentAttemptService.completeConfirm(response.orderId(), response.paymentKey(), response.approvedAt().toLocalDateTime());
            case "WAITING_FOR_DEPOSIT" ->
                    paymentAttemptService.issueVirtualAccount(response.orderId(), response.paymentKey(), response.virtualAccount().bankCode(),
                                                              response.virtualAccount().accountNumber(), response.secret(),
                                                              response.virtualAccount().dueDate().toLocalDateTime());
            default -> throw new IllegalStateException("예상하지 못한 결제 상태: " + response.status());
        }
    }
}
