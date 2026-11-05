package net.likelion.bebc25.fixgo.payment.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.payment.client.TossDto;
import net.likelion.bebc25.fixgo.payment.client.TossPaymentsClient;
import net.likelion.bebc25.fixgo.payment.dto.PaymentInternalDto;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttempt;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttemptStatus;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBill;
import net.likelion.bebc25.fixgo.payment.exception.PaymentException;
import net.likelion.bebc25.fixgo.payment.repository.PaymentAttemptRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

// TODO: 예외처리 보완 필요
@Service
@RequiredArgsConstructor
public class PaymentFacadeService {
    private final PaymentAttemptService paymentAttemptService;
    private final PaymentRefundService paymentRefundService;

    private final TossPaymentsClient tossPaymentsClient;

    // 삭제 예정
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final PaymentBillRepository paymentBillRepository;

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

    public void refundPayment(
            Long billId, String cancelReason, Long memberId, PaymentInternalDto.RefundAccount refundAccount
    ) {
        PaymentInternalDto.PreparedRefund prepared = paymentRefundService.prepareRefund(billId, cancelReason, memberId, refundAccount);
        TossDto.CancelRequest.RefundReceiveAccount tossAccount = TossDto.CancelRequest.RefundReceiveAccount.from(prepared.refundAccount());

        TossDto.CancelResponse response;
        try {
            response = tossPaymentsClient.cancel(prepared.paymentKey(), cancelReason, tossAccount);
        } catch (RestClientResponseException e) {
            // 토스가 명확히 거절했다(한도 초과 등). 결제되지 않았으므로 FAILED로 기록한다.
            TossDto.ErrorResponse error = e.getResponseBodyAs(TossDto.ErrorResponse.class);
            String code = (error != null) ? error.code() : "TOSS_HTTP_" + e.getStatusCode().value();
            String message = (error != null) ? error.message() : e.getMessage();
            paymentRefundService.failRefund(prepared.refundId(), code, message);
            throw e;
        } catch (ResourceAccessException e) {
            // 응답을 받지 못했다(타임아웃, 연결 실패). 승인됐는지 알 수 없으므로 FAILED로 바꾸지 않고 IN_PROGRESS로 둔다.
            // TODO: 토스 결제 조회 API로 실제 상태를 확인해 정리
            throw e;
        }

        TossDto.CancelResponse.Cancel cancel = response.cancels().getFirst();

        paymentRefundService.completeRefund(prepared.refundId(), cancel.transactionKey(), cancel.canceledAt().toLocalDateTime());
    }

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
