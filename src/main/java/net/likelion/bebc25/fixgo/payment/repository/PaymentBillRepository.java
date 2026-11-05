package net.likelion.bebc25.fixgo.payment.repository;

import jakarta.persistence.LockModeType;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBill;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBillStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @Query("select b from PaymentBill b where b.asReception.id = :asReceptionId")
    Optional<PaymentBill> findByAsReceptionIdForUpdate(@Param("asReceptionId") Long asReceptionId);

    Optional<PaymentBill> findByAsReceptionId(Long asReceptionId);

    boolean existsByAsReceptionId(Long asReceptionId);

    @Query(
            value = """
                    select new net.likelion.bebc25.fixgo.payment.dto.PaymentDto$PaymentListResponse(
                        b.id, r.receptionNumber, c.name, b.amount, b.status, a.method
                    )
                    from PaymentBill b
                    join b.asReception r
                    join r.customer c
                    left join PaymentAttempt a
                        on a.paymentBill = b
                       and a.status = net.likelion.bebc25.fixgo.payment.entity.PaymentAttemptStatus.DONE
                    where (:status is null or b.status = :status)
                      and (:keyword is null
                           or r.receptionNumber like concat('%', :keyword, '%')
                           or c.name like concat('%', :keyword, '%'))
                    order by b.id desc
                    """,
            countQuery = """
                    select count(b)
                    from PaymentBill b
                    join b.asReception r
                    join r.customer c
                    where (:status is null or b.status = :status)
                      and (:keyword is null
                           or r.receptionNumber like concat('%', :keyword, '%')
                           or c.name like concat('%', :keyword, '%'))
                    """
    )
    Page<PaymentDto.PaymentListResponse> findPaymentList(
            @Param("keyword") String keyword,
            @Param("status") PaymentBillStatus status,
            Pageable pageable
    );

    @Query("""
            select new net.likelion.bebc25.fixgo.payment.dto.PaymentDto$ReceptionInfo(
                r.receptionNumber, c.name, m.name, r.symptom, r.symptomDetail
            )
            from AsReception r
            join r.customer c
            join r.product p
            left join ProductModel m on m.id = p.productModelId
            where r.id = :asReceptionId
            """)
    Optional<PaymentDto.ReceptionInfo> findReceptionInfo(@Param("asReceptionId") Long asReceptionId);
}
