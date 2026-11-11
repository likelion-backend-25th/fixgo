package net.likelion.bebc25.fixgo.portal.service;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.portal.dto.CustomerPortalLinkResponse;
import net.likelion.bebc25.fixgo.portal.entity.CustomerPortalLink;
import net.likelion.bebc25.fixgo.portal.exception.PortalErrorCode;
import net.likelion.bebc25.fixgo.portal.exception.PortalException;
import net.likelion.bebc25.fixgo.portal.repository.CustomerPortalLinkRepository;
import net.likelion.bebc25.fixgo.support.AsReceptionFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({JpaConfig.class, AsReceptionFixture.class, CustomerPortalLinkService.class})
class CustomerPortalLinkServiceTest {

    @Autowired
    CustomerPortalLinkService customerPortalLinkService;
    @Autowired
    CustomerPortalLinkRepository customerPortalLinkRepository;
    @Autowired
    AsReceptionFixture asReceptionFixture;
    @Autowired
    EntityManager em;

    // 링크는 A/S 접수를 FK로 참조하므로 실제로 저장된 접수가 필요하다
    private AsReception reception;
    private Long asReceptionId;

    @BeforeEach
    void setUp() {
        reception = asReceptionFixture.save();
        asReceptionId = reception.getId();
    }

    @Nested
    @DisplayName("createLink")
    class CreateLink {

        @Test
        @DisplayName("발급한 토큰으로 조회하면 asReceptionId를 찾을 수 있다.")
        void createAndResolve() {
            String token = customerPortalLinkService.createLink(asReceptionId);

            assertThat(customerPortalLinkService.resolveAsReceptionId(token)).isEqualTo(asReceptionId);
        }

        @Test
        @DisplayName("DB에는 발급한 토큰 원본이 그대로 저장된다.")
        void storesToken() {
            String token = customerPortalLinkService.createLink(asReceptionId);

            CustomerPortalLink saved = customerPortalLinkRepository.findByAsReceptionId(asReceptionId).orElseThrow();
            assertThat(saved.getToken()).isEqualTo(token);
        }

        @Test
        @DisplayName("이미 링크가 있는 A/S 건에는 다시 발급할 수 없다.")
        void alreadyExists() {
            customerPortalLinkService.createLink(asReceptionId);

            assertThatThrownBy(() -> customerPortalLinkService.createLink(asReceptionId))
                    .isInstanceOf(PortalException.class)
                    .extracting("errorCode")
                    .isEqualTo(PortalErrorCode.LINK_ALREADY_EXISTS);
        }

        @Test
        @DisplayName("존재하지 않는 A/S 접수에는 발급할 수 없다.")
        void receptionNotFound() {
            assertThatThrownBy(() -> customerPortalLinkService.createLink(-1L))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("getLink")
    class GetLink {

        @Test
        @DisplayName("발급된 토큰과 만료 시각을 반환한다.")
        void returnsIssuedLink() {
            String token = customerPortalLinkService.createLink(asReceptionId);

            CustomerPortalLinkResponse response = customerPortalLinkService.getLink(asReceptionId);

            assertThat(response.token()).isEqualTo(token);
            assertThat(response.expiresAt()).isAfter(LocalDateTime.now());
        }

        @Test
        @DisplayName("발급된 링크가 없으면 LINK_NOT_FOUND")
        void notFound() {
            assertThatThrownBy(() -> customerPortalLinkService.getLink(asReceptionId))
                    .isInstanceOf(PortalException.class)
                    .extracting("errorCode")
                    .isEqualTo(PortalErrorCode.LINK_NOT_FOUND);
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
        void expired() {
            // given: 만료 시각을 과거로 해서 직접 저장한다
            String token = "expired-test-token";
            customerPortalLinkRepository.save(
                    CustomerPortalLink.create(reception, token, LocalDateTime.now().minusDays(1)));

            // when & then
            assertThatThrownBy(() -> customerPortalLinkService.resolveAsReceptionId(token))
                    .isInstanceOf(PortalException.class)
                    .extracting("errorCode")
                    .isEqualTo(PortalErrorCode.LINK_EXPIRED);
        }
    }

    @Nested
    @DisplayName("recreateLink")
    class RecreateLink {

        @Test
        @DisplayName("재발급하면 새 토큰은 유효하고 이전 토큰은 더 이상 조회되지 않는다.")
        void rotatesToken() {
            String oldToken = customerPortalLinkService.createLink(asReceptionId);

            String newToken = customerPortalLinkService.recreateLink(asReceptionId);
            em.flush();
            em.clear();

            assertThat(newToken).isNotEqualTo(oldToken);
            assertThat(customerPortalLinkService.resolveAsReceptionId(newToken)).isEqualTo(asReceptionId);
            assertThatThrownBy(() -> customerPortalLinkService.resolveAsReceptionId(oldToken))
                    .isInstanceOf(PortalException.class)
                    .extracting("errorCode")
                    .isEqualTo(PortalErrorCode.LINK_NOT_FOUND);
        }

        @Test
        @DisplayName("만료된 링크도 재발급하면 다시 유효해진다.")
        void renewsExpiredLink() {
            customerPortalLinkRepository.save(
                    CustomerPortalLink.create(reception, "expired-test-token", LocalDateTime.now().minusDays(1)));

            String newToken = customerPortalLinkService.recreateLink(asReceptionId);
            em.flush();
            em.clear();

            assertThat(customerPortalLinkService.resolveAsReceptionId(newToken)).isEqualTo(asReceptionId);
        }

        @Test
        @DisplayName("발급된 링크가 없는 A/S 건은 재발급할 수 없다.")
        void noLink() {
            assertThatThrownBy(() -> customerPortalLinkService.recreateLink(asReceptionId))
                    .isInstanceOf(PortalException.class)
                    .extracting("errorCode")
                    .isEqualTo(PortalErrorCode.LINK_NOT_FOUND);
        }
    }
}
