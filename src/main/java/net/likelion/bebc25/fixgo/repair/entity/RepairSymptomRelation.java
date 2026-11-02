package net.likelion.bebc25.fixgo.repair.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepairSymptomRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_preset_id", nullable = false)
    private RepairPreset repairPreset;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public RepairSymptomRelation(RepairPreset repairPreset, LocalDateTime createdAt) {
        this.repairPreset = repairPreset;
        this.createdAt = createdAt;
    }
}