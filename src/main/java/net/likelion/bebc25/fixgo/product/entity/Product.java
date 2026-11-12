package net.likelion.bebc25.fixgo.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.common.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

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

    public Product(Long productModelId, String serialNumber, LocalDateTime manufacturedAt) {
        this(productModelId, null, serialNumber, manufacturedAt, null);
    }

    public Product(
            Long productModelId, Long customerId, String serialNumber,
            LocalDateTime manufacturedAt, LocalDateTime purchasedAt
    ) {
        this.productModelId = productModelId;
        this.customerId = customerId;
        this.serialNumber = serialNumber;
        this.manufacturedAt = manufacturedAt;
        this.purchasedAt = purchasedAt;
    }

    public void registerPurchasedAt(LocalDateTime purchasedAt) {
        this.purchasedAt = purchasedAt;
    }

    public void updateCustomerId(Long customerId) {
        this.customerId = customerId;
    }
}
