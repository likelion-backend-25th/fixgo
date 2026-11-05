package net.likelion.bebc25.fixgo.payment.repository;

import net.likelion.bebc25.fixgo.payment.entity.PaymentRefund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, Long> {

    Optional<PaymentRefund> findByPaymentAttemptId(Long paymentAttemptId);
}
