package net.likelion.bebc25.fixgo.repair.service;

import net.likelion.bebc25.fixgo.repair.dto.repairWork.RepairWorkRequest;
import net.likelion.bebc25.fixgo.repair.dto.repairWork.RepairWorkResponse;
import net.likelion.bebc25.fixgo.repair.dto.repairWork.RepairWorkUpdate;
import net.likelion.bebc25.fixgo.repair.entity.RepairPreset;
import net.likelion.bebc25.fixgo.repair.entity.RepairWork;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.repository.RepairPresetRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairWorkRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairWorkServiceTest {

    private static final String TEST_EMAIL = "test@example.com";

    @Mock
    private RepairWorkRepository repairWorkRepository;

    @Mock
    private RepairRepository repairRepository;

    @Mock
    private RepairPresetRepository repairPresetRepository;

    @InjectMocks
    private RepairWorkService repairWorkService;

    private Repair repair;
    private RepairPreset repairPreset;
    private RepairWork repairWork;

    @BeforeEach
    void setUp() {
        repair = mock(Repair.class);
        repairPreset = mock(RepairPreset.class);
        repairWork = mock(RepairWork.class);
    }

    // Response 변환에 필요한 Mock 데이터 설정
    private void prepareRepairWorkResponse() {

        when(repairWork.getId()).thenReturn(1L);
        when(repairWork.getRepair()).thenReturn(repair);
        when(repair.getId()).thenReturn(1L);
        when(repairWork.getRepairPresetName()).thenReturn("기본 점검");
        when(repairWork.getWorkName()).thenReturn("메인보드 점검");
    }

    @Test
    @DisplayName("수리 작업 내역 등록 성공")
    void createRepairWork() {

        // given
        RepairWorkRequest requestDto = new RepairWorkRequest(
                1L,
                1L,
                "메인보드 점검"
        );

        when(repairRepository.findById(1L))
                .thenReturn(Optional.of(repair));

        when(repairPresetRepository.findById(1L))
                .thenReturn(Optional.of(repairPreset));

        when(repairPreset.getName()).thenReturn("기본 점검");
        when(repair.getId()).thenReturn(1L);

        when(repairWorkRepository.save(any(RepairWork.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        RepairWorkResponse result =
                repairWorkService.createRepairWork(requestDto, TEST_EMAIL);

        // then
        assertThat(result).isNotNull();
        assertThat(result.repairId()).isEqualTo(1L);
        assertThat(result.repairPresetName()).isEqualTo("기본 점검");
        assertThat(result.workName()).isEqualTo("메인보드 점검");

        verify(repairRepository).findById(1L);
        verify(repairPresetRepository).findById(1L);
        verify(repairWorkRepository).save(any(RepairWork.class));
    }

    @Test
    @DisplayName("수리 작업 내역 전체 조회 성공")
    void getAllRepairWorks() {

        // given
        prepareRepairWorkResponse();

        List<RepairWork> repairWorks = List.of(repairWork);

        when(repairWorkRepository.findAll()).thenReturn(repairWorks);

        // when
        List<RepairWorkResponse> result =
                repairWorkService.getAllRepairWorks();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).repairPresetName()).isEqualTo("기본 점검");
        assertThat(result.get(0).workName()).isEqualTo("메인보드 점검");

        verify(repairWorkRepository).findAll();
    }

    @Test
    @DisplayName("수리 작업 내역 단건 조회 성공")
    void getRepairWork() {

        // given
        prepareRepairWorkResponse();

        when(repairWorkRepository.findById(1L))
                .thenReturn(Optional.of(repairWork));

        // when
        RepairWorkResponse result = repairWorkService.getRepairWork(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.repairId()).isEqualTo(1L);

        verify(repairWorkRepository).findById(1L);
    }

    @Test
    @DisplayName("특정 수리 건의 작업 내역 조회 성공")
    void getRepairWorksByRepairId() {

        // given
        prepareRepairWorkResponse();

        when(repairWorkRepository.findByRepair_Id(1L))
                .thenReturn(List.of(repairWork));

        // when
        List<RepairWorkResponse> result =
                repairWorkService.getRepairWorksByRepairId(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).repairId()).isEqualTo(1L);

        verify(repairWorkRepository).findByRepair_Id(1L);
    }

    @Test
    @DisplayName("작업명으로 검색 성공")
    void searchRepairWorks() {

        // given
        prepareRepairWorkResponse();

        when(repairWorkRepository.findByWorkNameContaining("점검"))
                .thenReturn(List.of(repairWork));

        // when
        List<RepairWorkResponse> result =
                repairWorkService.searchRepairWorks("점검");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).workName()).isEqualTo("메인보드 점검");

        verify(repairWorkRepository).findByWorkNameContaining("점검");
    }

    @Test
    @DisplayName("수리 작업 내역 수정 성공")
    void updateRepairWork() {

        // given
        prepareRepairWorkResponse();

        RepairWorkUpdate updateDto = new RepairWorkUpdate(
                1L,
                "메인보드 교체"
        );

        when(repairWorkRepository.findById(1L))
                .thenReturn(Optional.of(repairWork));

        when(repairPresetRepository.findById(1L))
                .thenReturn(Optional.of(repairPreset));

        when(repairPreset.getName()).thenReturn("부품 교체");

        // when
        RepairWorkResponse result =
                repairWorkService.updateRepairWork(1L, updateDto, TEST_EMAIL);

        // then
        assertThat(result).isNotNull();

        verify(repairWorkRepository).findById(1L);
        verify(repairPresetRepository).findById(1L);
        verify(repairWork).updateRepairWork(
                "부품 교체",
                "메인보드 교체"
        );
    }

    @Test
    @DisplayName("수리 작업 내역 삭제 성공")
    void deleteRepairWork() {

        // given
        when(repairWorkRepository.findById(1L))
                .thenReturn(Optional.of(repairWork));

        // when
        repairWorkService.deleteRepairWork(1L);

        // then
        verify(repairWorkRepository).findById(1L);
        verify(repairWorkRepository).delete(repairWork);
    }

    @Test
    @DisplayName("존재하지 않는 수리 작업 내역 조회 시 예외 발생")
    void getRepairWorkNotFound() {

        // given
        when(repairWorkRepository.findById(1L))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> repairWorkService.getRepairWork(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 작업 내역을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 수리 데이터로 작업 내역 등록 시 예외 발생")
    void createRepairWorkNotFound() {

        // given
        RepairWorkRequest requestDto = new RepairWorkRequest(
                1L,
                1L,
                "메인보드 점검"
        );

        when(repairRepository.findById(1L))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                                   repairWorkService.createRepairWork(requestDto, TEST_EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 수리 내역을 찾을 수 없습니다.");

        verify(repairWorkRepository, never()).save(any());
        verify(repairPresetRepository, never()).findById(any());
    }

    @Test
    @DisplayName("존재하지 않는 프리셋으로 작업 내역 등록 시 예외 발생")
    void createRepairWorkPresetNotFound() {

        // given
        RepairWorkRequest requestDto = new RepairWorkRequest(
                1L,
                1L,
                "메인보드 점검"
        );

        when(repairRepository.findById(1L))
                .thenReturn(Optional.of(repair));

        when(repairPresetRepository.findById(1L))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                                   repairWorkService.createRepairWork(requestDto, TEST_EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 수리 프리셋을 찾을 수 없습니다.");

        verify(repairWorkRepository, never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 프리셋으로 작업 내역 수정 시 예외 발생")
    void updateRepairWorkPresetNotFound() {

        // given
        RepairWorkUpdate updateDto = new RepairWorkUpdate(
                1L,
                "메인보드 교체"
        );

        when(repairWorkRepository.findById(1L))
                .thenReturn(Optional.of(repairWork));

        when(repairPresetRepository.findById(1L))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                                   repairWorkService.updateRepairWork(1L, updateDto, TEST_EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 수리 프리셋을 찾을 수 없습니다.");

        verify(repairWork, never()).updateRepairWork(any(), any());
    }
}