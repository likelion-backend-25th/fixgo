package net.likelion.bebc25.fixgo.shipment.repository;

import jakarta.persistence.LockModeType;
import net.likelion.bebc25.fixgo.shipment.entity.Shipment;
import net.likelion.bebc25.fixgo.shipment.entity.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    // 동일한 A/S 접수에 배송 내역이 존재하는지 확인
    boolean existsByAsReceptionId(Long asReceptionId);

    // 배송 또는 방문 수령 내역 조회
    Optional<Shipment> findByAsReceptionId(Long asReceptionId);

    // 락 적용 - 동일한 배송을 여러 직원이 동시에 수정을 막기 위함
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Shipment> findWithLockById(@Param("id") Long id);

    // 배송 상태 조회 - 스케줄러 이용
    List<Shipment> findByStatus(ShipmentStatus status);

}
