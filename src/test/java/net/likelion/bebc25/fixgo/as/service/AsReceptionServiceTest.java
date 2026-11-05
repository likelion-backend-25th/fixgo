package net.likelion.bebc25.fixgo.as.service;

import net.likelion.bebc25.fixgo.as.dto.AsReceptionDto;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.repository.AsReceptionRepository;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.customer.repository.CustomerRepository;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("A/S 접수 서비스 테스트")
class AsReceptionServiceTest {

    @Mock
    private AsReceptionRepository asReceptionRepository;

    @Mock
    private AsStatusHistoryService asStatusHistoryService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CustomUserDetails userDetails;

    @Mock
    private Member receptionist;

    @Mock
    private Member technician;

    @Mock
    private Customer customer;

    @Mock
    private Product product;

    @Mock
    private AsReception asReception;

    private AsReceptionService asReceptionService;

    @BeforeEach
    void setUp() {
        asReceptionService = new AsReceptionService(asReceptionRepository, asStatusHistoryService, customerRepository, productRepository, memberRepository);
    }

    @Test
    @DisplayName("A/S 접수를 등록하고 최초 상태 이력을 저장한다.")
    void createReception_success() {
        // given
        AsReceptionDto.CreateReceptionRequest request = mock(AsReceptionDto.CreateReceptionRequest.class);

        when(userDetails.getMember()).thenReturn(receptionist);
        when(userDetails.getId()).thenReturn(1L);
        when(receptionist.getRole()).thenReturn(Role.ROLE_ADMIN);

        when(request.customerId()).thenReturn(1L);
        when(request.productId()).thenReturn(1L);
        when(request.technicianId()).thenReturn(2L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(receptionist));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(technician));

        when(request.toEntity(customer, product, receptionist, technician)).thenReturn(asReception);
        when(asReceptionRepository.save(asReception)).thenReturn(asReception);

        // when
        AsReceptionDto.DetailResponse response = asReceptionService.createReception(userDetails, request);

        // then
        assertNotNull(response);

        verify(asReceptionRepository).save(asReception);
        verify(asStatusHistoryService).record(asReception, null, AsReceptionStatus.RECEIVED.name(), "A/S 최초 접수", receptionist);
    }

    @Test
    @DisplayName("기술자는 A/S 접수를 등록할 수 없다.")
    void createReception_forbidden() {
        // given
        when(userDetails.getMember()).thenReturn(receptionist);
        when(receptionist.getRole()).thenReturn(Role.ROLE_TECHNICIAN);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> asReceptionService.createReception(userDetails, mock(AsReceptionDto.CreateReceptionRequest.class)));

        verifyNoInteractions(customerRepository, productRepository, asReceptionRepository, asStatusHistoryService);
    }

    @Test
    @DisplayName("존재하지 않는 고객이면 접수를 등록할 수 없다.")
    void createReception_customerNotFound() {
        // given
        AsReceptionDto.CreateReceptionRequest request = mock(AsReceptionDto.CreateReceptionRequest.class);

        when(userDetails.getMember()).thenReturn(receptionist);
        when(receptionist.getRole()).thenReturn(Role.ROLE_ADMIN);
        when(request.customerId()).thenReturn(1L);

        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NoSuchElementException.class, () -> asReceptionService.createReception(userDetails, request));

        verify(customerRepository).findById(1L);
        verify(asReceptionRepository, never()).save(any());
        verifyNoInteractions(asStatusHistoryService);
    }

    @Test
    @DisplayName("A/S 접수 상태를 변경하고 변경 이력을 저장한다.")
    void changeReceptionStatus_success() {
        // given
        Long receptionId = 1L;
        AsReceptionStatus currentStatus = AsReceptionStatus.RECEIVED;
        AsReceptionStatus newStatus = AsReceptionStatus.INSPECTION;
        String reason = "제품 점검 시작";

        when(asReceptionRepository.findById(receptionId)).thenReturn(Optional.of(asReception));
        when(asReception.getStatus()).thenReturn(currentStatus);
        when(userDetails.getId()).thenReturn(1L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(receptionist));

        // when
        asReceptionService.changeReceptionStatus(receptionId, newStatus, reason, userDetails);

        // then
        verify(asReception).changeStatus(newStatus);

        verify(asStatusHistoryService).record(asReception, currentStatus.name(), newStatus.name(), reason, receptionist);
    }

    @Test
    @DisplayName("존재하지 않는 A/S 접수이면 상태를 변경할 수 없다.")
    void changeReceptionStatus_receptionNotFound() {
        // given
        when(asReceptionRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NoSuchElementException.class, () -> asReceptionService.changeReceptionStatus(1L, AsReceptionStatus.INSPECTION, "점검 시작", userDetails));

        verifyNoInteractions(memberRepository, asStatusHistoryService);
    }

    @Test
    @DisplayName("현재와 동일한 상태로 변경할 수 없다.")
    void changeReceptionStatus_sameStatus() {
        // given
        when(asReceptionRepository.findById(1L)).thenReturn(Optional.of(asReception));
        when(asReception.getStatus()).thenReturn(AsReceptionStatus.RECEIVED);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> asReceptionService.changeReceptionStatus(1L, AsReceptionStatus.RECEIVED, "중복 변경", userDetails));

        verify(asReception, never()).changeStatus(any());
        verifyNoInteractions(memberRepository, asStatusHistoryService);
    }

    @Test
    @DisplayName("변경한 직원이 존재하지 않으면 상태를 변경할 수 없다.")
    void changeReceptionStatus_memberNotFound() {
        // given
        when(asReceptionRepository.findById(1L)).thenReturn(Optional.of(asReception));
        when(asReception.getStatus()).thenReturn(AsReceptionStatus.RECEIVED);
        when(userDetails.getId()).thenReturn(1L);
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NoSuchElementException.class, () -> asReceptionService.changeReceptionStatus(1L, AsReceptionStatus.INSPECTION, "점검 시작", userDetails));

        verify(asReception, never()).changeStatus(any());
        verifyNoInteractions(asStatusHistoryService);
    }
}
