package net.likelion.bebc25.fixgo.repair.entity.part;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.common.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Part extends BaseEntity {
    @Id // Primary Key로 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL에서의 Auto Increment와 같은 효과
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int unitPrice;

    @Column(nullable = false)
    private int standardMinutes;

    @Column(nullable = false)
    private int stockQuantity;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    PartStatus status;

    @Version
    private Long version;

    public Part(String name, int unitPrice, int standardMinutes, int stockQuantity) {
        this.name = name;
        this.unitPrice = unitPrice;
        this.standardMinutes = standardMinutes;
        this.stockQuantity = stockQuantity;
    }

    public void updateUnitPrice(int newUnitPrice) {
        this.unitPrice = newUnitPrice;
    }

    public void updateStandardMinutes(int newStandardMinutes) {
        this.standardMinutes = newStandardMinutes;
    }

    public void updateStockQuantity(int newStockQuantity) {
        this.stockQuantity = newStockQuantity;
    }

    public void updateStatus(PartStatus newStatus) {
        this.status = newStatus;
    }
}
