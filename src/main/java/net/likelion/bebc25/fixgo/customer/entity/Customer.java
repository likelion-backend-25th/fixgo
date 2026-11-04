package net.likelion.bebc25.fixgo.customer.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String email;

    private String zipcode;

    private String basicAddress;

    private String detailAddress;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Long createdBy;

    @Column(nullable = false)
    private Long updatedBy;

    public Customer(
            String name, String phone, String email, String zipcode,
            String basicAddress, String detailAddress, Long createdBy
    ) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.zipcode = zipcode;
        this.basicAddress = basicAddress;
        this.detailAddress = detailAddress;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
    }

    public void update(
            String name, String phone, String email,
            String zipcode, String basicAddress, String detailAddress,
            Long updatedBy
    ) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.zipcode = zipcode;
        this.basicAddress = basicAddress;
        this.detailAddress = detailAddress;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }
}
