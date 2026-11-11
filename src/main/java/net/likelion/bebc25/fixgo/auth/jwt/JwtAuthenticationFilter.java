package net.likelion.bebc25.fixgo.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.likelion.bebc25.fixgo.auth.exception.AuthErrorCode;
import net.likelion.bebc25.fixgo.auth.service.CustomUserDetailsService;
import net.likelion.bebc25.fixgo.global.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthenticationFilter(JwtProvider jwtProvider, CustomUserDetailsService userDetailsService) {
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 요청 헤더에서 JWT 토큰 추출
        String token = resolveToken(request);

        // 2. 토큰 유효성 검증
        if (StringUtils.hasText(token) && !jwtProvider.validateToken(token)) {
            ErrorResponse errorResponse = ErrorResponse.of(
                    AuthErrorCode.INVALID_TOKEN,
                    "유효하지 않은 토큰입니다."
            );
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            return;
        }

        if (StringUtils.hasText(token) && jwtProvider.validateToken(token)) {
            Long memberId = jwtProvider.getMemberId(token);

            // 3. 토큰 주체(PK) 기반 사용자 정보 조회 및 UserDetails 생성
            UserDetails userDetails = userDetailsService.loadUserById(memberId);

            // 4. 인증 완료 토큰(UsernamePasswordAuthenticationToken) 생성
            // - principal: 인증 주체 (UserDetails)
            // - credentials: 자격 증명 (비밀번호는 이미 토큰으로 검증되었으므로 보안상 null 지정)
            // - authorities: 인가 심사에 사용할 사용자 권한 목록 (ROLE_USER 등)
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 5. SecurityContextHolder에 인증 정보 저장 (이후 컨트롤러에서 @AuthenticationPrincipal로 사용 가능)
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 6. 체인의 다음 필터로 위임
        filterChain.doFilter(request, response);
    }

    // Authorization: Bearer <Token> 형식에서 실제 토큰 값만 분리 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        // null, 빈 문자열(""), 공백(" ") 여부를 일괄 검증하고 "Bearer " 접두사 일치 확인
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}