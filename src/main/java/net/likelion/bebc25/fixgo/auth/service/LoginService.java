package net.likelion.bebc25.fixgo.auth.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.auth.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;


}
