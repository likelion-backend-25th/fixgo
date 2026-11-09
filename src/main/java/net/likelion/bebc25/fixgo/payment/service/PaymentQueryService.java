package net.likelion.bebc25.fixgo.payment.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.payment.entity.*;
import net.likelion.bebc25.fixgo.payment.exception.PaymentErrorCode;
import net.likelion.bebc25.fixgo.payment.exception.PaymentException;
import net.likelion.bebc25.fixgo.payment.repository.PaymentAttemptRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentRefundRepository;
import net.likelion.bebc25.fixgo.payment.repository.VirtualAccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryService {
    private final PaymentBillRepository paymentBillRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final VirtualAccountRepository virtualAccountRepository;
    private final PaymentRefundRepository paymentRefundRepository;

    public PaymentDto.PaymentDetailResponse getDetail(Long billId) {
        // 1. 청구서와 접수 기본 정보를 조회한다.
        PaymentBill bill = paymentBillRepository.findById(billId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.BILL_NOT_FOUND, "결제 데이터가 없습니다."));
        PaymentDto.ReceptionInfo reception = paymentBillRepository.findReceptionInfo(bill.getAsReception().getId())
                .orElseThrow(() -> new IllegalStateException("결제 데이터가 없습니다."));

        // 2. 결제와 환불 이력을 준비한다. 환불 후에도 성공한 결제 이력은 유지된다.
        PaymentAttempt successfulPayment = paymentAttemptRepository
                .findByPaymentBillIdAndStatus(billId, PaymentAttemptStatus.DONE)
                .orElse(null);
        // 실패하거나 진행 중인 환불은 완료 내역에 포함하지 않는다.
        PaymentRefund completedRefund = paymentRefundRepository
                .findByPaymentAttemptPaymentBillIdAndStatus(billId, PaymentRefundStatus.DONE)
                .orElse(null);
        PaymentDto.PaymentHistoryItem paymentHistory = successfulPayment == null
                ? null : PaymentDto.PaymentHistoryItem.from(successfulPayment);
        PaymentDto.RefundHistoryItem refundHistory = completedRefund == null
                ? null : PaymentDto.RefundHistoryItem.from(completedRefund);

        // 3. 화면에 보여줄 결제와 가상계좌를 확인한다.
        PaymentAttempt currentPayment = findCurrentPayment(billId, successfulPayment);
        PaymentDto.AccountInfo account = findAccountInfo(currentPayment);
        boolean refundable = isRefundable(bill, currentPayment);

        return new PaymentDto.PaymentDetailResponse(
                bill.getId(),
                reception,
                bill.getAmount(),
                bill.getStatus(),
                currentPayment == null ? null : currentPayment.getStatus(),
                currentPayment == null ? null : currentPayment.getMethod(),
                account,
                paymentHistory,
                refundHistory,
                refundable
        );
    }

    private PaymentAttempt findCurrentPayment(Long billId, PaymentAttempt successfulPayment) {
        // 성공한 결제가 있으면 해당 결제를 보여준다.
        if (successfulPayment != null) {
            return successfulPayment;
        }

        // 완료 이력이 없으면 입금 대기 결제를 조회한다. 둘 다 없으면 결제 전이다.
        return paymentAttemptRepository
                .findByPaymentBillIdAndStatus(billId, PaymentAttemptStatus.WAITING_FOR_DEPOSIT)
                .orElse(null);
    }

    private PaymentDto.AccountInfo findAccountInfo(PaymentAttempt payment) {
        if (payment == null || payment.getMethod() != PaymentMethod.VIRTUAL_ACCOUNT) {
            return null;
        }

        VirtualAccount account = virtualAccountRepository.findByPaymentAttemptId(payment.getId())
                .orElseThrow(() -> new IllegalStateException("결제 데이터가 없습니다."));
        return new PaymentDto.AccountInfo(account.getBankCode(), account.getAccountNumber(), account.getDueAt());
    }

    private boolean isRefundable(PaymentBill bill, PaymentAttempt payment) {
        if (bill.getStatus() != PaymentBillStatus.PAID || payment == null) {
            return false;
        }

        // 카드, 가상계좌(토스 환불)와 현금(현장 환불)만 지원한다.
        if (payment.getMethod() != PaymentMethod.CARD
                && payment.getMethod() != PaymentMethod.VIRTUAL_ACCOUNT
                && payment.getMethod() != PaymentMethod.CASH) {
            return false;
        }

        // 처리 중인 환불이 있으면 중복 요청 버튼을 비활성화한다.
        return !paymentRefundRepository.existsByPaymentAttemptPaymentBillIdAndStatus(
                bill.getId(), PaymentRefundStatus.IN_PROGRESS);
    }

    public Page<PaymentDto.PaymentListResponse> getPaymentList(
            String keyword,
            PaymentBillStatus status,
            Pageable pageable
    ) {
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        // 정렬은 쿼리의 order by(최신순)로 고정한다.
        Pageable pageOnly = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        return paymentBillRepository.findPaymentList(normalizedKeyword, status, pageOnly);
    }

    /**
     * 고객 포탈 결제 영역. 청구서가 아직 발행되지 않았으면 비어 있다.
     */
    public Optional<PaymentDto.CustomerPaymentView> findCustomerView(Long asReceptionId) {
        return paymentBillRepository.findByAsReceptionId(asReceptionId)
                .map(this::toCustomerView);
    }

    private PaymentDto.CustomerPaymentView toCustomerView(PaymentBill bill) {
        return switch (bill.getStatus()) {
            case UNPAID -> {
                // bill은 입금 전까지 UNPAID다. 입금 대기인지는 attempt를 보고 판단한다.
                Optional<PaymentAttempt> waitingAttempt = paymentAttemptRepository
                        .findByPaymentBillIdAndStatus(bill.getId(), PaymentAttemptStatus.WAITING_FOR_DEPOSIT);

                if (waitingAttempt.isEmpty()) {
                    yield PaymentDto.CustomerPaymentView.unpaid(bill);
                }

                PaymentAttempt attempt = waitingAttempt.get();
                // 입금 대기 attempt는 reconcile에서 계좌와 함께 저장되므로 계좌가 반드시 있다
                VirtualAccount virtualAccount = virtualAccountRepository.findByPaymentAttemptId(attempt.getId())
                        .orElseThrow(() -> new IllegalStateException(
                                "입금 대기 중인 결제 시도에 가상계좌가 없습니다. attemptId=" + attempt.getId()));

                yield PaymentDto.CustomerPaymentView.waitingForDeposit(bill, attempt, virtualAccount);
            }
            case PAID -> {
                PaymentAttempt doneAttempt = paymentAttemptRepository
                        .findByPaymentBillIdAndStatus(bill.getId(), PaymentAttemptStatus.DONE)
                        .orElseThrow(() -> new IllegalStateException("결제 완료된 청구서에 완료된 결제 시도가 없습니다. billId=" + bill.getId()));
                yield PaymentDto.CustomerPaymentView.paid(bill, doneAttempt);
            }
            case REFUNDED -> {
                // 환불 후에도 성공한 결제 시도(DONE)는 남아 있다.
                PaymentAttempt doneAttempt = paymentAttemptRepository
                        .findByPaymentBillIdAndStatus(bill.getId(), PaymentAttemptStatus.DONE)
                        .orElseThrow(() -> new IllegalStateException("환불된 청구서에 완료된 결제 시도가 없습니다. billId=" + bill.getId()));
                yield PaymentDto.CustomerPaymentView.refunded(bill, doneAttempt);
            }
        };
    }
}
