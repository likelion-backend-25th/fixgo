package net.likelion.bebc25.fixgo.portal.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.portal.entity.CustomerPortalLink;
import net.likelion.bebc25.fixgo.portal.exception.PortalErrorCode;
import net.likelion.bebc25.fixgo.portal.exception.PortalException;
import net.likelion.bebc25.fixgo.portal.repository.CustomerPortalLinkRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;


@Service
@RequiredArgsConstructor
public class CustomerPortalLinkService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 16; // 128비트. Base64URL로 22자

    private final CustomerPortalLinkRepository customerPortalLinkRepository;

    @Value("${portal.link.valid-days:30}")
    private long validDays;

    /**
     * 고객 포털 링크를 발급하고 원본 토큰을 반환한다.
     * 원본 토큰은 DB에 저장되지 않으므로, 이 반환값이 고객에게 전달할 수 있는 유일한 값이다.
     * 접수 기능에서 호출하며, 분실 시 재발급에도 쓴다(한 A/S 건에 여러 링크 가능).
     */
    @Transactional
    public String issueLink(Long asRequestId) {
        CustomerPortalLink customerPortalLink = customerPortalLinkRepository.findByAsRequestId(asRequestId).orElse(null);
        if (customerPortalLink != null) {
            throw new PortalException(PortalErrorCode.LINK_ALREADY_EXISTS, "이미 링크가 발급되었습니다.");
        }
        String token = generateToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(validDays);
        customerPortalLinkRepository.save(CustomerPortalLink.issue(asRequestId, hash(token), expiresAt));
        return token;
    }

    /**
     * 토큰으로 A/S 접수 ID를 찾는다. 포털 API와 결제 API의 진입점에서 호출한다.
     * 토큰 원본은 저장하지 않으므로, 같은 방식으로 해시해서 조회한다.
     */
    @Transactional(readOnly = true)
    public Long resolveAsRequestId(String token) {
        CustomerPortalLink link = customerPortalLinkRepository.findByTokenHash(hash(token))
                .orElseThrow(() -> new PortalException(PortalErrorCode.LINK_NOT_FOUND, "유효하지 않은 링크입니다."));

        if (link.isExpiredAt(LocalDateTime.now())) {
            throw new PortalException(PortalErrorCode.LINK_EXPIRED, "만료된 링크입니다.");
        }
        return link.getAsRequestId();
    }

    /**
     * 링크를 재발급한다. 기존 토큰을 새 토큰으로 교체하고 만료 시각도 새로 설정한다.
     * 이전 링크는 즉시 무효가 된다.
     * <p>
     * 토큰 원본을 저장하지 않으므로 고객이 링크를 분실하면 같은 링크를 다시 보낼 수 없다.
     * 분실, 만료, 링크가 다른 사람에게 전달된 경우에 이 메서드로 새 링크를 발급한다.
     *
     * @return 새 원본 토큰. 고객에게 전달할 수 있는 유일한 값이다.
     */
    @Transactional
    public String reissueLink(Long asRequestId) {
        CustomerPortalLink link = customerPortalLinkRepository.findByAsRequestId(asRequestId)
                .orElseThrow(() -> new PortalException(PortalErrorCode.LINK_NOT_FOUND, "유효하지 않은 링크입니다."));
        String token = generateToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(validDays);
        link.rotate(hash(token), expiresAt);

        return token;
    }

    /**
     * 128비트 난수를 URL에 안전한 문자열(22자)로 만든다.
     */
    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * SHA-256 해시를 64자 16진수 문자열로 만든다. 발급과 조회가 반드시 같은 방식을 써야 한다.
     */
    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", e);
        }
    }
}