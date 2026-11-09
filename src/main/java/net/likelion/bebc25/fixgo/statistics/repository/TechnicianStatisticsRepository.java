package net.likelion.bebc25.fixgo.statistics.repository;

import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttemptStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TechnicianStatisticsRepository extends Repository<AsReception, Long> {

    // 기사별 배정, 완료, 진행 건수
    @Query("""
            SELECT
                m.id AS technicianId,
                m.name AS technicianName,
                COUNT(ar) AS assignedCount,
                SUM(
                    CASE
                        WHEN ar.status = :completedStatus THEN 1
                        ELSE 0
                    END
                ) AS completedCount,
                SUM(
                    CASE
                        WHEN ar.status <> :completedStatus THEN 1
                        ELSE 0
                    END
                ) AS inProgressCount
            FROM Member m
            LEFT JOIN AsReception ar
                ON ar.technician = m
                AND ar.createdAt >= :startDateTime
                AND ar.createdAt < :endDateTime
            WHERE m.role = :technicianRole
            GROUP BY m.id, m.name
            ORDER BY m.name
            """)
    List<TechnicianStatistics> findTechnicianStatistics(
            @Param("technicianRole") Role technicianRole,
            @Param("completedStatus") AsReceptionStatus completedStatus,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    @Query("""
            SELECT
                ar.technician.id AS technicianId,
                COALESCE(SUM(pa.amount), 0) AS revenue
            FROM AsReception ar
            JOIN PaymentBill pb
                ON pb.asReceptionId = ar.id
            JOIN PaymentAttempt pa
                ON pa.paymentBill = pb
            WHERE pa.status = :paymentStatus
              AND pa.approvedAt >= :startDateTime
              AND pa.approvedAt < :endDateTime
            GROUP BY ar.technician.id
            """)
    List<TechnicianRevenue> findTechnicianRevenue(
            @Param("paymentStatus") PaymentAttemptStatus paymentStatus,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    // 기사별 평균 수리 소요 시간 (초)
    @Query(value = """
            SELECT
                r.technician_id AS technicianId,
                AVG(
                    TIMESTAMPDIFF(
                        SECOND,
                        start_history.created_at,
                        complete_history.created_at
                    )
                ) AS averageDurationSeconds
            FROM as_status_history complete_history
            JOIN repair r
                ON r.as_reception_id = complete_history.as_reception_id
            JOIN as_status_history start_history
                ON start_history.as_reception_id = complete_history.as_reception_id
                AND start_history.to_status = 'REPAIRING'
                AND start_history.created_at < complete_history.created_at
            WHERE complete_history.from_status = 'REPAIRING'
              AND complete_history.to_status = 'COMPLETED'
              AND complete_history.created_at >= :startDateTime
              AND complete_history.created_at < :endDateTime
              AND start_history.created_at = (
                  SELECT MAX(previous_history.created_at)
                  FROM as_status_history previous_history
                  WHERE previous_history.as_reception_id = complete_history.as_reception_id
                    AND previous_history.to_status = 'REPAIRING'
                    AND previous_history.created_at < complete_history.created_at
              )
            GROUP BY r.technician_id
            """, nativeQuery = true)
    List<TechnicianAverageDuration> findTechnicianAverageDurations(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    // 기사별 건수 조회 결과
    interface TechnicianStatistics {
        Long getTechnicianId();

        String getTechnicianName();

        Long getAssignedCount();

        Long getCompletedCount();

        Long getInProgressCount();
    }

    // 기사별 매출 조회 결과
    interface TechnicianRevenue {
        Long getTechnicianId();

        Long getRevenue();
    }

    // 기사별 평균 수리 소요 시간 조회 결과
    interface TechnicianAverageDuration {
        Long getTechnicianId();

        Double getAverageDurationSeconds();
    }
}