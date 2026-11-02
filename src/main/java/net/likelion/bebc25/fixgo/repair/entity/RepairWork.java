package net.likelion.bebc25.fixgo.repair.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepairWork {

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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    public RepairWork(Repair repair, String repairPresetName, String workName, LocalDateTime createdAt, LocalDateTime updatedAt, String createdBy, String updatedBy) {
        this.repair = repair;
        this.repairPresetName = repairPresetName;
        this.workName = workName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    // 작업 내역 수정
    public void updateRepairWork(String repairPresetName, String workName) {
        this.repairPresetName = repairPresetName;
        this.workName = workName;
    }

    // 수정자 및 수정 일시 갱신
    public void updateAuditInfo(String updatedBy) {
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }
}