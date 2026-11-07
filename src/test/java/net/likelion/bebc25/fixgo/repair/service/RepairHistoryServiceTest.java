package net.likelion.bebc25.fixgo.repair.service;

import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairHistoryResponse;
import net.likelion.bebc25.fixgo.repair.entity.RepairWork;
import net.likelion.bebc25.fixgo.repair.entity.part.Part;
import net.likelion.bebc25.fixgo.repair.entity.part.RepairPartUsage;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.repository.RepairHistoryRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairPartUsageRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairWorkRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairHistoryServiceTest {

    @Mock
    private RepairHistoryRepository repairHistoryRepository;

    @Mock
    private RepairWorkRepository repairWorkRepository;

    @Mock
    private RepairPartUsageRepository repairPartUsageRepository;

    @InjectMocks
    private RepairHistoryService repairHistoryService;

    @Test
    @DisplayName("A/S ID로 수리 정보와 작업별 실제 사용 부품을 조회한다")
    void getRepairHistory() {

        // given
        Long asReceptionId = 1L;
        Long repairId = 10L;
        Long repairWorkId = 20L;

        Repair repair = mock(Repair.class);
        AsReception asReception = mock(AsReception.class);

        given(repair.getId()).willReturn(repairId);
        given(repair.getAsReception()).willReturn(asReception);
        given(asReception.getId()).willReturn(asReceptionId);

        RepairWork repairWork = new RepairWork(
                repair,
                "모터 교체 프리셋",
                "모터 교체"
        );

        // 직접 생성한 엔티티의 ID 설정
        ReflectionTestUtils.setField(repairWork, "id", repairWorkId);

        Part part = mock(Part.class);

        given(part.getId()).willReturn(5L);
        given(part.getName()).willReturn("모터");

        RepairPartUsage usage = mock(RepairPartUsage.class);

        given(usage.getRepairWork()).willReturn(repairWork);
        given(usage.getPart()).willReturn(part);
        given(usage.getQuantity()).willReturn(2);
        given(usage.getUnitPrice()).willReturn(30000);
        given(usage.getCreatedAt()).willReturn(LocalDateTime.now());

        given(repairHistoryRepository.findByAsReceptionId(asReceptionId))
                .willReturn(Optional.of(repair));

        given(repairWorkRepository.findByRepairId(repairId))
                .willReturn(List.of(repairWork));

        given(repairPartUsageRepository.findByRepairWorkIdIn(List.of(repairWorkId)))
                .willReturn(List.of(usage));

        // when
        RepairHistoryResponse response =
                repairHistoryService.getRepairHistory(asReceptionId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.repairId()).isEqualTo(repairId);
        assertThat(response.asReceptionId()).isEqualTo(asReceptionId);

        assertThat(response.repairWorks()).hasSize(1);

        var workResponse = response.repairWorks().get(0);

        assertThat(workResponse.repairWorkId()).isEqualTo(repairWorkId);
        assertThat(workResponse.repairPresetName())
                .isEqualTo("모터 교체 프리셋");
        assertThat(workResponse.workName()).isEqualTo("모터 교체");

        assertThat(workResponse.usedParts()).hasSize(1);

        var partResponse = workResponse.usedParts().get(0);

        assertThat(partResponse.partId()).isEqualTo(5L);
        assertThat(partResponse.partName()).isEqualTo("모터");
        assertThat(partResponse.quantity()).isEqualTo(2);
        assertThat(partResponse.unitPrice()).isEqualTo(30000);
        assertThat(partResponse.totalPrice()).isEqualTo(60000);

        verify(repairHistoryRepository).findByAsReceptionId(asReceptionId);
        verify(repairWorkRepository).findByRepairId(repairId);
        verify(repairPartUsageRepository)
                .findByRepairWorkIdIn(List.of(repairWorkId));
    }

    @Test
    @DisplayName("수리 작업이 없으면 빈 작업 목록을 반환한다")
    void getRepairHistory_NoRepairWorks() {

        // given
        Long asReceptionId = 1L;
        Long repairId = 10L;

        Repair repair = mock(Repair.class);
        AsReception asReception = mock(AsReception.class);

        given(repair.getId()).willReturn(repairId);
        given(repair.getAsReception()).willReturn(asReception);
        given(asReception.getId()).willReturn(asReceptionId);

        given(repairHistoryRepository.findByAsReceptionId(asReceptionId))
                .willReturn(Optional.of(repair));

        given(repairWorkRepository.findByRepairId(repairId))
                .willReturn(List.of());

        // when
        RepairHistoryResponse response =
                repairHistoryService.getRepairHistory(asReceptionId);

        // then
        assertThat(response.repairWorks()).isEmpty();

        verify(repairPartUsageRepository, never())
                .findByRepairWorkIdIn(anyList());
    }
}
