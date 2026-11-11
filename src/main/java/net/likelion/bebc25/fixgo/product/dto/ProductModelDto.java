package net.likelion.bebc25.fixgo.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.likelion.bebc25.fixgo.product.entity.Category;
import net.likelion.bebc25.fixgo.product.entity.ProductModel;

import java.time.LocalDateTime;

public class ProductModelDto {

    // 모델 등록
    public record CreateRequest(
            @Schema(description = "제품명", example = "갤럭시북")
            @NotBlank String name,
            @Schema(description = "모델명", example = "NT750XDA")
            @NotBlank String modelName,
            @Schema(description = "카테고리", example = "LAPTOP")
            @NotNull Category category,
            @Schema(description = "보증 개월 수", example = "24")
            @NotNull Integer warrantyMonths
    ) {
        public ProductModel toEntity(Long createdBy) {
            return new ProductModel(
                    name,
                    modelName,
                    category,
                    warrantyMonths,
                    createdBy
            );
        }
    }

    // 모델 정보 수정
    public record UpdateRequest(
            @Schema(description = "제품명", example = "갤럭시북")
            @NotBlank String name,
            @Schema(description = "모델명", example = "NT750XDA")
            @NotBlank String modelName,
            @Schema(description = "카테고리", example = "LAPTOP")
            @NotNull Category category,
            @Schema(description = "보증 개월 수", example = "24")
            @NotNull Integer warrantyMonths,
            @Schema(description = "활성 여부", example = "true")
            boolean isActive
    ) {
    }

    // 모델 검색
    public record SearchRequest(
            @Schema(description = "검색할 제품명", example = "갤럭시북")
            String name,
            @Schema(description = "검색할 모델명", example = "NT750XDA")
            String modelName
    ) {
    }

    public record Response(
            @Schema(description = "제품 모델 ID", example = "1")
            Long id,
            @Schema(description = "제품명", example = "갤럭시북")
            String name,
            @Schema(description = "모델명", example = "NT750XDA")
            String modelName,
            @Schema(description = "카테고리", example = "LAPTOP")
            Category category,
            @Schema(description = "보증 개월 수", example = "24")
            Integer warrantyMonths,
            @Schema(description = "활성 여부", example = "true")
            boolean isActive,
            @Schema(description = "생성 일시", example = "2026-01-01T10:00:00")
            LocalDateTime createdAt,
            @Schema(description = "수정 일시", example = "2026-01-02T10:00:00")
            LocalDateTime updatedAt,
            @Schema(description = "생성자 ID", example = "1")
            Long createdBy,
            @Schema(description = "수정자 ID", example = "1")
            Long updatedBy
    ) {
        public static ProductModelDto.Response from(ProductModel productModel) {
            return new ProductModelDto.Response(
                    productModel.getId(),
                    productModel.getName(),
                    productModel.getModelName(),
                    productModel.getCategory(),
                    productModel.getWarrantyMonths(),
                    productModel.isActive(),
                    productModel.getCreatedAt(),
                    productModel.getUpdatedAt(),
                    productModel.getCreatedBy(),
                    productModel.getUpdatedBy()
            );
        }
    }
}
