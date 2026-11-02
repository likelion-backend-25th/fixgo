package net.likelion.bebc25.fixgo.portal.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerPortalLinkTest {

    private static final Long AS_REQUEST_ID = 1L;
    private static final String VALID_HASH = "a".repeat(64);
    private static final LocalDateTime EXPIRES_AT = LocalDateTime.of(2026, 12, 1, 0, 0);

    @Nested
    @DisplayName("issue")
    class Issue {

        @Test
        @DisplayName("올바른 값으로 발급하면 값이 그대로 담긴다.")
        void success() {
            CustomerPortalLink link = CustomerPortalLink.issue(AS_REQUEST_ID, VALID_HASH, EXPIRES_AT);

            assertThat(link.getAsRequestId()).isEqualTo(AS_REQUEST_ID);
            assertThat(link.getTokenHash()).isEqualTo(VALID_HASH);
            assertThat(link.getExpiresAt()).isEqualTo(EXPIRES_AT);
        }

        @Test
        @DisplayName("asRequestId가 없으면 발급할 수 없다.")
        void nullAsRequestId() {
            assertThatThrownBy(() -> CustomerPortalLink.issue(null, VALID_HASH, EXPIRES_AT))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("tokenHash가 없으면 발급할 수 없다.")
        void nullTokenHash() {
            assertThatThrownBy(() -> CustomerPortalLink.issue(AS_REQUEST_ID, null, EXPIRES_AT))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("tokenHash가 64자가 아니면 발급할 수 없다. (원본 토큰을 실수로 넘기는 경우 방지)")
        void invalidTokenHashLength() {
            assertThatThrownBy(() -> CustomerPortalLink.issue(AS_REQUEST_ID, "a".repeat(63), EXPIRES_AT))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> CustomerPortalLink.issue(AS_REQUEST_ID, "a".repeat(22), EXPIRES_AT))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("만료 시각이 없으면 발급할 수 없다.")
        void nullExpiresAt() {
            assertThatThrownBy(() -> CustomerPortalLink.issue(AS_REQUEST_ID, VALID_HASH, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("isExpiredAt")
    class IsExpiredAt {

        private final CustomerPortalLink link = CustomerPortalLink.issue(AS_REQUEST_ID, VALID_HASH, EXPIRES_AT);

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

        private static final String NEW_HASH = "b".repeat(64);
        private static final LocalDateTime NEW_EXPIRES_AT = EXPIRES_AT.plusDays(30);

        @Test
        @DisplayName("토큰을 교체하면 해시와 만료 시각이 새 값으로 바뀌고 asRequestId는 그대로다.")
        void success() {
            CustomerPortalLink link = CustomerPortalLink.issue(AS_REQUEST_ID, VALID_HASH, EXPIRES_AT);

            link.rotate(NEW_HASH, NEW_EXPIRES_AT);

            assertThat(link.getTokenHash()).isEqualTo(NEW_HASH);
            assertThat(link.getExpiresAt()).isEqualTo(NEW_EXPIRES_AT);
            assertThat(link.getAsRequestId()).isEqualTo(AS_REQUEST_ID);
        }

        @Test
        @DisplayName("잘못된 해시로는 교체할 수 없고 기존 값이 유지된다.")
        void invalidHash() {
            CustomerPortalLink link = CustomerPortalLink.issue(AS_REQUEST_ID, VALID_HASH, EXPIRES_AT);

            assertThatThrownBy(() -> link.rotate("a".repeat(22), NEW_EXPIRES_AT))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(link.getTokenHash()).isEqualTo(VALID_HASH);
            assertThat(link.getExpiresAt()).isEqualTo(EXPIRES_AT);
        }
    }
}