package net.likelion.bebc25.fixgo.repair.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SymptomPreset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String productCategory;

    @Column
    private String name;

    public SymptomPreset(String productCategory, String name) {
        this.productCategory = productCategory;
        this.name = name;
    }
}
