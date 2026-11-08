package net.likelion.bebc25.fixgo.as.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
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
@Table(indexes = {
        @Index(name = "IDX_AS_CUSTOMER", columnList = "customer_id"),
        @Index(name = "IDX_AS_PRODUCT", columnList = "product_id"),
        @Index(name = "IDX_AS_RECEPTIONIST", columnList = "receptionist_id"),
        @Index(name = "IDX_AS_TECHNICIAN", columnList = "technician_id"),
        @Index(name = "IDX_AS_STATUS", columnList = "status"),
        @Index(name = "IDX_AS_CREATED", columnList = "created_at")
})
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
    private String memo;

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

    @Column
    private String fileUrl;

    public AsReception(
            String receptionNumber,
            Customer customer,
            Product product,
            Member receptionist,
            Member technician,
            String symptom,
            String symptomDetail,
            String memo,
            ReturnMethod returnMethod,
            String fileUrl
    ) {
        this.receptionNumber = receptionNumber;
        this.customer = customer;
        this.product = product;
        this.receptionist = receptionist;
        this.technician = technician;
        this.symptom = symptom;
        this.symptomDetail = symptomDetail;
        this.memo = memo;
        this.fileUrl = fileUrl;

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

    public void updateDetails(
            String symptom,
            String symptomDetail,
            String memo,
            ReturnMethod returnMethod
    ) {
        if ((symptom != null && symptom.isBlank())
                || (symptomDetail != null && symptomDetail.isBlank())) {
            throw new IllegalArgumentException("증상과 증상 상세는 비울 수 없습니다.");
        }

        if (symptom != null) {
            this.symptom = symptom;
        }
        if (symptomDetail != null) {
            this.symptomDetail = symptomDetail;
        }
        if (memo != null) {
            this.memo = memo.isBlank() ? null : memo;
        }
        if (returnMethod != null) {
            this.returnMethod = returnMethod;
        }
    }

    public void updateTechnician(Member newTechnician) {
        this.technician = newTechnician;
    }

    /**
     * A/S 접수 상태 변경
     */
    public void changeStatus(AsReceptionStatus status) {
        this.status = status;
        this.statusChangedAt = LocalDateTime.now();
    }

    public record UpdateReceptionRequest(
            @Size(max = 255) String symptom,
            @Size(max = 255) String symptomDetail,
            @Size(max = 1000) String memo,
            ReturnMethod returnMethod
    ) {
    }

}
