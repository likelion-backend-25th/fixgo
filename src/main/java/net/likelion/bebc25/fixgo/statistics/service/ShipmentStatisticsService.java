package net.likelion.bebc25.fixgo.statistics.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.shipment.entity.ShipmentStatus;
import net.likelion.bebc25.fixgo.statistics.dto.ShipmentStatisticsDto;
import net.likelion.bebc25.fixgo.statistics.repository.ShipmentStatisticsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShipmentStatisticsService {

    private final ShipmentStatisticsRepository shipmentStatisticsRepository;

    // 배송대기, 배송 중, 완료 건수 조회
    public ShipmentStatisticsDto getShipmentStatistics(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("조회 시작일과 종료일은 필수입니다.");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작일은 종료일보다 늦을 수 없습니다.");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        ShipmentStatisticsRepository.ShipmentStatistics statistics = shipmentStatisticsRepository.countByStatus(
                ShipmentStatus.READY, ShipmentStatus.SHIPPING, ShipmentStatus.DELIVERED, ShipmentStatus.PICKED_UP,
                startDateTime, endDateTime);

        return new ShipmentStatisticsDto(startDate, endDate, statistics.getReadyCount(), statistics.getShippingCount(),
                                         statistics.getCompletedCount());
    }
}