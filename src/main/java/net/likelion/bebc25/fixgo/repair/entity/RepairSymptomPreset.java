package net.likelion.bebc25.fixgo.repair.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;

@Getter
@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class RepairSymptomPreset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_id", nullable = false)
    private Repair repair;

    @Column(nullable = false)
    private String symptomPresetNam;

    public RepairSymptomPreset(Repair repair, String symptomPresetNam) {
        this.repair = repair;
        this.symptomPresetNam = symptomPresetNam;
    }
}
