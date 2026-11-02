package net.likelion.bebc25.fixgo.auth.entity;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
public class RefreshTokenTest {
    @Autowired
    private EntityManager em;

    private Long memberId;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;

    @BeforeEach
    void setup() {
        // 회원 등록
        Member member = new Member("admin01", "test1@test.com", "1234", "test1", Role.ROLE_ADMIN, "");
        em.persist(member); // insert 쿼리문 생성 (실행은 안함)

        memberId = member.getId();
        issuedAt = LocalDateTime.of(2026, 10, 30, 12, 0, 0);
        expiresAt = LocalDateTime.of(2026, 11, 13, 12, 0, 0);

        // 리프레시 토큰 등록
        RefreshToken refreshToken = new RefreshToken(
                memberId,
                member,
                "refresh-token-hash",
                issuedAt,
                expiresAt
        );
        em.persist(refreshToken);

        em.flush(); // 생성해 둔 쿼리문 실행
        em.clear(); // 캐시 삭제
    }

    @Test
    @DisplayName("리프레시 토큰 조회")
    void findRefreshToken() {
        RefreshToken refreshToken = em.find(RefreshToken.class, memberId);

        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken.getId()).isEqualTo(memberId);
        assertThat(refreshToken.getTokenHash()).isEqualTo("refresh-token-hash");
        assertThat(refreshToken.getIssuedAt()).isEqualTo(issuedAt);
        assertThat(refreshToken.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(refreshToken.getRevokedAt()).isNull();
        assertThat(refreshToken.getMember().getId()).isEqualTo(memberId);
        assertThat(refreshToken.getMember().getCode()).isEqualTo("admin01");
    }
}
