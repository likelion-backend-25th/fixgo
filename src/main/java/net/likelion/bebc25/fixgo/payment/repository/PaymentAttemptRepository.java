package net.likelion.bebc25.fixgo.payment.repository;

import net.likelion.bebc25.fixgo.payment.entity.PaymentAttempt;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {
    Optional<PaymentAttempt> findByPaymentBillIdAndStatus(Long billId, PaymentAttemptStatus status);

    List<PaymentAttempt> findAllByPaymentBillId(Long billId);

    Optional<PaymentAttempt> findByOrderId(String orderId);
}
