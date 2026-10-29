package net.likelion.bebc25.fixgo.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.common.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {
    @Id // Primary Key로 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL에서의 Auto Increment와 같은 효과
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;

    @Column
    private String phone;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private String profileImgUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @Column
    private LocalDateTime deleted_at;


    public Member(String code, String email, String passwordHash, String name, Role role, String profileImgUrl) {
        this.code = code;
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.profileImgUrl = profileImgUrl;
        this.role = role;
    }

    public Member(String code, String email, String passwordHash, String name, String phone, Role role, String profileImgUrl) {
        this(code, email, passwordHash, name, role, profileImgUrl);
        this.phone = phone;
    }
}
