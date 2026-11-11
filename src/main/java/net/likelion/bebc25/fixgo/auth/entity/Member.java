package net.likelion.bebc25.fixgo.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.auth.dto.MemberDto;
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
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private String profileImgUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @Column
    private LocalDateTime deletedAt = null;


    public Member(String email, String passwordHash, String name, String phone, String address, Role role, String profileImgUrl) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.profileImgUrl = profileImgUrl;
        this.role = role;
    }

    public void changeStatus(Status newStatus) {
        if (newStatus.equals(Status.DELETED)) {
            this.deletedAt = LocalDateTime.now();
        } else if (this.status.equals(Status.DELETED)) {
            this.deletedAt = null;
        }
        this.status = newStatus;
    }

    public void changeRole(Role newRole) {
        this.role = newRole;
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    public void updateProfile(MemberDto.ProfileUpdateRequest request) {
        this.name = request.name();
        this.phone = request.phone();
        this.address = request.address();
        this.profileImgUrl = request.profileImgUrl();
    }
}
