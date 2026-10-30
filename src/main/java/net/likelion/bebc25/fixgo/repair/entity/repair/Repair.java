package net.likelion.bebc25.fixgo.repair.entity.repair;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
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
}
