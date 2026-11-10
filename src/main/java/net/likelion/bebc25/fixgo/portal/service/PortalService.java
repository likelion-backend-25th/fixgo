package net.likelion.bebc25.fixgo.portal.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.as.dto.AsReceptionDto;
import net.likelion.bebc25.fixgo.as.service.AsReceptionService;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.payment.service.PaymentAttemptService;
import net.likelion.bebc25.fixgo.payment.service.PaymentFacadeService;
import net.likelion.bebc25.fixgo.payment.service.PaymentQueryService;
import net.likelion.bebc25.fixgo.portal.dto.PortalResponse;
import net.likelion.bebc25.fixgo.repair.dto.repair.CustomerQuoteView;
import net.likelion.bebc25.fixgo.repair.service.RepairQuoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 고객 포탈 페이지에서 필요한 데이터를 모은다.
 * 각 영역의 판단은 해당 도메인에서 한다.
 */
@Service
@RequiredArgsConstructor
public class PortalService {
    private final AsReceptionService asReceptionService;
    private final PaymentQueryService paymentQueryService;
    private final CustomerPortalLinkService customerPortalLinkService;
    private final PaymentAttemptService paymentAttemptService;
    private final PaymentFacadeService paymentFacadeService;
    private final RepairQuoteService repairQuoteService;

    /**
     * 토큰이 없거나 만료되면 PortalException이 발생한다.
     */
    @Transactional(readOnly = true)
    public PortalResponse getPortal(String token) {
        Long asReceptionId = customerPortalLinkService.resolveAsReceptionId(token);

        AsReceptionDto.CustomerReceptionView reception = asReceptionService.getCustomerView(asReceptionId);
        CustomerQuoteView quote = repairQuoteService.findCustomerView(asReceptionId).orElse(null);
        PaymentDto.CustomerPaymentView payment = paymentQueryService.findCustomerView(asReceptionId).orElse(null);

        return new PortalResponse(reception, quote, payment);
    }

    /**
     * 토큰으로 접수번호를 가져온 후 견적 승인에 넘긴다.
     */
    public void approveQuote(String token) {
        Long asReceptionId = customerPortalLinkService.resolveAsReceptionId(token);
        repairQuoteService.approveByCustomer(asReceptionId);
    }

    /**
     * 토큰으로 접수번호를 가져온 후 견적 거부에 넘긴다. 거부하면 점검비 청구서가 발행된다.
     */
    public void rejectQuote(String token) {
        Long asReceptionId = customerPortalLinkService.resolveAsReceptionId(token);
        repairQuoteService.rejectByCustomer(asReceptionId);
    }

    /**
     * 토큰으로 접수번호를 가져온 후 결제시작에 넘긴다.
     */
    public PaymentDto.StartPaymentResponse startPayment(String token) {
        Long asReceptionId = customerPortalLinkService.resolveAsReceptionId(token);
        return paymentAttemptService.startPayment(asReceptionId);
    }

    /**
     * 토큰으로 접수번호를 가져온 후 가상계좌 입금 전 취소로 넘긴다.
     */
    public void cancelVirtualAccount(String token) {
        Long asReceptionId = customerPortalLinkService.resolveAsReceptionId(token);
        paymentFacadeService.cancelVirtualAccountBeforeDeposit(asReceptionId);
    }
}
