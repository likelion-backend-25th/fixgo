package net.likelion.bebc25.fixgo.global.security;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.auth.jwt.JwtAuthenticationFilter;
import net.likelion.bebc25.fixgo.auth.jwt.JwtProvider;
import net.likelion.bebc25.fixgo.auth.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 보안 설정.
 * 고객 포털과 결제 승인만 비로그인으로 열고, 직원 화면은 로그인 필요를 유지한다.
 * TODO: 직원 로그인 구현 시 담당자가 인증 방식에 맞게 확장
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 예외 포워딩이 403으로 가려지지 않도록 허용
                        .requestMatchers("/error").permitAll()

                        // 인증 API (login, refresh)
                        .requestMatchers("/api/v1/login", "/api/v1/refresh").permitAll()

                        // 고객 결제는 로그인 없이 링크로 진입한다
                        .requestMatchers("/api/v1/payments/**").permitAll()

                        // TODO: 로컬 결제 테스트용 페이지. 실제 프론트 연결 후 제거
                        .requestMatchers("/payment.html", "/success.html", "/fail.html").permitAll()


                        // Swagger UI 및 API 문서
                        .requestMatchers(
                                "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs"
                        ).permitAll()

                        .anyRequest().authenticated()
                )
                // REST API 환경: CSRF 비활성화
                .csrf(AbstractHttpConfigurer::disable)

                // HTTP Basic 인증을 비활성화하고 무상태 JWT 인증 체계로 전환
                .httpBasic(AbstractHttpConfigurer::disable)

                // 기본 폼 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)

                // WebConfig의 CORS 설정을 Security 필터에서도 적용
                .cors(Customizer.withDefaults())

                // 세션을 사용하지 않는 Stateless 방식
                .sessionManagement(session ->
                                           session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 커스텀 JWT 인증 필터를 UsernamePasswordAuthenticationFilter 바로 앞에 배치
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtProvider, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class
                )
        ;

        return http.build();
    }
}