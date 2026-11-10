package net.likelion.bebc25.fixgo.shipment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.common.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Shipment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "as_reception_id", nullable = false, unique = true)
    private AsReception asReception;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnMethod returnMethod;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String recipientPhone;

    private String address;

    private String addressDetail;

    private String courier;

    private String courierCode;

    @Column(length = 255)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status = ShipmentStatus.READY;

    private LocalDateTime shippedAt;

    private LocalDateTime completedAt;

    private LocalDateTime lastCheckedAt;

    private String lastTrackingStatus;

    // 배송 및 방문 수령 내역 생성
    public static Shipment create(
            AsReception asReception,
            String recipientName,
            String recipientPhone,
            String address,
            String addressDetail,
            String courier,
            String courierCode,
            String trackingNumber
    ) {
        Shipment shipment = new Shipment();

        shipment.asReception = asReception;
        shipment.returnMethod = asReception.getReturnMethod();
        shipment.recipientName = recipientName;
        shipment.recipientPhone = recipientPhone;
        shipment.address = address;
        shipment.addressDetail = addressDetail;
        shipment.courier = courier;
        shipment.courierCode = courierCode;
        shipment.trackingNumber = trackingNumber;
        shipment.status = ShipmentStatus.READY;

        return shipment;
    }

    public void updateTrackingInfo(
            String courier,
            String courierCode,
            String trackingNumber
    ) {
        if (returnMethod != ReturnMethod.DELIVERY) {
            throw new IllegalStateException("택배 배송만 운송장 정보를 수정할 수 있습니다.");
        }

        if (status != ShipmentStatus.READY) {
            throw new IllegalStateException("배송 대기 상태일 때만 운송장 정보를 수정할 수 있습니다.");
        }

        this.courier = courier;
        this.courierCode = courierCode;
        this.trackingNumber = trackingNumber;
    }

    // 배송 시작
    public void markShipping() {
        if (returnMethod != ReturnMethod.DELIVERY) {
            throw new IllegalStateException("택배 배송만 발송할 수 있습니다.");
        }
        if (status != ShipmentStatus.READY) {
            throw new IllegalStateException("배송 대기 상태일 때만 발송할 수 있습니다.");
        }
        // 
        if (asReception.getReturnMethod() == ReturnMethod.DELIVERY) {
            if (courier == null || courier.isBlank() || courierCode == null || courierCode.isBlank() || trackingNumber == null || trackingNumber.isBlank()) {
                throw new IllegalStateException("택배사와 운송장번호를 등록해야합니다.");
            }
        }

        this.status = ShipmentStatus.SHIPPING;
        this.shippedAt = LocalDateTime.now();
    }

    // 택배 배송
    public void markDelivered() {
        if (returnMethod != ReturnMethod.DELIVERY) {
            throw new IllegalStateException("택배 배송만 배송 완료 처리가 가능합니다.");
        }
        if (status != ShipmentStatus.SHIPPING) {
            throw new IllegalStateException("배송 중인 건만 완료 처리할 수 있습니다.");
        }
        this.status = ShipmentStatus.DELIVERED;
        this.completedAt = LocalDateTime.now();
    }

    // 방문 수령
    public void markPickedUp() {
        if (returnMethod != ReturnMethod.PICKUP) {
            throw new IllegalStateException("방문 수령 건만 수령완료 처리할 수 있습니다.");
        }
        if (status != ShipmentStatus.READY) {
            throw new IllegalStateException("미수령 상태에서만 완료 처리할 수 있습니다.");
        }
        this.status = ShipmentStatus.PICKED_UP;
        this.completedAt = LocalDateTime.now();
    }

    // 스마트택배 배송 조회 결과
    public void updateTrackingStatus(String trackingStatus) {
        if (returnMethod != ReturnMethod.DELIVERY) {
            throw new IllegalStateException("택배 배송만 조회가 가능합니다.");
        }
        this.lastTrackingStatus = trackingStatus;
        this.lastCheckedAt = LocalDateTime.now();
    }

}
