package net.likelion.bebc25.fixgo.shipment.scheduler;

import net.likelion.bebc25.fixgo.shipment.entity.Shipment;
import net.likelion.bebc25.fixgo.shipment.entity.ShipmentStatus;
import net.likelion.bebc25.fixgo.shipment.repository.ShipmentRepository;
import net.likelion.bebc25.fixgo.shipment.service.ShipmentTrackingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentSchedulerTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ShipmentTrackingService shipmentTrackingService;

    @InjectMocks
    private ShipmentScheduler shipmentScheduler;

    @Test
    @DisplayName("배송 중인 건을 조회하고 배송 추적 서비스를 호출한다")
    void refreshShippingStatus_success() {
        // given
        Shipment shipment1 = mock(Shipment.class);
        Shipment shipment2 = mock(Shipment.class);

        when(shipment1.getId()).thenReturn(1L);
        when(shipment2.getId()).thenReturn(2L);

        when(shipmentRepository.findByStatus(ShipmentStatus.SHIPPING))
                .thenReturn(List.of(shipment1, shipment2));

        // when
        shipmentScheduler.refreshShippingStatus();

        // then
        verify(shipmentRepository)
                .findByStatus(ShipmentStatus.SHIPPING);

        verify(shipmentTrackingService).trackShipment(1L);
        verify(shipmentTrackingService).trackShipment(2L);
    }

    @Test
    @DisplayName("배송 중인 건이 없으면 배송 조회를 호출하지 않는다")
    void refreshShippingStatus_empty() {
        // given
        when(shipmentRepository.findByStatus(ShipmentStatus.SHIPPING))
                .thenReturn(List.of());

        // when
        shipmentScheduler.refreshShippingStatus();

        // then
        verify(shipmentRepository)
                .findByStatus(ShipmentStatus.SHIPPING);

        verifyNoInteractions(shipmentTrackingService);
    }

    @Test
    @DisplayName("배송 조회가 실패해도 다음 배송 건을 계속 조회한다")
    void refreshShippingStatus_continue_when_tracking_fails() {
        // given
        Shipment shipment1 = mock(Shipment.class);
        Shipment shipment2 = mock(Shipment.class);

        when(shipment1.getId()).thenReturn(1L);
        when(shipment2.getId()).thenReturn(2L);

        when(shipmentRepository.findByStatus(ShipmentStatus.SHIPPING))
                .thenReturn(List.of(shipment1, shipment2));

        doThrow(new IllegalStateException("유효하지 않은 운송장번호"))
                .when(shipmentTrackingService)
                .trackShipment(1L);

        // when & then
        assertThatCode(() -> shipmentScheduler.refreshShippingStatus())
                .doesNotThrowAnyException();

        verify(shipmentTrackingService).trackShipment(1L);
        verify(shipmentTrackingService).trackShipment(2L);
    }
}
