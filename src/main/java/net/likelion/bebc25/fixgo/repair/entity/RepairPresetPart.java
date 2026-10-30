package net.likelion.bebc25.fixgo.repair.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepairPresetPart {
//    @EmbeddedId
//    RepairPresetPartId id;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    Long repairPresetId;

    @Column(nullable = false)
    int productModelId;

    public RepairPresetPart(Long repairPresetId, int productModelId) {
        this.repairPresetId = repairPresetId;
        this.productModelId = productModelId;
    }
}
