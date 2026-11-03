package net.likelion.bebc25.fixgo.payment.repository;

import jakarta.persistence.LockModeType;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentBillRepository extends JpaRepository<PaymentBill, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from PaymentBill b where b.id = :id")
    Optional<PaymentBill> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from PaymentBill b where b.asReceptionId = :asReceptionId")
    Optional<PaymentBill> findByAsReceptionIdForUpdate(@Param("asReceptionId") Long asReceptionId);

    Optional<PaymentBill> findByAsReceptionId(Long asReceptionId);

    boolean existsByAsReceptionId(Long asReceptionId);
}
