package net.likelion.bebc25.fixgo.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class RefreshToken {
    @Id // Primary Key로 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL에서의 Auto Increment와 같은 효과
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false, unique = true)
    private String token_hash;

    @Column(nullable = false)
    private LocalDateTime issued_at;

    @Column(nullable = false)
    private LocalDateTime expires_at;

    @Column
    private LocalDateTime revoked_at;

    public RefreshToken(Long id, Member member, String token_hash, LocalDateTime issued_at, LocalDateTime expires_at) {
        this.id = id;
        this.member = member;
        this.token_hash = token_hash;
        this.issued_at = issued_at;
        this.expires_at = expires_at;
    }
}
