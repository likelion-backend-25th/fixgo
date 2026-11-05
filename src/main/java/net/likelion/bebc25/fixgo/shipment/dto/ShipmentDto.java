package net.likelion.bebc25.fixgo.shipment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.likelion.bebc25.fixgo.shipment.entity.ReturnMethod;
import net.likelion.bebc25.fixgo.shipment.entity.Shipment;
import net.likelion.bebc25.fixgo.shipment.entity.ShipmentEvent;
import net.likelion.bebc25.fixgo.shipment.entity.ShipmentStatus;

import java.time.LocalDateTime;
import java.util.List;

public class ShipmentDto {

    // 배송 등록
    public record CreateRequest(
            @NotNull
            Long asReceptionId,
            @NotBlank
            String courierCode,
            @NotBlank
            String trackingNumber
    ) {}

    // 택배사 및 운송장번호 수정 요청
    public record UpdateRequest(
            @NotBlank
            String courierCode,
            @NotBlank
            String trackingNumber
    ) {}

    // 배송 조회
    public record Response(
            Long id,
            Long asReceptionId,
            String receptionNumber,
            String recipientName,
            String recipientPhone,
            ReturnMethod returnMethod,
            String courier,
            String courierCode,
            String trackingNumber,
            ShipmentStatus shipmentStatus,
            LocalDateTime shippedAt,
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

        // 배송 이력 조회
        public record EventResponse(
                Long id,
                ShipmentStatus status,
                String description,
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
                Long id,
                Long asReceptionId,
                String receptionNumber,
                String recipientName,
                String receptionPhone,
                ReturnMethod returnMethod,
                String address,
                String addressDetail,
                String courier,
                String courierCode,
                String trackingNumber,
                ShipmentStatus status,
                LocalDateTime shippedAt,
                LocalDateTime completedAt,
                LocalDateTime lastCheckedAt,
                String lastTrackingStatus,
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

    }
}
