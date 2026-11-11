package net.likelion.bebc25.fixgo.shipment.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.likelion.bebc25.fixgo.shipment.entity.Shipment;
import net.likelion.bebc25.fixgo.shipment.entity.ShipmentStatus;
import net.likelion.bebc25.fixgo.shipment.repository.ShipmentRepository;
import net.likelion.bebc25.fixgo.shipment.service.ShipmentTrackingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentScheduler {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentTrackingService shipmentTrackingService;

    // 매일 오전 10시 / 1분마다 // 초 분 시 일 월
    // 1분마다 스케줄러 하는 것은 테스트 용
//    @Scheduled(cron = "0 0 10 * * *", zone = "Asia/Seoul")
    // 0 * * * * * = 1분마다/0 0 * * * * = 1시간 마다
    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
    public void refreshShippingStatus() {
        List<Shipment> shipments = shipmentRepository.findByStatus(ShipmentStatus.SHIPPING);

        log.info("배송 자동 조회 시작. 대상={}건", shipments.size());

        for (Shipment shipment : shipments) {
            try {
                shipmentTrackingService.trackShipment(shipment.getId());
                log.info("배송 자동 조회 성공. shipmentId={}", shipment.getId());
            } catch (Exception e) {
                log.error("배송 자동 조회 실패. shipmentId={}", shipment.getId(), e);
            }
        }

        log.info("배송 자동 조회 종료");
    }
}
