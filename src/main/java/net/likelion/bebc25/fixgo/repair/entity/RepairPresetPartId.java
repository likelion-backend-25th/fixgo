package net.likelion.bebc25.fixgo.repair.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class RepairPresetPartId {
    @Column
    private Long partsId;

    @Column
    private Long productModelId;
}
