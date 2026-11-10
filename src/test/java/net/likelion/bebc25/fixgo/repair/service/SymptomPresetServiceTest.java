package net.likelion.bebc25.fixgo.repair.service;

import net.likelion.bebc25.fixgo.repair.dto.SymptomPresetResponse;
import net.likelion.bebc25.fixgo.repair.entity.SymptomPreset;
import net.likelion.bebc25.fixgo.repair.repository.SymptomPresetRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SymptomPresetServiceTest {

    @Mock
    private SymptomPresetRepository symptomPresetRepository;

    @InjectMocks
    private SymptomPresetService symptomPresetService;

    @Test
    @DisplayName("고장 증상 프리셋 전체 조회 성공")
    void getSymptomPresets() {
        // given
        SymptomPreset preset1 = mock(SymptomPreset.class);
        SymptomPreset preset2 = mock(SymptomPreset.class);

        when(preset1.getId()).thenReturn(2L);
        when(preset1.getProductCategory()).thenReturn("스마트폰");
        when(preset1.getName()).thenReturn("화면 파손");

        when(preset2.getId()).thenReturn(1L);
        when(preset2.getProductCategory()).thenReturn("노트북");
        when(preset2.getName()).thenReturn("전원 불량");

        when(symptomPresetRepository.findAll(
                Sort.by(Sort.Direction.DESC, "id")
        )).thenReturn(List.of(preset1, preset2));

        // when
        List<SymptomPresetResponse> result =
                symptomPresetService.getSymptomPresets();

        // then
        assertThat(result).hasSize(2);

        assertThat(result.get(0).id()).isEqualTo(2L);
        assertThat(result.get(0).productCategory()).isEqualTo("스마트폰");
        assertThat(result.get(0).name()).isEqualTo("화면 파손");

        assertThat(result.get(1).id()).isEqualTo(1L);
        assertThat(result.get(1).productCategory()).isEqualTo("노트북");
        assertThat(result.get(1).name()).isEqualTo("전원 불량");

        verify(symptomPresetRepository).findAll(
                Sort.by(Sort.Direction.DESC, "id")
        );
    }

    @Test
    @DisplayName("증상명 또는 제품군으로 프리셋 검색 성공")
    void searchSymptomPresets() {
        // given
        String keyword = "화면";

        SymptomPreset preset = mock(SymptomPreset.class);

        when(preset.getId()).thenReturn(1L);
        when(preset.getProductCategory()).thenReturn("스마트폰");
        when(preset.getName()).thenReturn("화면 파손");

        when(symptomPresetRepository.search(keyword))
                .thenReturn(List.of(preset));

        // when
        List<SymptomPresetResponse> result =
                symptomPresetService.searchSymptomPresets(" 화면 ");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).productCategory()).isEqualTo("스마트폰");
        assertThat(result.get(0).name()).isEqualTo("화면 파손");

        verify(symptomPresetRepository).search(keyword);
    }

    @Test
    @DisplayName("검색 결과가 없으면 빈 목록을 반환한다")
    void searchSymptomPresetsEmpty() {
        // given
        when(symptomPresetRepository.search("존재하지 않는 증상"))
                .thenReturn(List.of());

        // when
        List<SymptomPresetResponse> result =
                symptomPresetService.searchSymptomPresets("존재하지 않는 증상");

        // then
        assertThat(result).isEmpty();

        verify(symptomPresetRepository).search("존재하지 않는 증상");
    }
}
