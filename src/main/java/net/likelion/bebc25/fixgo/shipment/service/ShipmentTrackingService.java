package net.likelion.bebc25.fixgo.shipment.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.shipment.client.SweetTrackerClient;
import net.likelion.bebc25.fixgo.shipment.dto.SweetTrackerDto;
import net.likelion.bebc25.fixgo.shipment.entity.ReturnMethod;
import net.likelion.bebc25.fixgo.shipment.entity.Shipment;
import net.likelion.bebc25.fixgo.shipment.repository.ShipmentRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShipmentTrackingService {
    private final ShipmentRepository shipmentRepository;
    private final SweetTrackerClient sweetTrackerClient;
    private final ShipmentService shipmentService;

    // SweetTracker 배송 추적 및 이력 저장
    public SweetTrackerDto.TrackingResponse trackShipment(Long shipmentId) {

        // 배송 정보 조회
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("배송 내역을 찾을 수 없습니다."));


        if (shipment.getReturnMethod() != ReturnMethod.DELIVERY) {
            throw new IllegalStateException("택배 배송만 조회할 수 있습니다.");
        }
        String courierCode = shipment.getCourierCode();
        String trackingNumber = shipment.getTrackingNumber();

        if (courierCode == null || courierCode.isBlank() || trackingNumber == null || trackingNumber.isBlank()) {
            throw new IllegalStateException("택배사 코드와 운송장번호가 필요합니다.");
        }

        // 외부 API호출 - DB 트챈잭션 밖에서 실행
        SweetTrackerDto.TrackingResponse response = sweetTrackerClient.getTrackingInfo(courierCode, trackingNumber);

        if (response == null) {
            throw new IllegalStateException("배송 조회 응답이 없습니다.");
        }

        // 배송 이력 저장
        shipmentService.saveTrackingEvents(
                shipmentId,
                courierCode,
                trackingNumber,
                response);

        return response;

    }
}
