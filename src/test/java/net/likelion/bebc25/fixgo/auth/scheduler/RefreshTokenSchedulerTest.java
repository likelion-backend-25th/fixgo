package net.likelion.bebc25.fixgo.auth.scheduler;

import net.likelion.bebc25.fixgo.auth.service.RefreshTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class RefreshTokenSchedulerTest {

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private RefreshTokenScheduler refreshTokenScheduler;

    @Test
    @DisplayName("스케줄러가 파기·만료 토큰 정리 서비스를 호출한다")
    void purgeInactiveRefreshTokens() {
        given(refreshTokenService.purgeInactiveRefreshTokens()).willReturn(2);

        refreshTokenScheduler.purgeInactiveRefreshTokens();

        then(refreshTokenService).should().purgeInactiveRefreshTokens();
    }
}
