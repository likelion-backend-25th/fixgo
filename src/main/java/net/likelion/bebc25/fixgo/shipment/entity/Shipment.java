package net.likelion.bebc25.fixgo.shipment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "as_requset_id", nullable = false, unique = true)
//    private AsRequest asRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnMethod returnMethod = ReturnMethod.DELIVERY;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String recipientPhone;

    private String address;

    private String addressDetail;

    private String courier;

    private String courierCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status = ShipmentStatus.READY;

    private LocalDateTime shippedAt;

    private LocalDateTime completedAt;

    private LocalDateTime lastCheckedAt;

    private String lastTrackingStatus;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = false)
    private String updatedBy;

}
