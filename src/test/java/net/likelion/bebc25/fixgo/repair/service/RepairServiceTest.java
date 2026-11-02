package net.likelion.bebc25.fixgo.repair.service;

import net.likelion.bebc25.fixgo.repair.dto.repair.RepairRequestDto;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairResponseDto;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.entity.repair.WarrantyResult;
import net.likelion.bebc25.fixgo.repair.mapper.RepairMapper;
import net.likelion.bebc25.fixgo.repair.repository.RepairRepository;
import org.junit.jupiter.api.BeforeEach;
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

    @Mock
    private RepairRepository repairRepository;

    private RepairMapper repairMapper;

    private RepairService repairService;

    @BeforeEach
    void setUp() {
        repairMapper = new RepairMapper();
        repairService = new RepairService(repairRepository, repairMapper);
    }

    // 테스트용 수리 정보 생성
    private Repair createRepair() {
        Repair repair = new Repair(10L, 20L, WarrantyResult.FREE, RepairStatus.REQUESTED);

        repair.updateRepairInfo(10L, 20L, "메인보드 점검", "부품 교체", "수리 진행", WarrantyResult.FREE, "보증 기간 내", 10000, 5000, 15000, null);

        // JPA에서 자동 생성되는 ID를 테스트에서 설정
        ReflectionTestUtils.setField(repair, "id", 1L);

        return repair;
    }

    // 테스트용 요청 DTO 생성
    private RepairRequestDto createRequestDto() {
        RepairRequestDto dto = new RepairRequestDto();
        dto.setAsRequestId(10L);
        dto.setTechnicianId(20L);
        dto.setDiagnosis("메인보드 점검");
        dto.setWorkNote("부품 교체");
        dto.setRepairDecision("수리 진행");
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
        RepairRequestDto dto = createRequestDto();

        Map<String, String> response = repairService.createRepair(dto);

        assertEquals("수리 정보가 등록되었습니다.", response.get("message"));
        verify(repairRepository, times(1)).save(any(Repair.class));
    }

    // 전체 수리 조회 테스트
    @Test
    void getAllRepairsTest() {
        List<Repair> repairs = List.of(createRepair());

        when(repairRepository.findAll()).thenReturn(repairs);

        List<RepairResponseDto> response = repairService.getAllRepairs();

        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).getId());
        verify(repairRepository, times(1)).findAll();
    }

    // 수리 상세 조회 테스트
    @Test
    void getRepairTest() {
        Repair repair = createRepair();

        when(repairRepository.findById(1L)).thenReturn(Optional.of(repair));

        RepairResponseDto response = repairService.getRepair(1L);

        assertEquals(1L, response.getId());
        assertEquals(10L, response.getAsRequestId());
        verify(repairRepository, times(1)).findById(1L);
    }

    // 수리 수정 테스트
    @Test
    void updateRepairTest() {
        Repair repair = createRepair();
        RepairRequestDto dto = createRequestDto();

        dto.setDiagnosis("CPU 점검");
        dto.setWorkNote("CPU 교체");

        when(repairRepository.findById(1L)).thenReturn(Optional.of(repair));

        RepairResponseDto response = repairService.updateRepair(1L, dto);

        assertEquals("CPU 점검", response.getDiagnosis());
        assertEquals("CPU 교체", response.getWorkNote());
        verify(repairRepository, times(1)).findById(1L);
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
}
