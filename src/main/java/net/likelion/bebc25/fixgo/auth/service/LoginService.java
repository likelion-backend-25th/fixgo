package net.likelion.bebc25.fixgo.auth.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.auth.dto.LoginDto;
import net.likelion.bebc25.fixgo.auth.dto.TokenDto;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.RefreshToken;
import net.likelion.bebc25.fixgo.auth.jwt.JwtProvider;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.auth.repository.RefreshTokenRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LoginService {
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;


    public TokenDto.Response login(LoginDto.LoginRequest request) {
        // 1. 클라이언트가 입력한 이메일과 비밀번호로 미인증 토큰 생성
        Authentication unauthenticatedToken =
                new UsernamePasswordAuthenticationToken(request.email(), request.password());

        // 2. AuthenticationManager를 통한 인증 검증 위임
        Authentication authentication = authenticationManager.authenticate(unauthenticatedToken);

        // 3. 인증된 Principal로부터 회원 상세 정보 추출
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long memberId = userDetails.getMember().getId();
        String email = userDetails.getUsername();
        String role = userDetails.getMember().getRole().name();

        // 4. JWT 토큰 생성
        String accessToken = jwtProvider.createAccessToken(memberId, email, role);
        String refreshToken = jwtProvider.createRefreshToken(memberId);
        refreshTokenService.saveRefreshToken(memberId, refreshToken);

        // 5. 발급된 토큰 응답 반환 (Access Token 유효기간 1시간 = 3600초)
        return TokenDto.Response.of(accessToken, refreshToken);
    }

    public TokenDto.Response refresh(TokenDto.RefreshRequest request) {
        String refreshToken = request.refreshToken();

        // 1. 토큰 페이로드에서 회원 PK 추출
        Long memberId = jwtProvider.getMemberId(refreshToken);

        // 2. Refresh Token 서명 및 만료 유효성 검증
        if (!jwtProvider.validateToken(refreshToken) || !refreshTokenService.isValid(memberId, refreshToken)) {
            throw new BadCredentialsException("유효하지 않거나 만료된 Refresh Token입니다.");
        }

        // 3. 데이터베이스 회원 존재 여부 및 최신 정보 조회
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserById(memberId);
        Member member = userDetails.getMember();

        // 4. 새 Access Token 및 Refresh Token 발급 (RTR 전략 적용)
        String newAccessToken = jwtProvider.createAccessToken(member.getId(), member.getEmail(), member.getRole().name());
        String newRefreshToken = jwtProvider.createRefreshToken(member.getId());

        refreshTokenService.saveRefreshToken(memberId, newRefreshToken);

        return TokenDto.Response.of(newAccessToken, newRefreshToken);
    }

    public void logout(CustomUserDetails userDetails) {
        Member member = userDetails.getMember();
        List<RefreshToken> refreshTokens = refreshTokenRepository.findByMemberIdAndRevokedAtIsNull(member.getId());
        for (RefreshToken refreshToken : refreshTokens) {
            refreshToken.revokeToken();
        }
    }
}
