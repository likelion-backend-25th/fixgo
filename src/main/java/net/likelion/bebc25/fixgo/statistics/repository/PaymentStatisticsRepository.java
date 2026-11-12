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

    // 기간 안 승인된 입금. 일자는 승인 시각이다.
    @Query("""
            select new net.likelion.bebc25.fixgo.statistics.dto.PaymentStatisticsDto$PaymentHistoryResponse(
                b.id, r.receptionNumber, c.name, b.amount, b.status, a.approvedAt
            )
            from PaymentAttempt a
            join a.paymentBill b
            join b.asReception r
            join r.customer c
            where b.status = net.likelion.bebc25.fixgo.payment.entity.PaymentBillStatus.PAID
              and a.status = net.likelion.bebc25.fixgo.payment.entity.PaymentAttemptStatus.DONE
              and a.approvedAt >= :startDateTime
              and a.approvedAt < :endDateTime
            order by a.approvedAt desc
            """)
    List<PaymentStatisticsDto.PaymentHistoryResponse> findPaidHistory(
            @Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime
    );

    // 기간 안 만들어진 미입금. 일자는 청구 시각이다.
    @Query("""
            select new net.likelion.bebc25.fixgo.statistics.dto.PaymentStatisticsDto$PaymentHistoryResponse(
                b.id, r.receptionNumber, c.name, b.amount, b.status, b.createdAt
            )
            from PaymentBill b
            join b.asReception r
            join r.customer c
            where b.status = net.likelion.bebc25.fixgo.payment.entity.PaymentBillStatus.UNPAID
              and b.createdAt >= :startDateTime
              and b.createdAt < :endDateTime
            order by b.createdAt desc
            """)
    List<PaymentStatisticsDto.PaymentHistoryResponse> findUnpaidHistory(
            @Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime
    );

    // 기간 안 완료된 환불. 일자는 환불 시각이다.
    @Query("""
            select new net.likelion.bebc25.fixgo.statistics.dto.PaymentStatisticsDto$PaymentHistoryResponse(
                b.id, r.receptionNumber, c.name, coalesce(pr.cancelAmount, 0), b.status, pr.canceledAt
            )
            from PaymentRefund pr
            join pr.paymentAttempt a
            join a.paymentBill b
            join b.asReception r
            join r.customer c
            where b.status = net.likelion.bebc25.fixgo.payment.entity.PaymentBillStatus.REFUNDED
              and pr.status = net.likelion.bebc25.fixgo.payment.entity.PaymentRefundStatus.DONE
              and pr.canceledAt >= :startDateTime
              and pr.canceledAt < :endDateTime
            order by pr.canceledAt desc
            """)
    List<PaymentStatisticsDto.PaymentHistoryResponse> findRefundHistory(
            @Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime
    );
}