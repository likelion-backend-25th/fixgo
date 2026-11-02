package net.likelion.bebc25.fixgo.auth.repository;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.RefreshToken;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DataJpaTest
@Import(JpaConfig.class)
public class RefreshTokenRepositoryTest {
    @Autowired
    private EntityManager em;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private Long memberId;
    private LocalDateTime beforeCreate;

    @BeforeEach
    void setup() {
        // 회원 등록
        Member member = new Member("admin01", "test1@test.com", "1234", "test1", Role.ROLE_ADMIN, "");
        em.persist(member); // insert 쿼리문 생성 (실행은 안함)

        memberId = member.getId();
        beforeCreate = LocalDateTime.now();

        // 리프레시 토큰 등록
        RefreshToken refreshToken = new RefreshToken(member, "refresh-token-hash");
        em.persist(refreshToken);

        em.flush(); // 생성해 둔 쿼리문 실행
        em.clear(); // 캐시 삭제
    }

    @Test
    @DisplayName("리프레시 토큰 등록 및 조회")
    void addNewRefreshToken() {
        Member member = new Member(
                "tech01",
                "tech1@test.com",
                "1234",
                "tech1",
                Role.ROLE_TECHNICIAN,
                ""
        );
        em.persist(member);
        em.flush();

        LocalDateTime newBeforeCreate = LocalDateTime.now();

        // @MapsId이므로 id는 null로 두고 member 연관으로 PK를 맞춘다
        RefreshToken refreshToken = new RefreshToken(member, "new-refresh-token-hash");

        refreshTokenRepository.save(refreshToken);
        em.flush();
        em.clear();

        List<RefreshToken> findTokens = refreshTokenRepository.findByMemberIdAndRevokedAtIsNull(member.getId());

        assertThat(findTokens).hasSize(1);
        RefreshToken findToken = findTokens.get(0);
        assertThat(findToken.getId()).isEqualTo(member.getId());
        assertThat(findToken.getTokenHash()).isEqualTo("new-refresh-token-hash");
        assertThat(findToken.getIssuedAt()).isCloseTo(newBeforeCreate, within(Duration.ofSeconds(2)));
        assertThat(findToken.getExpiresAt()).isEqualTo(findToken.getIssuedAt().plusDays(7));
        assertThat(findToken.getId()).isNotEqualTo(memberId);
    }

    @Test
    @DisplayName("회원 ID로 리프레시 토큰 조회")
    void findByMemberId() {
        List<RefreshToken> findTokens = refreshTokenRepository.findByMemberIdAndRevokedAtIsNull(memberId);

        assertThat(findTokens).hasSize(1);
        RefreshToken findToken = findTokens.get(0);
        assertThat(findToken.getId()).isEqualTo(memberId);
        assertThat(findToken.getTokenHash()).isEqualTo("refresh-token-hash");
        assertThat(findToken.getIssuedAt()).isCloseTo(beforeCreate, within(Duration.ofSeconds(2)));
        assertThat(findToken.getExpiresAt()).isEqualTo(findToken.getIssuedAt().plusDays(7));
        assertThat(findToken.getRevokedAt()).isNull();
        assertThat(refreshTokenRepository.findByMemberIdAndRevokedAtIsNull(999L)).isEmpty();
    }
}
