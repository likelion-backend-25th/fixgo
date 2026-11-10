package net.likelion.bebc25.fixgo.AI.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 통계 Tool 모음.
 * ChatClient에는 {@link #all()}로 펼쳐서 전달해야 각 Tools의 @Tool이 등록된다.
 */
@Component
@RequiredArgsConstructor
public class StatisticsTools {

    private final ReceptionStatisticsTools receptionStatisticsTools;
    private final PaymentStatisticsTools paymentStatisticsTools;
    private final ShipmentStatisticsTools shipmentStatisticsTools;
    private final TechnicianStatisticsTools technicianStatisticsTools;
    private final ModelStatisticsTools modelStatisticsTools;
    private final LongPendingStatisticsTools longPendingStatisticsTools;

    public Object[] all() {
        return new Object[]{
                receptionStatisticsTools,
                paymentStatisticsTools,
                shipmentStatisticsTools,
                technicianStatisticsTools,
                modelStatisticsTools,
                longPendingStatisticsTools
        };
    }
}
