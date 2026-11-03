package net.likelion.bebc25.fixgo.admin.dto;

import net.likelion.bebc25.fixgo.admin.entity.LaborRate;
import net.likelion.bebc25.fixgo.admin.entity.RateType;
import net.likelion.bebc25.fixgo.product.entity.Category;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LaborRateDto {
    public record CreateRequest(
            Category productCategory,
            int cost,
            RateType rateType,
            LocalDate effectiveDate
    ) {
        public LaborRate toEntity() {
            return new LaborRate(productCategory, cost, rateType, effectiveDate);
        }
    }

    public record UpdateRequest(
            Long id,
            int cost,
            LocalDate effectiveDate
    ) {}

    public record Response(
            Long id,
            Category productCategory,
            int cost,
            RateType rateType,
            LocalDate effectiveDate,
            LocalDateTime createdAT,
            LocalDateTime updatedAT,
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
