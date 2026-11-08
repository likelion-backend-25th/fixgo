package net.likelion.bebc25.fixgo.shipment.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.repository.AsReceptionRepository;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.shipment.dto.ShipmentDto;
import net.likelion.bebc25.fixgo.shipment.entity.ReturnMethod;
import net.likelion.bebc25.fixgo.shipment.entity.Shipment;
import net.likelion.bebc25.fixgo.shipment.entity.ShipmentEvent;
import net.likelion.bebc25.fixgo.shipment.entity.ShipmentEventStatus;
import net.likelion.bebc25.fixgo.shipment.repository.ShipmentEventRepository;
import net.likelion.bebc25.fixgo.shipment.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;
    private final ShipmentEventRepository shipmentEventRepository;
    private final AsReceptionRepository asReceptionRepository;

    // 택배 배송 등록
    @Transactional
    public ShipmentDto.Response createShipment(ShipmentDto.CreateRequest request) {

        AsReception asReception = getAsReceptionWithLock(request.asReceptionId());

        validateReception(
                asReception,
                ReturnMethod.DELIVERY,
                AsReceptionStatus.AWAITING_SHIPMENT);

        validateDuplicate(asReception.getId());

        Customer customer = asReception.getCustomer();

        Shipment shipment = Shipment.create(
                asReception,
                customer.getName(),
                customer.getPhone(),
                customer.getBasicAddress(),
                customer.getDetailAddress(),
                null,
                request.courierCode(),
                request.trackingNumber());

        return ShipmentDto.Response.from(shipmentRepository.save(shipment));
    }

    // 방문 수령 등록
    @Transactional
    public ShipmentDto.Response createPickup(Long asReceptionId) {
        AsReception asReception = getAsReceptionWithLock(asReceptionId);

        validateReception(
                asReception,
                ReturnMethod.PICKUP,
                AsReceptionStatus.AWAITING_PICKUP);

        validateDuplicate(asReception.getId());

        Customer customer = asReception.getCustomer();

        Shipment shipment = Shipment.create(
                asReception,
                customer.getName(),
                customer.getPhone(),
                customer.getBasicAddress(),
                customer.getDetailAddress(),
                null,
                null,
                null);

        return ShipmentDto.Response.from(shipmentRepository.save(shipment));
    }

    // 택배사, 운송장 번호 수정
    @Transactional
    public ShipmentDto.Response updateTrackingInfo(Long shipmentId, ShipmentDto.UpdateRequest request) {
        Shipment shipment = getShipmentForUpdate(shipmentId);

        validateReception(
                shipment.getAsReception(),
                ReturnMethod.DELIVERY,
                AsReceptionStatus.AWAITING_SHIPMENT);

        shipment.updateTrackingInfo(
                null,
                request.courierCode(),
                request.trackingNumber());

        return ShipmentDto.Response.from(shipment);
    }

    // 배송 시작
    @Transactional
    public ShipmentDto.Response markShipping(Long shipmentId) {
        Shipment shipment = getShipmentForUpdate(shipmentId);

        validateReception(
                shipment.getAsReception(),
                ReturnMethod.DELIVERY,
                AsReceptionStatus.AWAITING_SHIPMENT);

        shipment.markShipping();

        return ShipmentDto.Response.from(shipment);
    }

    // 배송 완료
    @Transactional
    public ShipmentDto.Response markDelivered(Long shipmentId) {
        Shipment shipment = getShipmentForUpdate(shipmentId);
        AsReception asReception = shipment.getAsReception();

        validateReception(
                asReception,
                ReturnMethod.DELIVERY,
                AsReceptionStatus.AWAITING_SHIPMENT);

        shipment.markDelivered();
        // 배송 완료시 접수 -> 완료
        asReception.changeStatus(AsReceptionStatus.COMPLETED);

        return ShipmentDto.Response.from(shipment);
    }

    // 방문 수령 완료
    @Transactional
    public ShipmentDto.Response markPickedUp(Long shipmentId) {
        Shipment shipment = getShipmentForUpdate(shipmentId);
        AsReception asReception = shipment.getAsReception();

        validateReception(
                asReception,
                ReturnMethod.PICKUP,
                AsReceptionStatus.AWAITING_PICKUP);
        shipment.markPickedUp();
        asReception.changeStatus(AsReceptionStatus.COMPLETED);

        return ShipmentDto.Response.from(shipment);
    }

    // 배송 조회
    public ShipmentDto.Response getShipment(Long shipmentId) {
        Shipment shipment = findShipment(shipmentId);

        return ShipmentDto.Response.from(shipment);
    }

    // A/S 접수 배송 조회
    public ShipmentDto.Response getShipmentByReception(Long asReceptionId) {

        Shipment shipment = shipmentRepository.findByAsReceptionId(asReceptionId)
                .orElseThrow(() -> new IllegalArgumentException("배송 내역을 찾을 수 없습니다."));

        return ShipmentDto.Response.from(shipment);
    }

    // 배송 상세 및 배송 이벤트 조회
    public ShipmentDto.DetailResponse getShipmentDetail(Long shipmentId) {
        Shipment shipment = findShipment(shipmentId);

        List<ShipmentEvent> events = shipmentEventRepository.findByShipmentIdOrderByOccurredAtAsc(shipmentId);

        return ShipmentDto.DetailResponse.from(shipment, events);
    }

    // AS 접수 조회 및 락
    private AsReception getAsReceptionWithLock(long asReceptionId) {
        return asReceptionRepository.findByIdWithLock(asReceptionId)
                .orElseThrow(() -> new IllegalArgumentException("A/S 접수 내역을 찾을 수 없습니다."));
    }

    // 배송 조회
    private Shipment findShipment(Long shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("배송 내역을 찾을 수 없습니다."));
    }

    // 배송 조회 및 락
    private Shipment getShipmentWithLock(Long shipmentId) {
        return shipmentRepository.findWithLockById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("배송 내역을 찾을 수 없습니다."));
    }

    // A/S 접수 상태 및 수령 방식 검증
    private void validateReception(AsReception asReception, ReturnMethod returnMethod, AsReceptionStatus status) {
        if (asReception.getReturnMethod() != returnMethod) {
            throw new IllegalStateException("A/S 접수의 수령 방식이 일치하지 않습니다.");
        }

        if (asReception.getStatus() != status) {
            throw new IllegalStateException("현재 A/S접수 상태에서는 처리할 수 없습니다.");
        }
    }

    // 중복 배송 및 수령 등록 검증
    private void validateDuplicate(Long asReceptionId) {
        if (shipmentRepository.existsByAsReceptionId(asReceptionId)) {
            throw new IllegalStateException("이미 배송 또는 수령 내역이 등록되어 있습니다.");
        }
    }

    // A/S접수와 배송내역을 순서대로 락
    // 배송 완료와 수령완료를 하고 난 뒤 A/S접수 상태를 COMPLETED로 전환할때
    // 다른 곳에서도 수정 요청하지 않게 락하기 위함
    private Shipment getShipmentForUpdate(Long shipmentId) {

        Shipment existingShipment = findShipment(shipmentId);
        Long asReceptionId = existingShipment.getAsReception().getId();

        // A/S 접수 락
        getAsReceptionWithLock(asReceptionId);

        // 배송내역 락
        Shipment shipment = getShipmentWithLock(shipmentId);

        if (!shipment.getAsReception().getId().equals(asReceptionId)) {
            throw new IllegalStateException("배송과 A/S 접수의 연결 정보가 변경되었습니다.");
        }
        return shipment;
    }

    // 스마트 택배 배송 단계를 Fixgo 배송 상태로 변환
    private Optional<ShipmentEventStatus> convertStatus(Integer level) {
        if (level == null) {
            return Optional.empty();
        }

        // 운송장 등록, 집화 완료, 배송중, 배송 출발, 배송완료
        // REGISTERED, PICKED_UP, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED
        return switch (level) {
            case 1 -> Optional.of(ShipmentEventStatus.REGISTERED);
            case 2 -> Optional.of(ShipmentEventStatus.PICKED_UP);
            case 3, 4 -> Optional.of(ShipmentEventStatus.IN_TRANSIT);
            case 5 -> Optional.of(ShipmentEventStatus.OUT_FOR_DELIVERY);
            case 6 -> Optional.of(ShipmentEventStatus.DELIVERED);
            default -> Optional.empty();
        };

    }
}
