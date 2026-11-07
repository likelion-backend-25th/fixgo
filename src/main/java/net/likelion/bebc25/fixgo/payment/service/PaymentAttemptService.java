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
     * 1개의 bill에 활성화된 attempt는 1개만 존재함을 보장한다. (DB에서 수동으로 추가하는 것은 막지 못함)
     */
    @Transactional
    public PaymentDto.StartPaymentResponse startPayment(Long asReceptionId) {
        // 같은 청구에 대한 동시 요청을 방지한다. 잠금이 없으면 두 요청이 모두 READY 없음을 보고 각자 새 시도를 만들 수 있다.
        PaymentBill paymentBill = paymentBillRepository.findByAsReceptionIdForUpdate(asReceptionId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.BILL_NOT_FOUND, "Bill이 존재하지 않습니다."));
        Long billId = paymentBill.getId();

        // UNPAID만 허용한다. PAID뿐 아니라 환불된 청구에서도 결제가 시작되면 안 된다.
        if (paymentBill.getStatus() != PaymentBillStatus.UNPAID) {
            throw new PaymentException(PaymentErrorCode.BILL_NOT_PAYABLE, "결제할 수 없는 Bill입니다.");
        }

        List<PaymentAttempt> attempts = paymentAttemptRepository.findAllByPaymentBillId(billId);

        // 승인 진행 중이거나 입금 대기 중인 시도가 있으면 새 결제를 시작하지 않는다.
        boolean hasInProgress = attempts.stream()
                .anyMatch(a -> a.getStatus().blocksNewAttempt());

        if (hasInProgress) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_IN_PROGRESS, "진행 중이거나 완료된 결제가 있습니다.");
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
    public PaymentErrorCode prepareConfirm(String orderId, Long amount) {
        PaymentAttempt attempt = paymentAttemptRepository.findByOrderIdForUpdate(orderId).orElseThrow(
                () -> new PaymentException(PaymentErrorCode.ATTEMPT_NOT_FOUND, "해당 결제 시도가 없습니다.")
        );

        PaymentBill bill = paymentBillRepository.findByIdForUpdate(attempt.getPaymentBill().getId())
                .orElseThrow(() -> new IllegalStateException("attempt에 연결된 bill이 없습니다. orderId=" + orderId));

        // READY가 아니면 이미 승인 중이거나 끝난 시도다. 상태를 건드리면 안되므로 bill 검사보다 먼저 거른다.
        if (attempt.getStatus() != PaymentAttemptStatus.READY) {
            throw new PaymentException(PaymentErrorCode.ATTEMPT_NOT_READY, "해당 결제 시도 상태가 READY가 아닙니다.");
        }

        PaymentErrorCode failure = null;
        if (bill.getStatus() != PaymentBillStatus.UNPAID) {
            failure = PaymentErrorCode.BILL_NOT_PAYABLE; // bill이 이미 결제되거나 환불된 상태이면 결제를 할 수 없다.
        } else if (!attempt.getAmount().equals(amount)) {
            failure = PaymentErrorCode.AMOUNT_MISMATCH; // DB 금액과 프론트가 보낸 금액이 다르면 안 된다.
        }

        if (failure != null) {
            attempt.markFailed(failure.name(), "결제를 진행할 수 없습니다.");
            return failure;
        }

        // attempt를 IN_PROGRESS로 바꾸고 Facade에 null을 반환한다.
        attempt.markInProgress();
        return null;
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

    /**
     * 토스 결제 상태를 attempt에 반영한다.
     * <p>
     * 호출하는 곳은 두 군데다.
     * 승인 스레드: 토스 승인 응답을 받은 직후 (PaymentFacadeService.confirmPayment)
     * 스케줄러: 응답을 받지 못해 IN_PROGRESS로 남은 결제를 토스에 조회한 뒤 (PaymentFacadeService.reconcileStuckAttempts)
     */
    @Transactional
    public PaymentAttemptStatus reconcile(TossDto.ConfirmResponse response) {
        // 1. attempt 잠금 조회. 승인 스레드와 스케줄러가 동시에 반영하지 못하게 한다
        PaymentAttempt attempt = paymentAttemptRepository.findByOrderIdForUpdate(response.orderId())
                .orElseThrow(() -> new IllegalStateException("attempt가 존재하지 않습니다."));

        // 2. IN_PROGRESS가 아니면 이미 다른 쪽에서 반영했거나, 승인 준비를 거치지 않은 결제다. 현재 상태를 그대로 돌려준다
        if (attempt.getStatus() != PaymentAttemptStatus.IN_PROGRESS) {
            return attempt.getStatus();
        }
        PaymentBill bill = attempt.getPaymentBill();

        // 3. 토스 상태에 따라 반영한다.
        switch (response.status()) {
            case "DONE" -> {
                attempt.markOnlineDone(response.paymentKey(), PaymentMethod.fromToss(response.method()), response.approvedAt().toLocalDateTime());
                bill.markPaid(response.approvedAt().toLocalDateTime());
            }
            case "ABORTED", "EXPIRED" -> {
                // ABORTED는 failure에 거절 사유가 있다.
                TossDto.ConfirmResponse.Failure failure = response.failure();
                String code = (failure != null) ? failure.code() : "TOSS_" + response.status();
                String message = (failure != null) ? failure.message() : "토스 결제 상태: " + response.status();
                attempt.markFailed(code, message);
            }
            case "WAITING_FOR_DEPOSIT" -> {
                // 입금 웹훅의 위조 여부를 secret으로 검증하므로 계좌 정보와 함께 저장한다
                TossDto.ConfirmResponse.VirtualAccount account = response.virtualAccount();
                attempt.markWaitingForDeposit(response.paymentKey());
                virtualAccountRepository.save(VirtualAccount.issue(
                        attempt, account.bankCode(), account.accountNumber(),
                        response.secret(), account.dueDate().toLocalDateTime()));
            }
            case "IN_PROGRESS", "READY" -> {
                log.info("토스 결제가 아직 확정 전이라 보류함. orderId={}, tossStatus={}", response.orderId(), response.status());
            }
            default -> {
                log.error("예상하지 못한 토스 결제 상태. orderId={}, tossStatus={}", response.orderId(), response.status());
            }
        }

        return attempt.getStatus();
    }

    @Transactional(readOnly = true)
    public List<String> findStuckOrderIds(LocalDateTime cutoff) {
        return paymentAttemptRepository.findAllByStatusAndUpdatedAtBefore(PaymentAttemptStatus.IN_PROGRESS, cutoff)
                .stream().map(PaymentAttempt::getOrderId).toList();
    }
}
