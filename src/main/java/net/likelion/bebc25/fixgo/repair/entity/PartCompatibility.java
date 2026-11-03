package net.likelion.bebc25.fixgo.repair.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.product.entity.ProductModel;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = @UniqueConstraint(name = "uk_parts_productModel", columnNames = {"part_id", "product_model_id"}),
        indexes = {@Index(name = "IDX_RPP_PART", columnList = "product_model_id")}
)

public class PartCompatibility {

    @EmbeddedId
    PartCompatibilityId id;

    @MapsId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    Part part;

    @MapsId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_model_id", nullable = false)
    ProductModel productModel;

    public PartCompatibility(Part part, ProductModel productModel) {
        this.part = part;
        this.productModel = productModel;
    }
}
