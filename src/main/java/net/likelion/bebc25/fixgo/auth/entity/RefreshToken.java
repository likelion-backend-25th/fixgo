package net.likelion.bebc25.fixgo.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    @Id // Primary Key로 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL에서의 Auto Increment와 같은 효과
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false, unique = true)
    private String tokenHash;

    @Column(nullable = false)
    private LocalDateTime issuedAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime expiresAt = this.issuedAt.plusDays(7);

    @Column
    private LocalDateTime revokedAt;

    public RefreshToken(Member member, String tokenHash) {
        this.member = member;
        this.tokenHash = tokenHash;
    }

    public void revokeToken() {
        this.revokedAt = LocalDateTime.now();
    }
}
