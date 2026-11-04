package net.likelion.bebc25.fixgo.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.payment.entity.*;
import net.likelion.bebc25.fixgo.payment.exception.PaymentErrorCode;
import net.likelion.bebc25.fixgo.payment.exception.PaymentException;
import net.likelion.bebc25.fixgo.payment.repository.PaymentAttemptRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import net.likelion.bebc25.fixgo.payment.repository.VirtualAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentAttemptService {
    private final PaymentBillRepository paymentBillRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final VirtualAccountRepository virtualAccountRepository;

    /**
     * 결제를 시작한다. READY인 결제 시도가 이미 있으면 재사용한다.
     */
    @Transactional
    public PaymentDto.StartPaymentResponse startPayment(Long asReceptionId) {
        // 같은 청구에 대한 동시 요청을 방지한다. 잠금이 없으면 READY 없음을 확인한 후 시도한다.
        PaymentBill paymentBill = paymentBillRepository.findByAsReceptionIdForUpdate(asReceptionId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.BILL_NOT_FOUND, "청구서가 존재하지 않습니다."));
        Long billId = paymentBill.getId();

        // UNPAID만 허용한다. PAID뿐 아니라 환불된 청구에서도 결제가 시작되면 안 된다.
        if (paymentBill.getStatus() != PaymentBillStatus.UNPAID) {
            throw new IllegalStateException("결제할 수 없습니다.");
        }

        List<PaymentAttempt> attempts = paymentAttemptRepository.findAllByPaymentBillId(billId);

        // 승인 진행 중이거나 입금 대기 중인 시도가 있으면 새 결제를 시작하지 않는다.
        boolean hasInProgress = attempts.stream()
                .anyMatch(a -> a.getStatus().isInProgress());

        if (hasInProgress) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_IN_PROGRESS, "진행 중인 결제가 있습니다.");
        }

        // READY 시도가 있으면 재사용, 없으면 새로 만든다.
        PaymentAttempt attempt = attempts.stream()
                .filter(a -> a.getStatus() == PaymentAttemptStatus.READY)
                .findFirst()
                .orElseGet(() -> paymentAttemptRepository.save(PaymentAttempt.create(paymentBill)));

        return PaymentDto.StartPaymentResponse.from(attempt);
    }

    /**
     * 승인 준비: 승인 가능한 상태인지 검증하고 IN_PROGRESS로 바꾼다.
     */
    @Transactional
    public void prepareConfirm(String orderId, Long amount) {
        PaymentAttempt attempt = paymentAttemptRepository.findByOrderIdForUpdate(orderId).orElseThrow(
                () -> new PaymentException(PaymentErrorCode.ATTEMPT_NOT_FOUND, "해당 결제 시도가 없습니다.")
        );

        PaymentBill bill = paymentBillRepository.findByIdForUpdate(attempt.getPaymentBill().getId())
                .orElseThrow(() -> new IllegalStateException("Payment Bill Not Found"));

        // READY가 아니면 이미 승인 중이거나 끝난 시도다. 상태를 건드리면 안되므로 bill 검사보다 먼저 거른다.
        if (attempt.getStatus() != PaymentAttemptStatus.READY) {
            throw new PaymentException(PaymentErrorCode.ATTEMPT_NOT_READY, "해당 결제 시도 상태가 READY가 아닙니다.");
        }

        // 한 bill에 활성 시도는 하나뿐이다.
        if (bill.getStatus() != PaymentBillStatus.UNPAID) {
            throw new PaymentException(PaymentErrorCode.BILL_NOT_PAYABLE, "결제할 수 없는 청구입니다.");
        }

        // 프론트가 보낸 금액은 신뢰하지 않는다. DB 금액과 다르면 승인하지 않는다.
        if (!attempt.getAmount().equals(amount)) {
            throw new PaymentException(PaymentErrorCode.AMOUNT_MISMATCH, "금액이 다릅니다.");
        }

        attempt.markInProgress();
    }

    /**
     * 승인 성공 반영: 토스 승인이 성공한 뒤에만 호출한다.
     * attempt를 DONE으로, bill을 PAID로 한 트랜잭션에서 바꾼다.
     * <p>
     * 잠금을 걸지 않는다. attempt가 IN_PROGRESS인 동안에는
     * 같은 bill을 바꾸는 다른 경로(startPayment, prepareConfirm, 현금 결제)가 모두 거절되기 때문이다.
     */
    @Transactional
    public void completeConfirm(String orderId, String paymentKey, LocalDateTime approvedAt) {
        PaymentAttempt attempt = paymentAttemptRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException("Payment Attempt Not Found"));

        attempt.markCardDone(paymentKey, approvedAt);
        PaymentBill bill = attempt.getPaymentBill();

        bill.markPaid(approvedAt);
    }

    /**
     * 가상계좌 발급 반영: 토스 승인 응답이 WAITING_FOR_DEPOSIT일 때만 호출한다.
     * attempt를 WAITING_FOR_DEPOSIT으로 바꾸고, 계좌 정보를 VirtualAccount에 저장한다.
     * 아직 입금 전이므로 bill은 UNPAID 그대로 둔다. 입금 완료는 웹훅에서 처리한다.
     * <p>
     * 잠금을 걸지 않는 이유는 completeConfirm과 같다.
     */
    @Transactional
    public void issueVirtualAccount(
            String orderId, String paymentKey, String bankCode,
            String accountNumber, String webhookSecret, LocalDateTime dueAt
    ) {
        PaymentAttempt attempt = paymentAttemptRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException("Payment Attempt Not Found"));

        attempt.markWaitingForDeposit(paymentKey);
        VirtualAccount virtualAccount = VirtualAccount.issue(attempt, bankCode, accountNumber, webhookSecret, dueAt);
        virtualAccountRepository.save(virtualAccount);
    }

    @Transactional
    public void failConfirm(String orderId, String failureCode, String failureMessage) {
        PaymentAttempt attempt = paymentAttemptRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException("Payment Attempt Not Found"));

        attempt.markFailed(failureCode, failureMessage);
    }

    /**
     * 가상계좌 입금 전 취소
     * 취소 성공 시 attempt를 FAILED로 바꾼다.
     */
    @Transactional
    public void cancelVirtualAccount(String orderId) {
        PaymentAttempt attempt = paymentAttemptRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException("Payment Attempt Not Found"));
        switch (attempt.getStatus()) {
            case WAITING_FOR_DEPOSIT -> attempt.markFailed("VIRTUAL_ACCOUNT_CANCELED", "가상계좌 입금전 취소");
            case FAILED -> log.info("이미 가상계좌가 취소되어 있습니다. {}", attempt.getOrderId());
            case DONE -> log.error("취소 처리된 결제에 입금이 들어옴 {}", attempt.getOrderId());
            default -> log.warn("취소할 수 없는 상태 {}", attempt.getOrderId());
        }
    }
}
