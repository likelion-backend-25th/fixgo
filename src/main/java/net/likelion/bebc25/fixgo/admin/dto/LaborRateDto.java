package net.likelion.bebc25.fixgo.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import net.likelion.bebc25.fixgo.admin.entity.LaborRate;
import net.likelion.bebc25.fixgo.admin.entity.RateType;
import net.likelion.bebc25.fixgo.product.entity.Category;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LaborRateDto {
    public record UpdateRequest(
            @NotNull(message = "요금 ID는 필수입니다.")
            Long id,
            @Min(value = 0, message = "비용은 0 이상이어야 합니다.")
            int cost
    ) {}

    public record Response(
            Long id,
            Category productCategory,
            int cost,
            RateType rateType,
            LocalDate effectiveDate,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Long createdBy,
            Long updatedBy
    ) {
        public static Response from(LaborRate laborRate) {
            return new Response(
                    laborRate.getId(),
                    laborRate.getProductCategory(),
                    laborRate.getCost(),
                    laborRate.getRateType(),
                    laborRate.getEffectiveDate(),
                    laborRate.getCreatedAt(),
                    laborRate.getUpdatedAt(),
                    laborRate.getCreatedBy(),
                    laborRate.getUpdatedBy()
            );
        }
    }
}
