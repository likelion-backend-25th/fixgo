package net.likelion.bebc25.fixgo.statistics.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.shipment.entity.ShipmentStatus;
import net.likelion.bebc25.fixgo.statistics.dto.ShipmentStatisticsDto;
import net.likelion.bebc25.fixgo.statistics.repository.ShipmentStatisticsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShipmentStatisticsService {

    private final ShipmentStatisticsRepository shipmentStatisticsRepository;

    // 배송대기, 배송 중, 완료 건수 조회
    public ShipmentStatisticsDto getShipmentStatistics() {
        ShipmentStatisticsRepository.ShipmentStatistics statistics = shipmentStatisticsRepository.countByStatus(
                ShipmentStatus.READY, ShipmentStatus.SHIPPING, ShipmentStatus.DELIVERED, ShipmentStatus.PICKED_UP);

        return new ShipmentStatisticsDto(statistics.getReadyCount(), statistics.getShippingCount(),
                                         statistics.getCompletedCount());
    }
}