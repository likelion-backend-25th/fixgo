package net.likelion.bebc25.fixgo.repair.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import net.likelion.bebc25.fixgo.repair.dto.preset.RepairPresetRequest;
import net.likelion.bebc25.fixgo.repair.dto.preset.RepairPresetResponse;
import net.likelion.bebc25.fixgo.repair.dto.preset.RepairPresetUpdate;
import net.likelion.bebc25.fixgo.repair.entity.RepairPreset;
import net.likelion.bebc25.fixgo.repair.repository.RepairPresetRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private EntityManager entityManager;

    @Mock
    private Query query;

    @InjectMocks
    private RepairPresetService repairPresetService;

    private RepairPreset preset;

    // 공통 Entity 생성
    private void setUpPreset() {
        preset = new RepairPreset(
                "액정 교체",
                "액정 파손 수리"
        );

        preset.setId(1L);
    }

    // 단가 계산 Mock 설정
    private void setUpBasePrice(Long basePrice) {
        when(entityManager.createNativeQuery(anyString()))
                .thenReturn(query);

        when(query.setParameter(eq(1), any()))
                .thenReturn(query);

        when(query.getSingleResult())
                .thenReturn(basePrice);
    }

    @Test
    @DisplayName("수리 프리셋을 정상적으로 등록한다.")
    void createPresetSuccess() {
        // given
        setUpPreset();
        setUpBasePrice(50000L);

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
        assertThat(result.basePrice()).isEqualTo(50000L);

        verify(repairPresetRepository).save(any(RepairPreset.class));
        verify(entityManager).createNativeQuery(anyString());
    }

    @Test
    @DisplayName("등록된 수리 프리셋 전체 목록을 조회한다.")
    void getAllPresetsSuccess() {
        // given
        setUpPreset();
        setUpBasePrice(50000L);

        when(repairPresetRepository.findAll())
                .thenReturn(List.of(preset));

        // when
        List<RepairPresetResponse> result =
                repairPresetService.getAllPresets();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("액정 교체");
        assertThat(result.get(0).basePrice()).isEqualTo(50000L);

        verify(repairPresetRepository).findAll();
        verify(entityManager).createNativeQuery(anyString());
    }

    @Test
    @DisplayName("수리 프리셋 ID로 상세 정보를 조회한다.")
    void getPresetSuccess() {
        // given
        setUpPreset();
        setUpBasePrice(50000L);

        when(repairPresetRepository.findById(1L))
                .thenReturn(Optional.of(preset));

        // when
        RepairPresetResponse result =
                repairPresetService.getPreset(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("액정 교체");
        assertThat(result.basePrice()).isEqualTo(50000L);

        verify(repairPresetRepository).findById(1L);
    }

    @Test
    @DisplayName("수리 프리셋 정보를 정상적으로 수정한다.")
    void updatePresetSuccess() {
        // given
        setUpPreset();
        setUpBasePrice(50000L);

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

        verify(repairPresetRepository).findById(1L);
        verify(repairPresetRepository, never()).save(any(RepairPreset.class));
    }

    @Test
    @DisplayName("수리 프리셋을 정상적으로 삭제한다.")
    void deletePresetSuccess() {
        // given
        setUpPreset();

        when(repairPresetRepository.findById(1L))
                .thenReturn(Optional.of(preset));

        // when
        repairPresetService.deletePreset(1L);

        // then
        verify(repairPresetRepository).delete(preset);
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
    @DisplayName("연결된 부품의 단가를 합산하여 기본 단가로 반환한다.")
    void calculateBasePriceSuccess() {
        // given
        setUpPreset();
        setUpBasePrice(75000L);

        when(repairPresetRepository.findById(1L))
                .thenReturn(Optional.of(preset));

        // when
        RepairPresetResponse result =
                repairPresetService.getPreset(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.basePrice()).isEqualTo(75000L);

        verify(entityManager).createNativeQuery(anyString());
        verify(query).setParameter(1, 1L);
    }

    @Test
    @DisplayName("검색어가 공백이면 전체 프리셋 조회")
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