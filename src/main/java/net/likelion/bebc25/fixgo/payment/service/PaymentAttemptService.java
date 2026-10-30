//package net.likelion.bebc25.fixgo.payment.service;
//
//import lombok.RequiredArgsConstructor;
//import net.likelion.bebc25.fixgo.payment.repository.PaymentAttemptRepository;
//import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class PaymentAttemptService {
//    private final PaymentBillRepository paymentBillRepository;
//    private final PaymentAttemptRepository paymentAttemptRepository;
//
//    @Transactional
//    public void createPaymentAttempt(Long billId) {
//        PaymentBill paymentBill = paymentBillRepository.findById(billId).orElseThrow(() -> new IllegalArgumentException("Payment Bill Not Found"));
//
//        if (paymentBill.getStatus() == PaymentBillStatus.PAID) {
//            throw new IllegalStateException("이미 결제 완료된 청구서입니다.");
//        }
//
//        if (paymentAttemptRepository.existsByBillIdAndStatus(billId, PaymentAttemptStatus.READY)) ;
//        String orderId = "PAY-" + UUID.randomUUID().toString().replace("-", "");
//
//        PaymentAttempt paymentAttempt = PaymentAttempt.createOnline(paymentBill, orderId);
//
//        paymentAttemptRepository.save(paymentAttempt);
//    }
//}
