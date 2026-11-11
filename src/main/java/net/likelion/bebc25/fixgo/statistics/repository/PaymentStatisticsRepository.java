package net.likelion.bebc25.fixgo.statistics.repository;

import net.likelion.bebc25.fixgo.payment.entity.PaymentAttempt;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttemptStatus;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBillStatus;
import net.likelion.bebc25.fixgo.payment.entity.PaymentRefundStatus;
import net.likelion.bebc25.fixgo.statistics.dto.PaymentStatisticsDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentStatisticsRepository extends Repository<PaymentAttempt, Long> {

    // 조회 기간 내 승인 완료된 결제 금액
    @Query("""
            SELECT COALESCE(SUM(pa.amount), 0)
            FROM PaymentAttempt pa
            WHERE pa.status = :status
              AND pa.approvedAt >= :startDateTime
              AND pa.approvedAt < :endDateTime
            """)
    Long sumApprovedAmount(
            @Param("status") PaymentAttemptStatus status, @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    // 조회 기간 내 환불 완료 금액
    @Query("""
            SELECT COALESCE(SUM(pr.cancelAmount), 0)
            FROM PaymentRefund pr
            WHERE pr.status = :refundStatus
              AND pr.canceledAt >= :startDateTime
              AND pr.canceledAt < :endDateTime
            """)
    Long sumRefundAmount(
            @Param("refundStatus") PaymentRefundStatus refundStatus,
            @Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime
    );

    // 현재 미입금 건수
    @Query("""
            SELECT COUNT(pb)
            FROM PaymentBill pb
            WHERE pb.status = :billStatus
            """)
    Long countUnpaidBills(
            @Param("billStatus") PaymentBillStatus billStatus
    );

    // 현재 미입금 청구서. 모델명은 제품명, 청구일은 청구서 생성 시각이다.
    @Query("""
            select new net.likelion.bebc25.fixgo.statistics.dto.PaymentStatisticsDto$UnpaidBillResponse(
                b.id, r.receptionNumber, c.name, m.name, b.amount, b.createdAt, b.status
            )
            from PaymentBill b
            join b.asReception r
            join r.customer c
            join r.product p
            left join ProductModel m on m.id = p.productModelId
            where b.status = net.likelion.bebc25.fixgo.payment.entity.PaymentBillStatus.UNPAID
            order by b.id desc
            """)
    List<PaymentStatisticsDto.UnpaidBillResponse> findUnpaidBills();
}