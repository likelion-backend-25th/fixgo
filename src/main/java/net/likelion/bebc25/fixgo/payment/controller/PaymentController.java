package net.likelion.bebc25.fixgo.payment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.likelion.bebc25.fixgo.payment.client.TossDto;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.payment.service.PaymentConfirmService;
import net.likelion.bebc25.fixgo.payment.service.PaymentWebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentConfirmService paymentConfirmService;
    private final PaymentWebhookService paymentWebhookService;

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmPayment(
            @RequestBody PaymentDto.ConfirmPaymentRequest request
    ) {
        paymentConfirmService.confirmPayment(request.paymentKey(), request.orderId(), request.amount());
        return ResponseEntity.noContent().build();
    }

    /**
     * 토스에서 보내는 웹훅 처리.
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleTossWebhook(
            @RequestBody TossDto.DepositCallbackWebhook request
    ) {
        paymentWebhookService.handleDepositCallback(request);
        return ResponseEntity.ok().build();
    }
}
