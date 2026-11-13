package net.likelion.bebc25.fixgo.auth.entity;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DataJpaTest
@Import(JpaConfig.class)
public class RefreshTokenTest {
    @Autowired
    private EntityManager em;

    private Long memberId;
    private Long refreshTokenId;
    private LocalDateTime beforeCreate;

    @BeforeEach
    void setup() {
        // 회원 등록
        Member member = new Member(
                "test1@test.com",
                "1234",
                "test1",
                "010-1234-5678",
                "서울시 강남구",
                Role.ROLE_ADMIN);
        em.persist(member); // insert 쿼리문 생성 (실행은 안함)

        memberId = member.getId();
        beforeCreate = LocalDateTime.now();

        // 리프레시 토큰 등록
        RefreshToken refreshToken = new RefreshToken(member, "refresh-token-hash");
        em.persist(refreshToken);

        em.flush(); // 생성해 둔 쿼리문 실행
        refreshTokenId = refreshToken.getId();
        em.clear(); // 캐시 삭제
    }

    @Test
    @DisplayName("리프레시 토큰 조회")
    void findRefreshToken() {
        RefreshToken refreshToken = em.find(RefreshToken.class, refreshTokenId);

        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken.getId()).isEqualTo(refreshTokenId);
        assertThat(refreshToken.getTokenHash()).isEqualTo("refresh-token-hash");
        assertThat(refreshToken.getIssuedAt()).isCloseTo(beforeCreate, within(Duration.ofSeconds(2)));
        assertThat(refreshToken.getExpiresAt()).isEqualTo(refreshToken.getIssuedAt().plusDays(7));
        assertThat(refreshToken.getRevokedAt()).isNull();
        assertThat(refreshToken.getMember().getId()).isEqualTo(memberId);
        assertThat(refreshToken.getMember().getEmail()).isEqualTo("test1@test.com");
    }
}
