package net.likelion.bebc25.fixgo.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 보안 설정.
 * 현재는 결제 경로만 비로그인 접근을 허용하고, 나머지는 Spring Boot 기본 동작(로그인 필요)을 유지한다.
 * TODO: 직원 로그인 구현 시 담당자가 인증 방식에 맞게 확장
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 고객 결제는 로그인 없이 링크로 진입한다
                        .requestMatchers("/api/v1/payments/**").permitAll()
                        // TODO: 로컬 결제 테스트용 페이지. 실제 프론트 연결 후 제거
                        .requestMatchers("/payment.html", "/success.html", "/fail.html").permitAll()
                        .anyRequest().authenticated()
                )
                // 결제 API는 브라우저 세션 없이 호출되는 POST라 CSRF 토큰 검사를 제외한다
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/v1/payments/**"))
                // 기존 기본 동작 유지 (자동 로그인 화면)
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}