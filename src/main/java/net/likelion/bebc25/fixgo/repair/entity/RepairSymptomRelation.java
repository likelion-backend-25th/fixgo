package net.likelion.bebc25.fixgo.repair.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class RepairSymptomRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_preset_id", nullable = false)
    private RepairPreset repairPreset;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
