package net.likelion.bebc25.fixgo.payment.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttempt;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttemptStatus;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBill;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBillStatus;
import net.likelion.bebc25.fixgo.payment.repository.PaymentAttemptRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentAttemptService {
    private final PaymentBillRepository paymentBillRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;

    /**
     * 결제를 시작한다. READY인 결제 시도가 이미 있으면 재사용한다.
     *
     * @param billId 결제할 청구서 ID
     * @return
     */
    @Transactional
    public PaymentDto.StartPaymentResponse startPayment(Long billId) {
        // 같은 청구에 대한 동시 요청을 방지한다. 잠금이 없으면 READY 없음을 확인한 후 시도한다.
        PaymentBill paymentBill = paymentBillRepository.findByIdForUpdate(billId)
                .orElseThrow(() -> new IllegalArgumentException("Payment Bill Not Found"));

        // UNPAID만 허용한다. PAID뿐 아니라 환불된 청구에서도 결제가 시작되면 안 된다.
        if (paymentBill.getStatus() != PaymentBillStatus.UNPAID) {
            throw new IllegalStateException("결제할 수 없습니다.");
        }

        // TODO: WAITING_FOR_DEPOSIT 시도가 있으면 거절한다.
        // READY 시도가 있으면 재사용, 없으면 새로 만든다.
        PaymentAttempt attempt = paymentAttemptRepository
                .findByPaymentBillIdAndStatus(paymentBill.getId(), PaymentAttemptStatus.READY)
                .orElseGet(() -> paymentAttemptRepository.save(PaymentAttempt.create(paymentBill)));

        return PaymentDto.StartPaymentResponse.from(attempt);
    }
}
