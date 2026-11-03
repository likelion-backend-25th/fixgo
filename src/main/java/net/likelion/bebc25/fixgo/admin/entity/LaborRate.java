package net.likelion.bebc25.fixgo.admin.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.common.entity.BaseEntity;
import net.likelion.bebc25.fixgo.product.entity.Category;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LaborRate extends BaseEntity {
    @Id // Primary Key로 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL에서의 Auto Increment와 같은 효과
    private Long id;

    @Column(nullable = false)
    private Category productCategory;

    @Column(nullable = false)
    private int cost;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RateType rateType;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    public LaborRate(Category productCategory, int cost, RateType rateType, LocalDate effectiveDate) {
        this.productCategory = productCategory;
        this.cost = cost;
        this.rateType = rateType;
        this.effectiveDate = effectiveDate;
    }
}
