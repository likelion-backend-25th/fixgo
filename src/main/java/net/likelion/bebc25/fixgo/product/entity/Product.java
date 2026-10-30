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
    private String createdBy;

    @Column(nullable = false)
    private String updatedBy;

    public Product(String serialNumber, LocalDateTime manufacturedAt, String createdBy) {
        this.serialNumber = serialNumber;
        this.manufacturedAt = manufacturedAt;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
    }
}
