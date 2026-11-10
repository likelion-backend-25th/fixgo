package net.likelion.bebc25.fixgo.as.repository;

import jakarta.persistence.LockModeType;
import net.likelion.bebc25.fixgo.as.dto.AsReceptionDto;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.shipment.dto.ShipmentDto;
import net.likelion.bebc25.fixgo.shipment.entity.ReturnMethod;
import net.likelion.bebc25.fixgo.shipment.entity.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AsReceptionRepository extends JpaRepository<AsReception, Long> {
    @Query(value = """
            select new net.likelion.bebc25.fixgo.as.dto.AsReceptionDto$ListResponse(
                r.id,
                r.receptionNumber,
                r.createdAt,
                r.customer.name,
                r.customer.phone,
                (select pm.modelName from ProductModel pm where pm.id = r.product.productModelId),
                r.status,
                rp.status,
                t.name
            )
            from AsReception r
            join r.technician t
            left join Repair rp on rp.asReception = r
            where (:keyword is null
                or lower(r.receptionNumber) like lower(concat('%', :keyword, '%'))
                or lower(r.customer.name) like lower(concat('%', :keyword, '%'))
                or lower(r.symptom) like lower(concat('%', :keyword, '%')))
              and (:status is null or r.status = :status)
              and (:technicianId is null or t.id = :technicianId)
            """, countQuery = """
            select count(r)
            from AsReception r
            join r.technician t
            where (:keyword is null
                or lower(r.receptionNumber) like lower(concat('%', :keyword, '%'))
                or lower(r.customer.name) like lower(concat('%', :keyword, '%'))
                or lower(r.symptom) like lower(concat('%', :keyword, '%')))
              and (:status is null or r.status = :status)
              and (:technicianId is null or t.id = :technicianId)
            """)
    Page<AsReceptionDto.ListResponse> search(
            @Param("keyword") String keyword,
            @Param("status") AsReceptionStatus status,
            @Param("technicianId") Long technicianId,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AsReception> findByIdAndStatusNotIn(Long id, List<AsReceptionStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AsReception a WHERE a.id = :id")
    Optional<AsReception> findByIdWithLock(@Param("id") Long id);


    // 접수 ID 와 담당 기사 ID 함께 확인
    Optional<AsReception> findByIdAndTechnician_Id(Long id, Long technicianId);


    // 배송 및 방문 수령 통합 목록 조회
    @Query(value = """
            SELECT new net.likelion.bebc25.fixgo.shipment.dto.ShipmentDto$ManagementResponse(
                a.id,s.id,a.receptionNumber,c.name,c.phone,a.returnMethod,a.status,s.status,s.courier,s.trackingNumber,s.shippedAt)
            FROM AsReception a
            JOIN a.customer c
            LEFT JOIN Shipment s ON s.asReception = a
            WHERE ((a.returnMethod = :delivery
                    AND a.status = :awaitingShipment
                    AND s.id IS NULL)
                OR (a.returnMethod = :pickup
                    AND a.status = :awaitingPickup
                    AND s.id IS NULL)
                OR s.id IS NOT NULL)
            AND (:status = 'ALL'
                OR (:status = 'AWAITING_SHIPMENT'
                    AND a.returnMethod = :delivery
                    AND a.status = :awaitingShipment
                    AND s.id IS NULL)
                OR (:status = 'READY'
                    AND a.returnMethod = :delivery
                    AND a.status = :awaitingShipment
                    AND s.status = :ready)
                OR (:status = 'SHIPPING'
                    AND a.returnMethod = :delivery
                    AND s.status = :shipping)
                OR (:status = 'DELIVERED'
                    AND a.returnMethod = :delivery
                    AND s.status = :delivered)
                OR (:status = 'AWAITING_PICKUP'
                    AND a.returnMethod = :pickup
                    AND a.status = :awaitingPickup
                    AND (s.id IS NULL OR s.status = :ready))
                OR (:status = 'PICKED_UP'
                    AND a.returnMethod = :pickup
                    AND s.status = :pickedUp))
                ORDER BY a.id DESC
            """,
            countQuery = """
                    SELECT COUNT(a.id)
                    FROM AsReception a
                    LEFT JOIN Shipment s ON s.asReception = a
                    WHERE ((a.returnMethod = :delivery
                            AND a.status = :awaitingShipment
                            AND s.id IS NULL)
                        OR (a.returnMethod = :pickup
                            AND a.status = :awaitingPickup
                            AND s.id IS NULL)
                        OR s.id IS NOT NULL)
                    AND (:status = 'ALL'
                        OR (:status = 'AWAITING_SHIPMENT'
                            AND a.returnMethod = :delivery
                            AND a.status = :awaitingShipment
                            AND s.id IS NULL)
                        OR (:status = 'READY'
                            AND a.returnMethod = :delivery
                            AND a.status = :awaitingShipment
                            AND s.status = :ready)
                        OR (:status = 'SHIPPING'
                            AND a.returnMethod = :delivery
                            AND s.status = :shipping)
                        OR (:status = 'DELIVERED'
                            AND a.returnMethod = :delivery
                            AND s.status = :delivered)
                        OR (:status = 'AWAITING_PICKUP'
                            AND a.returnMethod = :pickup
                            AND a.status = :awaitingPickup
                            AND (s.id IS NULL OR s.status = :ready))
                        OR (:status = 'PICKED_UP'
                            AND a.returnMethod = :pickup
                            AND s.status = :pickedUp)
                    )""")
    Page<ShipmentDto.ManagementResponse> findShipmentManagementList(
            @Param("status") String status,
            @Param("awaitingShipment") AsReceptionStatus awaitingShipment,
            @Param("awaitingPickup") AsReceptionStatus awaitingPickup,
            @Param("delivery") ReturnMethod delivery,
            @Param("pickup") ReturnMethod pickup,
            @Param("ready") ShipmentStatus ready,
            @Param("shipping") ShipmentStatus shipping,
            @Param("delivered") ShipmentStatus delivered,
            @Param("pickedUp") ShipmentStatus pickedUp,
            Pageable pageable
    );

}
