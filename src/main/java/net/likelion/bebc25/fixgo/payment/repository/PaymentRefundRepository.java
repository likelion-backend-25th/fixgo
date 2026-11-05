package net.likelion.bebc25.fixgo.payment.repository;

import net.likelion.bebc25.fixgo.payment.entity.PaymentRefund;
import net.likelion.bebc25.fixgo.payment.entity.PaymentRefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, Long> {

    Optional<PaymentRefund> findByPaymentAttemptPaymentBillIdAndStatus(Long billId, PaymentRefundStatus status);

    boolean existsByPaymentAttemptPaymentBillIdAndStatus(Long billId, PaymentRefundStatus status);

    Optional<PaymentRefund> findByPaymentAttemptId(Long paymentAttemptId);

    @Query("select r from PaymentRefund r where r.paymentAttempt.paymentBill.id = :paymentBillId")
    Optional<PaymentRefund> findByPaymentBillId(
            @Param("paymentBillId") Long paymentBillId
    );
}
