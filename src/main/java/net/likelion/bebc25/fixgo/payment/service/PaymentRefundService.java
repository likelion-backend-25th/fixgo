package net.likelion.bebc25.fixgo.payment.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.payment.dto.PaymentInternalDto;
import net.likelion.bebc25.fixgo.payment.entity.*;
import net.likelion.bebc25.fixgo.payment.exception.PaymentErrorCode;
import net.likelion.bebc25.fixgo.payment.exception.PaymentException;
import net.likelion.bebc25.fixgo.payment.repository.PaymentAttemptRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentRefundRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentRefundService {
    private final PaymentBillRepository paymentBillRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;

    @Transactional
    public PaymentInternalDto.PreparedRefund prepareRefund(Long billId, String cancelReason, Long memberId, PaymentInternalDto.RefundAccount refundAccount) {
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

        // 결제 수단에 따라 토스에 전달할 계좌를 결정한다.
        PaymentInternalDto.RefundAccount preparedAccount = null;

        switch (attempt.getMethod()) {
            case CARD -> {
                // 카드 환불에는 계좌가 필요하지 않다.
            }
            case VIRTUAL_ACCOUNT -> {
                validateRefundAccount(refundAccount);
                preparedAccount = refundAccount;
            }
            default -> throw new IllegalStateException("온라인 환불을 지원하지 않는 결제수단입니다.");
        }

        // PaymentRefund 생성 후 저장
        PaymentRefund refund = PaymentRefund.create(attempt, cancelReason, memberId);
        paymentRefundRepository.save(refund);

        return new PaymentInternalDto.PreparedRefund(
                refund.getId(),
                attempt.getPaymentKey(),
                preparedAccount
        );

    }

    @Transactional
    public void completeRefund(Long refundId, String transactionKey, LocalDateTime canceledAt) {
        PaymentRefund refund = paymentRefundRepository.findById(refundId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 환불"));
        PaymentBill bill = refund.getPaymentAttempt().getPaymentBill();

        refund.markDone(transactionKey, canceledAt);
        bill.markRefunded();
    }

    @Transactional
    public void failRefund(Long refundId, String errorCode, String errorMessage) {
        PaymentRefund refund = paymentRefundRepository.findById(refundId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 환불"));
        refund.markFailed(errorCode, errorMessage);
    }

    /**
     * 환불 계좌에 누락된 정보가 없는 지 확인
     */
    private void validateRefundAccount(
            PaymentInternalDto.RefundAccount account
    ) {
        if (account == null
                || account.bank() == null
                || account.bank().isBlank()
                || account.accountNumber() == null
                || account.accountNumber().isBlank()
                || account.holderName() == null
                || account.holderName().isBlank()) {
            throw new IllegalArgumentException("환불 계좌 정보가 누락되었습니다.");
        }
    }
}
