package net.likelion.bebc25.fixgo.statistics.repository;

import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ReceptionStatisticsRepository extends Repository<AsReception, Long> {

    // 기간 내 접수 건수와 그중 완료 건수
    @Query("""
            SELECT
                count(ar) as receivedCount,
                cast(coalesce(sum(
                    case when ar.status = :completedStatus then 1 else 0 end
                ), 0) as long) as completedCount
            FROM AsReception ar
            WHERE ar.createdAt >= :startDateTime
              AND ar.createdAt < :endDateTime
            """)
    ReceptionStatistics countByPeriod(
            @Param("completedStatus") AsReceptionStatus completedStatus,
            @Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime
    );

    interface ReceptionStatistics {
        Long getReceivedCount();

        Long getCompletedCount();
    }
}