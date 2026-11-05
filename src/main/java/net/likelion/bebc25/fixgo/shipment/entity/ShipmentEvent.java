package net.likelion.bebc25.fixgo.shipment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShipmentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(nullable = false, unique = true)
    private String eventKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentEventStatus status;

    @Column(length = 255)
    private String description;

    private LocalDateTime occurredAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ShipmentEvent create(
            Shipment shipment,
            String eventKey,
            ShipmentEventStatus status,
            String description,
            LocalDateTime occurredAt
    ) {
        ShipmentEvent event = new ShipmentEvent();

        event.shipment = shipment;
        event.eventKey = eventKey;
        event.status = status;
        event.description = description;
        event.occurredAt = occurredAt;

        return event;
    }

}
