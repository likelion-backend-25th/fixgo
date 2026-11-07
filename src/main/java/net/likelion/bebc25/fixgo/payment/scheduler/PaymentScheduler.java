package net.likelion.bebc25.fixgo.payment.scheduler;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.payment.service.PaymentFacadeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PaymentScheduler {
    private final PaymentFacadeService paymentFacadeService;

    @Scheduled(fixedDelay = 60_000) // 1분마다
    public void reconcile() {
        paymentFacadeService.reconcileStuckAttempts(LocalDateTime.now().minusMinutes(2)); // 2분 넘게 멈춘 것만
    }
}