package net.likelion.bebc25.fixgo.admin.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.common.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Parts extends BaseEntity {
    @Id // Primary Key로 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL에서의 Auto Increment와 같은 효과
    private Long id;

    @Column(nullable = false, unique = true)
    private String partCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int unitPrice;

    @Column(nullable = false)
    private int standardMinutes;

    @Column(nullable = false)
    private int stockQuantity;

    @Column(nullable = false)
    private boolean isDiscontinued = false;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private int version = 0;

    public Parts(String partCode, String name, int unitPrice, int standardMinutes, int stockQuantity) {
        this.partCode = partCode;
        this.name = name;
        this.unitPrice = unitPrice;
        this.standardMinutes = standardMinutes;
        this.stockQuantity = stockQuantity;
    }
}
