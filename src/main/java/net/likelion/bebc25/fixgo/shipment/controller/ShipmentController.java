package net.likelion.bebc25.fixgo.shipment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.shipment.dto.ShipmentDto;
import net.likelion.bebc25.fixgo.shipment.dto.SweetTrackerDto;
import net.likelion.bebc25.fixgo.shipment.service.ShipmentService;
import net.likelion.bebc25.fixgo.shipment.service.ShipmentShippingService;
import net.likelion.bebc25.fixgo.shipment.service.ShipmentTrackingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shipments")
@PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
public class ShipmentController {
    private final ShipmentService shipmentService;
    private final ShipmentTrackingService shipmentTrackingService;
    private final ShipmentShippingService shipmentShippingService;

    // 택배 배송 등록
    @PostMapping
    public ResponseEntity<ShipmentDto.Response> createShipment(
            @Valid @RequestBody ShipmentDto.CreateRequest request
    ) {
        ShipmentDto.Response response = shipmentService.createShipment(request);

        return ResponseEntity.created(URI.create("/api/v1/shipments/" + response.id())).body(response);
    }

    // 방문 수령 등록
    @PostMapping("/pickups")
    public ResponseEntity<ShipmentDto.Response> createPickup(
            @Valid @RequestBody ShipmentDto.PickupCreateRequest request
    ) {
        ShipmentDto.Response response = shipmentService.createPickup(request.asReceptionId());

        return ResponseEntity.created(URI.create("/api/v1/shipments/" + response.id())).body(response);
    }

    // 배송 단건 조회
    @GetMapping("/{shipmentId}")
    public ShipmentDto.Response getShipment(@PathVariable Long shipmentId) {
        return shipmentService.getShipment(shipmentId);
    }

    // A/S 접수별 배송 조회
    @GetMapping("/receptions/{asReceptionId}")
    public ShipmentDto.Response getShipmentByReception(@PathVariable Long asReceptionId) {
        return shipmentService.getShipmentByReception(asReceptionId);
    }

    // 배송 상세 및 이벤트 조회
    @GetMapping("/{shipmentId}/details")
    public ShipmentDto.DetailResponse getShipmentDetail(@PathVariable Long shipmentId) {
        return shipmentService.getShipmentDetail(shipmentId);
    }

    // 택배사 및 운송장번호 수정
    @PatchMapping("/{shipmentId}/tracking-info")
    public ShipmentDto.Response updateTrackingInfo(
            @PathVariable Long shipmentId,
            @Valid @RequestBody ShipmentDto.UpdateRequest request
    ) {
        return shipmentService.updateTrackingInfo(shipmentId, request);
    }

    // 배송 시작 및 이메일 알림 발송
    @PatchMapping("/{shipmentId}/shipping")
    public ShipmentDto.Response markShipping(@PathVariable Long shipmentId) {
        return shipmentShippingService.startShipping(shipmentId);
    }

    // 배송 완료(수동 처리)
    @PatchMapping("/{shipmentId}/delivered")
    public ShipmentDto.Response markDelivered(@PathVariable Long shipmentId) {
        return shipmentService.markDelivered(shipmentId);
    }

    // 방문 수령 완료
    @PatchMapping("/{shipmentId}/pickup")
    public ShipmentDto.Response markPickedUp(@PathVariable Long shipmentId) {
        return shipmentService.markPickedUp(shipmentId);
    }

    // SweetTracker 배송 조회 및 이력 갱신
    @PostMapping("/{shipmentId}/tracking/refresh")
    public SweetTrackerDto.TrackingResponse trackShipment(@PathVariable Long shipmentId) {
        return shipmentTrackingService.trackShipment(shipmentId);
    }

    // 배송 및 방문 수령 통합 목록 조회
    @GetMapping("/management")
    public Page<ShipmentDto.ManagementResponse> getShipmentManagementList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "ALL") String returnMethod,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return shipmentService.getShipmentManagementList(
                keyword,
                status,
                returnMethod,
                PageRequest.of(page, size)
        );
    }

}
