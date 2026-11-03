package net.likelion.bebc25.fixgo.payment.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttempt;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttemptStatus;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBill;
import net.likelion.bebc25.fixgo.payment.entity.VirtualAccount;
import net.likelion.bebc25.fixgo.payment.repository.PaymentAttemptRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import net.likelion.bebc25.fixgo.payment.repository.VirtualAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryService {
    private final PaymentBillRepository paymentBillRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final VirtualAccountRepository virtualAccountRepository;

    public PaymentDto.PaymentSummaryResponse getSummary(Long asReceptionId) {
        PaymentBill bill = paymentBillRepository.findByAsReceptionId(asReceptionId).orElse(null);
        if (bill == null) {
            return PaymentDto.PaymentSummaryResponse.notIssued();
        }
        return switch (bill.getStatus()) {
            case UNPAID -> {
                // bill은 입금 전까지 UNPAID다. 입금 대기인지는 attempt를 보고 판단한다.
                Optional<PaymentAttempt> waitingAttempt = paymentAttemptRepository
                        .findByPaymentBillIdAndStatus(bill.getId(), PaymentAttemptStatus.WAITING_FOR_DEPOSIT);

                if (waitingAttempt.isEmpty()) {
                    yield PaymentDto.PaymentSummaryResponse.unpaid(bill);
                }

                PaymentAttempt attempt = waitingAttempt.get();
                // 입금 대기 attempt는 issueVirtualAccount에서 계좌와 함께 저장되므로 계좌가 반드시 있다
                VirtualAccount virtualAccount = virtualAccountRepository.findByPaymentAttemptId(attempt.getId())
                        .orElseThrow(() -> new IllegalStateException(
                                "입금 대기 중인 결제 시도에 가상계좌가 없습니다. attemptId=" + attempt.getId()));

                yield PaymentDto.PaymentSummaryResponse.waitingForDeposit(bill, attempt, virtualAccount);
            }
            case PAID -> {
                PaymentAttempt doneAttempt = paymentAttemptRepository
                        .findByPaymentBillIdAndStatus(bill.getId(), PaymentAttemptStatus.DONE)
                        .orElseThrow(() -> new IllegalStateException("결제 완료된 청구서에 완료된 결제 시도가 없습니다. billId=" + bill.getId()));
                yield PaymentDto.PaymentSummaryResponse.paid(bill, doneAttempt);
            }
            // TODO: 환불 기능 구현 시 고객에게 보여줄 내용
            case REFUNDED, PARTIALLY_REFUNDED -> throw new IllegalStateException("환불은 아직 미구현입니다.");

        };

    }
}
