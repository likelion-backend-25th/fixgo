package net.likelion.bebc25.fixgo.portal.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.repository.AsReceptionRepository;
import net.likelion.bebc25.fixgo.portal.dto.CustomerPortalLinkResponse;
import net.likelion.bebc25.fixgo.portal.entity.CustomerPortalLink;
import net.likelion.bebc25.fixgo.portal.exception.PortalErrorCode;
import net.likelion.bebc25.fixgo.portal.exception.PortalException;
import net.likelion.bebc25.fixgo.portal.repository.CustomerPortalLinkRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;


@Service
@RequiredArgsConstructor
public class CustomerPortalLinkService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 16; // 128비트. Base64URL로 22자

    private final CustomerPortalLinkRepository customerPortalLinkRepository;
    private final AsReceptionRepository asReceptionRepository;

    @Value("${portal.link.valid-days:30}")
    private long validDays;

    /**
     * 고객 포털 링크를 발급하고 반환한다.
     */
    @Transactional
    public String createLink(Long asReceptionId) {
        if (customerPortalLinkRepository.existsByAsReceptionId(asReceptionId)) {
            throw new PortalException(PortalErrorCode.LINK_ALREADY_EXISTS, "이미 링크가 발급되었습니다.");
        }
        AsReception asReception = asReceptionRepository.findById(asReceptionId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 접수입니다."));

        String token = generateToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(validDays);
        customerPortalLinkRepository.save(CustomerPortalLink.create(asReception, token, expiresAt));
        return token;
    }

    /**
     * 접수에 발급된 고객 안내 링크를 조회한다.
     */
    @Transactional(readOnly = true)
    public CustomerPortalLinkResponse getLink(Long asReceptionId) {
        CustomerPortalLink link = customerPortalLinkRepository.findByAsReceptionId(asReceptionId)
                .orElseThrow(() -> new PortalException(PortalErrorCode.LINK_NOT_FOUND, "발급된 고객 안내 링크가 없습니다."));
        return CustomerPortalLinkResponse.from(link);
    }

    /**
     * 토큰으로 A/S 접수 ID를 찾는다.
     */
    @Transactional(readOnly = true)
    public Long resolveAsReceptionId(String token) {
        CustomerPortalLink link = customerPortalLinkRepository.findByToken(token)
                .orElseThrow(() -> new PortalException(PortalErrorCode.LINK_NOT_FOUND, "유효하지 않은 링크입니다."));

        if (link.isExpiredAt(LocalDateTime.now())) {
            throw new PortalException(PortalErrorCode.LINK_EXPIRED, "만료된 링크입니다.");
        }
        return link.getAsReception().getId();
    }

    /**
     * 링크를 재발급한다. 기존 토큰을 새 토큰으로 교체하고 만료 시각도 새로 설정한다.
     * 이전 링크는 즉시 무효가 된다.
     * <p>
     * 분실, 만료, 링크가 다른 사람에게 전달된 경우에 이 메서드로 새 링크를 발급한다.
     *
     * @return 새 토큰
     */
    @Transactional
    public String recreateLink(Long asReceptionId) {
        CustomerPortalLink link = customerPortalLinkRepository.findByAsReceptionId(asReceptionId)
                .orElseThrow(() -> new PortalException(PortalErrorCode.LINK_NOT_FOUND, "유효하지 않은 링크입니다."));
        String token = generateToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(validDays);
        link.rotate(token, expiresAt);

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
}