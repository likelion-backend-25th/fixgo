package net.likelion.bebc25.fixgo.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.likelion.bebc25.fixgo.product.entity.Category;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.product.entity.ProductModel;

import java.time.LocalDateTime;

public class ProductDto {

    // 수리 대상 제품 정보 요청
    public record CreateRequest(
            @Schema(description = "제품 모델 ID", example = "1")
            @NotNull Long productModelId,
            @Schema(description = "고객 ID", example = "1")
            Long customerId,
            @Schema(description = "시리얼 번호", example = "SN-ABC-12345")
            @NotBlank String serialNumber,
            @Schema(description = "제조 일시", example = "2025-01-01T00:00:00")
            @NotNull LocalDateTime manufacturedAt,
            @Schema(description = "구매 일시", example = "2025-06-01T00:00:00")
            LocalDateTime purchasedAt
    ) {
        public Product toEntity(Long createdBy) {
            return new Product(
                    productModelId,
                    customerId,
                    serialNumber,
                    manufacturedAt,
                    purchasedAt,
                    createdBy
            );
        }
    }

    // 수리 대상 제품 정보 수정
    public record UpdateRequest(
            @Schema(description = "제품 모델 ID", example = "1")
            @NotNull Long productModelId,
            @Schema(description = "고객 ID", example = "1")
            Long customerId,
            @Schema(description = "시리얼 번호", example = "SN-ABC-12345")
            @NotBlank String serialNumber,
            @Schema(description = "제조 일시", example = "2025-01-01T00:00:00")
            @NotNull LocalDateTime manufacturedAt,
            @Schema(description = "구매 일시", example = "2025-06-01T00:00:00")
            LocalDateTime purchasedAt
    ) {
    }

    // 수리 대상 제품 검색
    public record SearchRequest(
            @Schema(description = "검색할 시리얼 번호", example = "SN-ABC-12345")
            String serialNumber
    ) {
    }

    public record Response(
            // product
            @Schema(description = "제품 ID", example = "1")
            Long id,
            @Schema(description = "시리얼 번호", example = "SN-ABC-12345")
            String serialNumber,
            @Schema(description = "제조 일시", example = "2025-01-01T00:00:00")
            LocalDateTime manufacturedAt,
            @Schema(description = "구매 일시", example = "2025-06-01T00:00:00")
            LocalDateTime purchasedAt,
            @Schema(description = "고객 ID", example = "1")
            Long customerId,
            @Schema(description = "제품 모델 ID", example = "1")
            Long productModelId,
            // product model
            @Schema(description = "제품명", example = "갤럭시북")
            String name,
            @Schema(description = "모델명", example = "NT750XDA")
            String modelName,
            @Schema(description = "카테고리", example = "LAPTOP")
            Category category,
            @Schema(description = "보증 개월 수", example = "24")
            Integer warrantyMonths,
            @Schema(description = "생성 일시", example = "2026-01-01T10:00:00")
            LocalDateTime createdAt,
            @Schema(description = "수정 일시", example = "2026-01-02T10:00:00")
            LocalDateTime updatedAt,
            @Schema(description = "생성자 ID", example = "1")
            Long createdBy,
            @Schema(description = "수정자 ID", example = "1")
            Long updatedBy
    ) {
        public static ProductDto.Response from(Product product, ProductModel model) {
            return new ProductDto.Response(
                    // product
                    product.getId(),
                    product.getSerialNumber(),
                    product.getManufacturedAt(),
                    product.getPurchasedAt(),
                    product.getCustomerId(),
                    product.getProductModelId(),
                    // product model
                    model.getName(),
                    model.getModelName(),
                    model.getCategory(),
                    model.getWarrantyMonths(),
                    product.getCreatedAt(),
                    product.getUpdatedAt(),
                    product.getCreatedBy(),
                    product.getUpdatedBy()
            );
        }
    }
}
