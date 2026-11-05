package net.likelion.bebc25.fixgo.repair.dto.part;

import net.likelion.bebc25.fixgo.repair.entity.MovementType;
import net.likelion.bebc25.fixgo.repair.entity.part.Part;
import net.likelion.bebc25.fixgo.repair.entity.part.PartStatus;

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

    public record PriceUpdateRequest(
            Long id,
            int unitPrice
    ) {}

    public record StandardMinutesUpdateRequest(
            Long id,
            int standardMinutes
    ) {}

    public record SingleStockUpdateRequest(
            Long id,
            MovementType movementType,
            int quantity,
            String reason
    ) {}

    public record SearchRequest(
            String partName,
            PartStatus status
    ) {}

    public record BriefResponse(
            Long id,
            String name,
            int unitPrice,
            int stockQuantity
    ) {
        public static BriefResponse from(Part part) {
            return new BriefResponse(
                    part.getId(),
                    part.getName(),
                    part.getUnitPrice(),
                    part.getStockQuantity()
            );
        }
    }

    public record DetailResponse(
            Long id,
            String name,
            int unitPrice,
            int standardMinutes,
            int stockQuantity,
            PartStatus status,
            Long version
    ) {
        public static DetailResponse from(Part part) {
            return new DetailResponse(
                    part.getId(),
                    part.getName(),
                    part.getUnitPrice(),
                    part.getStandardMinutes(),
                    part.getStockQuantity(),
                    part.getStatus(),
                    part.getVersion()
            );
        }
    }
}
