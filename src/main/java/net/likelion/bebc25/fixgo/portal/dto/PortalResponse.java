package net.likelion.bebc25.fixgo.portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import net.likelion.bebc25.fixgo.as.dto.AsReceptionDto;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.repair.dto.repair.CustomerQuoteView;

/**
 * 고객 포탈 화면 데이터.
 * reception은 항상 있고, 나머지 영역은 해당 단계의 데이터가 생기기 전까지 null이다.
 * 점검 중, 수리 중 화면은 reception.status와 진행이력으로 그린다.
 */
public record PortalResponse(
        @Schema(description = "접수 정보(항상 존재)")
        AsReceptionDto.CustomerReceptionView reception,
        @Schema(description = "견적 정보(해당 단계 이전에는 null)")
        CustomerQuoteView quote,
        @Schema(description = "결제 정보(해당 단계 이전에는 null)")
        PaymentDto.CustomerPaymentView payment
) {
}
