package net.likelion.bebc25.fixgo.payment.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBill;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBillType;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 청구서(PaymentBill) 발행을 담당하는 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentBillService {
    private final PaymentBillRepository paymentBillRepository;

    /**
     * A/S 접수 한 건에 대한 청구서를 발행한다. 접수 당 청구서는 하나만 존재할 수 있다.
     *
     * @param asRequestId 청구 대상 A/S 접수 ID
     * @param amount      청구 금액 (원)
     * @return 저장된 청구서 ID
     */
    @Transactional
    public Long issueBill(Long asRequestId, Long amount, PaymentBillType paymentBillType) {
        // 동시 요청 두 개가 모두 이 검사를 통과할 수 있으며, 최종 방어는 DB의 asRequestId unique 제약이 한다.
        if (paymentBillRepository.existsByAsRequestId(asRequestId)) {
            throw new IllegalArgumentException("이미 청구서가 존재합니다.");
        }

        PaymentBill paymentBill = PaymentBill.issue(asRequestId, amount, paymentBillType);
        paymentBillRepository.save(paymentBill);

        return paymentBill.getId();
    }
}
