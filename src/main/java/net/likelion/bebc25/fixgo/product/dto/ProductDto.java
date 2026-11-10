package net.likelion.bebc25.fixgo.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.likelion.bebc25.fixgo.product.entity.Category;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.product.entity.ProductModel;

import java.time.LocalDateTime;

public class ProductDto {

    // 수리 대상 제품 정보 요청
    public record CreateRequest(
            @NotNull Long productModelId,
            Long customerId,
            @NotBlank String serialNumber,
            @NotNull LocalDateTime manufacturedAt,
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
            @NotNull Long productModelId,
            Long customerId,
            @NotBlank String serialNumber,
            @NotNull LocalDateTime manufacturedAt,
            LocalDateTime purchasedAt
    ) {
    }

    // 수리 대상 제품 검색
    public record SearchRequest(
            String serialNumber
    ) {
    }

    public record Response(
            // product
            Long id,
            String serialNumber,
            LocalDateTime manufacturedAt,
            LocalDateTime purchasedAt,
            Long customerId,
            Long productModelId,
            // product model
            String name,
            String modelName,
            Category category,
            Integer warrantyMonths,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Long createdBy,
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
