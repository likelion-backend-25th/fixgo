package net.likelion.bebc25.fixgo.portal.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.as.entity.AsReception;

import java.time.LocalDateTime;

/**
 * 고객 포털 링크. A/S 접수 시 발급되어, 고객이 로그인 없이 진행 상황 확인,견적 승인,결제를 할 수 있다.
 * <p>
 * 직원이 링크를 다시 조회하거나 재전송할 수 있도록 토큰 원본을 저장한다.
 * DB 유출 시 링크가 그대로 쓰일 수 있으므로, 만료(expiresAt)와 재발급(rotate)으로 피해 범위를 제한한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerPortalLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "as_reception_id", nullable = false, unique = true)
    private AsReception asReception;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * 링크를 발급한다.
     *
     * @param asReception 링크가 가리키는 A/S 접수
     * @param token       서비스가 생성한 토큰
     * @param expiresAt   만료 시각
     */
    public static CustomerPortalLink create(AsReception asReception, String token, LocalDateTime expiresAt) {
        if (asReception == null) {
            throw new IllegalArgumentException("asReceptionId는 필수입니다.");
        }
        validateToken(token, expiresAt);

        CustomerPortalLink link = new CustomerPortalLink();
        link.asReception = asReception;
        link.token = token;
        link.expiresAt = expiresAt;
        return link;
    }

    private static void validateToken(String token, LocalDateTime expiresAt) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token은 필수입니다.");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("expiresAt은 필수입니다.");
        }
    }

    /**
     * 토큰을 새 값으로 교체한다. 이전 토큰으로 만든 링크는 더 이상 조회되지 않는다.
     */
    public void rotate(String newToken, LocalDateTime newExpiresAt) {
        validateToken(newToken, newExpiresAt);
        this.token = newToken;
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