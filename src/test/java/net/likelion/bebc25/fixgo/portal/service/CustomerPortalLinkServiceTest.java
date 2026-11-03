package net.likelion.bebc25.fixgo.portal.service;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.portal.entity.CustomerPortalLink;
import net.likelion.bebc25.fixgo.portal.exception.PortalErrorCode;
import net.likelion.bebc25.fixgo.portal.exception.PortalException;
import net.likelion.bebc25.fixgo.portal.repository.CustomerPortalLinkRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(CustomerPortalLinkService.class)
class CustomerPortalLinkServiceTest {

    private static final Long AS_RECEPTION_ID = 1L;

    @Autowired
    CustomerPortalLinkService customerPortalLinkService;
    @Autowired
    CustomerPortalLinkRepository customerPortalLinkRepository;
    @Autowired
    EntityManager em;

    /**
     * 서비스와 같은 방식(SHA-256, 16진수)으로 해시한다.
     * 테스트에서 토큰을 알고 있는 링크를 직접 만들 때 쓴다.
     */
    private static String sha256(String token) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
    }

    @Nested
    @DisplayName("issueLink")
    class IssueLink {

        @Test
        @DisplayName("발급한 토큰으로 조회하면 asReceptionId를 찾을 수 있다.")
        void issueAndResolve() {
            String token = customerPortalLinkService.issueLink(AS_RECEPTION_ID);

            assertThat(customerPortalLinkService.resolveAsReceptionId(token)).isEqualTo(AS_RECEPTION_ID);
        }

        @Test
        @DisplayName("DB에는 토큰 원본이 아니라 해시가 저장된다.")
        void storesHashNotToken() throws Exception {
            String token = customerPortalLinkService.issueLink(AS_RECEPTION_ID);

            CustomerPortalLink saved = customerPortalLinkRepository.findByAsReceptionId(AS_RECEPTION_ID).orElseThrow();
            assertThat(saved.getTokenHash()).isNotEqualTo(token);
            assertThat(saved.getTokenHash()).isEqualTo(sha256(token));
        }

        @Test
        @DisplayName("이미 링크가 있는 A/S 건에는 다시 발급할 수 없다.")
        void alreadyExists() {
            customerPortalLinkService.issueLink(AS_RECEPTION_ID);

            assertThatThrownBy(() -> customerPortalLinkService.issueLink(AS_RECEPTION_ID))
                    .isInstanceOf(PortalException.class)
                    .extracting("errorCode")
                    .isEqualTo(PortalErrorCode.LINK_ALREADY_EXISTS);
        }
    }

    @Nested
    @DisplayName("resolveAsReceptionId")
    class ResolveAsReceptionId {

        @Test
        @DisplayName("존재하지 않는 토큰이면 LINK_NOT_FOUND")
        void notFound() {
            assertThatThrownBy(() -> customerPortalLinkService.resolveAsReceptionId("not-exist-token"))
                    .isInstanceOf(PortalException.class)
                    .extracting("errorCode")
                    .isEqualTo(PortalErrorCode.LINK_NOT_FOUND);
        }

        @Test
        @DisplayName("만료된 링크면 LINK_EXPIRED")
        void expired() throws Exception {
            // given: 토큰을 알고 있는 링크를 만료 시각을 과거로 해서 직접 저장한다
            String token = "expired-test-token";
            customerPortalLinkRepository.save(
                    CustomerPortalLink.issue(AS_RECEPTION_ID, sha256(token), LocalDateTime.now().minusDays(1)));

            // when & then
            assertThatThrownBy(() -> customerPortalLinkService.resolveAsReceptionId(token))
                    .isInstanceOf(PortalException.class)
                    .extracting("errorCode")
                    .isEqualTo(PortalErrorCode.LINK_EXPIRED);
        }
    }

    @Nested
    @DisplayName("reissueLink")
    class ReissueLink {

        @Test
        @DisplayName("재발급하면 새 토큰은 유효하고 이전 토큰은 더 이상 조회되지 않는다.")
        void rotatesToken() {
            String oldToken = customerPortalLinkService.issueLink(AS_RECEPTION_ID);

            String newToken = customerPortalLinkService.reissueLink(AS_RECEPTION_ID);
            em.flush();
            em.clear();

            assertThat(newToken).isNotEqualTo(oldToken);
            assertThat(customerPortalLinkService.resolveAsReceptionId(newToken)).isEqualTo(AS_RECEPTION_ID);
            assertThatThrownBy(() -> customerPortalLinkService.resolveAsReceptionId(oldToken))
                    .isInstanceOf(PortalException.class)
                    .extracting("errorCode")
                    .isEqualTo(PortalErrorCode.LINK_NOT_FOUND);
        }

        @Test
        @DisplayName("만료된 링크도 재발급하면 다시 유효해진다.")
        void renewsExpiredLink() throws Exception {
            customerPortalLinkRepository.save(
                    CustomerPortalLink.issue(AS_RECEPTION_ID, sha256("expired-test-token"), LocalDateTime.now().minusDays(1)));

            String newToken = customerPortalLinkService.reissueLink(AS_RECEPTION_ID);
            em.flush();
            em.clear();

            assertThat(customerPortalLinkService.resolveAsReceptionId(newToken)).isEqualTo(AS_RECEPTION_ID);
        }

        @Test
        @DisplayName("발급된 링크가 없는 A/S 건은 재발급할 수 없다.")
        void noLink() {
            assertThatThrownBy(() -> customerPortalLinkService.reissueLink(AS_RECEPTION_ID))
                    .isInstanceOf(PortalException.class)
                    .extracting("errorCode")
                    .isEqualTo(PortalErrorCode.LINK_NOT_FOUND);
        }
    }
}