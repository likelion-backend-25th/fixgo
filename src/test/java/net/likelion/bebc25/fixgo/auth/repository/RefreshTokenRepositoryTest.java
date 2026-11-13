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

import org.springframework.test.util.ReflectionTestUtils;

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
    @DisplayName("리프레시 토큰 등록 및 조회")
    void addNewRefreshToken() {
        Member member = new Member(
                "tech1@test.com",
                "1234",
                "tech1",
                "010-1234-5678",
                "서울시 서초구",
                Role.ROLE_TECHNICIAN);
        em.persist(member);
        em.flush();

        LocalDateTime newBeforeCreate = LocalDateTime.now();

        RefreshToken refreshToken = new RefreshToken(member, "new-refresh-token-hash");

        refreshTokenRepository.save(refreshToken);
        em.flush();
        em.clear();

        List<RefreshToken> findTokens = refreshTokenRepository.findByMemberIdAndRevokedAtIsNull(member.getId());

        assertThat(findTokens).hasSize(1);
        RefreshToken findToken = findTokens.get(0);
        assertThat(findToken.getId()).isNotNull();
        assertThat(findToken.getId()).isNotEqualTo(refreshTokenId);
        assertThat(findToken.getMember().getId()).isEqualTo(member.getId());
        assertThat(findToken.getTokenHash()).isEqualTo("new-refresh-token-hash");
        assertThat(findToken.getIssuedAt()).isCloseTo(newBeforeCreate, within(Duration.ofSeconds(2)));
        assertThat(findToken.getExpiresAt()).isEqualTo(findToken.getIssuedAt().plusDays(7));
    }

    @Test
    @DisplayName("회원 ID로 리프레시 토큰 조회")
    void findByMemberId() {
        List<RefreshToken> findTokens = refreshTokenRepository.findByMemberIdAndRevokedAtIsNull(memberId);

        assertThat(findTokens).hasSize(1);
        RefreshToken findToken = findTokens.get(0);
        assertThat(findToken.getId()).isEqualTo(refreshTokenId);
        assertThat(findToken.getMember().getId()).isEqualTo(memberId);
        assertThat(findToken.getTokenHash()).isEqualTo("refresh-token-hash");
        assertThat(findToken.getIssuedAt()).isCloseTo(beforeCreate, within(Duration.ofSeconds(2)));
        assertThat(findToken.getExpiresAt()).isEqualTo(findToken.getIssuedAt().plusDays(7));
        assertThat(findToken.getRevokedAt()).isNull();
        assertThat(refreshTokenRepository.findByMemberIdAndRevokedAtIsNull(999L)).isEmpty();
    }

    @Test
    @DisplayName("파기되거나 만료된 리프레시 토큰만 삭제한다")
    void deleteExpiredOrRevoked() {
        Member member = em.find(Member.class, memberId);

        RefreshToken revoked = new RefreshToken(member, "revoked-token");
        revoked.revokeToken();
        refreshTokenRepository.save(revoked);

        RefreshToken expired = new RefreshToken(member, "expired-token");
        ReflectionTestUtils.setField(expired, "expiresAt", LocalDateTime.now().minusMinutes(1));
        refreshTokenRepository.save(expired);

        RefreshToken active = new RefreshToken(member, "active-token");
        refreshTokenRepository.save(active);
        em.flush();
        em.clear();

        int deleted = refreshTokenRepository.deleteExpiredOrRevoked(LocalDateTime.now());
        em.flush();
        em.clear();

        assertThat(deleted).isEqualTo(2);
        assertThat(refreshTokenRepository.findAll())
                .extracting(RefreshToken::getTokenHash)
                .containsExactlyInAnyOrder("refresh-token-hash", "active-token");
    }
}
