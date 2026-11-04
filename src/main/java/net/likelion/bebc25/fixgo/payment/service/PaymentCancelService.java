package net.likelion.bebc25.fixgo.payment.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.payment.client.TossDto;
import net.likelion.bebc25.fixgo.payment.client.TossPaymentsClient;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttempt;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttemptStatus;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBill;
import net.likelion.bebc25.fixgo.payment.repository.PaymentAttemptRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

// TODO: 예외처리 보완 필요
@Service
@RequiredArgsConstructor
public class PaymentCancelService {
    private final TossPaymentsClient tossPaymentsClient;
    private final PaymentAttemptService paymentAttemptService;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final PaymentBillRepository paymentBillRepository;

    public void cancelVirtualAccountBeforeDeposit(Long asReceptionId) {
        PaymentBill bill = paymentBillRepository.findByAsReceptionId(asReceptionId)
                .orElseThrow(() -> new IllegalStateException("Payment Bill Not Found"));
        PaymentAttempt attempt = paymentAttemptRepository.findByPaymentBillIdAndStatus(bill.getId(), PaymentAttemptStatus.WAITING_FOR_DEPOSIT)
                .orElseThrow(() -> new IllegalStateException("Payment attempt not found"));
        TossDto.CancelResponse response;

        try {
            response = tossPaymentsClient.cancel(attempt.getPaymentKey(), "가상계좌 입금 전 취소", null);
        } catch (RestClientResponseException e) {
            // 토스가 명확히 거절했다. 취소 되지 않았으므로 attempt 상태를 그대로 유지한다.
            TossDto.ErrorResponse error = e.getResponseBodyAs(TossDto.ErrorResponse.class);
            String code = (error != null) ? error.code() : "TOSS_HTTP_" + e.getStatusCode().value();
            String message = (error != null) ? error.message() : e.getMessage();
            throw e;
        } catch (ResourceAccessException e) {
            // 응답을 받지 못했다(타임아웃, 연결 실패).
            // TODO: 토스 결제 조회 API로 실제 상태를 확인해 정리
            throw e;
        }

        // 취소 반영
        paymentAttemptService.cancelVirtualAccount(attempt.getOrderId());
    }

}
