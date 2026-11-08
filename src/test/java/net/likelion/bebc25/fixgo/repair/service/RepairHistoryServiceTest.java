package net.likelion.bebc25.fixgo.repair.service;

import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairHistoryResponse;
import net.likelion.bebc25.fixgo.repair.entity.RepairWork;
import net.likelion.bebc25.fixgo.repair.entity.part.Part;
import net.likelion.bebc25.fixgo.repair.entity.part.RepairPartUsage;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.repository.RepairHistoryRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairPartUsageRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairWorkRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    @DisplayName("완료된 수리의 내역서에 작업, 사용 부품과 최종 금액이 포함된다")
    void getRepairStatement_returnsCompletedRepair() {
        Long receptionId = 1L;
        Long repairId = 10L;
        Long workId = 20L;
        Repair repair = mock(Repair.class);
        AsReception reception = mock(AsReception.class);
        Customer customer = mock(Customer.class);
        Member technician = mock(Member.class);
        Part part = mock(Part.class);
        RepairWork work = new RepairWork(repair, "모터 교체 프리셋", "모터 교체");
        ReflectionTestUtils.setField(work, "id", workId);
        RepairPartUsage usage = mock(RepairPartUsage.class);

        given(repairHistoryRepository.findByAsReceptionId(receptionId)).willReturn(Optional.of(repair));
        given(repair.getId()).willReturn(repairId);
        given(repair.getStatus()).willReturn(RepairStatus.COMPLETED);
        given(repair.getAsReception()).willReturn(reception);
        given(reception.getReceptionNumber()).willReturn("AS-1");
        given(reception.getCustomer()).willReturn(customer);
        given(customer.getName()).willReturn("테스트 고객");
        given(repair.getTechnician()).willReturn(technician);
        given(technician.getName()).willReturn("테스트 기사");
        given(repair.getWorkNote()).willReturn("교체 후 동작 확인");
        given(repair.getFinalAmount()).willReturn(90000);
        given(repairWorkRepository.findByRepairId(repairId)).willReturn(List.of(work));
        given(repairPartUsageRepository.findByRepairWorkIdIn(List.of(workId))).willReturn(List.of(usage));
        given(usage.getRepairWork()).willReturn(work);
        given(usage.getPart()).willReturn(part);
        given(part.getName()).willReturn("모터");
        given(usage.getQuantity()).willReturn(2);
        given(usage.getUnitPrice()).willReturn(30000);

        var response = repairHistoryService.getRepairStatement(receptionId);

        assertThat(response.receptionNumber()).isEqualTo("AS-1");
        assertThat(response.customerName()).isEqualTo("테스트 고객");
        assertThat(response.technicianName()).isEqualTo("테스트 기사");
        assertThat(response.workNote()).isEqualTo("교체 후 동작 확인");
        assertThat(response.issuedAt()).isNotNull();
        assertThat(response.finalAmount()).isEqualTo(90000);
        assertThat(response.repairWorks()).singleElement().satisfies(workResponse -> {
            assertThat(workResponse.workName()).isEqualTo("모터 교체");
            assertThat(workResponse.usedParts()).singleElement().satisfies(partResponse -> {
                assertThat(partResponse.partName()).isEqualTo("모터");
                assertThat(partResponse.quantity()).isEqualTo(2);
                assertThat(partResponse.totalPrice()).isEqualTo(60000);
            });
        });
    }

    @Test
    @DisplayName("연결된 수리 건이 없으면 수리내역서 조회 시 404를 반환한다")
    void getRepairStatement_repairNotFound() {
        given(repairHistoryRepository.findByAsReceptionId(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> repairHistoryService.getRepairStatement(1L))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
        verifyNoInteractions(repairWorkRepository, repairPartUsageRepository);
    }

    @Test
    @DisplayName("수리가 완료되지 않았으면 수리내역서 조회 시 409를 반환한다")
    void getRepairStatement_repairNotCompleted() {
        Repair repair = mock(Repair.class);
        given(repairHistoryRepository.findByAsReceptionId(1L)).willReturn(Optional.of(repair));
        given(repair.getStatus()).willReturn(RepairStatus.DIAGNOSING);

        assertThatThrownBy(() -> repairHistoryService.getRepairStatement(1L))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
        verifyNoInteractions(repairWorkRepository, repairPartUsageRepository);
    }

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
