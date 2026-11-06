package net.likelion.bebc25.fixgo.repair.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.repair.entity.part.Part;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {@Index(name = "IDX_RPP_PART", columnList = "repair_preset_id, part_id")})
public class RepairPresetPart {
    @EmbeddedId
    RepairPresetPartId id;

    @MapsId("repairPresetId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_preset_id", nullable = false)
    RepairPreset repairPreset;

    @MapsId("partId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    Part part;

    public RepairPresetPart(RepairPreset repairPreset, Part part) {
        this.repairPreset = repairPreset;
        this.part = part;
    }
}
