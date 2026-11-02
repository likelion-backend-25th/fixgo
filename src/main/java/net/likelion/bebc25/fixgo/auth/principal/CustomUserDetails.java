package net.likelion.bebc25.fixgo.auth.principal;


import lombok.Getter;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CustomUserDetails implements UserDetails {

    @Getter
    private final Member member;
    private final Map<String, Object> attributes;

    // 일반 폼 로그인 및 JWT 필터용 생성자
    public CustomUserDetails(Member member) {
        this.member = member;
        this.attributes = Collections.emptyMap();
    }

    public Long getId() {
        return member.getId();
    }

    // 권한 목록 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 권한 문자열을 SimpleGrantedAuthority 객체로 변환 (ROLE_ 접두사 필수)
        return List.of(new SimpleGrantedAuthority(member.getRole().name()));
    }

    @Override
    public String getPassword() {
        return member.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return member.getEmail();
    }

}