package net.likelion.bebc25.fixgo.repair.service;

import net.likelion.bebc25.fixgo.repair.dto.repairWork.RepairWorkRequest;
import net.likelion.bebc25.fixgo.repair.dto.repairWork.RepairWorkResponse;
import net.likelion.bebc25.fixgo.repair.dto.repairWork.RepairWorkUpdate;
import net.likelion.bebc25.fixgo.repair.entity.RepairWork;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.mapper.RepairWorkMapper;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairWorkServiceTest {

    @Mock
    private RepairWorkRepository repairWorkRepository;

    @Mock
    private RepairRepository repairRepository;

    @Mock
    private RepairWorkMapper repairWorkMapper;

    @InjectMocks
    private RepairWorkService repairWorkService;

    private Repair repair;
    private RepairWork repairWork;
    private RepairWorkResponse responseDto;

    @BeforeEach
    void setUp() {
        repair = mock(Repair.class);
        repairWork = mock(RepairWork.class);
        responseDto = new RepairWorkResponse();
    }

    @Test
    @DisplayName("수리 작업 내역 등록 성공")
    void createRepairWork() {
        // given
        RepairWorkRequest requestDto = new RepairWorkRequest();
        requestDto.setRepairId(1L);
        requestDto.setRepairPresetName("기본 점검");
        requestDto.setWorkName("메인보드 점검");

        when(repairRepository.findById(1L)).thenReturn(Optional.of(repair));

        when(repairWorkMapper.toEntity(requestDto, repair)).thenReturn(repairWork);

        when(repairWorkRepository.save(repairWork)).thenReturn(repairWork);

        when(repairWorkMapper.toResponseDto(repairWork)).thenReturn(responseDto);

        // when
        RepairWorkResponse result = repairWorkService.createRepairWork(requestDto);

        // then
        assertThat(result).isNotNull();

        verify(repairRepository).findById(1L);
        verify(repairWorkRepository).save(repairWork);
        verify(repairWorkMapper).toResponseDto(repairWork);
    }

    @Test
    @DisplayName("수리 작업 내역 전체 조회 성공")
    void getAllRepairWorks() {
        // given
        List<RepairWork> repairWorks = List.of(repairWork);

        when(repairWorkRepository.findAll()).thenReturn(repairWorks);

        when(repairWorkMapper.toResponseDto(repairWork)).thenReturn(responseDto);

        // when
        List<RepairWorkResponse> result = repairWorkService.getAllRepairWorks();

        // then
        assertThat(result).hasSize(1);

        verify(repairWorkRepository).findAll();
    }

    @Test
    @DisplayName("수리 작업 내역 단건 조회 성공")
    void getRepairWork() {
        // given
        when(repairWorkRepository.findById(1L)).thenReturn(Optional.of(repairWork));

        when(repairWorkMapper.toResponseDto(repairWork)).thenReturn(responseDto);

        // when
        RepairWorkResponse result = repairWorkService.getRepairWork(1L);

        // then
        assertThat(result).isNotNull();

        verify(repairWorkRepository).findById(1L);
    }

    @Test
    @DisplayName("특정 수리 건의 작업 내역 조회 성공")
    void getRepairWorksByRepairId() {
        // given
        when(repairWorkRepository.findByRepairId(1L)).thenReturn(List.of(repairWork));

        when(repairWorkMapper.toResponseDto(repairWork)).thenReturn(responseDto);

        // when
        List<RepairWorkResponse> result = repairWorkService.getRepairWorksByRepairId(1L);

        // then
        assertThat(result).hasSize(1);

        verify(repairWorkRepository).findByRepairId(1L);
    }

    @Test
    @DisplayName("작업명으로 검색 성공")
    void searchRepairWorks() {
        // given
        when(repairWorkRepository.findByWorkName("점검")).thenReturn(List.of(repairWork));

        when(repairWorkMapper.toResponseDto(repairWork)).thenReturn(responseDto);

        // when
        List<RepairWorkResponse> result = repairWorkService.searchRepairWorks("점검");

        // then
        assertThat(result).hasSize(1);

        verify(repairWorkRepository).findByWorkName("점검");
    }

    @Test
    @DisplayName("수리 작업 내역 수정 성공")
    void updateRepairWork() {
        // given
        RepairWorkUpdate updateDto = new RepairWorkUpdate();
        updateDto.setRepairPresetName("부품 교체");
        updateDto.setWorkName("메인보드 교체");

        when(repairWorkRepository.findById(1L)).thenReturn(Optional.of(repairWork));

        when(repairWorkMapper.toResponseDto(repairWork)).thenReturn(responseDto);

        // when
        RepairWorkResponse result = repairWorkService.updateRepairWork(1L, updateDto);

        // then
        assertThat(result).isNotNull();

        verify(repairWorkRepository).findById(1L);
        verify(repairWorkMapper).updateEntity(repairWork, updateDto);
        verify(repairWork).updateAuditInfo("test");
    }

    @Test
    @DisplayName("수리 작업 내역 삭제 성공")
    void deleteRepairWork() {
        // given
        when(repairWorkRepository.findById(1L)).thenReturn(Optional.of(repairWork));

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
        when(repairWorkRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> repairWorkService.getRepairWork(1L)).isInstanceOf(IllegalArgumentException.class).hasMessage("해당 작업 내역을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 수리 데이터로 작업 내역 등록 시 예외 발생")
    void createRepairWorkNotFound() {
        // given
        RepairWorkRequest requestDto = new RepairWorkRequest();
        requestDto.setRepairId(1L);

        when(repairRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> repairWorkService.createRepairWork(requestDto)).isInstanceOf(IllegalArgumentException.class).hasMessage("해당 수리 내역을 찾을 수 없습니다.");

        verify(repairWorkRepository, never()).save(any());
    }
}