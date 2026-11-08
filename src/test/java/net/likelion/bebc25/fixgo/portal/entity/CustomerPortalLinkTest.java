package net.likelion.bebc25.fixgo.portal.entity;

import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.support.AsReceptionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerPortalLinkTest {

    private static final String TOKEN = "test-token-1234567890a";
    private static final LocalDateTime EXPIRES_AT = LocalDateTime.of(2026, 12, 1, 0, 0);

    private final AsReception reception = AsReceptionFixture.transientReception();

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("올바른 값으로 발급하면 값이 그대로 담긴다.")
        void success() {
            CustomerPortalLink link = CustomerPortalLink.create(reception, TOKEN, EXPIRES_AT);

            assertThat(link.getAsReception()).isSameAs(reception);
            assertThat(link.getToken()).isEqualTo(TOKEN);
            assertThat(link.getExpiresAt()).isEqualTo(EXPIRES_AT);
        }

        @Test
        @DisplayName("A/S 접수가 없으면 발급할 수 없다.")
        void nullAsReception() {
            assertThatThrownBy(() -> CustomerPortalLink.create(null, TOKEN, EXPIRES_AT))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("토큰이 없거나 비어 있으면 발급할 수 없다.")
        void blankToken() {
            assertThatThrownBy(() -> CustomerPortalLink.create(reception, null, EXPIRES_AT))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> CustomerPortalLink.create(reception, " ", EXPIRES_AT))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("만료 시각이 없으면 발급할 수 없다.")
        void nullExpiresAt() {
            assertThatThrownBy(() -> CustomerPortalLink.create(reception, TOKEN, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("isExpiredAt")
    class IsExpiredAt {

        private final CustomerPortalLink link = CustomerPortalLink.create(reception, TOKEN, EXPIRES_AT);

        @Test
        @DisplayName("만료 시각 이전이면 만료되지 않았다.")
        void beforeExpiry() {
            assertThat(link.isExpiredAt(EXPIRES_AT.minusSeconds(1))).isFalse();
        }

        @Test
        @DisplayName("만료 시각과 정확히 같으면 만료된 것으로 본다.")
        void atExpiry() {
            assertThat(link.isExpiredAt(EXPIRES_AT)).isTrue();
        }

        @Test
        @DisplayName("만료 시각 이후면 만료되었다.")
        void afterExpiry() {
            assertThat(link.isExpiredAt(EXPIRES_AT.plusSeconds(1))).isTrue();
        }
    }

    @Nested
    @DisplayName("rotate")
    class Rotate {

        private static final String NEW_TOKEN = "new-token-1234567890ab";
        private static final LocalDateTime NEW_EXPIRES_AT = EXPIRES_AT.plusDays(30);

        @Test
        @DisplayName("토큰을 교체하면 토큰과 만료 시각이 새 값으로 바뀌고 A/S 접수는 그대로다.")
        void success() {
            CustomerPortalLink link = CustomerPortalLink.create(reception, TOKEN, EXPIRES_AT);

            link.rotate(NEW_TOKEN, NEW_EXPIRES_AT);

            assertThat(link.getToken()).isEqualTo(NEW_TOKEN);
            assertThat(link.getExpiresAt()).isEqualTo(NEW_EXPIRES_AT);
            assertThat(link.getAsReception()).isSameAs(reception);
        }

        @Test
        @DisplayName("빈 토큰으로는 교체할 수 없고 기존 값이 유지된다.")
        void blankToken() {
            CustomerPortalLink link = CustomerPortalLink.create(reception, TOKEN, EXPIRES_AT);

            assertThatThrownBy(() -> link.rotate(" ", NEW_EXPIRES_AT))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(link.getToken()).isEqualTo(TOKEN);
            assertThat(link.getExpiresAt()).isEqualTo(EXPIRES_AT);
        }
    }
}
