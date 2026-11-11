package net.likelion.bebc25.fixgo.shipment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Shipment API", description = "배송/방문 수령 등록·조회·상태 변경 API")
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
    @Operation(summary = "택배 배송 등록", description = "택배 배송을 등록한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "배송 등록 성공"),
    })
    public ResponseEntity<ShipmentDto.Response> createShipment(
            @Valid @RequestBody ShipmentDto.CreateRequest request
    ) {
        ShipmentDto.Response response = shipmentService.createShipment(request);

        return ResponseEntity.created(URI.create("/api/v1/shipments/" + response.id())).body(response);
    }

    // 방문 수령 등록
    @PostMapping("/pickups")
    @Operation(summary = "방문 수령 등록", description = "방문 수령을 등록한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "방문 수령 등록 성공"),
    })
    public ResponseEntity<ShipmentDto.Response> createPickup(
            @Valid @RequestBody ShipmentDto.PickupCreateRequest request
    ) {
        ShipmentDto.Response response = shipmentService.createPickup(request.asReceptionId());

        return ResponseEntity.created(URI.create("/api/v1/shipments/" + response.id())).body(response);
    }

    // 배송 단건 조회
    @GetMapping("/{shipmentId}")
    @Operation(summary = "배송 단건 조회", description = "배송 ID로 배송 정보를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배송 조회 성공"),
    })
    public ShipmentDto.Response getShipment(@PathVariable Long shipmentId) {
        return shipmentService.getShipment(shipmentId);
    }

    // A/S 접수별 배송 조회
    @GetMapping("/receptions/{asReceptionId}")
    @Operation(summary = "접수별 배송 조회", description = "A/S 접수별 배송 정보를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배송 조회 성공"),
    })
    public ShipmentDto.Response getShipmentByReception(@PathVariable Long asReceptionId) {
        return shipmentService.getShipmentByReception(asReceptionId);
    }

    // 배송 상세 및 이벤트 조회
    @GetMapping("/{shipmentId}/details")
    @Operation(summary = "배송 상세 조회", description = "배송 상세 및 이벤트 이력을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배송 상세 조회 성공"),
    })
    public ShipmentDto.DetailResponse getShipmentDetail(@PathVariable Long shipmentId) {
        return shipmentService.getShipmentDetail(shipmentId);
    }

    // 택배사 및 운송장번호 수정
    @PatchMapping("/{shipmentId}/tracking-info")
    @Operation(summary = "운송장 정보 수정", description = "택배사 및 운송장번호를 수정한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "운송장 정보 수정 성공"),
    })
    public ShipmentDto.Response updateTrackingInfo(
            @PathVariable Long shipmentId,
            @Valid @RequestBody ShipmentDto.UpdateRequest request
    ) {
        return shipmentService.updateTrackingInfo(shipmentId, request);
    }

    // 배송 시작 및 이메일 알림 발송
    @PatchMapping("/{shipmentId}/shipping")
    @Operation(summary = "배송 시작", description = "배송을 시작하고 이메일 알림을 발송한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배송 시작 성공"),
    })
    public ShipmentDto.Response markShipping(@PathVariable Long shipmentId) {
        return shipmentShippingService.startShipping(shipmentId);
    }

    // 배송 완료(수동 처리)
    @PatchMapping("/{shipmentId}/delivered")
    @Operation(summary = "배송 완료", description = "배송 완료를 수동 처리한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배송 완료 처리 성공"),
    })
    public ShipmentDto.Response markDelivered(@PathVariable Long shipmentId) {
        return shipmentService.markDelivered(shipmentId);
    }

    // 방문 수령 완료
    @PatchMapping("/{shipmentId}/pickup")
    @Operation(summary = "방문 수령 완료", description = "방문 수령 완료를 처리한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "방문 수령 완료 처리 성공"),
    })
    public ShipmentDto.Response markPickedUp(@PathVariable Long shipmentId) {
        return shipmentService.markPickedUp(shipmentId);
    }

    // SweetTracker 배송 조회 및 이력 갱신
    @PostMapping("/{shipmentId}/tracking/refresh")
    @Operation(summary = "배송 추적 갱신", description = "SweetTracker로 배송을 조회하고 이력을 갱신한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배송 추적 갱신 성공"),
    })
    public SweetTrackerDto.TrackingResponse trackShipment(@PathVariable Long shipmentId) {
        return shipmentTrackingService.trackShipment(shipmentId);
    }

    // 배송 및 방문 수령 통합 목록 조회
    @GetMapping("/management")
    @Operation(summary = "배송 관리 목록 조회", description = "배송 및 방문 수령 통합 목록을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배송 관리 목록 조회 성공"),
    })
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
