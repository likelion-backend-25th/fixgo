package net.likelion.bebc25.fixgo.payment.repository;

import jakarta.persistence.LockModeType;
import net.likelion.bebc25.fixgo.payment.entity.PaymentRefund;
import net.likelion.bebc25.fixgo.payment.entity.PaymentRefundStatus;
import net.likelion.bebc25.fixgo.payment.service.dto.RefundDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, Long> {

    Optional<PaymentRefund> findByPaymentAttemptPaymentBillIdAndStatus(Long billId, PaymentRefundStatus status);

    boolean existsByPaymentAttemptPaymentBillIdAndStatus(Long billId, PaymentRefundStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from PaymentRefund r where r.id = :id")
    Optional<PaymentRefund> findByIdForUpdate(Long id);

    @Query("""
            select new net.likelion.bebc25.fixgo.payment.service.dto.RefundDto$StuckRefund(r.id, a.paymentKey)
            from PaymentRefund r join r.paymentAttempt a
            where r.status = :status and r.createdAt < :cutoff
            """)
    List<RefundDto.StuckRefund> findStuck(PaymentRefundStatus status, LocalDateTime cutoff);
}
