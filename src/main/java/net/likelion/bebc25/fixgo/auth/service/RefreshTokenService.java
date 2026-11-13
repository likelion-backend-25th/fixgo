package net.likelion.bebc25.fixgo.auth.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.RefreshToken;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.auth.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    // 새로 발급된 Refresh Token 저장
    public void saveRefreshToken(Long memberId, String tokenValue) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 계정입니다.")
        );
        RefreshToken refreshToken = new RefreshToken(member, tokenValue);
        refreshTokenRepository.save(refreshToken);
    }

    // 토큰 유효성 검사
    public boolean isValid(Long memberId, String tokenValue) {
        List<RefreshToken> tokenList = refreshTokenRepository.findByMemberIdAndRevokedAtIsNull(memberId);
        if (tokenList.isEmpty()) {
            return false;
        }

        return tokenList.stream()
                .map(RefreshToken::getTokenHash).toList()
                .contains(tokenValue);
    }

    public void revokeRefreshTokens(Long memberId) {
        List<RefreshToken> refreshTokens = refreshTokenRepository.findByMemberIdAndRevokedAtIsNull(memberId);
        for (RefreshToken refreshToken : refreshTokens) {
            refreshToken.revokeToken();
        }
    }

    @Transactional
    public int purgeInactiveRefreshTokens() {
        return refreshTokenRepository.deleteExpiredOrRevoked(LocalDateTime.now());
    }
}
