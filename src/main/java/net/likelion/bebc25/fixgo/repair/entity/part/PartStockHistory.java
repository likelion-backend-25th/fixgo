package net.likelion.bebc25.fixgo.repair.entity.part;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.repair.entity.MovementType;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {@Index(name = "IDX_PSH_PART_TIME", columnList = "part_id")})
public class PartStockHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id")
    private Part part;

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
    private Long createdBy;

    public PartStockHistory(Part part, MovementType movementType, int quantityDelta, String reason, Long createdBy) {
        this.part = part;
        this.movementType = movementType;
        this.quantityDelta = quantityDelta;
        this.reason = reason;
        this.createdBy = createdBy;
    }

    public PartStockHistory(Part part, RepairPartUsage repairPartUsage, MovementType movementType, int quantityDelta, String reason, Long createdBy) {
        this(part, movementType, quantityDelta, reason, createdBy);
        this.repairPartUsage = repairPartUsage;
    }
}
