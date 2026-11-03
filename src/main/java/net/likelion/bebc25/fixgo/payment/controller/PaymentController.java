package net.likelion.bebc25.fixgo.payment.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.payment.service.PaymentConfirmService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentConfirmService paymentConfirmService;

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmPayment(
            @RequestBody PaymentDto.ConfirmPaymentRequest request
    ) {
        paymentConfirmService.confirmPayment(request.paymentKey(), request.orderId(), request.amount());
        return ResponseEntity.noContent().build();
    }
}
