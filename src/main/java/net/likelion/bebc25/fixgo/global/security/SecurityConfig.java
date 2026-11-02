package net.likelion.bebc25.fixgo.global.security;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 보안 설정.
 * 고객 포털과 결제 승인만 비로그인으로 열고, 직원 화면은 로그인 필요를 유지한다.
 * TODO: 직원 로그인 구현 시 담당자가 인증 방식에 맞게 확장
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 포털 조회가 실패하면 /error 로 넘어간다. 여기가 막혀 있으면 로그인 화면으로 돌아간다.
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
                        .requestMatchers("/api/v1/portal/**").permitAll()
                        .requestMatchers("/api/v1/payments/confirm").permitAll()
                        // TODO: 로컬 결제 테스트용 페이지. 실제 프론트 연결 후 제거
                        .requestMatchers("/payment.html", "/success.html", "/fail.html").permitAll()
                        .anyRequest().authenticated()
                )
                // 고객 브라우저는 세션 CSRF 토큰이 없다.
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/v1/portal/**", "/api/v1/payments/**"))
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}