package net.likelion.bebc25.fixgo.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtProvider {
    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    // Access Token 생성 메서드
    public String createAccessToken(Long memberId, String email, String role) {
        Date now = new Date();  // 토큰 생성 시점
        Date validity = new Date(now.getTime() + accessTokenExpiration);    // 토큰 만료 시점

        return Jwts.builder()
                .issuer("fixgo")                  // 발행 주체
                .subject(String.valueOf(memberId))      // 토큰 소유자 식별 정보
                .claim("email", email)
                .claim("roles", List.of(role))
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey)
                .compact();
    }

    // Refresh Token 생성 메서드 (최소한의 식별 정보만 포함)
    public String createRefreshToken(Long memberId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .issuer("fixgo")
                .subject(String.valueOf(memberId))
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey)
                .compact();
    }

    // 토큰 서명 및 만료 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("유효하지 않은 JWT 서명 또는 형식입니다: {}", e.getMessage());
        }
        return false;
    }

    // 토큰에서 페이로드 Claims 추출
    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims(); // 만료된 토큰이라도 클레임 정보는 반환하여 갱신에 활용
        }
    }

    // 토큰의 비공개 클레임에서 사용자 이메일 추출
    public String getEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    // 토큰의 주체(Subject)에서 회원 PK 추출
    public Long getMemberId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

}
