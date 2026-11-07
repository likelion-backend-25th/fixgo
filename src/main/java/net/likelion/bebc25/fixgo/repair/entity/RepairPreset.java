package net.likelion.bebc25.fixgo.repair.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.common.entity.BaseEntity;

@Getter
@Entity
@NoArgsConstructor
public class RepairPreset extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    public RepairPreset(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void updatePreset(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // 테스트용 ID 설정
    public void setId(Long id) {
        this.id = id;
    }
}