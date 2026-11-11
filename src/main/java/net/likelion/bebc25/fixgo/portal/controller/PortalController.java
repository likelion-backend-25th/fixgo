package net.likelion.bebc25.fixgo.portal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.portal.dto.PortalResponse;
import net.likelion.bebc25.fixgo.portal.service.PortalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Portal API", description = "고객 포탈(토큰 기반) 접수/견적/결제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/portal/{token}")
public class PortalController {
    private final PortalService portalService;

    /**
     * 고객 포탈 화면 데이터(접수, 견적, 결제)를 조회한다.
     */
    @GetMapping
    @SecurityRequirements
    @Operation(summary = "포탈 조회", description = "고객 포탈 화면 데이터(접수, 견적, 결제)를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "포탈 조회 성공"),
    })
    public ResponseEntity<PortalResponse> getPortal(@PathVariable String token) {
        return ResponseEntity.ok(portalService.getPortal(token));
    }

    /**
     * 견적을 승인한다. 이후 수리 대기 상태가 된다.
     */
    @PostMapping("/quote/approve")
    @SecurityRequirements
    @Operation(summary = "견적 승인", description = "견적을 승인한다. 이후 수리 대기 상태가 된다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "견적 승인 성공"),
    })
    public ResponseEntity<Void> approveQuote(@PathVariable String token) {
        portalService.approveQuote(token);
        return ResponseEntity.noContent().build();
    }

    /**
     * 견적을 거부한다. 점검비 청구서가 발행되고 결제 대기 상태가 된다.
     */
    @PostMapping("/quote/reject")
    @SecurityRequirements
    @Operation(summary = "견적 거부", description = "견적을 거부한다. 점검비 청구서가 발행되고 결제 대기 상태가 된다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "견적 거부 성공"),
    })
    public ResponseEntity<Void> rejectQuote(@PathVariable String token) {
        portalService.rejectQuote(token);
        return ResponseEntity.noContent().build();
    }

    /**
     * 결제를 시작하고, 프론트가 토스 결제창을 띄우는 데 필요한 값을 반환한다.
     */
    @PostMapping("/payments/start")
    @SecurityRequirements
    @Operation(summary = "결제 시작", description = "결제를 시작하고, 프론트가 토스 결제창을 띄우는 데 필요한 값을 반환한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결제 시작 성공"),
    })
    public ResponseEntity<PaymentDto.StartPaymentResponse> startPayment(@PathVariable String token) {
        return ResponseEntity.ok(portalService.startPayment(token));
    }

    /**
     * 발급된 가상계좌를 취소합니다.
     */
    @PostMapping("/payments/cancel")
    @SecurityRequirements
    @Operation(summary = "가상계좌 취소", description = "발급된 가상계좌를 취소한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "가상계좌 취소 성공"),
    })
    public ResponseEntity<Void> cancelPayment(@PathVariable String token) {
        portalService.cancelVirtualAccount(token);
        return ResponseEntity.noContent().build();
    }
}
