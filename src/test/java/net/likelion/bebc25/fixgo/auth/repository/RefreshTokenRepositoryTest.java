package net.likelion.bebc25.fixgo.auth.repository;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.RefreshToken;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class RefreshTokenRepositoryTest {
    @Autowired
    private EntityManager em;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

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

        LocalDateTime newIssuedAt = LocalDateTime.of(2026, 10, 30, 15, 0, 0);
        LocalDateTime newExpiresAt = LocalDateTime.of(2026, 11, 13, 15, 0, 0);

        // @MapsId이므로 id는 null로 두고 member 연관으로 PK를 맞춘다
        RefreshToken refreshToken = new RefreshToken(
                null,
                member,
                "new-refresh-token-hash",
                newIssuedAt,
                newExpiresAt
        );

        refreshTokenRepository.save(refreshToken);
        em.flush();
        em.clear();

        RefreshToken findToken = refreshTokenRepository.findByMemberId(member.getId()).orElse(null);

        assertThat(findToken).isNotNull();
        assertThat(findToken.getId()).isEqualTo(member.getId());
        assertThat(findToken.getTokenHash()).isEqualTo("new-refresh-token-hash");
        assertThat(findToken.getIssuedAt()).isEqualTo(newIssuedAt);
        assertThat(findToken.getExpiresAt()).isEqualTo(newExpiresAt);
        assertThat(findToken.getId()).isNotEqualTo(memberId);
    }

    @Test
    @DisplayName("회원 ID로 리프레시 토큰 조회")
    void findByMemberId() {
        RefreshToken findToken = refreshTokenRepository.findByMemberId(memberId).orElse(null);

        assertThat(findToken).isNotNull();
        assertThat(findToken.getId()).isEqualTo(memberId);
        assertThat(findToken.getTokenHash()).isEqualTo("refresh-token-hash");
        assertThat(findToken.getIssuedAt()).isEqualTo(issuedAt);
        assertThat(findToken.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(findToken.getRevokedAt()).isNull();
        assertThat(refreshTokenRepository.findByMemberId(999L)).isEmpty();
    }
}
