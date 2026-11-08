package net.likelion.bebc25.fixgo.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.likelion.bebc25.fixgo.payment.client.TossDto;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttempt;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttemptStatus;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBill;
import net.likelion.bebc25.fixgo.payment.entity.VirtualAccount;
import net.likelion.bebc25.fixgo.payment.exception.PaymentErrorCode;
import net.likelion.bebc25.fixgo.payment.exception.PaymentException;
import net.likelion.bebc25.fixgo.payment.repository.PaymentAttemptRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import net.likelion.bebc25.fixgo.payment.repository.VirtualAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentWebhookService {
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final VirtualAccountRepository virtualAccountRepository;
    private final PaymentBillRepository paymentBillRepository;

    /**
     * 토스 가상계좌 웹훅(DEPOSIT_CALLBACK)을 처리한다.
     * <p>
     * 에러를 응답하면 토스가 재전송하므로, 재전송으로 해결되지 않는 경우는 에러를 내지 않는다.
     * 예외: secret이 다르면 토스가 보낸 요청이 아니므로 거절한다.
     * <p>
     * 같은 웹훅이 여러 번 올 수 있다. attempt를 잠그고 현재 상태를 확인해 이미 반영된 웹훅은 무시한다.
     * 잠금 순서는 attempt → bill이다.
     */
    @Transactional
    public void handleDepositCallback(TossDto.DepositCallbackWebhook webhook) {
        PaymentAttempt attempt = findVerifiedAttempt(webhook);

        PaymentBill bill = paymentBillRepository.findByIdForUpdate(attempt.getPaymentBill().getId())
                .orElseThrow(() -> new IllegalStateException(
                        "결제 시도에 연결된 청구서가 없습니다. orderId=" + webhook.orderId()));

        switch (webhook.status()) {
            case "DONE" -> applyDeposit(attempt, bill, webhook.createdAt());  // 가상계좌 입금 완료
            case "WAITING_FOR_DEPOSIT" -> applyDepositError(attempt, bill); // 가상계좌 입금 오류
            case "CANCELED" -> applyCancel(attempt); // 가상계좌 취소 또는 환불
        }
    }

    /**
     * 웹훅의 orderId와 secret을 검증한다.
     */
    private PaymentAttempt findVerifiedAttempt(TossDto.DepositCallbackWebhook webhook) {
        // 외부에서 들어온 orderId라 위조 요청일 수 있다. 진짜 토스라면 재전송으로 실패가 드러나도록 거절한다.
        PaymentAttempt attempt = paymentAttemptRepository.findByOrderIdForUpdate(webhook.orderId())
                .orElseThrow(() -> reject(PaymentErrorCode.ATTEMPT_NOT_FOUND, webhook));


        // 가상계좌 결제가 아닌 orderId로 온 웹훅. secret도 검증할 수 없으므로 거절한다.
        VirtualAccount virtualAccount = virtualAccountRepository.findByPaymentAttemptId(attempt.getId())
                .orElseThrow(() -> reject(PaymentErrorCode.VIRTUAL_ACCOUNT_NOT_FOUND, webhook));

        // 웹훅의 secret과 db에 저장된 secret이 일치하는 지 확인한다.
        if (!virtualAccount.matchesSecret(webhook.secret())) {
            throw reject(PaymentErrorCode.SECRET_MISMATCH, webhook);
        }
        return attempt;
    }

    /**
     * 입금완료. 재전송이면 무시한다.
     * 실패 처리된 결제에 입금 완료가 오는 경우 기록한다.
     */
    private void applyDeposit(PaymentAttempt attempt, PaymentBill bill, LocalDateTime depositedAt) {
        switch (attempt.getStatus()) {
            case DONE -> {
            }
            case FAILED -> log.error("만료/취소된 결제에 입금 웩훅. 확인 필요. orderId={}", attempt.getOrderId());
            default -> {
                attempt.markVirtualAccountDone(depositedAt);
                bill.markPaid(depositedAt);
            }
        }
    }

    /**
     * 입금 오류 (송금 한도 초과, 네트워크 오류 등). 입금 완료를 입금 대기로 되돌리고 구매자에게 재입금을 안내한다.
     * 입금 완료 상태가 아니면 가상계좌 발급이나 재전송이므로 무시한다.
     * 가상계좌 발급은 승인 요청 또는 스케줄러에서 처리한다.
     */
    private void applyDepositError(PaymentAttempt attempt, PaymentBill bill) {
        if (attempt.getStatus() != PaymentAttemptStatus.DONE) {
            return;
        }
        // TODO: DONE -> WAITING_FOR_DEPOSIT, approvedAt 비우기 / 구매자 재입금 안내
    }

    /**
     * 가상계좌 취소. 입금 전 취소는 FAILED로 반영한다. (우리 요청이 타임아웃이 일어난 경우 대비)
     */
    private void applyCancel(PaymentAttempt attempt) {
        switch (attempt.getStatus()) {
            case WAITING_FOR_DEPOSIT -> attempt.markFailed("VIRTUAL_ACCOUNT_CANCELED", "가상계좌 입금 전 취소");
            case DONE -> log.info("가상계좌 웹훅: 환불 웹훅");
            default -> {
            } // 이미 반영 됨
        }
    }

    /**
     * 웹훅을 거절한다. 토스가 보낸 웹훅이면 웹훅이 재전송되므로, 원인을 알 수 있게 로그를 남긴다.
     */
    private PaymentException reject(PaymentErrorCode code, TossDto.DepositCallbackWebhook webhook) {
        log.warn("가상계좌 웹훅 거절. reason={}, orderId={}, status={}", code, webhook.orderId(), webhook.status());
        return new PaymentException(code, "웹훅 검증에 실패했습니다.");
    }
}
