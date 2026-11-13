package net.likelion.bebc25.fixgo.auth.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.likelion.bebc25.fixgo.auth.service.RefreshTokenService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenScheduler {

    private final RefreshTokenService refreshTokenService;

    // 6시간마다 파기·만료된 Refresh Token 삭제
    @Scheduled(cron = "0 0 */6 * * *", zone = "Asia/Seoul")
    public void purgeInactiveRefreshTokens() {
        int deleted = refreshTokenService.purgeInactiveRefreshTokens();
        if (deleted > 0) {
            log.info("파기·만료 Refresh Token 삭제 완료. count={}", deleted);
        }
    }
}
