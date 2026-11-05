package net.likelion.bebc25.fixgo.repair.service;

import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.repository.AsReceptionRepository;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairRequest;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairResponse;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairDecision;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.entity.repair.WarrantyResult;
import net.likelion.bebc25.fixgo.repair.mapper.RepairMapper;
import net.likelion.bebc25.fixgo.repair.repository.RepairRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
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

    private RepairMapper repairMapper;

    private RepairService repairService;

    @Mock
    private RepairStatusService repairStatusService;

    @BeforeEach
    void setUp() {
        repairMapper = new RepairMapper();

        repairService = new RepairService(repairRepository, repairMapper, asReceptionRepository, memberRepository, repairStatusService);
    }

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

        Repair repair = new Repair(asReception, technician, WarrantyResult.FREE, RepairStatus.DIAGNOSING);

        repair.updateRepairInfo(asReception, technician, "메인보드 점검", "부품 교체", RepairDecision.REPAIRABLE, WarrantyResult.FREE, "보증 기간 내", 10000, 5000, 15000, null);

        ReflectionTestUtils.setField(repair, "id", 1L);

        return repair;
    }

    // 테스트용 요청 DTO 생성
    private RepairRequest createRequestDto() {

        RepairRequest dto = new RepairRequest();

        dto.setAsReceptionId(10L);
        dto.setTechnicianId(20L);
        dto.setDiagnosis("메인보드 점검");
        dto.setWorkNote("부품 교체");
        dto.setRepairDecision(RepairDecision.REPAIRABLE);
        dto.setWarrantyResult("FREE");
        dto.setReason("보증 기간 내");
        dto.setLaborRate(10000);
        dto.setInspectionCost(5000);
        dto.setFinalAmount(15000);
        dto.setStatus("REQUESTED");

        return dto;
    }

    // 수리 등록 테스트
    @Test
    void createRepairTest() {

        RepairRequest dto = createRequestDto();

        AsReception asReception = createAsReception();
        Member technician = createTechnician();

        when(asReceptionRepository.findById(10L)).thenReturn(Optional.of(asReception));

        when(memberRepository.findById(20L)).thenReturn(Optional.of(technician));

        Map<String, String> response = repairService.createRepair(dto, TEST_EMAIL);

        assertEquals("수리 정보가 등록되었습니다.", response.get("message"));

        verify(asReceptionRepository, times(1)).findById(10L);
        verify(memberRepository, times(1)).findById(20L);
        verify(repairRepository, times(1)).save(any(Repair.class));
    }

    // 전체 수리 조회 테스트
    @Test
    void getAllRepairsTest() {

        Repair repair = createRepair();

        when(repair.getAsReception().getId()).thenReturn(10L);
        when(repair.getTechnician().getId()).thenReturn(20L);

        List<Repair> repairs = List.of(repair);

        when(repairRepository.findAll()).thenReturn(repairs);

        List<RepairResponse> response = repairService.getAllRepairs();

        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).getId());
        assertEquals(10L, response.get(0).getAsReceptionId());
        assertEquals(20L, response.get(0).getTechnicianId());

        verify(repairRepository, times(1)).findAll();
    }

    // 수리 상세 조회 테스트
    @Test
    void getRepairTest() {

        Repair repair = createRepair();

        when(repair.getAsReception().getId()).thenReturn(10L);
        when(repair.getTechnician().getId()).thenReturn(20L);

        when(repairRepository.findById(1L)).thenReturn(Optional.of(repair));

        RepairResponse response = repairService.getRepair(1L);

        assertEquals(1L, response.getId());
        assertEquals(10L, response.getAsReceptionId());
        assertEquals(20L, response.getTechnicianId());

        verify(repairRepository, times(1)).findById(1L);
    }

    // A/S 접수번호로 수리 상세 조회 테스트
    @Test
    void getRepairByReceptionNumberTest() {

        Repair repair = createRepair();
        String receptionNumber = "AS-2026-1112";

        when(repair.getAsReception().getId()).thenReturn(10L);
        when(repair.getTechnician().getId()).thenReturn(20L);

        when(repairRepository.findByReceptionNumber(receptionNumber)).thenReturn(Optional.of(repair));

        RepairResponse response = repairService.getRepairByReceptionNumber(receptionNumber);

        assertEquals(1L, response.getId());
        assertEquals(10L, response.getAsReceptionId());
        assertEquals(20L, response.getTechnicianId());

        verify(repairRepository, times(1)).findByReceptionNumber(receptionNumber);
    }

    // 수리 수정 테스트
    @Test
    void updateRepairTest() {

        Repair repair = createRepair();
        RepairRequest dto = createRequestDto();

        dto.setDiagnosis("CPU 점검");
        dto.setWorkNote("CPU 교체");

        AsReception asReception = createAsReception();
        Member technician = createTechnician();

        when(repairRepository.findById(1L)).thenReturn(Optional.of(repair));

        when(asReceptionRepository.findById(10L)).thenReturn(Optional.of(asReception));

        when(memberRepository.findById(20L)).thenReturn(Optional.of(technician));

        RepairResponse response = repairService.updateRepair(1L, dto, TEST_EMAIL);

        assertEquals("CPU 점검", response.getDiagnosis());
        assertEquals("CPU 교체", response.getWorkNote());

        verify(repairRepository, times(1)).findById(1L);
        verify(asReceptionRepository, times(1)).findById(10L);
        verify(memberRepository, times(1)).findById(20L);
    }

    // 수리 삭제 테스트
    @Test
    void deleteRepairTest() {

        Repair repair = createRepair();

        when(repairRepository.findById(1L)).thenReturn(Optional.of(repair));

        repairService.deleteRepair(1L);

        verify(repairRepository, times(1)).delete(repair);
    }

    // 존재하지 않는 수리 정보 조회 테스트
    @Test
    void getRepairNotFoundTest() {

        when(repairRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> repairService.getRepair(999L));
    }

    // 수리 상태 변경 테스트
    // DIAGNOSING -> REQUESTED
    @Test
    @DisplayName("수리 상태 변경을 공통 상태 관리 서비스에 위임한다.")
    void changeRepairStatusTest() {

        Repair repair = createRepair();

        when(repairRepository.findById(1L)).thenReturn(Optional.of(repair));

        repairService.changeRepairStatus(1L, RepairStatus.REQUESTED, TEST_EMAIL);

        verify(repairStatusService).changeStatus(repair, RepairStatus.REQUESTED, TEST_EMAIL);

        assertEquals(RepairStatus.DIAGNOSING, repair.getStatus());
    }

    // 허용되지 않은 상태 변경 테스트
    // DIAGNOSING -> APPROVED
    @Test
    @DisplayName("허용되지 않은 상태 변경 요청을 공통 상태 관리 서비스에 위임한다.")
    void changeRepairStatusInvalidTest() {

        Repair repair = createRepair();

        when(repairRepository.findById(1L)).thenReturn(Optional.of(repair));

        doThrow(new IllegalArgumentException("허용되지 않은 상태 변경입니다.")).when(repairStatusService).changeStatus(repair, RepairStatus.APPROVED, TEST_EMAIL);

        assertThrows(IllegalArgumentException.class, () -> repairService.changeRepairStatus(1L, RepairStatus.APPROVED, TEST_EMAIL));

        verify(repairStatusService).changeStatus(repair, RepairStatus.APPROVED, TEST_EMAIL);

        assertEquals(RepairStatus.DIAGNOSING, repair.getStatus());
    }
}
