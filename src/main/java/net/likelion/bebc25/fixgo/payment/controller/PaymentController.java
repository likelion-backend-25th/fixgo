package net.likelion.bebc25.fixgo.payment.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.payment.service.PaymentAttemptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentAttemptService paymentAttemptService;

    /**
     * 결제를 시작하고, 프론트가 토스 결제창을 띄우는 데 필요한 값을 반환한다.
     * <p>
     * TODO: 링크(토큰) 기능 도입 시 billId 대신 토큰으로 청구서를 찾도록 변경
     * TODO: 공통 예외 처리 필요
     */
    @PostMapping("/bills/{billId}/start")
    public ResponseEntity<PaymentDto.StartPaymentResponse> startPayment(@PathVariable Long billId) {
        return ResponseEntity.ok(paymentAttemptService.startPayment(billId));
    }

//    @PostMapping("/bills/{billId}/cofirm")
//    public ResponseEntity<PaymentDto.ConfirmPaymentResponse> confirmPayment(@PathVariable Long billId,
//                                                                            @RequestBody PaymentDto.ConfirmPaymentRequest request) {
//
//        return ResponseEntity.ok(paymentAttemptService.)
//    }
}
