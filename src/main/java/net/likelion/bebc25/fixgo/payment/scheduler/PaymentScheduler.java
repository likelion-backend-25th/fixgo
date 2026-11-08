package net.likelion.bebc25.fixgo.payment.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.likelion.bebc25.fixgo.payment.service.PaymentAttemptService;
import net.likelion.bebc25.fixgo.payment.service.PaymentFacadeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentScheduler {
    private final PaymentFacadeService paymentFacadeService;
    private final PaymentAttemptService paymentAttemptService;

    @Scheduled(fixedDelay = 60_000) // 1분마다
    public void reconcile() {
        paymentFacadeService.reconcileStuckAttempts(LocalDateTime.now().minusMinutes(2)); // 2분 넘게 멈춘 것만
    }

    @Scheduled(fixedDelay = 60_000)
    public void expireOverdueDeposits() {
        int count = paymentAttemptService.expireOverdueDeposits(LocalDateTime.now().minusMinutes(10)); // 기한 후 10분 유예
        if (count > 0) {
            log.info("입금 기한 만료 처리. count={}", count);
        }
    }

}