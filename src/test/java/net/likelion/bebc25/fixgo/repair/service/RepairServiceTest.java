package net.likelion.bebc25.fixgo.repair.service;

import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.repository.AsReceptionRepository;
import net.likelion.bebc25.fixgo.as.service.AsStatusHistoryService;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairAbortRequest;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairMessageResponse;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairRequest;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairResponse;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairDecision;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.entity.repair.WarrantyResult;
import net.likelion.bebc25.fixgo.repair.repository.RepairRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairServiceTest {

    private static final String TEST_EMAIL = "test@example.com";

    @Mock
    private RepairRepository repairRepository;

    @Mock
    private AsReceptionRepository asReceptionRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RepairStatusService repairStatusService;

    @Mock
    private AsStatusHistoryService asStatusHistoryService;

    @Mock
    private PartService partService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private RepairService repairService;

    // 테스트용 A/S 접수 정보 생성
    private AsReception createAsReception() {
        return mock(AsReception.class);
    }

    // 테스트용 담당 기사 생성
    private Member createTechnician() {
        return mock(Member.class);
    }

    // 테스트용 수리 정보 생성
    private Repair createRepair() {

        AsReception asReception = createAsReception();
        Member technician = createTechnician();

        Repair repair = new Repair(
                asReception,
                technician,
                WarrantyResult.FREE,
                RepairStatus.DIAGNOSING
        );

        repair.updateRepairInfo(
                asReception,
                technician,
                "메인보드 점검",
                "부품 교체",
                RepairDecision.REPAIRABLE,
                WarrantyResult.FREE,
                "보증 기간 내",
                10000,
                5000,
                15000,
                null
        );

        ReflectionTestUtils.setField(repair, "id", 1L);

        return repair;
    }

    // 테스트용 요청 DTO 생성
    private RepairRequest createRequestDto() {

        return new RepairRequest(
                10L,
                20L,
                "메인보드 점검",
                "부품 교체",
                RepairDecision.REPAIRABLE,
                "FREE",
                "보증 기간 내",
                10000,
                5000,
                15000,
                "REQUESTED",
                null
        );
    }

    // ==========================================
    // 수리 등록 테스트
    // ==========================================

    @Test
    @DisplayName("수리 정보를 정상적으로 등록한다.")
    void createRepairTest() {

        RepairRequest dto = createRequestDto();

        AsReception asReception = createAsReception();
        Member technician = createTechnician();

        when(asReceptionRepository.findByIdWithLock(10L))
                .thenReturn(Optional.of(asReception));

        when(asReception.getTechnician()).thenReturn(technician);
        when(technician.getId()).thenReturn(20L);

        RepairMessageResponse response =
                repairService.createRepair(dto, TEST_EMAIL);

        assertEquals("수리 정보가 등록되었습니다.", response.message());

        verify(asReceptionRepository).findByIdWithLock(10L);
        verify(repairRepository).save(any(Repair.class));
    }

    @Test
    @DisplayName("접수 상태에서 수리를 등록하면 점검으로 바뀌고 점검 시작 이력이 남는다.")
    void createRepairStartsInspectionTest() {

        RepairRequest dto = createRequestDto();

        AsReception asReception = createAsReception();
        Member technician = createTechnician();
        Member changedBy = mock(Member.class);

        when(asReception.getStatus()).thenReturn(AsReceptionStatus.RECEIVED);
        when(asReceptionRepository.findByIdWithLock(10L)).thenReturn(Optional.of(asReception));
        when(asReception.getTechnician()).thenReturn(technician);
        when(technician.getId()).thenReturn(20L);
        when(memberRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(changedBy));

        repairService.createRepair(dto, TEST_EMAIL);

        verify(asReception).changeStatus(AsReceptionStatus.INSPECTION);
        verify(asStatusHistoryService).record(asReception, "RECEIVED", "INSPECTION", "점검 시작", changedBy);
    }

    @Test
    @DisplayName("이미 점검 이후 상태면 수리 등록 시 접수 상태를 바꾸지 않는다.")
    void createRepairKeepsLaterStatusTest() {

        RepairRequest dto = createRequestDto();

        AsReception asReception = createAsReception();
        Member technician = createTechnician();

        when(asReception.getStatus()).thenReturn(AsReceptionStatus.INSPECTION);
        when(asReceptionRepository.findByIdWithLock(10L)).thenReturn(Optional.of(asReception));
        when(asReception.getTechnician()).thenReturn(technician);
        when(technician.getId()).thenReturn(20L);

        repairService.createRepair(dto, TEST_EMAIL);

        verify(asReception, never()).changeStatus(any());
        verifyNoInteractions(asStatusHistoryService);
    }

    @Test
    @DisplayName("존재하지 않는 A/S 접수 ID로 수리 등록 시 예외가 발생한다.")
    void createRepairAsReceptionNotFoundTest() {

        RepairRequest dto = createRequestDto();

        when(asReceptionRepository.findByIdWithLock(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> repairService.createRepair(dto, TEST_EMAIL)
        );

        verify(asReceptionRepository).findByIdWithLock(10L);
        verifyNoInteractions(memberRepository);
        verify(repairRepository, never()).save(any(Repair.class));
    }

    @Test
    @DisplayName("접수 담당자와 다른 기사 ID로 수리를 등록할 수 없다.")
    void createRepairRejectsMismatchedTechnician() {

        RepairRequest dto = createRequestDto();

        AsReception asReception = createAsReception();

        when(asReceptionRepository.findByIdWithLock(10L))
                .thenReturn(Optional.of(asReception));
        Member assigned = createTechnician();
        when(asReception.getTechnician()).thenReturn(assigned);
        when(assigned.getId()).thenReturn(30L);

        assertThrows(
                IllegalArgumentException.class,
                () -> repairService.createRepair(dto, TEST_EMAIL)
        );

        verify(asReceptionRepository).findByIdWithLock(10L);
        verify(repairRepository, never()).save(any(Repair.class));
    }

    // ==========================================
    // 수리 목록 및 상세 조회 테스트
    // ==========================================

    @Test
    @DisplayName("전체 수리 목록을 조회한다.")
    void getAllRepairsTest() {

        Repair repair = createRepair();

        when(repair.getAsReception().getId()).thenReturn(10L);
        when(repair.getTechnician().getId()).thenReturn(20L);

        List<Repair> repairs = List.of(repair);

        when(repairRepository.findAllWithDetails()).thenReturn(repairs);

        List<RepairResponse> response = repairService.getAllRepairs();

        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).id());
        assertEquals(10L, response.get(0).asReceptionId());
        assertEquals(20L, response.get(0).technicianId());

        verify(repairRepository).findAllWithDetails();
    }

    @Test
    @DisplayName("수리 상세 정보를 조회한다.")
    void getRepairTest() {

        Repair repair = createRepair();

        when(repair.getAsReception().getId()).thenReturn(10L);
        when(repair.getTechnician().getId()).thenReturn(20L);

        when(repairRepository.findById(1L))
                .thenReturn(Optional.of(repair));

        RepairResponse response = repairService.getRepair(1L);

        assertEquals(1L, response.id());
        assertEquals(10L, response.asReceptionId());
        assertEquals(20L, response.technicianId());

        verify(repairRepository).findById(1L);
    }

    @Test
    @DisplayName("A/S 접수번호로 수리 상세 정보를 조회한다.")
    void getRepairByReceptionNumberTest() {

        Repair repair = createRepair();
        String receptionNumber = "AS-2026-1112";

        when(repair.getAsReception().getId()).thenReturn(10L);
        when(repair.getTechnician().getId()).thenReturn(20L);

        when(repairRepository.findByAsReception_ReceptionNumber(receptionNumber))
                .thenReturn(Optional.of(repair));

        RepairResponse response =
                repairService.getRepairByReceptionNumber(receptionNumber);

        assertEquals(1L, response.id());
        assertEquals(10L, response.asReceptionId());
        assertEquals(20L, response.technicianId());

        verify(repairRepository)
                .findByAsReception_ReceptionNumber(receptionNumber);
    }

    @Test
    @DisplayName("존재하지 않는 수리 정보를 조회하면 예외가 발생한다.")
    void getRepairNotFoundTest() {

        when(repairRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> repairService.getRepair(999L)
        );

        verify(repairRepository).findById(999L);
    }

    // ==========================================
    // 담당 기사별 수리 조회 테스트
    // ==========================================

    @Test
    @DisplayName("로그인한 기사에게 배정된 수리 목록을 조회한다.")
    void getMyRepairsTest() {

        Long technicianId = 20L;

        Repair repair = createRepair();

        when(repair.getAsReception().getId()).thenReturn(10L);
        when(repair.getTechnician().getId()).thenReturn(technicianId);

        when(repairRepository.findByAsReception_Technician_Id(technicianId))
                .thenReturn(List.of(repair));

        List<RepairResponse> response =
                repairService.getMyRepairs(technicianId);

        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).id());
        assertEquals(10L, response.get(0).asReceptionId());
        assertEquals(technicianId, response.get(0).technicianId());

        verify(repairRepository).findByAsReception_Technician_Id(technicianId);
    }

    @Test
    @DisplayName("로그인한 기사에게 배정된 수리 상세 정보를 조회한다.")
    void getMyRepairTest() {

        Long repairId = 1L;
        Long technicianId = 20L;

        Repair repair = createRepair();

        when(repair.getAsReception().getId()).thenReturn(10L);
        when(repair.getTechnician().getId()).thenReturn(technicianId);

        when(repairRepository.findByIdAndAsReception_Technician_Id(repairId, technicianId))
                .thenReturn(Optional.of(repair));

        RepairResponse response =
                repairService.getMyRepair(repairId, technicianId);

        assertEquals(repairId, response.id());
        assertEquals(10L, response.asReceptionId());
        assertEquals(technicianId, response.technicianId());

        verify(repairRepository)
                .findByIdAndAsReception_Technician_Id(repairId, technicianId);
    }

    @Test
    @DisplayName("본인에게 배정되지 않은 수리 건은 조회할 수 없다.")
    void getMyRepairNotAssignedTest() {

        Long repairId = 1L;
        Long technicianId = 30L;

        when(repairRepository.findByIdAndAsReception_Technician_Id(repairId, technicianId))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> repairService.getMyRepair(repairId, technicianId)
        );

        verify(repairRepository)
                .findByIdAndAsReception_Technician_Id(repairId, technicianId);
    }

    // ==========================================
    // 수리 정보 수정 테스트
    // ==========================================

    @Test
    @DisplayName("수리 정보를 정상적으로 수정한다.")
    void updateRepairTest() {

        Repair repair = createRepair();

        RepairRequest dto = new RepairRequest(
                10L,
                20L,
                "CPU 점검",
                "CPU 교체",
                RepairDecision.REPAIRABLE,
                "FREE",
                "보증 기간 내",
                10000,
                5000,
                15000,
                "REQUESTED",
                null
        );

        AsReception asReception = createAsReception();
        Member technician = createTechnician();

        when(repairRepository.findById(1L))
                .thenReturn(Optional.of(repair));

        when(asReceptionRepository.findByIdWithLock(10L))
                .thenReturn(Optional.of(asReception));

        when(asReception.getTechnician()).thenReturn(technician);
        when(technician.getId()).thenReturn(20L);

        RepairResponse response =
                repairService.updateRepair(1L, dto, TEST_EMAIL);

        assertEquals("CPU 점검", response.diagnosis());
        assertEquals("CPU 교체", response.workNote());
        assertEquals(technician, repair.getTechnician());

        verify(repairRepository).findById(1L);
        verify(asReceptionRepository).findByIdWithLock(10L);
    }

    @Test
    @DisplayName("존재하지 않는 A/S 접수 ID로 수리 수정 시 예외가 발생한다.")
    void updateRepairAsReceptionNotFoundTest() {

        Repair repair = createRepair();

        when(repairRepository.findById(1L))
                .thenReturn(Optional.of(repair));

        when(asReceptionRepository.findByIdWithLock(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> repairService.updateRepair(
                        1L,
                        createRequestDto(),
                        TEST_EMAIL
                )
        );

        verify(repairRepository).findById(1L);
        verify(asReceptionRepository).findByIdWithLock(10L);
        verifyNoInteractions(memberRepository);
    }

    @Test
    @DisplayName("접수 담당자와 다른 기사 ID로 수리를 수정할 수 없다.")
    void updateRepairRejectsMismatchedTechnician() {

        Repair repair = createRepair();
        AsReception reception = createAsReception();
        Member assigned = createTechnician();

        when(repairRepository.findById(1L))
                .thenReturn(Optional.of(repair));

        when(asReceptionRepository.findByIdWithLock(10L))
                .thenReturn(Optional.of(reception));
        when(reception.getTechnician()).thenReturn(assigned);
        when(assigned.getId()).thenReturn(30L);

        assertThrows(
                IllegalArgumentException.class,
                () -> repairService.updateRepair(
                        1L,
                        createRequestDto(),
                        TEST_EMAIL
                )
        );

        verify(repairRepository).findById(1L);
        verify(asReceptionRepository).findByIdWithLock(10L);
    }

    // ==========================================
    // 수리 삭제 테스트
    // ==========================================

    @Test
    @DisplayName("수리 정보를 삭제한다.")
    void deleteRepairTest() {

        Repair repair = createRepair();

        when(repairRepository.findById(1L))
                .thenReturn(Optional.of(repair));

        repairService.deleteRepair(1L);

        verify(repairRepository).delete(repair);
    }

    // ==========================================
    // 수리 상태 변경 테스트
    // ==========================================

    @Test
    @DisplayName("수리 상태 변경을 공통 상태 관리 서비스에 위임한다.")
    void changeRepairStatusTest() {

        Repair repair = createRepair();

        when(repairRepository.findById(1L))
                .thenReturn(Optional.of(repair));

        repairService.changeRepairStatus(
                1L,
                RepairStatus.REQUESTED,
                TEST_EMAIL
        );

        verify(repairStatusService)
                .changeStatus(repair, RepairStatus.REQUESTED, TEST_EMAIL);

        assertEquals(RepairStatus.DIAGNOSING, repair.getStatus());
    }

    @Test
    @DisplayName("허용되지 않은 상태 변경 요청은 예외가 발생한다.")
    void changeRepairStatusInvalidTest() {

        Repair repair = createRepair();

        when(repairRepository.findById(1L))
                .thenReturn(Optional.of(repair));

        doThrow(new IllegalArgumentException("허용되지 않은 상태 변경입니다."))
                .when(repairStatusService)
                .changeStatus(repair, RepairStatus.APPROVED, TEST_EMAIL);

        assertThrows(
                IllegalArgumentException.class,
                () -> repairService.changeRepairStatus(
                        1L,
                        RepairStatus.APPROVED,
                        TEST_EMAIL
                )
        );

        verify(repairStatusService)
                .changeStatus(repair, RepairStatus.APPROVED, TEST_EMAIL);

        assertEquals(RepairStatus.DIAGNOSING, repair.getStatus());
    }

    // ==========================================
    // REQ-REPAIR-05 수리 시작 테스트
    // ==========================================

    @Test
    @DisplayName("담당 기사가 수리를 시작한다.")
    void startRepairSuccessTest() {

        Repair repair = createRepair();

        when(repairRepository.findByIdAndAsReception_Technician_Id(1L, 1L))
                .thenReturn(Optional.of(repair));

        repairService.startRepair(1L, 1L, "technician");

        verify(repairRepository)
                .findByIdAndAsReception_Technician_Id(1L, 1L);

        verify(repairStatusService)
                .changeStatus(repair, RepairStatus.REPAIRING, "technician", "수리 시작");
    }

    // ==========================================
    // 수리 완료 테스트
    // ==========================================

    @Test
    @DisplayName("담당 기사가 수리 중인 건을 완료하면 상태 변경과 재고 차감이 호출된다.")
    void completeRepairSuccessTest() {

        Repair repair = createRepair();
        Customer customer = new Customer("고객", "010-1234-5678", "customer@example.com",
                "12345", "서울", "101호", 1L);
        when(repair.getAsReception().getCustomer()).thenReturn(customer);
        when(repair.getAsReception().getId()).thenReturn(100L);
        ReflectionTestUtils.setField(repair, "status", RepairStatus.REPAIRING);

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getId()).thenReturn(1L);
        when(userDetails.getUsername()).thenReturn("technician");

        when(repairRepository.findByIdAndAsReception_Technician_Id(1L, 1L))
                .thenReturn(Optional.of(repair));

        repairService.completeRepair(1L, userDetails);

        verify(repairStatusService)
                .changeStatus(repair, RepairStatus.COMPLETED, "technician", "수리 완료");
        verify(partService).updateMultiStockQuantity(userDetails, 1L);
        verify(eventPublisher).publishEvent(new RepairService.RepairCompletedEvent(
                100L, null, customer.getName(), customer.getEmail(), false));
    }

    @Test
    @DisplayName("수리 중이 아닌 건은 완료할 수 없고 재고도 차감하지 않는다.")
    void completeRepairNotRepairingTest() {

        Repair repair = createRepair(); // DIAGNOSING 상태

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getId()).thenReturn(1L);

        when(repairRepository.findByIdAndAsReception_Technician_Id(1L, 1L))
                .thenReturn(Optional.of(repair));

        assertThrows(
                IllegalArgumentException.class,
                () -> repairService.completeRepair(1L, userDetails)
        );

        verifyNoInteractions(repairStatusService);
        verifyNoInteractions(partService);
    }

    @Test
    @DisplayName("본인에게 배정되지 않은 수리 건은 완료할 수 없다.")
    void completeRepairNotAssignedTest() {

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getId()).thenReturn(30L);

        when(repairRepository.findByIdAndAsReception_Technician_Id(1L, 30L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> repairService.completeRepair(1L, userDetails)
        );

        verifyNoInteractions(repairStatusService);
        verifyNoInteractions(partService);
    }

    @Test
    @DisplayName("점검 중 수리를 중단하면 배송 또는 수령 대기로 넘긴다")
    void abortInspectionMovesToReturnWaiting() {
        Repair repair = createRepair();
        AsReception reception = repair.getAsReception();
        Member technician = repair.getTechnician();
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(repairRepository.findByIdWithLock(1L)).thenReturn(Optional.of(repair));
        when(reception.getId()).thenReturn(10L);
        when(reception.getStatus()).thenReturn(AsReceptionStatus.INSPECTION);
        when(reception.getTechnician()).thenReturn(technician);
        when(reception.statusAwaitingReturn()).thenReturn(AsReceptionStatus.AWAITING_SHIPMENT);
        when(asReceptionRepository.findByIdWithLock(10L)).thenReturn(Optional.of(reception));
        when(technician.getId()).thenReturn(1L);
        when(technician.getRole()).thenReturn(Role.ROLE_TECHNICIAN);
        when(userDetails.getId()).thenReturn(1L);
        when(userDetails.getMember()).thenReturn(technician);
        when(userDetails.getUsername()).thenReturn(TEST_EMAIL);
        when(memberRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(technician));

        repairService.abortInspection(
                1L,
                new RepairAbortRequest("REPAIR_IMPOSSIBLE", "부품 단종"),
                userDetails
        );

        assertEquals(RepairStatus.REJECTED, repair.getStatus());
        assertEquals(RepairDecision.UNREPAIRABLE, repair.getRepairDecision());
        assertEquals("수리 불가: 부품 단종", repair.getReasonForRejection());
        assertEquals(0, repair.getFinalAmount());
        verify(reception).changeStatus(AsReceptionStatus.AWAITING_SHIPMENT);
        verify(asStatusHistoryService, times(2)).record(any(), any(), any(), any(), any());
    }
}
