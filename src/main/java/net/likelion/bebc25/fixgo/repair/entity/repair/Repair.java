package net.likelion.bebc25.fixgo.repair.entity.repair;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.as.entity.AsRequest;
import net.likelion.bebc25.fixgo.auth.entity.Member;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Repair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A/S 접수 정보 (1:1)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "as_request_id", nullable = false, unique = true)
    private AsRequest asRequest;

    // 담당 기사 정보 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false)
    private Member technician;

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


    // 기본 생성자: 접수 정보 및 담당 기사 정보 포함
    public Repair(AsRequest asRequest, Member technician, WarrantyResult warrantyResult, RepairStatus status) {
        this(asRequest, technician, warrantyResult, status, LocalDateTime.now(), LocalDateTime.now(), "test", "test");
    }

    // 감사 정보 포함 생성자
    public Repair(AsRequest asRequest, Member technician, WarrantyResult warrantyResult, RepairStatus status, LocalDateTime createdAt, LocalDateTime updatedAt, String createdBy, String updatedBy) {
        this.asRequest = asRequest;
        this.technician = technician;
        this.warrantyResult = warrantyResult;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }


    // 수리 정보 수정
    public void updateRepairInfo(AsRequest asRequest, Member technician, String diagnosis, String workNote, String repairDecision, WarrantyResult warrantyResult, String reason, int laborRate, int inspectionCost, int finalAmount, String reasonForRejection) {
        this.asRequest = asRequest;
        this.technician = technician;
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
