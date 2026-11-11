package net.likelion.bebc25.fixgo.shipment.entity;

import net.likelion.bebc25.fixgo.as.entity.AsReception;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShipmentTest {

    @Test
    @DisplayName("택배 배송을 등록하면 초기 상태는 READY이다")
    void create_delivery_success() {
        // given
        AsReception asReception = mock(AsReception.class);
        when(asReception.getReturnMethod())
                .thenReturn(ReturnMethod.DELIVERY);

        // when
        Shipment shipment = Shipment.create(
                asReception,
                "홍길동",
                "01012345678",
                "서울시 강남구",
                "101호",
                "CJ대한통운",
                "04",
                "123456789012"
        );

        // then
        assertThat(shipment.getReturnMethod())
                .isEqualTo(ReturnMethod.DELIVERY);
        assertThat(shipment.getStatus())
                .isEqualTo(ShipmentStatus.READY);
        assertThat(shipment.getCourier())
                .isEqualTo("CJ대한통운");
        assertThat(shipment.getCourierCode())
                .isEqualTo("04");
        assertThat(shipment.getTrackingNumber())
                .isEqualTo("123456789012");
    }

    @Test
    @DisplayName("방문 수령 등록 시 배송 정보는 null이다")
    void create_pickup_success() {
        // given
        AsReception asReception = mock(AsReception.class);
        when(asReception.getReturnMethod())
                .thenReturn(ReturnMethod.PICKUP);

        // when
        Shipment shipment = Shipment.create(
                asReception,
                "홍길동",
                "01012345678",
                "서울시 강남구",
                "101호",
                null,
                null,
                null
        );

        // then
        assertThat(shipment.getReturnMethod())
                .isEqualTo(ReturnMethod.PICKUP);
        assertThat(shipment.getStatus())
                .isEqualTo(ShipmentStatus.READY);
        assertThat(shipment.getCourier()).isNull();
        assertThat(shipment.getCourierCode()).isNull();
        assertThat(shipment.getTrackingNumber()).isNull();
    }

    @Test
    @DisplayName("배송을 시작하면 상태가 SHIPPING으로 변경된다")
    void markShipping_success() {
        // given
        AsReception asReception = mock(AsReception.class);
        when(asReception.getReturnMethod())
                .thenReturn(ReturnMethod.DELIVERY);

        Shipment shipment = Shipment.create(
                asReception,
                "홍길동",
                "01012345678",
                "서울시 강남구",
                "101호",
                "CJ대한통운",
                "04",
                "123456789012"
        );

        // when
        shipment.markShipping();

        // then
        assertThat(shipment.getStatus())
                .isEqualTo(ShipmentStatus.SHIPPING);
        assertThat(shipment.getShippedAt()).isNotNull();
    }

    @Test
    @DisplayName("배송 시작 시 운송장번호가 없으면 예외가 발생한다")
    void markShipping_fail_without_tracking_number() {
        // given
        AsReception asReception = mock(AsReception.class);
        when(asReception.getReturnMethod())
                .thenReturn(ReturnMethod.DELIVERY);

        Shipment shipment = Shipment.create(
                asReception,
                "홍길동",
                "01012345678",
                "서울시 강남구",
                "101호",
                "CJ대한통운",
                "04",
                null
        );

        // when & then
        assertThatThrownBy(shipment::markShipping)
                .isInstanceOf(IllegalStateException.class);

        assertThat(shipment.getStatus())
                .isEqualTo(ShipmentStatus.READY);
    }

    @Test
    @DisplayName("배송 중인 건을 완료 처리하면 DELIVERED로 변경된다")
    void markDelivered_success() {
        // given
        AsReception asReception = mock(AsReception.class);
        when(asReception.getReturnMethod())
                .thenReturn(ReturnMethod.DELIVERY);

        Shipment shipment = Shipment.create(
                asReception,
                "홍길동",
                "01012345678",
                "서울시 강남구",
                "101호",
                "CJ대한통운",
                "04",
                "123456789012"
        );
        shipment.markShipping();

        // when
        shipment.markDelivered();

        // then
        assertThat(shipment.getStatus())
                .isEqualTo(ShipmentStatus.DELIVERED);
    }
}
