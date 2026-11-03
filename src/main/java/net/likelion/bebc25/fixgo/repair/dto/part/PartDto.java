package net.likelion.bebc25.fixgo.repair.dto.part;

import net.likelion.bebc25.fixgo.repair.entity.MovementType;
import net.likelion.bebc25.fixgo.repair.entity.Part;

public class PartDto {
    public record CreateRequest(
            String name,
            int unitPrice,
            int standardMinutes,
            int stockQuantity
    ) {
        public Part toEntity() {
            return new Part(name, unitPrice, standardMinutes, stockQuantity);
        }
    }

    public record priceUpdateRequest(
            Long id,
            int unitPrice
    ) {}

    public record stockUpdateRequest(
            Long id,
            MovementType movementType,
            int quantity,
            String reason
    ) {}

    public record Response(
            Long id,
            Long name,
            Long unitPrice,
            Long standardMinutes,
            Long stockQuantity,
            int version
    ) {}
}
