package net.likelion.bebc25.fixgo.repair.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_parts_productModel", columnNames = {"parts_id", "product_model_id"}))
public class PartCompatibility {

    @EmbeddedId
    PartCompatibilityId id;

    @MapsId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    Part part;

    @Column(nullable = false)
    int productModelId;

    public PartCompatibility(Part part, int productModelId) {
        this.part = part;
        this.productModelId = productModelId;
    }
}
