package net.likelion.bebc25.fixgo.portal.dto;

import net.likelion.bebc25.fixgo.as.dto.AsReceptionDto;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.repair.dto.repair.CustomerQuoteView;

/**
 * 고객 포탈 화면 데이터.
 * reception은 항상 있고, 나머지 영역은 해당 단계의 데이터가 생기기 전까지 null이다.
 * 점검 중, 수리 중 화면은 reception.status와 진행이력으로 그린다.
 */
public record PortalResponse(
        AsReceptionDto.CustomerReceptionView reception,
        CustomerQuoteView quote,
        PaymentDto.CustomerPaymentView payment
) {
}
