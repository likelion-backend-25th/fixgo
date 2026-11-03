package net.likelion.bebc25.fixgo.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.likelion.bebc25.fixgo.payment.client.TossDto;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttempt;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBill;
import net.likelion.bebc25.fixgo.payment.entity.VirtualAccount;
import net.likelion.bebc25.fixgo.payment.exception.PaymentErrorCode;
import net.likelion.bebc25.fixgo.payment.exception.PaymentException;
import net.likelion.bebc25.fixgo.payment.repository.PaymentAttemptRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import net.likelion.bebc25.fixgo.payment.repository.VirtualAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // 외부에서 들어온 orderId라 위조 요청일 수 있다. 진짜 토스라면 재전송으로 실패가 드러나도록 거절한다.
        PaymentAttempt attempt = paymentAttemptRepository.findByOrderIdForUpdate(webhook.orderId())
                .orElseThrow(() -> {
                    log.warn("가상계좌 웹훅: 결제 시도를 찾을 수 없어 거절함. orderId={}", webhook.orderId());
                    return new PaymentException(PaymentErrorCode.ATTEMPT_NOT_FOUND, "결제 시도를 찾을 수 없습니다.");
                });


        // 가상계좌 결제가 아닌 orderId로 온 웹훅. secret도 검증할 수 없으므로 거절한다.
        VirtualAccount virtualAccount = virtualAccountRepository.findByPaymentAttemptId(attempt.getId())
                .orElseThrow(() -> {
                    log.warn("가상계좌 웹훅: 가상계좌 정보가 없어 거절함. orderId={}, attemptStatus={}",
                             webhook.orderId(), attempt.getStatus());
                    return new PaymentException(PaymentErrorCode.VIRTUAL_ACCOUNT_NOT_FOUND, "가상계좌 정보를 찾을 수 없습니다.");
                });

        if (!virtualAccount.getWebhookSecret().equals(webhook.secret())) {
            // secret이 다르면 토스가 보낸 요청이 아닐 가능성이 크다. secret 값은 로그에 남기지 않는다.
            log.warn("가상계좌 웹훅: secret 불일치로 거절함. orderId={}", webhook.orderId());
            throw new PaymentException(PaymentErrorCode.SECRET_MISMATCH, "웹훅 검증에 실패했습니다.");
        }

        PaymentBill bill = paymentBillRepository.findByIdForUpdate(attempt.getPaymentBill().getId())
                .orElseThrow(() -> new IllegalStateException(
                        "결제 시도에 연결된 청구서가 없습니다. orderId=" + webhook.orderId()));


        switch (webhook.status()) {
            case "DONE" -> {
                switch (attempt.getStatus()) {
                    case WAITING_FOR_DEPOSIT -> {
                        attempt.markVirtualAccountDone(webhook.createdAt());
                        bill.markPaid(webhook.createdAt());
                    }
                    case DONE -> log.info("가상계좌 웹훅: 이미 입금 완료 반영된 결제. orderId={}", webhook.orderId());
                    case FAILED ->
                            log.error("가상계좌 웹훅: 실패 처리된 결제에 입금이 들어옴. 이중 결제 여부 확인 필요. orderId={}", webhook.orderId());
                    default -> log.warn("가상계좌 웹훅: 입금 완료를 반영할 수 없는 결제 시도 상태. orderId={}, attemptStatus={}",
                                        webhook.orderId(), attempt.getStatus());
                }
            }
            // TODO: 가상계좌 취소 / 환불
            case "CANCELED" -> {
                switch (attempt.getStatus()) {
                    case WAITING_FOR_DEPOSIT -> {

                    }
                    case DONE -> {
                    }
                    case FAILED -> {
                    }
                    default -> {
                    }
                }
            }
        }

        // 웹훅이 CANCELED인 경우
    }
}
