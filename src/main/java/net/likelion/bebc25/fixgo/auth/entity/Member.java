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
    private String password_hash;

    @Column(nullable = false)
    private String name;

    @Column
    private String phone;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private String profile_img_url;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private LocalDateTime deleted_at;


    public Member(Long id, String code, String email, String password_hash, String name, Role role, String profile_img_url, Status status, LocalDateTime deleted_at) {
        this.id = id;
        this.code = code;
        this.email = email;
        this.password_hash = password_hash;
        this.name = name;
        this.role = role;
        this.profile_img_url = profile_img_url;
        this.status = status;
        this.deleted_at = deleted_at;
    }

    public Member(Long id, String code, String email, String password_hash, String name, String phone, Role role, String profile_img_url, Status status, LocalDateTime deleted_at) {
        this(id, code, email, password_hash, name, role, profile_img_url, status, deleted_at);
        this.phone = phone;
    }
}
