package net.likelion.bebc25.fixgo.shipment.repository;

import net.likelion.bebc25.fixgo.shipment.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    // 동일한 A/S 접수에 배송 내역이 존재하는지 확인
    boolean existsByAsReceptionId(Long asReceptionId);

}
