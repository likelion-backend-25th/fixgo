package net.likelion.bebc25.fixgo.repair.entity.repair;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Repair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "as_request_id")
    private Long asRequestId;

    @Column(name = "technician_id")
    private Long technicianId;

    private String diagnosis;

    @Column(name = "work_note")
    private String workNote;

    @Column(name = "repair_decision")
    private String repairDecision;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WarrantyResult warrantyResult;

    private String reason;

    @Column(name = "labor_rate")
    private int laborRate;

    @Column(name = "inspection_cost")
    private int inspectionCost;

    @Column(name = "final_amount")
    private int finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RepairStatus status;

    @Column(name = "reason_for_rejection")
    private String reasonForRejection;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    // 기본 수리 생성자
    public Repair(WarrantyResult warrantyResult, RepairStatus status) {
        this(null, null, warrantyResult, status, LocalDateTime.now(), LocalDateTime.now(), "test", "test");
    }

    // 접수 및 담당 기사 정보 포함 생성자
    public Repair(Long asRequestId, Long technicianId, WarrantyResult warrantyResult, RepairStatus status) {
        this(asRequestId, technicianId, warrantyResult, status, LocalDateTime.now(), LocalDateTime.now(), "test", "test");
    }

    // 감사 정보 포함 생성자
    public Repair(Long asRequestId, Long technicianId, WarrantyResult warrantyResult, RepairStatus status, LocalDateTime createdAt, LocalDateTime updatedAt, String createdBy, String updatedBy) {

        this.asRequestId = asRequestId;
        this.technicianId = technicianId;
        this.warrantyResult = warrantyResult;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    // 수리 정보 수정
    public void updateRepairInfo(Long asRequestId, Long technicianId, String diagnosis, String workNote, String repairDecision, WarrantyResult warrantyResult, String reason, int laborRate, int inspectionCost, int finalAmount, String reasonForRejection) {
        this.asRequestId = asRequestId;
        this.technicianId = technicianId;
        this.diagnosis = diagnosis;
        this.workNote = workNote;
        this.repairDecision = repairDecision;
        this.warrantyResult = warrantyResult;
        this.reason = reason;
        this.laborRate = laborRate;
        this.inspectionCost = inspectionCost;
        this.finalAmount = finalAmount;
        this.reasonForRejection = reasonForRejection;
    }

    // 상태 변경
    public void changeStatus(RepairStatus status, String updatedBy) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    // 수정 감사 정보 갱신
    public void updateAuditInfo(String updatedBy) {
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }
}
