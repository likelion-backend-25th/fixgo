package net.likelion.bebc25.fixgo.shipment.service;

import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.repository.AsReceptionRepository;
import net.likelion.bebc25.fixgo.shipment.dto.ShipmentDto;
import net.likelion.bebc25.fixgo.shipment.entity.ReturnMethod;
import net.likelion.bebc25.fixgo.shipment.entity.ShipmentStatus;
import net.likelion.bebc25.fixgo.shipment.repository.ShipmentEventRepository;
import net.likelion.bebc25.fixgo.shipment.repository.ShipmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ShipmentEventRepository shipmentEventRepository;

    @Mock
    private AsReceptionRepository asReceptionRepository;

    @InjectMocks
    private ShipmentService shipmentService;


    // 배송 관리 목록 조회

    @Test
    @DisplayName("검색어와 배송 상태 및 수령 방법을 적용해 목록을 조회한다")
    void getShipmentManagementList_success() {
        // given
        Pageable pageable = PageRequest.of(0, 20);

        Page<ShipmentDto.ManagementResponse> expected =
                Page.empty(pageable);

        when(asReceptionRepository.findShipmentManagementList(
                eq("홍길동"),
                eq("SHIPPING"),
                eq("DELIVERY"),
                eq(AsReceptionStatus.AWAITING_SHIPMENT),
                eq(AsReceptionStatus.AWAITING_PICKUP),
                eq(ReturnMethod.DELIVERY),
                eq(ReturnMethod.PICKUP),
                eq(ShipmentStatus.READY),
                eq(ShipmentStatus.SHIPPING),
                eq(ShipmentStatus.DELIVERED),
                eq(ShipmentStatus.PICKED_UP),
                eq(pageable)
        )).thenReturn(expected);

        // when
        Page<ShipmentDto.ManagementResponse> result =
                shipmentService.getShipmentManagementList(
                        " 홍길동 ",
                        "shipping",
                        "delivery",
                        pageable
                );

        // then
        assertThat(result).isSameAs(expected);

        verify(asReceptionRepository).findShipmentManagementList(
                eq("홍길동"),
                eq("SHIPPING"),
                eq("DELIVERY"),
                eq(AsReceptionStatus.AWAITING_SHIPMENT),
                eq(AsReceptionStatus.AWAITING_PICKUP),
                eq(ReturnMethod.DELIVERY),
                eq(ReturnMethod.PICKUP),
                eq(ShipmentStatus.READY),
                eq(ShipmentStatus.SHIPPING),
                eq(ShipmentStatus.DELIVERED),
                eq(ShipmentStatus.PICKED_UP),
                eq(pageable)
        );
    }

    @Test
    @DisplayName("지원하지 않는 배송 상태는 예외가 발생한다")
    void getShipmentManagementList_invalid_status() {
        // given
        Pageable pageable = PageRequest.of(0, 20);

        // when & then
        assertThatThrownBy(() ->
                                   shipmentService.getShipmentManagementList(
                                           null,
                                           "INVALID",
                                           "ALL",
                                           pageable
                                   ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지원하지 않는 배송 관리 상태입니다.");

        verifyNoInteractions(asReceptionRepository);
    }

    @Test
    @DisplayName("지원하지 않는 수령 방법은 예외가 발생한다")
    void getShipmentManagementList_invalid_return_method() {
        // given
        Pageable pageable = PageRequest.of(0, 20);

        // when & then
        assertThatThrownBy(() ->
                                   shipmentService.getShipmentManagementList(
                                           null,
                                           "ALL",
                                           "INVALID",
                                           pageable
                                   ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지원하지 않는 수령 방법입니다.");

        verifyNoInteractions(asReceptionRepository);
    }

    @Test
    @DisplayName("검색어가 공백이면 null로 변환하여 전달한다")
    void getShipmentManagementList_blank_keyword() {
        // given
        Pageable pageable = PageRequest.of(0, 20);

        Page<ShipmentDto.ManagementResponse> expected =
                Page.empty(pageable);

        when(asReceptionRepository.findShipmentManagementList(
                isNull(),
                eq("ALL"),
                eq("ALL"),
                eq(AsReceptionStatus.AWAITING_SHIPMENT),
                eq(AsReceptionStatus.AWAITING_PICKUP),
                eq(ReturnMethod.DELIVERY),
                eq(ReturnMethod.PICKUP),
                eq(ShipmentStatus.READY),
                eq(ShipmentStatus.SHIPPING),
                eq(ShipmentStatus.DELIVERED),
                eq(ShipmentStatus.PICKED_UP),
                eq(pageable)
        )).thenReturn(expected);

        // when
        Page<ShipmentDto.ManagementResponse> result =
                shipmentService.getShipmentManagementList(
                        "   ",
                        null,
                        null,
                        pageable
                );

        // then
        assertThat(result.getContent()).isEmpty();

        verify(asReceptionRepository).findShipmentManagementList(
                isNull(),
                eq("ALL"),
                eq("ALL"),
                eq(AsReceptionStatus.AWAITING_SHIPMENT),
                eq(AsReceptionStatus.AWAITING_PICKUP),
                eq(ReturnMethod.DELIVERY),
                eq(ReturnMethod.PICKUP),
                eq(ShipmentStatus.READY),
                eq(ShipmentStatus.SHIPPING),
                eq(ShipmentStatus.DELIVERED),
                eq(ShipmentStatus.PICKED_UP),
                eq(pageable)
        );
    }


    // 택배 배송 등록 검증

    @Test
    @DisplayName("이미 배송이 등록된 접수는 중복 등록할 수 없다")
    void createShipment_duplicate_fail() {
        // given
        AsReception asReception = mock(AsReception.class);

        ShipmentDto.CreateRequest request =
                new ShipmentDto.CreateRequest(
                        1L,
                        "04",
                        "123456789012"
                );

        when(asReceptionRepository.findByIdWithLock(1L))
                .thenReturn(Optional.of(asReception));

        when(asReception.getId()).thenReturn(1L);

        when(asReception.getReturnMethod())
                .thenReturn(ReturnMethod.DELIVERY);

        when(asReception.getStatus())
                .thenReturn(AsReceptionStatus.AWAITING_SHIPMENT);

        when(shipmentRepository.existsByAsReceptionId(1L))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() ->
                                   shipmentService.createShipment(request)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 배송 또는 수령 내역이 등록되어 있습니다.");

        verify(shipmentRepository, never())
                .save(any());
    }

    @Test
    @DisplayName("방문 수령 접수는 택배 배송으로 등록할 수 없다")
    void createShipment_invalid_return_method() {
        // given
        AsReception asReception = mock(AsReception.class);

        ShipmentDto.CreateRequest request =
                new ShipmentDto.CreateRequest(
                        1L,
                        "04",
                        "123456789012"
                );

        when(asReceptionRepository.findByIdWithLock(1L))
                .thenReturn(Optional.of(asReception));

        when(asReception.getReturnMethod())
                .thenReturn(ReturnMethod.PICKUP);

        // when & then
        assertThatThrownBy(() ->
                                   shipmentService.createShipment(request)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("A/S 접수의 수령 방식이 일치하지 않습니다.");

        verify(shipmentRepository, never())
                .save(any());
    }

    @Test
    @DisplayName("배송 대기 상태가 아닌 접수는 택배 등록을 할 수 없다")
    void createShipment_invalid_status() {
        // given
        AsReception asReception = mock(AsReception.class);

        ShipmentDto.CreateRequest request =
                new ShipmentDto.CreateRequest(
                        1L,
                        "04",
                        "123456789012"
                );

        when(asReceptionRepository.findByIdWithLock(1L))
                .thenReturn(Optional.of(asReception));

        when(asReception.getReturnMethod())
                .thenReturn(ReturnMethod.DELIVERY);

        when(asReception.getStatus())
                .thenReturn(AsReceptionStatus.COMPLETED);

        // when & then
        assertThatThrownBy(() ->
                                   shipmentService.createShipment(request)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("현재 A/S접수 상태에서는 처리할 수 없습니다.");

        verify(shipmentRepository, never())
                .save(any());
    }

    @Test
    @DisplayName("존재하지 않는 A/S 접수는 배송을 등록할 수 없다")
    void createShipment_reception_not_found() {
        // given
        ShipmentDto.CreateRequest request =
                new ShipmentDto.CreateRequest(
                        999L,
                        "04",
                        "123456789012"
                );

        when(asReceptionRepository.findByIdWithLock(999L))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                                   shipmentService.createShipment(request)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A/S 접수 내역을 찾을 수 없습니다.");

        verify(shipmentRepository, never())
                .save(any());
    }
}
