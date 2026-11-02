package net.likelion.bebc25.fixgo.auth.service;

import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.RefreshToken;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("Refresh Token을 저장한다")
    void saveRefreshToken_success() {
        Member member = createMember(1L);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        refreshTokenService.saveRefreshToken(1L, "refresh-token-value");

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        then(refreshTokenRepository).should().save(captor.capture());
        assertThat(captor.getValue().getMember()).isEqualTo(member);
        assertThat(captor.getValue().getTokenHash()).isEqualTo("refresh-token-value");
    }

    @Test
    @DisplayName("존재하지 않는 회원의 Refresh Token은 저장할 수 없다")
    void saveRefreshToken_memberNotFound() {
        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.saveRefreshToken(999L, "token"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 계정입니다.");
        then(refreshTokenRepository).should(never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("폐기되지 않은 토큰이 일치하면 유효하다")
    void isValid_true() {
        Member member = createMember(1L);
        RefreshToken refreshToken = new RefreshToken(member, "valid-token");
        given(refreshTokenRepository.findByMemberIdAndRevokedAtIsNull(1L))
                .willReturn(List.of(refreshToken));

        assertThat(refreshTokenService.isValid(1L, "valid-token")).isTrue();
    }

    @Test
    @DisplayName("저장된 활성 토큰이 없으면 유효하지 않다")
    void isValid_empty() {
        given(refreshTokenRepository.findByMemberIdAndRevokedAtIsNull(1L))
                .willReturn(List.of());

        assertThat(refreshTokenService.isValid(1L, "any-token")).isFalse();
    }

    @Test
    @DisplayName("토큰 값이 일치하지 않으면 유효하지 않다")
    void isValid_mismatch() {
        Member member = createMember(1L);
        RefreshToken refreshToken = new RefreshToken(member, "stored-token");
        given(refreshTokenRepository.findByMemberIdAndRevokedAtIsNull(1L))
                .willReturn(List.of(refreshToken));

        assertThat(refreshTokenService.isValid(1L, "other-token")).isFalse();
    }

    private Member createMember(Long id) {
        Member member = new Member(
                "admin01",
                "admin@test.com",
                "encoded-password",
                "관리자",
                Role.ROLE_ADMIN,
                ""
        );
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }
}
