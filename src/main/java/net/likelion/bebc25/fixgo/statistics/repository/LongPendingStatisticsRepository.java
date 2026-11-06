package net.likelion.bebc25.fixgo.statistics.repository;

import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LongPendingStatisticsRepository extends Repository<AsReception, Long> {

    @Query("""
            SELECT
                ar.receptionNumber AS receptionNumber,
                c.name AS customerName,
                pm.modelName AS modelName,
                ar.status AS status,
                m.name AS technicianName,
                ar.statusChangedAt AS statusChangedAt
            FROM AsReception ar
            JOIN ar.customer c
            JOIN ar.product p
            JOIN ProductModel pm ON pm.id = p.productModelId
            LEFT JOIN ar.technician m
            WHERE ar.status <> :completedStatus
              AND ar.statusChangedAt <= :threshold
            ORDER BY ar.statusChangedAt ASC
            """)
    List<LongPendingStatistics> findLongPendingReceptions(
            @Param("completedStatus") AsReceptionStatus completedStatus,
            @Param("threshold") LocalDateTime threshold
    );

    interface LongPendingStatistics {
        String getReceptionNumber();

        String getCustomerName();

        String getModelName();

        AsReceptionStatus getStatus();

        String getTechnicianName();

        LocalDateTime getStatusChangedAt();
    }
}