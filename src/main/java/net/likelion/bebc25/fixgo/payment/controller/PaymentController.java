package net.likelion.bebc25.fixgo.payment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.likelion.bebc25.fixgo.payment.client.TossDto;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBillStatus;
import net.likelion.bebc25.fixgo.payment.service.PaymentFacadeService;
import net.likelion.bebc25.fixgo.payment.service.PaymentQueryService;
import net.likelion.bebc25.fixgo.payment.service.PaymentWebhookService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentFacadeService paymentFacadeService;
    private final PaymentWebhookService paymentWebhookService;
    private final PaymentQueryService paymentQueryService;

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmPayment(
            @Valid @RequestBody PaymentDto.ConfirmPaymentRequest request
    ) {
        paymentFacadeService.confirmPayment(request.paymentKey(), request.orderId(), request.amount());
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

    @PostMapping("/{billId}/refund")
    public ResponseEntity<Void> refundPayment(
            @Valid @RequestBody PaymentDto.RefundPaymentRequest request,
            @PathVariable Long billId
    ) {
        PaymentDto.RefundAccountRequest account = request.refundAccount();
        paymentFacadeService.refundPayment(billId, request.cancelReason(), 1L,
                                           account == null ? null : account.toInternal());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{billId}")
    public PaymentDto.PaymentDetailResponse getDetail(@PathVariable Long billId) {
        return paymentQueryService.getDetail(billId);
    }

    @GetMapping
    public Page<PaymentDto.PaymentListResponse> getPaymentList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) PaymentBillStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return paymentQueryService.getPaymentList(keyword, status, pageable);
    }
}
