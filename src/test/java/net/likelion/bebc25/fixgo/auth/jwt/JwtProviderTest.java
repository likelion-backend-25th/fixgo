package net.likelion.bebc25.fixgo.auth.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderTest {

    private static final String SECRET = Base64.getEncoder()
            .encodeToString("test-jwt-secret-key-for-unit-tests-32bytes!".getBytes());
    private static final long ACCESS_EXPIRATION = 3_600_000L;
    private static final long REFRESH_EXPIRATION = 7_200_000L;

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(SECRET, ACCESS_EXPIRATION, REFRESH_EXPIRATION);
    }

    @Test
    @DisplayName("Access Token을 생성하면 memberId, email, roles를 포함한다")
    void createAccessToken() {
        String token = jwtProvider.createAccessToken(1L, "admin@test.com", "ROLE_ADMIN");

        assertThat(jwtProvider.validateToken(token)).isTrue();
        assertThat(jwtProvider.getMemberId(token)).isEqualTo(1L);
        assertThat(jwtProvider.getEmail(token)).isEqualTo("admin@test.com");
        @SuppressWarnings("unchecked")
        List<String> roles = jwtProvider.parseClaims(token).get("roles", List.class);
        assertThat(roles).containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Refresh Token에는 회원 ID와 발급마다 다른 토큰 ID가 포함된다")
    void createRefreshToken() {
        String token = jwtProvider.createRefreshToken(1L);
        String nextToken = jwtProvider.createRefreshToken(1L);

        assertThat(jwtProvider.validateToken(token)).isTrue();
        assertThat(jwtProvider.getMemberId(token)).isEqualTo(1L);
        assertThat(jwtProvider.getEmail(token)).isNull();
        String tokenId = jwtProvider.parseClaims(token).getId();
        assertThat(tokenId).isNotBlank();
        assertThat(jwtProvider.parseClaims(nextToken).getId()).isNotEqualTo(tokenId);
    }

    @Test
    @DisplayName("서명이 다른 토큰은 유효하지 않다")
    void validateToken_invalidSignature() {
        JwtProvider otherProvider = new JwtProvider(
                Base64.getEncoder().encodeToString("another-secret-key-for-unit-tests-32b!".getBytes()),
                ACCESS_EXPIRATION,
                REFRESH_EXPIRATION
        );
        String token = otherProvider.createAccessToken(1L, "admin@test.com", "ROLE_ADMIN");

        assertThat(jwtProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰은 유효하지 않지만 Claims는 조회할 수 있다")
    void validateToken_expired() {
        JwtProvider shortLived = new JwtProvider(SECRET, 1L, 1L);
        String token = shortLived.createAccessToken(1L, "admin@test.com", "ROLE_ADMIN");

        awaitExpiration();

        assertThat(shortLived.validateToken(token)).isFalse();
        assertThat(shortLived.getMemberId(token)).isEqualTo(1L);
        assertThat(shortLived.getEmail(token)).isEqualTo("admin@test.com");
    }

    @Test
    @DisplayName("형식이 잘못된 토큰은 유효하지 않다")
    void validateToken_malformed() {
        assertThat(jwtProvider.validateToken("not.a.jwt")).isFalse();
        assertThat(jwtProvider.validateToken("")).isFalse();
    }

    private static void awaitExpiration() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
