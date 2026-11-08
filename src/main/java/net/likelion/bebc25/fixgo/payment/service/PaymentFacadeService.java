package net.likelion.bebc25.fixgo.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.likelion.bebc25.fixgo.payment.client.TossDto;
import net.likelion.bebc25.fixgo.payment.client.TossPaymentsClient;
import net.likelion.bebc25.fixgo.payment.dto.PaymentInternalDto;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttemptStatus;
import net.likelion.bebc25.fixgo.payment.exception.PaymentErrorCode;
import net.likelion.bebc25.fixgo.payment.exception.PaymentException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentFacadeService {
    private final PaymentAttemptService paymentAttemptService;
    private final PaymentRefundService paymentRefundService;

    private final TossPaymentsClient tossPaymentsClient;

    /**
     * 온라인 결제를 승인한다.
     * <p>
     * 흐름: 승인 준비(IN_PROGRESS로 커밋) → 토스 승인 호출(트랜잭션 밖) → 응답 반영(reconcile)
     * <p>
     * 토스 호출 전에 IN_PROGRESS를 먼저 커밋하므로, 이후 어느 단계에서 실패해도 attempt는 IN_PROGRESS로 남는다.
     * IN_PROGRESS로 남은 attempt는 스케줄러가 토스 결제 조회로 실제 상태를 확인해 정리한다.
     * IN_PROGRESS인 동안에는 같은 청구서로 새 결제를 시작할 수 없으므로 이중 결제가 일어나지 않는다.
     *
     * @param paymentKey 토스 결제창 인증 후 받은 결제 키
     * @param orderId    결제 시작 시 발급한 주문 번호
     * @param amount     클라이언트가 보낸 결제 금액 (DB 금액과 비교해 조작 여부를 검증한다)
     */
    public void confirmPayment(String paymentKey, String orderId, Long amount) {
        // 1. 승인 준비: 금액, 청구서 상태를 검증하고 attempt를 IN_PROGRESS로 바꾼다.
        PaymentErrorCode failure = paymentAttemptService.prepareConfirm(orderId, amount);
        if (failure != null) {
            // prepareConfirm이 attempt를 FAILED로 변경했다. 토스 승인으로 넘어가지 않는다.
            throw new PaymentException(failure, "결제를 진행할 수 없습니다.");
        }

        // 2. 토스 승인 요청. DB 커넥션을 오래 잡지 않도록 트랜잭션 밖에서 호출한다.
        TossDto.ConfirmResponse response;
        try {
            response = tossPaymentsClient.confirm(paymentKey, orderId, amount);
        } catch (HttpClientErrorException e) {
            // 4xx: 토스가 확정적으로 거절했다(한도 초과, 카드사 거절 등).
            // 바로 FAILED로 기록해야 사용자가 다른 수단으로 즉시 다시 결제할 수 있다.
            TossDto.ErrorResponse error = e.getResponseBodyAs(TossDto.ErrorResponse.class);
            String code = (error != null) ? error.code() : "TOSS_HTTP_" + e.getStatusCode().value();
            String message = (error != null) ? error.message() : e.getMessage();
            paymentAttemptService.failConfirm(orderId, code, message);
            throw new PaymentException(PaymentErrorCode.TOSS_REJECTED, message);
        } catch (HttpServerErrorException | ResourceAccessException e) {
            // 5xx, 타임아웃: 토스에서 승인됐는지 알 수 없다. 실패로 단정하면 돈은 빠졌는데 FAILED가 될 수 있다.
            // IN_PROGRESS로 두고, 스케줄러가 토스 조회로 정리한다.
            throw new PaymentException(PaymentErrorCode.PAYMENT_PENDING, "결제 확인 중입니다.");
        }

        // 3. 승인 응답 반영. 스케줄러와 같은 reconcile을 쓰므로, 스케줄러가 먼저 반영했더라도 결과가 같다. (스케줄러가 먼저 반영할 가능성은 매우 낮다.)
        PaymentAttemptStatus result = paymentAttemptService.reconcile(response);

        // 카드는 DONE, 가상계좌는 계좌 발급(WAITING_FOR_DEPOSIT)이 성공이다.
        // 그 외는 토스 응답이 확정 상태가 아닌 경우다. 성공으로 응답하지 않고 스케줄러가 다시 확인한다.
        if (result != PaymentAttemptStatus.DONE && result != PaymentAttemptStatus.WAITING_FOR_DEPOSIT) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_PENDING, "결제 확인 중입니다.");
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

    /**
     * 입금 대기 중인 가상계좌를 취소한다.
     * 흐름: 취소할 paymentKey 조회 → 토스 취소 호출(트랜잭션 밖) → 성공 시 attempt를 FAILED로 반영
     */
    public void cancelVirtualAccountBeforeDeposit(Long asReceptionId) {
        String paymentKey = paymentAttemptService.getCancelablePaymentKey(asReceptionId);

        try {
            tossPaymentsClient.cancel(paymentKey, "가상계좌 입금 전 취소", null);
        } catch (HttpClientErrorException e) {
            // 4xx: 토스가 요청을 거절했다.
            TossDto.ErrorResponse error = e.getResponseBodyAs(TossDto.ErrorResponse.class);
            String code = (error != null) ? error.code() : "TOSS_HTTP_" + e.getStatusCode().value();
            String message = (error != null) ? error.message() : e.getMessage();

            // 이전 요청이 타임아웃됐지만 토스에서는 취소된 경우. 성공으로 반영한다.
            if ("ALREADY_CANCELED_PAYMENT".equals(code)) {
                paymentAttemptService.cancelVirtualAccount(paymentKey);
                return;
            }
            // attempt는 상태를 유지한다.
            throw new PaymentException(PaymentErrorCode.TOSS_REJECTED, message);
        } catch (HttpServerErrorException | ResourceAccessException e) {
            // 5xx(토스 서버 오류), 타임아웃/연결 실패: 취소됐는지 알 수 없다. 상태를 바꾸지 않고 사용자 재시도로 정리한다.
            throw new PaymentException(PaymentErrorCode.PAYMENT_PENDING, "취소 확인 중입니다.");
        }

        // 취소 반영
        paymentAttemptService.cancelVirtualAccount(paymentKey);
    }


    /**
     * 토스 응답을 받지 못해 IN_PROGRESS로 남은 결제를 토스 조회 결과로 정리한다.
     * 한 건이 실패해도 나머지는 계속 처리한다. 처리하지 못한 건은 다음 주기에 다시 시도한다.
     */
    public void reconcileStuckAttempts(LocalDateTime cutoff) {
        for (String orderId : paymentAttemptService.findStuckOrderIds(cutoff)) {
            try {
                TossDto.ConfirmResponse payment = tossPaymentsClient.getByOrderId(orderId);
                paymentAttemptService.reconcile(payment);
            } catch (HttpClientErrorException.NotFound e) {
                // 인증을 마친 결제만 IN_PROGRESS가 되므로 토스에 결제가 있어야 한다. 실패로 단정하지 않고 사람이 확인한다.
                log.error("토스에 결제가 없음. orderId={}", orderId);
            } catch (Exception e) {
                // 조회 타임아웃, 토스 5xx, 반영 중 예외 등. 다음 주기에 다시 시도한다.
                log.warn("실패. orderId={}", orderId, e);
            }
        }
    }
}
