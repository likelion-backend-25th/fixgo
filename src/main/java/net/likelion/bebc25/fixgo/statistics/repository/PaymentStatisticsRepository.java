package net.likelion.bebc25.fixgo.statistics.repository;

import net.likelion.bebc25.fixgo.payment.entity.PaymentAttempt;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttemptStatus;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBillStatus;
import net.likelion.bebc25.fixgo.payment.entity.PaymentRefundStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

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
}