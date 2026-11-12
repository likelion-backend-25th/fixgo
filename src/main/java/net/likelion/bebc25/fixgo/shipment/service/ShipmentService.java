package net.likelion.bebc25.fixgo.shipment.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.repository.AsReceptionRepository;
import net.likelion.bebc25.fixgo.as.service.AsStatusHistoryService;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.shipment.dto.ShipmentDto;
import net.likelion.bebc25.fixgo.shipment.dto.SweetTrackerDto;
import net.likelion.bebc25.fixgo.shipment.entity.*;
import net.likelion.bebc25.fixgo.shipment.repository.ShipmentEventRepository;
import net.likelion.bebc25.fixgo.shipment.repository.ShipmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;
    private final ShipmentEventRepository shipmentEventRepository;
    private final AsReceptionRepository asReceptionRepository;
    private final AsStatusHistoryService asStatusHistoryService;

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
                getCourierName(request.courierCode()),
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
                getCourierName(request.courierCode()),
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

        validateReception(asReception, ReturnMethod.DELIVERY, AsReceptionStatus.AWAITING_SHIPMENT);

        shipment.markDelivered();
        // 배송 완료시 접수 -> 완료
        completeReception(asReception, "택배 배송 완료");

        return ShipmentDto.Response.from(shipment);
    }

    // 방문 수령 완료
    @Transactional
    public ShipmentDto.Response markPickedUp(Long shipmentId) {
        Shipment shipment = getShipmentForUpdate(shipmentId);
        AsReception asReception = shipment.getAsReception();

        validateReception(asReception, ReturnMethod.PICKUP, AsReceptionStatus.AWAITING_PICKUP);
        shipment.markPickedUp();
        completeReception(asReception, "방문 수령 완료");

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

    // SweetTracker 배송 이력 저장
    @Transactional
    public void saveTrackingEvents(
            Long shipmentId,
            String courierCode,
            String trackingNumber,
            SweetTrackerDto.TrackingResponse response
    ) {
        // AsReception에 먼저 락 그 후 Shipment에 락
        Shipment shipment = getShipmentForUpdate(shipmentId);

        // 택배 배송만 추적
        if (shipment.getReturnMethod() != ReturnMethod.DELIVERY) {
            throw new IllegalStateException("택배 배송만 배송이력을 저장합니다.");
        }

        // 외부 API호출 중 운송장 정보가 변경됐는지 확인
        if (!courierCode.equals(shipment.getCourierCode())
                || !trackingNumber.equals(shipment.getTrackingNumber())) {
            throw new IllegalStateException("배송 조회 중 운송장 정보가 변경되었습니다.");
        }
        List<SweetTrackerDto.TrackingDetail> details = response.trackingDetails();

        if (details == null) {
            details = List.of();
        }

        for (SweetTrackerDto.TrackingDetail detail : details) {
            if (detail == null) {
                continue;
            }

            Optional<ShipmentEventStatus> status = convertStatus(detail.level());

            // 변환할 수 없는 배송 단계는 저장하지 않음
            if (status.isEmpty()) {
                continue;
            }
            String eventData = String.join("|",
                                           String.valueOf(shipmentId),
                                           courierCode,
                                           trackingNumber,
                                           String.valueOf(detail.time()),
                                           String.valueOf(detail.timeString()),
                                           String.valueOf(detail.level()),
                                           String.valueOf(detail.location()),
                                           String.valueOf(detail.kind()));
            String eventKey = UUID.nameUUIDFromBytes(eventData.getBytes(StandardCharsets.UTF_8)).toString();

            // 이미 저장된 이벤트는 건너뛰기
            if (shipmentEventRepository.existsByEventKey(eventKey)) {
                continue;
            }

            LocalDateTime occurredAt = parseOccurredAt(detail.timeString());

            String description = String.join(" / ",
                                             detail.kind() == null ? "" : detail.kind(),
                                             detail.location() == null ? "" : detail.location());

            ShipmentEvent event = ShipmentEvent.create(
                    shipment,
                    eventKey,
                    status.get(),
                    description,
                    occurredAt);
            shipmentEventRepository.save(event);
        }
        // 마지막 배송 조회 정보 갱신
        shipment.updateTrackingStatus(getLastTrackingStatus(response));

        // 배송 완료 시 상태 자동 변경
        completeDeliveryIfNeeded(shipment, response);


    }

    // SweetTracker 배송 이벤트 발생 시각 변환
    private LocalDateTime parseOccurredAt(String timeString) {
        if (timeString == null || timeString.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(
                    timeString,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("배송 이력의 날짜 형식이 올바르지 않습니다.", e);
        }
    }

    // SweetTracker 배송 완료시 A/S 접수까지 자동완료
    private void completeDeliveryIfNeeded(Shipment shipment, SweetTrackerDto.TrackingResponse response) {
        boolean delivered = Boolean.TRUE.equals(response.complete())
                && Integer.valueOf(6).equals(response.level());

        if (!delivered) {
            return;
        }

        AsReception asReception = shipment.getAsReception();

        if (shipment.getStatus() == ShipmentStatus.DELIVERED) {
            return;
        }

        if (shipment.getStatus() != ShipmentStatus.SHIPPING) {
            return;
        }

        validateReception(asReception,
                          ReturnMethod.DELIVERY,
                          AsReceptionStatus.AWAITING_SHIPMENT);

        shipment.markDelivered();

        completeReception(asReception, "택배 배송 완료");

    }

    // 스마트 택배 마지막 배송 상태
    private void completeReception(AsReception asReception, String reason) {
        AsReceptionStatus previousStatus = asReception.getStatus();
        asReception.changeStatus(AsReceptionStatus.COMPLETED);
        // 완료 API와 자동 조회 경로는 처리자 정보를 전달받지 않는다.
        asStatusHistoryService.record(
                asReception, previousStatus.name(), AsReceptionStatus.COMPLETED.name(), reason, null);
    }

    private String getLastTrackingStatus(SweetTrackerDto.TrackingResponse response) {
        List<SweetTrackerDto.TrackingDetail> details = response.trackingDetails();

        if (details == null || details.isEmpty()) {
            return "배송 정보 없음";
        }

        SweetTrackerDto.TrackingDetail latest = details.stream()
                .filter(detail -> detail != null && detail.time() != null)
                .max((a, b) -> Long.compare(a.time(), b.time()))
                .orElse(null);

        if (latest == null || latest.kind() == null || latest.kind().isBlank()) {
            return "배송 정보 없음";
        }

        return latest.kind();
    }

    // 배송 시작 이메일에 필요한 정보 조회
    public ShippingEmailData getShippingEmailData(Long shipmentId) {

        Shipment shipment = findShipment(shipmentId);

        if (shipment.getReturnMethod() != ReturnMethod.DELIVERY) {
            throw new IllegalStateException("택배 배송만 이메일 발송이 가능합니다.");
        }
        AsReception asReception = shipment.getAsReception();
        Customer customer = asReception.getCustomer();

        if (customer.getEmail() == null || customer.getEmail().isBlank()) {
            throw new IllegalStateException("고객 이메일이 등록되어 있지 않습니다.");
        }

        return new ShippingEmailData(
                asReception.getId(),
                customer.getEmail(),
                asReception.getReceptionNumber(),
                shipment.getCourier(),
                shipment.getCourierCode(),
                shipment.getTrackingNumber()
        );
    }

    // 택배사 코드를 택배사 이름으로 변환
    private String getCourierName(String courierCode) {
        return switch (courierCode) {
            case "01" -> "우체국택배";
            case "04" -> "CJ대한통운";
            case "05" -> "한진택배";
            case "06" -> "로젠택배";
            case "08" -> "롯데택배";
            default -> throw new IllegalArgumentException(
                    "지원하지 않는 택배사 코드입니다."
            );
        };
    }

    // 배송 및 방문 수령 통합 목록 조회
    public Page<ShipmentDto.ManagementResponse> getShipmentManagementList(
            String keyword,
            String status,
            String returnMethod,
            Pageable pageable
    ) {
        // 검색어 정리
        if (keyword == null || keyword.isBlank()) {
            keyword = null;
        } else {
            keyword = keyword.trim();
        }
        // 배송 상태 필터
        if (status == null) {
            status = "ALL";
        }

        status = status.trim().toUpperCase();

        // 배송 관리 상태 필터 검증
        switch (status) {
            case "ALL",
                 "AWAITING_SHIPMENT",
                 "READY",
                 "SHIPPING",
                 "DELIVERED",
                 "AWAITING_PICKUP",
                 "PICKED_UP" -> {
            }
            default -> throw new IllegalArgumentException(
                    "지원하지 않는 배송 관리 상태입니다."
            );
        }

        // 수령방법 필터
        if (returnMethod == null) {
            returnMethod = "ALL";
        }
        returnMethod = returnMethod.trim().toUpperCase(Locale.ROOT);

        switch (returnMethod) {
            case "ALL", "DELIVERY", "PICKUP" -> {
            }
            default -> throw new IllegalArgumentException(
                    "지원하지 않는 수령 방법입니다."
            );
        }

        return asReceptionRepository.findShipmentManagementList(
                keyword,
                status,
                returnMethod,
                AsReceptionStatus.AWAITING_SHIPMENT,
                AsReceptionStatus.AWAITING_PICKUP,
                ReturnMethod.DELIVERY,
                ReturnMethod.PICKUP,
                ShipmentStatus.READY,
                ShipmentStatus.SHIPPING,
                ShipmentStatus.DELIVERED,
                ShipmentStatus.PICKED_UP,
                pageable
        );
    }

    // 배송 알림 이메일 record
    public record ShippingEmailData(
            Long asReceptionId,
            String recipientEmail,
            String receptionNumber,
            String courier,
            String courierCode,
            String trackingNumber
    ) {}
}
