package net.likelion.bebc25.fixgo.portal.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 고객 포털 링크. A/S 접수 시 발급되어, 고객이 로그인 없이 진행 상황 확인·견적 승인·결제를 하는 입장권이다.
 * <p>
 * 토큰 원본은 저장하지 않고 SHA-256 해시만 저장한다.
 * 조회 시에는 고객이 가져온 토큰을 같은 방식으로 해시해서 tokenHash와 비교한다.
 *
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerPortalLink {

    /**
     * SHA-256 해시를 16진수 문자열로 표현한 길이
     */
    private static final int TOKEN_HASH_LENGTH = 64;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long asRequestId;

    @Column(nullable = false, unique = true, length = TOKEN_HASH_LENGTH)
    private String tokenHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * 링크를 발급한다.
     *
     * @param asRequestId 링크가 가리키는 A/S 접수 ID
     * @param tokenHash   서비스가 생성한 토큰의 SHA-256 해시 (64자 16진수)
     * @param expiresAt   만료 시각
     */
    public static CustomerPortalLink issue(Long asRequestId, String tokenHash, LocalDateTime expiresAt) {
        if (asRequestId == null) {
            throw new IllegalArgumentException("asRequestId는 필수입니다.");
        }
        validateToken(tokenHash, expiresAt);

        CustomerPortalLink link = new CustomerPortalLink();
        link.asRequestId = asRequestId;
        link.tokenHash = tokenHash;
        link.expiresAt = expiresAt;
        return link;
    }

    private static void validateToken(String tokenHash, LocalDateTime expiresAt) {
        if (tokenHash == null || tokenHash.length() != TOKEN_HASH_LENGTH) {
            throw new IllegalArgumentException("tokenHash는 " + TOKEN_HASH_LENGTH + "자 해시여야 합니다.");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("expiresAt은 필수입니다.");
        }
    }

    /**
     * 토큰을 새 값으로 교체한다. 이전 토큰으로 만든 링크는 더 이상 조회되지 않는다.
     * 고객이 링크를 분실했거나 만료된 경우에 쓴다. (토큰 원본을 저장하지 않으므로 같은 링크를 다시 보낼 수 없다)
     */
    public void rotate(String newTokenHash, LocalDateTime newExpiresAt) {
        validateToken(newTokenHash, newExpiresAt);
        this.tokenHash = newTokenHash;
        this.expiresAt = newExpiresAt;
    }

    /**
     * 주어진 시각 기준으로 만료되었는지 판단한다.
     * 현재 시각을 내부에서 구하지 않고 인자로 받는다. 테스트에서 시각을 고정할 수 있게 하기 위해서다.
     */
    public boolean isExpiredAt(LocalDateTime now) {
        return !now.isBefore(expiresAt);
    }
}