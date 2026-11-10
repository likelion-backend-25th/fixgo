package net.likelion.bebc25.fixgo.repair.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import net.likelion.bebc25.fixgo.repair.dto.preset.RepairPresetRequest;
import net.likelion.bebc25.fixgo.repair.dto.preset.RepairPresetResponse;
import net.likelion.bebc25.fixgo.repair.dto.preset.RepairPresetUpdate;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairPresetPartRequest;
import net.likelion.bebc25.fixgo.repair.entity.RepairPreset;
import net.likelion.bebc25.fixgo.repair.entity.part.Part;
import net.likelion.bebc25.fixgo.repair.repository.PartRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairPresetPartRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairPresetRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairPresetServiceTest {

    @Mock
    private RepairPresetRepository repairPresetRepository;

    @Mock
    private RepairPresetPartRepository repairPresetPartRepository;

    @Mock
    private PartRepository partRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @InjectMocks
    private RepairPresetService repairPresetService;

    private RepairPreset preset;

    // 공통 프리셋 Entity 생성
    private void setUpPreset() {
        preset = new RepairPreset(
                "액정 교체",
                "액정 파손 수리"
        );

        preset.setId(1L);
    }

    // 기본 단가 및 예상 작업시간 계산 Mock 설정
    private void setUpCalculations(Long basePrice, Long estimatedMinutes) {
        lenient().when(entityManager.createNativeQuery(anyString()))
                .thenReturn(query);

        lenient().when(query.setParameter(eq(1), any()))
                .thenReturn(query);

        // 첫 번째 호출: 기본 단가
        // 두 번째 호출: 예상 작업시간
        lenient().when(query.getSingleResult())
                .thenReturn(basePrice, estimatedMinutes);
    }

    @Test
    @DisplayName("수리 프리셋을 정상적으로 등록한다.")
    void createPresetSuccess() {
        // given
        setUpPreset();
        setUpCalculations(0L, 0L);

        RepairPresetRequest request = mock(RepairPresetRequest.class);

        when(request.getName()).thenReturn("액정 교체");
        when(request.getDescription()).thenReturn("액정 파손 수리");

        when(repairPresetRepository.save(any(RepairPreset.class)))
                .thenReturn(preset);

        // when
        RepairPresetResponse result =
                repairPresetService.createPreset(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("액정 교체");
        assertThat(result.description()).isEqualTo("액정 파손 수리");
        assertThat(result.basePrice()).isEqualTo(0L);
        assertThat(result.estimatedMinutes()).isEqualTo(0L);

        verify(repairPresetRepository).save(any(RepairPreset.class));
        verify(entityManager, times(2)).createNativeQuery(anyString());
        verify(repairPresetPartRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("부품과 기본 수량을 포함하여 수리 프리셋을 등록한다.")
    void createPresetWithPartsSuccess() {
        // given
        setUpPreset();
        setUpCalculations(50000L, 60L);

        RepairPresetRequest request = mock(RepairPresetRequest.class);

        RepairPresetPartRequest partRequest =
                new RepairPresetPartRequest(10L, 2);

        when(request.getName()).thenReturn("액정 교체");
        when(request.getDescription()).thenReturn("액정 파손 수리");
        when(request.getParts()).thenReturn(List.of(partRequest));

        Part part = new Part(
                "액정",
                25000,
                30,
                10
        );

        when(repairPresetRepository.save(any(RepairPreset.class)))
                .thenReturn(preset);

        when(partRepository.findById(10L))
                .thenReturn(Optional.of(part));

        // when
        RepairPresetResponse result =
                repairPresetService.createPreset(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.basePrice()).isEqualTo(50000L);
        assertThat(result.estimatedMinutes()).isEqualTo(60L);

        verify(partRepository).findById(10L);
        verify(repairPresetPartRepository).saveAll(anyList());
        verify(entityManager, times(2)).createNativeQuery(anyString());
    }

    @Test
    @DisplayName("등록된 수리 프리셋 전체 목록을 조회한다.")
    void getAllPresetsSuccess() {
        // given
        setUpPreset();
        setUpCalculations(50000L, 60L);

        when(repairPresetRepository.findAll())
                .thenReturn(List.of(preset));

        // when
        List<RepairPresetResponse> result =
                repairPresetService.getAllPresets();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("액정 교체");
        assertThat(result.get(0).basePrice()).isEqualTo(50000L);
        assertThat(result.get(0).estimatedMinutes()).isEqualTo(60L);

        verify(repairPresetRepository).findAll();
        verify(entityManager, times(2)).createNativeQuery(anyString());
    }

    @Test
    @DisplayName("수리 프리셋 ID로 상세 정보를 조회한다.")
    void getPresetSuccess() {
        // given
        setUpPreset();
        setUpCalculations(50000L, 60L);

        when(repairPresetRepository.findById(1L))
                .thenReturn(Optional.of(preset));

        // when
        RepairPresetResponse result =
                repairPresetService.getPreset(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("액정 교체");
        assertThat(result.basePrice()).isEqualTo(50000L);
        assertThat(result.estimatedMinutes()).isEqualTo(60L);

        verify(repairPresetRepository).findById(1L);
        verify(entityManager, times(2)).createNativeQuery(anyString());
    }

    @Test
    @DisplayName("수리 프리셋 정보를 정상적으로 수정한다.")
    void updatePresetSuccess() {
        // given
        setUpPreset();
        setUpCalculations(50000L, 60L);

        RepairPresetUpdate request = new RepairPresetUpdate(
                "배터리 교체",
                "배터리 수리"
        );

        when(repairPresetRepository.findById(1L))
                .thenReturn(Optional.of(preset));

        // when
        RepairPresetResponse result =
                repairPresetService.updatePreset(1L, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("배터리 교체");
        assertThat(result.description()).isEqualTo("배터리 수리");
        assertThat(result.basePrice()).isEqualTo(50000L);
        assertThat(result.estimatedMinutes()).isEqualTo(60L);

        verify(repairPresetRepository).findById(1L);
        verify(repairPresetRepository, never()).save(any(RepairPreset.class));

        // parts가 null이면 기존 부품 구성을 유지한다.
        verify(repairPresetPartRepository, never())
                .deleteByRepairPreset_Id(anyLong());
    }

    @Test
    @DisplayName("수리 프리셋 수정 시 부품 구성을 교체한다.")
    void updatePresetPartsSuccess() {
        // given
        setUpPreset();
        setUpCalculations(50000L, 60L);

        RepairPresetPartRequest partRequest =
                new RepairPresetPartRequest(10L, 2);

        RepairPresetUpdate request = new RepairPresetUpdate(
                "액정 교체",
                "액정 파손 수리",
                List.of(partRequest)
        );

        Part part = new Part(
                "액정",
                25000,
                30,
                10
        );

        when(repairPresetRepository.findById(1L))
                .thenReturn(Optional.of(preset));

        when(partRepository.findById(10L))
                .thenReturn(Optional.of(part));

        // when
        RepairPresetResponse result =
                repairPresetService.updatePreset(1L, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.basePrice()).isEqualTo(50000L);
        assertThat(result.estimatedMinutes()).isEqualTo(60L);

        InOrder inOrder = inOrder(
                repairPresetPartRepository,
                entityManager,
                partRepository
        );

        inOrder.verify(repairPresetPartRepository)
                .deleteByRepairPreset_Id(1L);
        inOrder.verify(entityManager).flush();
        inOrder.verify(partRepository).findById(10L);

        verify(repairPresetPartRepository).saveAll(anyList());
        verify(entityManager, times(2)).createNativeQuery(anyString());
    }

    @Test
    @DisplayName("수리 프리셋 삭제 시 연결된 부품 구성도 삭제한다.")
    void deletePresetSuccess() {
        // given
        setUpPreset();

        when(repairPresetRepository.findById(1L))
                .thenReturn(Optional.of(preset));

        // when
        repairPresetService.deletePreset(1L);

        // then
        InOrder inOrder = inOrder(
                repairPresetPartRepository,
                repairPresetRepository
        );

        inOrder.verify(repairPresetPartRepository)
                .deleteByRepairPreset_Id(1L);

        inOrder.verify(repairPresetRepository)
                .delete(preset);
    }

    @Test
    @DisplayName("존재하지 않는 수리 프리셋 조회 시 예외가 발생한다.")
    void getPresetNotFound() {
        // given
        when(repairPresetRepository.findById(999L))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> repairPresetService.getPreset(999L))
                .isInstanceOf(
                        org.springframework.web.server.ResponseStatusException.class
                )
                .hasMessageContaining("해당 수리 프리셋을 찾을 수 없습니다.");

        verify(repairPresetRepository).findById(999L);
    }

    @Test
    @DisplayName("연결된 부품의 단가와 기본 수량을 반영하여 기본 단가와 예상 작업시간을 반환한다.")
    void calculateBasePriceSuccess() {
        // given
        setUpPreset();
        setUpCalculations(75000L, 45L);

        when(repairPresetRepository.findById(1L))
                .thenReturn(Optional.of(preset));

        // when
        RepairPresetResponse result =
                repairPresetService.getPreset(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.basePrice()).isEqualTo(75000L);
        assertThat(result.estimatedMinutes()).isEqualTo(45L);

        verify(entityManager, times(2)).createNativeQuery(anyString());
        verify(query, times(2)).setParameter(1, 1L);
    }

    @Test
    @DisplayName("검색어가 공백이면 전체 프리셋을 조회한다.")
    void searchPresetsBlankTest() {
        // given
        when(repairPresetRepository.findAll())
                .thenReturn(List.of());

        // when
        List<RepairPresetResponse> result =
                repairPresetService.searchPresets(" ");

        // then
        assertThat(result).isEmpty();

        verify(repairPresetRepository).findAll();
        verify(repairPresetRepository, never())
                .findByNameContaining(anyString());
    }
}
