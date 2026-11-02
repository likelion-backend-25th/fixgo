package net.likelion.bebc25.fixgo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 기본 강도(10)의 BCrypt 해시 인코더 빈 생성
        return new BCryptPasswordEncoder();
    }
}