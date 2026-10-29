package net.likelion.bebc25.fixgo.payment.repository;

import net.likelion.bebc25.fixgo.payment.entity.PaymentBill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentBillRepository extends JpaRepository<PaymentBill, Long> {
    Optional<PaymentBill> findByAsRequestId(Long asRequestId);

    boolean existsByAsRequestId(Long asRequestId);
}
