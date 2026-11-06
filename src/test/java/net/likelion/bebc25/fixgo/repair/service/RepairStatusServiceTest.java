package net.likelion.bebc25.fixgo.repair.service;

import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.service.AsStatusHistoryService;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.repository.RepairRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("수리 상태 전이 테스트")
class RepairStatusServiceTest {

    private final String updatedBy = "tester";

    @Mock
    private Repair repair;

    @Mock
    private AsReception asReception;

    @Mock
    private Member changedBy;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AsStatusHistoryService asStatusHistoryService;

    @Mock
    private RepairRepository repairRepository;

    private RepairStatusService repairStatusService;

    @BeforeEach
    void setUp() {
        repairStatusService = new RepairStatusService(asStatusHistoryService, memberRepository, repairRepository);
    }

    // 잠금 조회 및 현재 상태 설정
    private void prepareStatusChange(RepairStatus currentStatus) {
        when(repair.getId()).thenReturn(1L);

        when(repairRepository.findByIdWithLock(1L)).thenReturn(Optional.of(repair));

        when(repair.getStatus()).thenReturn(currentStatus);
    }

    // 정상 상태 변경에 필요한 추가 설정
    private void prepareValidStatusChange() {
        when(repair.getAsReception()).thenReturn(asReception);

        when(memberRepository.findByEmail(updatedBy)).thenReturn(Optional.of(changedBy));
    }

    @Test
    @DisplayName("점검 중 상태에서 견적 요청 상태로 변경할 수 있다.")
    void changeDiagnosingToRequested() {
        prepareStatusChange(RepairStatus.DIAGNOSING);
        prepareValidStatusChange();

        repairStatusService.changeStatus(repair, RepairStatus.REQUESTED, updatedBy);

        verify(repairRepository).findByIdWithLock(1L);

        verify(repair).changeStatus(RepairStatus.REQUESTED, updatedBy);

        verify(asStatusHistoryService).record(asReception, "DIAGNOSING", "REQUESTED", null, changedBy);
    }

    @Test
    @DisplayName("견적 요청 상태에서 승인 상태로 변경할 수 있다.")
    void changeRequestedToApproved() {
        prepareStatusChange(RepairStatus.REQUESTED);
        prepareValidStatusChange();

        repairStatusService.changeStatus(repair, RepairStatus.APPROVED, updatedBy);

        verify(repair).changeStatus(RepairStatus.APPROVED, updatedBy);

        verify(asStatusHistoryService).record(asReception, "REQUESTED", "APPROVED", null, changedBy);
    }

    @Test
    @DisplayName("견적 요청 상태에서 거절 상태로 변경할 수 있다.")
    void changeRequestedToRejected() {
        prepareStatusChange(RepairStatus.REQUESTED);
        prepareValidStatusChange();

        repairStatusService.changeStatus(repair, RepairStatus.REJECTED, updatedBy);

        verify(repair).changeStatus(RepairStatus.REJECTED, updatedBy);

        verify(asStatusHistoryService).record(asReception, "REQUESTED", "REJECTED", null, changedBy);
    }

    @Test
    @DisplayName("승인 상태에서 수리 중 상태로 변경할 수 있다.")
    void changeApprovedToRepairing() {
        prepareStatusChange(RepairStatus.APPROVED);
        prepareValidStatusChange();

        repairStatusService.changeStatus(repair, RepairStatus.REPAIRING, updatedBy);

        verify(repair).changeStatus(RepairStatus.REPAIRING, updatedBy);

        verify(asStatusHistoryService).record(asReception, "APPROVED", "REPAIRING", null, changedBy);
    }

    @Test
    @DisplayName("수리 중 상태에서 완료 상태로 변경할 수 있다.")
    void changeRepairingToCompleted() {
        prepareStatusChange(RepairStatus.REPAIRING);
        prepareValidStatusChange();

        repairStatusService.changeStatus(repair, RepairStatus.COMPLETED, updatedBy);

        verify(repair).changeStatus(RepairStatus.COMPLETED, updatedBy);

        verify(asStatusHistoryService).record(asReception, "REPAIRING", "COMPLETED", null, changedBy);
    }

    @Test
    @DisplayName("허용되지 않은 상태 변경을 요청하면 예외가 발생한다.")
    void changeInvalidStatus() {
        prepareStatusChange(RepairStatus.DIAGNOSING);

        assertThrows(IllegalArgumentException.class, () -> repairStatusService.changeStatus(repair, RepairStatus.COMPLETED, updatedBy));

        verify(repair, never()).changeStatus(any(), anyString());
        verifyNoInteractions(memberRepository, asStatusHistoryService);
    }

    @Test
    @DisplayName("거절된 상태에서는 다른 상태로 변경할 수 없다.")
    void changeRejectedStatus() {
        prepareStatusChange(RepairStatus.REJECTED);

        assertThrows(IllegalArgumentException.class, () -> repairStatusService.changeStatus(repair, RepairStatus.REQUESTED, updatedBy));

        verify(repair, never()).changeStatus(any(), anyString());
        verifyNoInteractions(memberRepository, asStatusHistoryService);
    }

    @Test
    @DisplayName("완료된 상태에서는 다른 상태로 변경할 수 없다.")
    void changeCompletedStatus() {
        prepareStatusChange(RepairStatus.COMPLETED);

        assertThrows(IllegalArgumentException.class, () -> repairStatusService.changeStatus(repair, RepairStatus.REQUESTED, updatedBy));

        verify(repair, never()).changeStatus(any(), anyString());
        verifyNoInteractions(memberRepository, asStatusHistoryService);
    }

    @Test
    @DisplayName("현재 상태와 동일한 상태로 변경할 수 없다.")
    void changeSameStatus() {
        prepareStatusChange(RepairStatus.DIAGNOSING);

        assertThrows(IllegalArgumentException.class, () -> repairStatusService.changeStatus(repair, RepairStatus.DIAGNOSING, updatedBy));

        verify(repair, never()).changeStatus(any(), anyString());
        verifyNoInteractions(memberRepository, asStatusHistoryService);
    }

    @Test
    @DisplayName("존재하지 않는 수리 정보는 상태 변경할 수 없다.")
    void changeStatusWhenRepairNotFound() {
        when(repair.getId()).thenReturn(1L);

        when(repairRepository.findByIdWithLock(1L)).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () -> repairStatusService.changeStatus(repair, RepairStatus.REQUESTED, updatedBy));

        verifyNoInteractions(memberRepository, asStatusHistoryService);
    }
}
