package net.likelion.bebc25.fixgo.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.payment.client.TossDto;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBillStatus;
import net.likelion.bebc25.fixgo.payment.service.PaymentAttemptService;
import net.likelion.bebc25.fixgo.payment.service.PaymentFacadeService;
import net.likelion.bebc25.fixgo.payment.service.PaymentQueryService;
import net.likelion.bebc25.fixgo.payment.service.PaymentWebhookService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Payment API", description = "결제 승인/환불/조회 및 토스 웹훅 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentFacadeService paymentFacadeService;
    private final PaymentWebhookService paymentWebhookService;
    private final PaymentQueryService paymentQueryService;
    private final PaymentAttemptService paymentAttemptService;

    @PostMapping("/confirm")
    @SecurityRequirements
    @Operation(summary = "결제 승인", description = "토스 결제창 성공 후 전달된 값으로 결제를 승인한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "결제 승인 성공"),
    })
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
    @SecurityRequirements
    @Operation(summary = "토스 웹훅 처리", description = "토스에서 보내는 웹훅을 처리한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "웹훅 처리 성공"),
    })
    public ResponseEntity<Void> handleTossWebhook(
            @RequestBody TossDto.DepositCallbackWebhook request
    ) {
        paymentWebhookService.handleDepositCallback(request);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    @PostMapping("/{billId}/refund")
    @Operation(summary = "결제 환불", description = "결제 완료된 청구서를 전액 환불한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "환불 성공"),
    })
    public ResponseEntity<Void> refundPayment(
            @Valid @RequestBody PaymentDto.RefundPaymentRequest request,
            @PathVariable Long billId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        paymentFacadeService.refundPayment(billId, userDetails.getId(), request);
        return ResponseEntity.ok().build();
    }

    /**
     * 현장 현금 결제 처리 (직원 전용).
     */
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    @PostMapping("/{billId}/cash")
    @Operation(summary = "현금 결제", description = "현장 현금 결제를 처리한다. (직원 전용)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "현금 결제 성공"),
    })
    public ResponseEntity<Void> payByCash(
            @PathVariable Long billId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        paymentAttemptService.payByCash(billId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN', 'TECHNICIAN')")
    @GetMapping("/{billId}")
    @Operation(summary = "결제 상세 조회", description = "청구서 ID로 결제 상세 정보를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결제 상세 조회 성공"),
    })
    public PaymentDto.PaymentDetailResponse getDetail(@PathVariable Long billId) {
        return paymentQueryService.getDetail(billId);
    }

    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN', 'TECHNICIAN')")
    @GetMapping
    @Operation(summary = "결제 목록 조회", description = "키워드·상태 조건으로 결제 목록을 페이징 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결제 목록 조회 성공"),
    })
    public Page<PaymentDto.PaymentListResponse> getPaymentList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) PaymentBillStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return paymentQueryService.getPaymentList(keyword, status, pageable);
    }
}
