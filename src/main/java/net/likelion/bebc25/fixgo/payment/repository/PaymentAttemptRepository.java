package net.likelion.bebc25.fixgo.payment.repository;

import jakarta.persistence.LockModeType;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttempt;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {
    Optional<PaymentAttempt> findByOrderIdAndStatus(String orderId, PaymentAttemptStatus status);

    Optional<PaymentAttempt> findByPaymentBillIdAndStatus(Long billId, PaymentAttemptStatus status);

    List<PaymentAttempt> findAllByPaymentBillId(Long billId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from PaymentAttempt a where a.orderId = :orderId")
    Optional<PaymentAttempt> findByOrderIdForUpdate(String orderId);

    boolean existsByOrderIdAndStatus(String orderId, PaymentAttemptStatus status);

    Optional<PaymentAttempt> findByOrderId(String orderId);
}
