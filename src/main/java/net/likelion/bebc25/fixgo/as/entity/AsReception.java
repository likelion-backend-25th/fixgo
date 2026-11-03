package net.likelion.bebc25.fixgo.as.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.common.entity.BaseEntity;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.shipment.entity.ReturnMethod;

import java.time.LocalDateTime;

@Entity
@Table(indexes = {@Index(name = "IDX_AS_CUSTOMER", columnList = "customer_id"), @Index(name = "IDX_AS_PRODUCT", columnList = "product_id"), @Index(name = "IDX_AS_RECEPTIONIST", columnList = "receptionist_id"), @Index(name = "IDX_AS_TECHNICIAN", columnList = "technician_id"), @Index(name = "IDX_AS_STATUS", columnList = "status"), @Index(name = "IDX_AS_CREATED", columnList = "created_at")})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AsReception extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String receptionNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private Member receptionist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member technician;

    @Column(nullable = false)
    private String symptom;

    @Column(nullable = false)
    private String symptomDetail;

    @Column(length = 1000)
    private String Memo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnMethod returnMethod = ReturnMethod.DELIVERY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AsReceptionStatus status = AsReceptionStatus.RECEIVED;

    @Column(nullable = false)
    private LocalDateTime statusChangedAt;

    @Column
    private LocalDateTime closedAt;

    @OneToOne(fetch = FetchType.LAZY)
    private AsAttachment attachment;

    public AsReception(String receptionNumber, Customer customer, Product product, Member receptionist, Member technician, String symptom, String symptomDetail, String requestMemo, ReturnMethod returnMethod, AsAttachment attachment) {
        this.receptionNumber = receptionNumber;
        this.customer = customer;
        this.product = product;
        this.receptionist = receptionist;
        this.technician = technician;
        this.symptom = symptom;
        this.symptomDetail = symptomDetail;
        this.Memo = requestMemo;
        this.attachment = attachment;

        if (returnMethod != null) {
            this.returnMethod = returnMethod;
        }
    }

    @PrePersist
    private void initializeStatusChangedAt() {
        if (statusChangedAt == null) {
            statusChangedAt = LocalDateTime.now();
        }
    }

}
