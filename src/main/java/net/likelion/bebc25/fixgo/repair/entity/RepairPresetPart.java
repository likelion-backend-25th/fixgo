package net.likelion.bebc25.fixgo.repair.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.product.entity.ProductModel;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {@Index(name = "IDX_RPP_PART", columnList = "product_model_id")})
public class RepairPresetPart {
    @EmbeddedId
    RepairPresetPartId id;

    @MapsId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_preset_id", nullable = false)
    RepairPreset repairPreset;

    @MapsId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_model_id", nullable = false)
    ProductModel productModel;

    public RepairPresetPart(RepairPreset repairPreset, ProductModel productModel) {
        this.repairPreset = repairPreset;
        this.productModel = productModel;
    }
}
