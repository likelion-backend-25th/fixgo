package net.likelion.bebc25.fixgo.portal.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.payment.service.PaymentAttemptService;
import net.likelion.bebc25.fixgo.payment.service.PaymentQueryService;
import net.likelion.bebc25.fixgo.portal.dto.PortalResponse;
import org.springframework.stereotype.Service;

/**
 * 고객 포탈 페이지에서 필요한 데이터를 모은다.
 * 각 영역의 판단은 해당 도메인에서 한다.
 */
@Service
@RequiredArgsConstructor
public class PortalService {
    private final PaymentQueryService paymentQueryService;
    private final CustomerPortalLinkService customerPortalLinkService;
    private final PaymentAttemptService paymentAttemptService;

    /**
     * 토큰이 없거나 만료되면 PortalException이 발생한다.
     */
    public PortalResponse getPortal(String token) {
        Long asRequestId = customerPortalLinkService.resolveAsRequestId(token);

        PaymentDto.PaymentSummaryResponse paymentSummary = paymentQueryService.getSummary(asRequestId);

        return new PortalResponse(paymentSummary);
    }

    /**
     * 토큰으로 접수번호를 가져온 후 결제시작에 넘긴다.
     */
    public PaymentDto.StartPaymentResponse startPayment(String token) {
        Long asRequestId = customerPortalLinkService.resolveAsRequestId(token);
        return paymentAttemptService.startPayment(asRequestId);
    }
}
