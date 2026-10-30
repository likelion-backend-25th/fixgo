package net.likelion.bebc25.fixgo.repair.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PartStockHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @MapsId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id")
    private Part part;

    @MapsId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_part_usage_id")
    private RepairPartUsage repairPartUsage;

    @Column
    @Enumerated(EnumType.STRING)
    private MovementType movementType;

    @Column(nullable = false)
    private int quantityDelta;

    @Column
    private String reason;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private String createdBy;

    public PartStockHistory(Part part, MovementType movementType, int quantityDelta, String reason, String createdBy) {
        this.part = part;
        this.movementType = movementType;
        this.quantityDelta = quantityDelta;
        this.reason = reason;
        this.createdBy = createdBy;
    }

    public PartStockHistory(Part part, RepairPartUsage repairPartUsage, MovementType movementType, int quantityDelta, String reason, String createdBy) {
        this(part, movementType, quantityDelta, reason, createdBy);
        this.repairPartUsage = repairPartUsage;
    }
}
