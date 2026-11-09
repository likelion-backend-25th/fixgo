package net.likelion.bebc25.fixgo.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.likelion.bebc25.fixgo.payment.client.TossDto;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.payment.entity.*;
import net.likelion.bebc25.fixgo.payment.exception.PaymentErrorCode;
import net.likelion.bebc25.fixgo.payment.exception.PaymentException;
import net.likelion.bebc25.fixgo.payment.repository.PaymentAttemptRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentRefundRepository;
import net.likelion.bebc25.fixgo.payment.service.dto.RefundDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRefundService {
    private final PaymentBillRepository paymentBillRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;

    @Transactional
    public RefundDto.PreparedRefund prepareRefund(Long billId, Long memberId, PaymentDto.RefundPaymentRequest request) {
        // 결제 완료된 attempt
        PaymentAttempt attempt = findVerifiedAttempt(billId);

        // IN_PROGRESS 상태인 환불이 있는 지 확인
        if (paymentRefundRepository.existsByPaymentAttemptPaymentBillIdAndStatus(billId, PaymentRefundStatus.IN_PROGRESS)) {
            throw new PaymentException(PaymentErrorCode.REFUND_IN_PROGRESS, "이미 환불 처리 중입니다.");
        }

        // 결제 수단에 따라 토스에 전달할 계좌를 결정한다.
        RefundDto.RefundAccount refundAccount = switch (attempt.getMethod()) {
            case CARD -> null; // 카드 환불에는 계좌가 필요 없다.
            case VIRTUAL_ACCOUNT -> { // 필드 검증은 @Vaild가 한다.
                if (request.refundAccount() == null) {
                    throw new PaymentException(PaymentErrorCode.REFUND_ACCOUNT_REQUIRED, "환불 계좌 정보가 필요합니다.");
                }
                yield request.refundAccount().toRefundDto();
            }
            default -> throw new IllegalStateException("온라인 환불을 지원하지 않는 결제수단입니다.");
        };

        // PaymentRefund 생성 후 저장
        PaymentRefund refund = paymentRefundRepository.save(
                PaymentRefund.create(attempt, request.cancelReason(), memberId));
        return new RefundDto.PreparedRefund(refund.getId(), attempt.getPaymentKey(), refundAccount);
    }

    /**
     * 결제 완료된 bill을 검증하고 해당 attempt를 찾아 반환한다.
     */
    private PaymentAttempt findVerifiedAttempt(Long billId) {
        // 청구서 잠금 조회
        PaymentBill bill = paymentBillRepository.findByIdForUpdate(billId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.BILL_NOT_FOUND, "청구서가 없습니다."));

        // 청구서가 PAID인지 확인
        if (!bill.getStatus().equals(PaymentBillStatus.PAID)) {
            throw new PaymentException(PaymentErrorCode.BILL_NOT_REFUNDABLE, "환불할 수 없는 상태입니다.");
        }

        // 완료된 결제 시도 조회
        PaymentAttempt attempt = paymentAttemptRepository.findByPaymentBillIdAndStatus(billId, PaymentAttemptStatus.DONE)
                .orElseThrow(() -> new IllegalStateException("결제 시도가 없습니다."));

        return attempt;
    }

    /**
     * 환불 완료. 이미 반영됐으면 무시한다.
     */
    @Transactional
    public void completeRefund(Long refundId, String transactionKey, LocalDateTime canceledAt) {
        PaymentRefund refund = paymentRefundRepository.findByIdForUpdate(refundId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 환불"));
        if (refund.getStatus() != PaymentRefundStatus.IN_PROGRESS) {
            return;
        }
        PaymentBill bill = refund.getPaymentAttempt().getPaymentBill();

        refund.markDone(transactionKey, canceledAt);
        bill.markRefunded();
    }

    @Transactional
    public void failRefund(Long refundId, String errorCode, String errorMessage) {
        PaymentRefund refund = paymentRefundRepository.findByIdForUpdate(refundId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 환불"));
        if (refund.getStatus() != PaymentRefundStatus.IN_PROGRESS) {
            return;
        }
        refund.markFailed(errorCode, errorMessage);
    }

    @Transactional
    public void reconcile(Long refundId, TossDto.CancelResponse response) {
        switch (response.status()) {
            case "CANCELED" -> {
                TossDto.Cancel cancel = response.cancels().getFirst();
                completeRefund(refundId, cancel.transactionKey(), cancel.canceledAt().toLocalDateTime());
            }
            case "DONE" -> failRefund(refundId, "TOSS_NOT_CANCELED", "토스에 환불이 반영되지 않음.");
            default -> log.warn("환불 정리 불가 상태. refundId={}, tossStatus={}", refundId, response.status());
        }
    }

    public List<RefundDto.StuckRefund> findStuck(LocalDateTime cutoff) {
        return paymentRefundRepository.findStuck(PaymentRefundStatus.IN_PROGRESS, cutoff);
    }
}
