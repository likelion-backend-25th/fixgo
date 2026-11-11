package net.likelion.bebc25.fixgo.shipment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.shipment.entity.*;

import java.time.LocalDateTime;
import java.util.List;

public class ShipmentDto {

    // 배송 등록
    public record CreateRequest(
            @NotNull
            @Schema(description = "A/S 접수 ID", example = "1")
            Long asReceptionId,
            @NotBlank
            @Schema(description = "택배사 코드", example = "04")
            String courierCode,
            @NotBlank
            @Schema(description = "운송장번호", example = "123456789012")
            String trackingNumber
    ) {}

    // 방문 수령
    public record PickupCreateRequest(
            @NotNull
            @Schema(description = "A/S 접수 ID", example = "1")
            Long asReceptionId
    ) {}

    // 택배사 및 운송장번호 수정 요청
    public record UpdateRequest(
            @NotBlank
            @Schema(description = "택배사 코드", example = "04")
            String courierCode,
            @NotBlank
            @Schema(description = "운송장번호", example = "123456789012")
            String trackingNumber
    ) {}

    // 배송 조회
    public record Response(
            @Schema(description = "배송 ID", example = "1")
            Long id,
            @Schema(description = "A/S 접수 ID", example = "1")
            Long asReceptionId,
            @Schema(description = "접수번호", example = "AS-20240101-001")
            String receptionNumber,
            @Schema(description = "수령인 이름", example = "홍길동")
            String recipientName,
            @Schema(description = "수령인 연락처", example = "01012345678")
            String recipientPhone,
            @Schema(description = "반납 방식", example = "DELIVERY")
            ReturnMethod returnMethod,
            @Schema(description = "택배사명", example = "CJ대한통운")
            String courier,
            @Schema(description = "택배사 코드", example = "04")
            String courierCode,
            @Schema(description = "운송장번호", example = "123456789012")
            String trackingNumber,
            @Schema(description = "배송 상태", example = "SHIPPING")
            ShipmentStatus status,
            @Schema(description = "발송 시각", example = "2024-01-01T10:00:00")
            LocalDateTime shippedAt,
            @Schema(description = "완료 시각", example = "2024-01-03T15:00:00")
            LocalDateTime completedAt
    ) {
        public static Response from(Shipment shipment) {
            return new Response(
                    shipment.getId(),
                    shipment.getAsReception().getId(),
                    shipment.getAsReception().getReceptionNumber(),
                    shipment.getRecipientName(),
                    shipment.getRecipientPhone(),
                    shipment.getReturnMethod(),
                    shipment.getCourier(),
                    shipment.getCourierCode(),
                    shipment.getTrackingNumber(),
                    shipment.getStatus(),
                    shipment.getShippedAt(),
                    shipment.getCompletedAt()
            );
        }
    }

    // 배송 이력 조회
    public record EventResponse(
            @Schema(description = "이벤트 ID", example = "1")
            Long id,
            @Schema(description = "이벤트 상태", example = "IN_TRANSIT")
            ShipmentEventStatus status,
            @Schema(description = "이벤트 설명", example = "배송중")
            String description,
            @Schema(description = "발생 시각", example = "2024-01-02T09:00:00")
            LocalDateTime occurredAt
    ) {
        public static EventResponse from(ShipmentEvent event) {
            return new EventResponse(
                    event.getId(),
                    event.getStatus(),
                    event.getDescription(),
                    event.getOccurredAt()
            );
        }
    }

    // 배송 상세 조회
    public record DetailResponse(
            @Schema(description = "배송 ID", example = "1")
            Long id,
            @Schema(description = "A/S 접수 ID", example = "1")
            Long asReceptionId,
            @Schema(description = "접수번호", example = "AS-20240101-001")
            String receptionNumber,
            @Schema(description = "수령인 이름", example = "홍길동")
            String recipientName,
            @Schema(description = "수령인 연락처", example = "01012345678")
            String recipientPhone,
            @Schema(description = "반납 방식", example = "DELIVERY")
            ReturnMethod returnMethod,
            @Schema(description = "주소", example = "서울시 강남구")
            String address,
            @Schema(description = "상세 주소", example = "테헤란로 123")
            String addressDetail,
            @Schema(description = "택배사명", example = "CJ대한통운")
            String courier,
            @Schema(description = "택배사 코드", example = "04")
            String courierCode,
            @Schema(description = "운송장번호", example = "123456789012")
            String trackingNumber,
            @Schema(description = "배송 상태", example = "SHIPPING")
            ShipmentStatus status,
            @Schema(description = "발송 시각", example = "2024-01-01T10:00:00")
            LocalDateTime shippedAt,
            @Schema(description = "완료 시각", example = "2024-01-03T15:00:00")
            LocalDateTime completedAt,
            @Schema(description = "마지막 추적 시각", example = "2024-01-02T12:00:00")
            LocalDateTime lastCheckedAt,
            @Schema(description = "마지막 추적 상태", example = "배송중")
            String lastTrackingStatus,
            @Schema(description = "배송 이벤트 목록")
            List<EventResponse> events
    ) {
        public static DetailResponse from(Shipment shipment, List<ShipmentEvent> events) {
            return new DetailResponse(
                    shipment.getId(),
                    shipment.getAsReception().getId(),
                    shipment.getAsReception().getReceptionNumber(),
                    shipment.getRecipientName(),
                    shipment.getRecipientPhone(),
                    shipment.getReturnMethod(),
                    shipment.getAddress(),
                    shipment.getAddressDetail(),
                    shipment.getCourier(),
                    shipment.getCourierCode(),
                    shipment.getTrackingNumber(),
                    shipment.getStatus(),
                    shipment.getShippedAt(),
                    shipment.getCompletedAt(),
                    shipment.getLastCheckedAt(),
                    shipment.getLastTrackingStatus(),
                    events.stream()
                            .map(EventResponse::from)
                            .toList()
            );
        }
    }

    // 배송 관리 통합 목록
    public record ManagementResponse(
            @Schema(description = "A/S 접수 ID", example = "1")
            Long asReceptionId,
            @Schema(description = "배송 ID", example = "1")
            Long shipmentId,
            @Schema(description = "접수번호", example = "AS-20240101-001")
            String receptionNumber,
            @Schema(description = "고객명", example = "홍길동")
            String customerName,
            @Schema(description = "수령인 연락처", example = "01012345678")
            String recipientPhone,
            @Schema(description = "반납 방식", example = "DELIVERY")
            ReturnMethod returnMethod,
            @Schema(description = "접수 상태", example = "SHIPPING")
            AsReceptionStatus asReceptionStatus,
            @Schema(description = "배송 상태", example = "SHIPPING")
            ShipmentStatus shipmentStatus,
            @Schema(description = "택배사명", example = "CJ대한통운")
            String courier,
            @Schema(description = "운송장번호", example = "123456789012")
            String trackingNumber,
            @Schema(description = "발송 시각", example = "2024-01-01T10:00:00")
            LocalDateTime shippedAt
    ) {}
}
