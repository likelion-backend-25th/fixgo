package net.likelion.bebc25.fixgo.auth.service;

import net.likelion.bebc25.fixgo.auth.dto.LoginDto;
import net.likelion.bebc25.fixgo.auth.dto.TokenDto;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.jwt.JwtProvider;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private CustomUserDetailsService userDetailsService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private LoginService loginService;

    @Test
    @DisplayName("로그인에 성공하면 Access/Refresh Token을 발급하고 Refresh Token을 저장한다")
    void login_success() {
        Member member = createMember(1L);
        CustomUserDetails userDetails = new CustomUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(jwtProvider.createAccessToken(1L, "admin@test.com", "ROLE_ADMIN"))
                .willReturn("access-token");
        given(jwtProvider.createRefreshToken(1L)).willReturn("refresh-token");

        TokenDto.Response response = loginService.login(
                new LoginDto.LoginRequest("admin@test.com", "password123")
        );

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600L);
        then(refreshTokenService).should().saveRefreshToken(1L, "refresh-token");
    }

    @Test
    @DisplayName("인증에 실패하면 로그인에 실패한다")
    void login_badCredentials() {
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> loginService.login(
                new LoginDto.LoginRequest("admin@test.com", "wrong")
        )).isInstanceOf(BadCredentialsException.class);

        then(jwtProvider).should(never()).createAccessToken(any(), any(), any());
        then(refreshTokenService).should(never()).saveRefreshToken(any(), any());
    }

    @Test
    @DisplayName("유효한 Refresh Token으로 새 Access/Refresh Token을 재발급한다")
    void refresh_success() {
        Member member = createMember(1L);
        CustomUserDetails userDetails = new CustomUserDetails(member);

        given(jwtProvider.getMemberId("old-refresh-token")).willReturn(1L);
        given(jwtProvider.validateToken("old-refresh-token")).willReturn(true);
        given(refreshTokenService.isValid(1L, "old-refresh-token")).willReturn(true);
        given(userDetailsService.loadUserById(1L)).willReturn(userDetails);
        given(jwtProvider.createAccessToken(1L, "admin@test.com", "ROLE_ADMIN"))
                .willReturn("new-access-token");
        given(jwtProvider.createRefreshToken(1L)).willReturn("new-refresh-token");

        TokenDto.Response response = loginService.refresh(
                new TokenDto.RefreshRequest("old-refresh-token")
        );

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600L);
        then(refreshTokenService).should().saveRefreshToken(1L, "new-refresh-token");
    }

    @Test
    @DisplayName("JWT 서명이 유효하지 않으면 재발급에 실패한다")
    void refresh_invalidJwt() {
        given(jwtProvider.getMemberId("invalid-token")).willReturn(1L);
        given(jwtProvider.validateToken("invalid-token")).willReturn(false);

        assertThatThrownBy(() -> loginService.refresh(new TokenDto.RefreshRequest("invalid-token")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("유효하지 않거나 만료된 Refresh Token입니다.");

        then(refreshTokenService).should(never()).isValid(any(), any());
        then(refreshTokenService).should(never()).saveRefreshToken(any(), any());
    }

    @Test
    @DisplayName("DB에 저장되지 않은 Refresh Token이면 재발급에 실패한다")
    void refresh_notStored() {
        given(jwtProvider.getMemberId("unknown-token")).willReturn(1L);
        given(jwtProvider.validateToken("unknown-token")).willReturn(true);
        given(refreshTokenService.isValid(1L, "unknown-token")).willReturn(false);

        assertThatThrownBy(() -> loginService.refresh(new TokenDto.RefreshRequest("unknown-token")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("유효하지 않거나 만료된 Refresh Token입니다.");

        then(userDetailsService).should(never()).loadUserById(any());
        then(refreshTokenService).should(never()).saveRefreshToken(any(), any());
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
