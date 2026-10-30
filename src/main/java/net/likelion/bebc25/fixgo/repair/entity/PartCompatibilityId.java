package net.likelion.bebc25.fixgo.repair.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class PartCompatibilityId implements Serializable {
    @Column
    private Long partsId;

    @Column
    private Long productModelId;
}
