package net.likelion.bebc25.fixgo.product.dto;

import net.likelion.bebc25.fixgo.product.entity.Category;
import net.likelion.bebc25.fixgo.product.entity.ProductModel;

import java.time.LocalDateTime;

public class ProductModelDto {

    // 모델 등록
    public record CreateRequest(
            String name,
            String modelName,
            Category category,
            Integer warrantyMonths
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
            String name,
            String modelName,
            Category category,
            Integer warrantyMonths,
            boolean isActive
    ) {
    }

    // 모델 검색
    public record SearchRequest(
            String name,
            String modelName
    ) {
    }

    public record Response(
            Long id,
            String name,
            String modelName,
            Category category,
            Integer warrantyMonths,
            boolean isActive,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Long createdBy,
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
