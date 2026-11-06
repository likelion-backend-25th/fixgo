package net.likelion.bebc25.fixgo.shipment.repository;

import net.likelion.bebc25.fixgo.shipment.entity.ShipmentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShipmentEventRepository extends JpaRepository<ShipmentEvent, Long> {


    // 배송 이력 시간순 조회
    List<ShipmentEvent> findByShipmentIdOrderByOccurredAtAsc(@Param("shipmentId") Long shipmentId);

    // 동일한 배송 이벤트 중복 저장 안되게
    boolean existsByEventKey(String eventKey);
}
