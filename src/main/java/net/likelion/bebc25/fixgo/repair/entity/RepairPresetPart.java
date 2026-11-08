package net.likelion.bebc25.fixgo.repair.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.repair.entity.part.Part;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {
        @Index(
                name = "IDX_REPAIR_PRESET_PART",
                columnList = "repair_preset_id, part_id"
        )
})
public class RepairPresetPart {

    @EmbeddedId
    private RepairPresetPartId id;

    @MapsId("repairPresetId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_preset_id", nullable = false)
    private RepairPreset repairPreset;

    @MapsId("partId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;

    @Column(nullable = false)
    private int quantity;

    public RepairPresetPart(
            RepairPreset repairPreset,
            Part part,
            int quantity
    ) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("기본 부품 수량은 1개 이상이어야 합니다.");
        }

        this.repairPreset = repairPreset;
        this.part = part;
        this.quantity = quantity;
    }

    public void updateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("기본 부품 수량은 1개 이상이어야 합니다.");
        }
        this.quantity = quantity;
    }
}
