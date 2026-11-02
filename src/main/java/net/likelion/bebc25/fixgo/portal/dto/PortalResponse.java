package net.likelion.bebc25.fixgo.portal.dto;

import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;

// TODO: 접수 정보, 수리 정보, 점검 정보 등 추가
public record PortalResponse(
        PaymentDto.PaymentSummaryResponse payment
) {
}
