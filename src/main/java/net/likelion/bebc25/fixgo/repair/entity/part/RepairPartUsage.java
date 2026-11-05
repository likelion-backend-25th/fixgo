package net.likelion.bebc25.fixgo.repair.entity.part;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.repair.entity.RepairWork;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepairPartUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @MapsId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    private RepairWork repairWork;

    @MapsId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int unitPrice;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    public RepairPartUsage(RepairWork repairWork, Part part, int quantity, int unitPrice) {
        this.repairWork = repairWork;
        this.part = part;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
}
