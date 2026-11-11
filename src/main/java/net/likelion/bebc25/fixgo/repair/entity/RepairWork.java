package net.likelion.bebc25.fixgo.repair.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.common.entity.BaseEntity;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepairWork extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_id", nullable = false)
    private Repair repair;

    @Column(name = "repair_preset_name", nullable = false)
    private String repairPresetName;

    @Column(name = "work_name", nullable = false)
    private String workName;

    public RepairWork(
            Repair repair,
            String repairPresetName,
            String workName
    ) {
        this.repair = repair;
        this.repairPresetName = repairPresetName;
        this.workName = workName;
    }

    // 작업 내역 수정
    public void updateRepairWork(String repairPresetName, String workName) {
        this.repairPresetName = repairPresetName;
        this.workName = workName;
    }
}