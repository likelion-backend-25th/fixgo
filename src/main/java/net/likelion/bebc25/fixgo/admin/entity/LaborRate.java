package net.likelion.bebc25.fixgo.admin.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LaborRate {
    @Id // Primary Key로 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL에서의 Auto Increment와 같은 효과
    private Long id;
}
