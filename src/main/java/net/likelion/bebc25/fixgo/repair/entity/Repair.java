package net.likelion.bebc25.fixgo.repair.entity;

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

    private String work_note;

    private String repair_decision;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WarrantyResult warrantyResult;

    private String reason;

    private int labor_rate;

    private int inspection_cost;

    private int final_amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RepairStatus status;

    private String reason_for_rejection;

    private LocalDateTime confirmed_at;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;
}
