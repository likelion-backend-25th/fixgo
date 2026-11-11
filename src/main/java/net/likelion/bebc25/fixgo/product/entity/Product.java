package net.likelion.bebc25.fixgo.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productModelId;

    private Long customerId;

    @Column(nullable = false, unique = true)
    private String serialNumber;

    @Column(nullable = false)
    private LocalDateTime manufacturedAt;

    private LocalDateTime purchasedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Long createdBy;

    @Column(nullable = false)
    private Long updatedBy;

    public Product(Long productModelId, String serialNumber, LocalDateTime manufacturedAt, Long createdBy) {
        this(productModelId, null, serialNumber, manufacturedAt, null, createdBy);
    }

    public Product(Long productModelId, Long customerId, String serialNumber,
                   LocalDateTime manufacturedAt, LocalDateTime purchasedAt, Long createdBy) {
        this.productModelId = productModelId;
        this.customerId = customerId;
        this.serialNumber = serialNumber;
        this.manufacturedAt = manufacturedAt;
        this.purchasedAt = purchasedAt;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
    }

    public void update(Long productModelId, Long customerId, String serialNumber,
                       LocalDateTime manufacturedAt, LocalDateTime purchasedAt, Long updatedBy) {
        this.productModelId = productModelId;
        this.customerId = customerId;
        this.serialNumber = serialNumber;
        this.manufacturedAt = manufacturedAt;
        this.purchasedAt = purchasedAt;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }
}
