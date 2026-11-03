package net.likelion.bebc25.fixgo.shipment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.likelion.bebc25.fixgo.shipment.entity.ShipmentStatus;

import java.time.LocalDateTime;

public class ShipmentDto {

    // 배송 등록
    public record CreateRequest(
            @NotNull
            Long asReceptionId,
            @NotBlank
            String recipientName,
            @NotBlank
            String recipientPhone,
            @NotBlank
            String address,
            String addressDetail,
            @NotBlank
            String courier,
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
            String address,
            String addressDetail,
            String courier,
            String trackingNumber,
            ShipmentStatus shipmentStatus,
            LocalDateTime shippedAt,
            LocalDateTime completedAt,
            Long createdBy,
            Long updatedBy
    ) {}
}
