package net.likelion.bebc25.fixgo.payment.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.payment.dto.PaymentSummary;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttempt;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttemptStatus;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBill;
import net.likelion.bebc25.fixgo.payment.repository.PaymentAttemptRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryService {
    private final PaymentBillRepository paymentBillRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;

    public PaymentSummary getSummary(Long asRequestId) {
        PaymentBill bill = paymentBillRepository.findByAsRequestId(asRequestId).orElse(null);
        if (bill == null) {
            return PaymentSummary.notIssued();
        }
        return switch (bill.getStatus()) {
            case UNPAID -> PaymentSummary.unpaid(bill);
            case PAID -> {
                PaymentAttempt doneAttempt = paymentAttemptRepository
                        .findByPaymentBillIdAndStatus(bill.getId(), PaymentAttemptStatus.DONE)
                        .orElseThrow(() -> new IllegalStateException("결제 완료된 청구서에 완료된 결제 시도가 없습니다. billId=" + bill.getId()));
                yield PaymentSummary.paid(bill, doneAttempt);
            }
            // TODO: 환불 기능 구현 시 고객에게 보여줄 내용
            case REFUNDED, PARTIALLY_REFUNDED -> throw new IllegalStateException("환불은 아직 미구현입니다.");

        };

    }
}
