package net.likelion.bebc25.fixgo.repair.entity.repair;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.common.entity.BaseEntity;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Repair extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A/S 접수 정보 (1:1)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "as_reception_id", nullable = false, unique = true)
    private AsReception asReception;

    // 담당 기사 정보 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false)
    private Member technician;

    private String diagnosis;

    @Column(name = "work_note")
    private String workNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "repair_decision")
    private RepairDecision repairDecision;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WarrantyResult warrantyResult;

    private String reason;

    // 고정 공임비
    @Column(name = "labor_rate")
    private int laborRate;

    // 점검비
    @Column(name = "inspection_cost")
    private int inspectionCost;

    // 최종 청구 금액
    @Column(name = "final_amount")
    private int finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RepairStatus status;

    @Column(name = "reason_for_rejection")
    private String reasonForRejection;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    // 기본 생성자: 접수 정보 및 담당 기사 정보 포함
    public Repair(
            AsReception asReception,
            Member technician,
            WarrantyResult warrantyResult,
            RepairStatus status
    ) {
        this.asReception = asReception;
        this.technician = technician;
        this.warrantyResult = warrantyResult;
        this.status = status;
    }

    // 수리 정보 수정
    public void updateRepairInfo(
            AsReception asReception,
            Member technician,
            String diagnosis,
            String workNote,
            RepairDecision repairDecision,
            WarrantyResult warrantyResult,
            String reason,
            int laborRate,
            int inspectionCost,
            int finalAmount,
            String reasonForRejection
    ) {
        this.asReception = asReception;
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
    public void changeStatus(RepairStatus status) {
        this.status = status;
    }

    // 수리비 확정
    public void confirmRepairCost() {
        if (this.confirmedAt != null) {
            throw new IllegalArgumentException("이미 수리비가 확정되었습니다.");
        }

        this.confirmedAt = LocalDateTime.now();
    }

    // 수리비 수정
    public void updateRepairCost(
            int laborRate,
            int inspectionCost,
            int finalAmount
    ) {
        this.laborRate = laborRate;
        this.inspectionCost = inspectionCost;
        this.finalAmount = finalAmount;
    }
}