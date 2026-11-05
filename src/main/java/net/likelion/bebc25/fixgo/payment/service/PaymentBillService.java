package net.likelion.bebc25.fixgo.payment.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.repository.AsReceptionRepository;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBill;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBillType;
import net.likelion.bebc25.fixgo.payment.exception.PaymentErrorCode;
import net.likelion.bebc25.fixgo.payment.exception.PaymentException;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final AsReceptionRepository asReceptionRepository;

    /**
     * A/S 접수 한 건에 대한 청구서를 발행한다. 접수 당 청구서는 하나만 존재할 수 있다.
     *
     * @param asReceptionId 청구 대상 A/S 접수 ID
     * @param amount        청구 금액 (원)
     * @return 저장된 청구서 ID
     */
    @Transactional
    public Long createBill(Long asReceptionId, Long amount, PaymentBillType paymentBillType) {
        AsReception asReception = asReceptionRepository.findById(asReceptionId)
                .orElseThrow(() -> new IllegalArgumentException("A/S 접수가 존재하지 않습니다."));

        // 동시 요청 두 개가 모두 이 검사를 통과할 수 있으며, 최종 방어는 DB의 asReceptionId unique 제약이 한다.
        if (paymentBillRepository.existsByAsReceptionId(asReceptionId)) {
            throw new PaymentException(PaymentErrorCode.BILL_ALREADY_EXISTS, "이미 bill이 존재합니다.");
        }

        PaymentBill paymentBill = PaymentBill.create(asReception, amount, paymentBillType);

        // 동시 요청은 둘 다 이 검사를 통과할 수 있으므로 save의 unique 제약 위반을 잡아 같은 예외로 바꾼다.
        try {
            paymentBillRepository.save(paymentBill);
        } catch (DataIntegrityViolationException e) {
            throw new PaymentException(PaymentErrorCode.BILL_ALREADY_EXISTS, "이미 bill이 존재합니다.");
        }

        return paymentBill.getId();
    }
}
