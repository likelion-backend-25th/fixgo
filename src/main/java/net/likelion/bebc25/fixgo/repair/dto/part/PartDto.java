package net.likelion.bebc25.fixgo.repair.dto.part;

import io.swagger.v3.oas.annotations.media.Schema;
import net.likelion.bebc25.fixgo.product.entity.Category;
import net.likelion.bebc25.fixgo.repair.entity.MovementType;
import net.likelion.bebc25.fixgo.repair.entity.part.Part;
import net.likelion.bebc25.fixgo.repair.entity.part.PartStatus;

public class PartDto {
    public record CreateRequest(
            @Schema(description = "부품명", example = "메인보드")
            String name,
            @Schema(description = "단가", example = "50000")
            int unitPrice,
            @Schema(description = "표준 작업시간(분)", example = "60")
            int standardMinutes,
            @Schema(description = "재고 수량", example = "20")
            int stockQuantity
    ) {
        public Part toEntity() {
            return new Part(name, unitPrice, standardMinutes, stockQuantity);
        }
    }

    public record PriceUpdateRequest(
            @Schema(description = "부품 ID", example = "1")
            Long id,
            @Schema(description = "변경할 단가", example = "55000")
            int unitPrice
    ) {}

    public record StandardMinutesUpdateRequest(
            @Schema(description = "부품 ID", example = "1")
            Long id,
            @Schema(description = "변경할 표준 작업시간(분)", example = "90")
            int standardMinutes
    ) {}

    public record StatusUpdateRequest(
            @Schema(description = "부품 ID", example = "1")
            Long id,
            @Schema(description = "변경할 상태. 단종 해제는 재고에 맞는 이전 상태만 허용한다.", example = "DISCONTINUED")
            PartStatus status
    ) {}

    public record SingleStockUpdateRequest(
            @Schema(description = "부품 ID", example = "1")
            Long id,
            @Schema(description = "입출고 유형", example = "OUT")
            MovementType movementType,
            @Schema(description = "수량", example = "2")
            int quantity,
            @Schema(description = "입출고 사유", example = "수리 사용")
            String reason
    ) {}

    public record SearchRequest(
            @Schema(description = "부품명 검색어", example = "메인보드")
            String partName,
            @Schema(description = "부품 상태", example = "ACTIVE")
            PartStatus status,
            @Schema(description = "호환 제품군", example = "LAPTOP")
            Category category,
            @Schema(description = "호환 제품 모델 ID", example = "1")
            Long productModelId
    ) {}

    public record BriefResponse(
            @Schema(description = "부품 ID", example = "1")
            Long id,
            @Schema(description = "부품명", example = "메인보드")
            String name,
            @Schema(description = "단가", example = "50000")
            int unitPrice,
            @Schema(description = "재고 수량", example = "20")
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
            @Schema(description = "부품 ID", example = "1")
            Long id,
            @Schema(description = "부품명", example = "메인보드")
            String name,
            @Schema(description = "단가", example = "50000")
            int unitPrice,
            @Schema(description = "표준 작업시간(분)", example = "60")
            int standardMinutes,
            @Schema(description = "재고 수량", example = "20")
            int stockQuantity,
            @Schema(description = "부품 상태", example = "ACTIVE")
            PartStatus status,
            @Schema(description = "낙관적 락 버전", example = "1")
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
