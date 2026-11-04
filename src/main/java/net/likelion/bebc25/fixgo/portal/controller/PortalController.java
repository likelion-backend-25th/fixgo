package net.likelion.bebc25.fixgo.portal.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.portal.dto.PortalResponse;
import net.likelion.bebc25.fixgo.portal.service.PortalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/portal/{token}")
public class PortalController {
    private final PortalService portalService;

    /**
     * 고객 포탈 화면 데이터를 조회한다.
     * TODO: 현재는 결제 영역만 있음. 접수, 견적, 점검 영역 추가 필요. PortalService에서  PortalResponse에 모아서 보냄.
     */
    @GetMapping
    public ResponseEntity<PortalResponse> getPortal(@PathVariable String token) {
        return ResponseEntity.ok(portalService.getPortal(token));
    }

    /**
     * 결제를 시작하고, 프론트가 토스 결제창을 띄우는 데 필요한 값을 반환한다.
     */
    @PostMapping("/payments/start")
    public ResponseEntity<PaymentDto.StartPaymentResponse> startPayment(@PathVariable String token) {
        return ResponseEntity.ok(portalService.startPayment(token));
    }

    /**
     * 발급된 가상계좌를 취소합니다.
     */
    @PostMapping("/payments/cancel")
    public ResponseEntity<Void> cancelPayment(@PathVariable String token) {
        portalService.cancelVirtualAccount(token);
        return ResponseEntity.noContent().build();
    }
}
