package net.likelion.bebc25.fixgo.as.service;

import net.likelion.bebc25.fixgo.as.dto.AsReceptionDto;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.repository.AsReceptionRepository;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.entity.Status;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.customer.repository.CustomerRepository;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.product.repository.ProductRepository;
import net.likelion.bebc25.fixgo.shipment.entity.ReturnMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("A/S 접수 서비스 테스트")
class AsReceptionServiceTest {

    @Mock
    AsReceptionRepository receptionRepository;
    @Mock
    AsStatusHistoryService asStatusHistoryService;
    @Mock
    CustomerRepository customerRepository;
    @Mock
    ProductRepository productRepository;
    @Mock
    MemberRepository memberRepository;
    @InjectMocks
    AsReceptionService service;

    // 상태 변경 테스트에서 쓰는 mock
    @Mock
    CustomUserDetails mockUserDetails;
    @Mock
    Member mockReceptionist;
    @Mock
    AsReception mockAsReception;

    // ===== 접수 등록 / 조회 (develop) =====

    @Test
    void blankKeywordDoesNotRestrictSearchAndFiltersArePreserved() {
        var pageable = PageRequest.of(0, 10);
        when(receptionRepository.search(null, AsReceptionStatus.RECEIVED, 2L, pageable)).thenReturn(Page.empty(pageable));

        var result = service.getReceptions(new AsReceptionDto.ReceptionSearchRequest("  ", AsReceptionStatus.RECEIVED, 2L), pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getSize()).isEqualTo(10);
        verify(receptionRepository).search(null, AsReceptionStatus.RECEIVED, 2L, pageable);
    }

    @Test
    void missingReceptionReturnsNotFound() {
        when(receptionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getReception(999L)).isInstanceOfSatisfying(ResponseStatusException.class, exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void technicianCannotCreateReception() {
        CustomUserDetails technician = user(Role.ROLE_TECHNICIAN);

        assertThatThrownBy(() -> service.createReception(technician, receptionRequest())).isInstanceOf(IllegalArgumentException.class).hasMessage("권한이 없습니다.");
        verify(receptionRepository, never()).save(any());
    }

    @Test
    void createReceptionRequiresExistingCustomerProductAndActiveTechnician() {
        CustomUserDetails receptionist = user(Role.ROLE_RECEPTIONIST);
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createReception(receptionist, receptionRequest())).isInstanceOf(NoSuchElementException.class).hasMessage("존재하지 않는 고객입니다.");

        Customer customer = customer();
        Product product = product();
        Member activeTechnician = member(3L, "김기사", Role.ROLE_TECHNICIAN);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.createReception(receptionist, receptionRequest())).isInstanceOf(NoSuchElementException.class).hasMessage("지원하지 않는 제품입니다.");

        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(memberRepository.findById(receptionist.getId())).thenReturn(Optional.of(receptionist.getMember()));
        when(memberRepository.findById(3L)).thenReturn(Optional.of(member(3L, "접수직원", Role.ROLE_RECEPTIONIST)));
        assertThatThrownBy(() -> service.createReception(receptionist, receptionRequest())).isInstanceOf(IllegalArgumentException.class).hasMessage("수리 기사만 담당자로 배정할 수 있습니다.");

        activeTechnician.changeStatus(Status.INACTIVE);
        when(memberRepository.findById(3L)).thenReturn(Optional.of(activeTechnician));
        assertThatThrownBy(() -> service.createReception(receptionist, receptionRequest())).isInstanceOf(IllegalArgumentException.class).hasMessage("활성 상태의 기사만 배정할 수 있습니다.");
        verify(receptionRepository, never()).save(any());
    }

    @Test
    void createReceptionSavesReceptionForReceptionist() {
        CustomUserDetails receptionist = user(Role.ROLE_RECEPTIONIST);
        Member technician = member(3L, "김기사", Role.ROLE_TECHNICIAN);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer()));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product()));
        when(memberRepository.findById(receptionist.getId())).thenReturn(Optional.of(receptionist.getMember()));
        when(memberRepository.findById(3L)).thenReturn(Optional.of(technician));
        when(receptionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AsReceptionDto.CreateReceptionResponse response = service.createReception(receptionist, receptionRequest());

        ArgumentCaptor<AsReception> captor = ArgumentCaptor.forClass(AsReception.class);
        verify(receptionRepository).save(captor.capture());
        assertThat(captor.getValue().getSymptom()).isEqualTo("충전불량");
        assertThat(captor.getValue().getReturnMethod()).isEqualTo(ReturnMethod.PICKUP);
        assertThat(captor.getValue().getTechnician()).isSameAs(technician);
        assertThat(captor.getValue().getStatus()).isEqualTo(AsReceptionStatus.RECEIVED);
        assertThat(response.receptionNumber()).startsWith("AS-");
        assertThat(response.technicianId()).isEqualTo(3L);
    }

    @Test
    void updateTechnicianRejectsClosedReceptionAndMissingTechnician() {
        when(receptionRepository.findByIdAndStatusNotIn(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateTechnician(10L, 3L)).isInstanceOf(NoSuchElementException.class).hasMessage("존재하지 않는 접수입니다.");

        @SuppressWarnings("unchecked") ArgumentCaptor<List<AsReceptionStatus>> statuses = ArgumentCaptor.forClass(List.class);
        verify(receptionRepository).findByIdAndStatusNotIn(eq(10L), statuses.capture());
        assertThat(statuses.getValue()).containsExactly(AsReceptionStatus.AWAITING_PAYMENT, AsReceptionStatus.AWAITING_SHIPMENT, AsReceptionStatus.AWAITING_PICKUP, AsReceptionStatus.COMPLETED);

        AsReception reception = reception();
        when(receptionRepository.findByIdAndStatusNotIn(any(), any())).thenReturn(Optional.of(reception));
        when(memberRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateTechnician(10L, 9L)).isInstanceOf(NoSuchElementException.class).hasMessage("존재하지 않는 직원입니다.");
        assertThat(reception.getTechnician().getName()).isEqualTo("김기사");
    }

    @Test
    void updateTechnicianChangesAssignedTechnician() {
        AsReception reception = reception();
        Member newTechnician = member(9L, "박기사", Role.ROLE_TECHNICIAN);
        when(receptionRepository.findByIdAndStatusNotIn(any(), any())).thenReturn(Optional.of(reception));
        when(memberRepository.findById(9L)).thenReturn(Optional.of(newTechnician));

        AsReceptionDto.DetailResponse response = service.updateTechnician(10L, 9L);

        assertThat(reception.getTechnician()).isSameAs(newTechnician);
        assertThat(response.technician().name()).isEqualTo("박기사");
    }

    // ===== 상태 이력 / 상태 변경 (내 작업) =====

    @Test
    @DisplayName("A/S 접수를 등록하고 최초 상태 이력을 저장한다.")
    void createReception_recordsInitialStatusHistory() {
        CustomUserDetails receptionist = user(Role.ROLE_RECEPTIONIST);
        Member technician = member(3L, "김기사", Role.ROLE_TECHNICIAN);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer()));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product()));
        when(memberRepository.findById(receptionist.getId())).thenReturn(Optional.of(receptionist.getMember()));
        when(memberRepository.findById(3L)).thenReturn(Optional.of(technician));
        when(receptionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.createReception(receptionist, receptionRequest());

        verify(asStatusHistoryService).record(any(AsReception.class), isNull(), eq(AsReceptionStatus.RECEIVED.name()), eq("A/S 최초 접수"), eq(receptionist.getMember()));
    }

    @Test
    @DisplayName("기술자는 A/S 접수를 등록할 수 없고 이력도 저장하지 않는다.")
    void createReception_forbidden_noHistory() {
        CustomUserDetails technician = user(Role.ROLE_TECHNICIAN);

        assertThrows(IllegalArgumentException.class, () -> service.createReception(technician, receptionRequest()));

        verifyNoInteractions(customerRepository, productRepository, receptionRepository, asStatusHistoryService);
    }

    @Test
    @DisplayName("존재하지 않는 고객이면 접수를 등록할 수 없고 이력도 저장하지 않는다.")
    void createReception_customerNotFound_noHistory() {
        CustomUserDetails receptionist = user(Role.ROLE_RECEPTIONIST);
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.createReception(receptionist, receptionRequest()));

        verify(receptionRepository, never()).save(any());
        verifyNoInteractions(asStatusHistoryService);
    }

    @Test
    @DisplayName("A/S 접수 상태를 변경하고 변경 이력을 저장한다.")
    void changeReceptionStatus_success() {
        Long receptionId = 1L;
        AsReceptionStatus currentStatus = AsReceptionStatus.RECEIVED;
        AsReceptionStatus newStatus = AsReceptionStatus.INSPECTION;
        String reason = "제품 점검 시작";

        when(receptionRepository.findById(receptionId)).thenReturn(Optional.of(mockAsReception));
        when(mockAsReception.getStatus()).thenReturn(currentStatus);
        when(mockUserDetails.getId()).thenReturn(1L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(mockReceptionist));

        service.changeReceptionStatus(receptionId, newStatus, reason, mockUserDetails);

        verify(mockAsReception).changeStatus(newStatus);
        verify(asStatusHistoryService).record(mockAsReception, currentStatus.name(), newStatus.name(), reason, mockReceptionist);
    }

    @Test
    @DisplayName("존재하지 않는 A/S 접수이면 상태를 변경할 수 없다.")
    void changeReceptionStatus_receptionNotFound() {
        when(receptionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.changeReceptionStatus(1L, AsReceptionStatus.INSPECTION, "점검 시작", mockUserDetails));

        verifyNoInteractions(memberRepository, asStatusHistoryService);
    }

    @Test
    @DisplayName("현재와 동일한 상태로 변경할 수 없다.")
    void changeReceptionStatus_sameStatus() {
        when(receptionRepository.findById(1L)).thenReturn(Optional.of(mockAsReception));
        when(mockAsReception.getStatus()).thenReturn(AsReceptionStatus.RECEIVED);

        assertThrows(IllegalArgumentException.class, () -> service.changeReceptionStatus(1L, AsReceptionStatus.RECEIVED, "중복 변경", mockUserDetails));

        verify(mockAsReception, never()).changeStatus(any());
        verifyNoInteractions(memberRepository, asStatusHistoryService);
    }

    @Test
    @DisplayName("변경한 직원이 존재하지 않으면 상태를 변경할 수 없다.")
    void changeReceptionStatus_memberNotFound() {
        when(receptionRepository.findById(1L)).thenReturn(Optional.of(mockAsReception));
        when(mockAsReception.getStatus()).thenReturn(AsReceptionStatus.RECEIVED);
        when(mockUserDetails.getId()).thenReturn(1L);
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.changeReceptionStatus(1L, AsReceptionStatus.INSPECTION, "점검 시작", mockUserDetails));

        verify(mockAsReception, never()).changeStatus(any());
        verifyNoInteractions(asStatusHistoryService);
    }

    // ===== 공통 헬퍼 =====

    private AsReceptionDto.CreateReceptionRequest receptionRequest() {
        return new AsReceptionDto.CreateReceptionRequest(1L, 2L, 3L, "충전불량", "충전이 안 됩니다", "메모", ReturnMethod.PICKUP, null);
    }

    private CustomUserDetails user(Role role) {
        return new CustomUserDetails(member(4L, "최접수", role));
    }

    private Member member(Long id, String name, Role role) {
        Member member = new Member(name + "@test.com", "password", name, "010-0000-0001", "서울시 강남구", role, "profile.png");
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private Customer customer() {
        return new Customer("한소희", "010-1111-2222", "hansohee@test.com", "12345", "서울", "강남구", 1L);
    }

    private Product product() {
        return new Product(1L, "SN-1004", LocalDateTime.now(), 1L);
    }

    private AsReception reception() {
        return new AsReception("AS-100", customer(), product(), member(4L, "최접수", Role.ROLE_RECEPTIONIST), member(3L, "김기사", Role.ROLE_TECHNICIAN), "충전불량", "충전이 안 됩니다", "메모", ReturnMethod.DELIVERY, null);
    }
}