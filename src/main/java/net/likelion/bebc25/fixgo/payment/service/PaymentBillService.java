package net.likelion.bebc25.fixgo.payment.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBill;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentBillService {
    private final PaymentBillRepository paymentBillRepository;

    @Transactional
    public void createPaymentBill(Long asRequestId, Long amount) {
        if (paymentBillRepository.existsByAsRequestId(asRequestId)) {
            throw new IllegalArgumentException("이미 청구서가 존재합니다.");
        }

        PaymentBill paymentBill = new PaymentBill(asRequestId, amount);
        paymentBillRepository.save(paymentBill);
    }
}
