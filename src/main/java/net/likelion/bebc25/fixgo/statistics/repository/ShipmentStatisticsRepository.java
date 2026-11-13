package net.likelion.bebc25.fixgo.statistics.repository;

import net.likelion.bebc25.fixgo.shipment.entity.Shipment;
import net.likelion.bebc25.fixgo.shipment.entity.ShipmentStatus;
import net.likelion.bebc25.fixgo.statistics.dto.ShipmentStatisticsDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ShipmentStatisticsRepository extends Repository<Shipment, Long> {

    // 배송대기, 배송 중, 완료 건수
    @Query("""
            SELECT
                cast(coalesce(sum(
                    case when s.status = :readyStatus then 1 else 0 end
                ), 0) as long) as readyCount,
                cast(coalesce(sum(
                    case when s.status = :shippingStatus then 1 else 0 end
                ), 0) as long) as shippingCount,
                cast(coalesce(sum(
                    case when s.status = :deliveredStatus or s.status = :pickedUpStatus then 1 else 0 end
                ), 0) as long) as completedCount
            FROM Shipment s
            WHERE s.createdAt >= :startDateTime
              AND s.createdAt < :endDateTime
            """)
    ShipmentStatistics countByStatus(
            @Param("readyStatus") ShipmentStatus readyStatus, @Param("shippingStatus") ShipmentStatus shippingStatus,
            @Param("deliveredStatus") ShipmentStatus deliveredStatus,
            @Param("pickedUpStatus") ShipmentStatus pickedUpStatus,
            @Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime
    );

    // 기간 안 등록된 배송. 일자는 배송 생성 시각이다.
    @Query("""
            select new net.likelion.bebc25.fixgo.statistics.dto.ShipmentStatisticsDto$ShipmentHistoryResponse(
                s.id, r.id, r.receptionNumber, c.name, s.returnMethod, s.status, s.createdAt
            )
            from Shipment s
            join s.asReception r
            join r.customer c
            where s.createdAt >= :startDateTime
              and s.createdAt < :endDateTime
            order by s.createdAt desc, s.id desc
            """)
    List<ShipmentStatisticsDto.ShipmentHistoryResponse> findShipmentHistory(
            @Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime
    );

    interface ShipmentStatistics {
        Long getReadyCount();

        Long getShippingCount();

        Long getCompletedCount();
    }
}