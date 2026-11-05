package net.likelion.bebc25.fixgo.repair.service;

import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.service.AsStatusHistoryService;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
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

    private RepairStatusService repairStatusService;

    @BeforeEach
    void setUp() {
        repairStatusService = new RepairStatusService(asStatusHistoryService, memberRepository);
    }

    // 상태 변경 공통 준비
    private void prepareStatusChange(RepairStatus currentStatus) {
        when(repair.getStatus()).thenReturn(currentStatus);
        when(repair.getAsReception()).thenReturn(asReception);
        when(memberRepository.findByEmail(updatedBy)).thenReturn(Optional.of(changedBy));
    }

    @Test
    @DisplayName("점검 중 상태에서 견적 요청 상태로 변경할 수 있다.")
    void changeDiagnosingToRequested() {
        prepareStatusChange(RepairStatus.DIAGNOSING);

        repairStatusService.changeStatus(repair, RepairStatus.REQUESTED, updatedBy);

        verify(repair).changeStatus(RepairStatus.REQUESTED, updatedBy);

        verify(asStatusHistoryService).record(asReception, "DIAGNOSING", "REQUESTED", null, changedBy);
    }

    @Test
    @DisplayName("견적 요청 상태에서 승인 상태로 변경할 수 있다.")
    void changeRequestedToApproved() {
        prepareStatusChange(RepairStatus.REQUESTED);

        repairStatusService.changeStatus(repair, RepairStatus.APPROVED, updatedBy);

        verify(repair).changeStatus(RepairStatus.APPROVED, updatedBy);

        verify(asStatusHistoryService).record(asReception, "REQUESTED", "APPROVED", null, changedBy);
    }

    @Test
    @DisplayName("견적 요청 상태에서 거절 상태로 변경할 수 있다.")
    void changeRequestedToRejected() {
        prepareStatusChange(RepairStatus.REQUESTED);

        repairStatusService.changeStatus(repair, RepairStatus.REJECTED, updatedBy);

        verify(repair).changeStatus(RepairStatus.REJECTED, updatedBy);

        verify(asStatusHistoryService).record(asReception, "REQUESTED", "REJECTED", null, changedBy);
    }

    @Test
    @DisplayName("승인 상태에서 수리 중 상태로 변경할 수 있다.")
    void changeApprovedToRepairing() {
        prepareStatusChange(RepairStatus.APPROVED);

        repairStatusService.changeStatus(repair, RepairStatus.REPAIRING, updatedBy);

        verify(repair).changeStatus(RepairStatus.REPAIRING, updatedBy);

        verify(asStatusHistoryService).record(asReception, "APPROVED", "REPAIRING", null, changedBy);
    }

    @Test
    @DisplayName("수리 중 상태에서 완료 상태로 변경할 수 있다.")
    void changeRepairingToCompleted() {
        prepareStatusChange(RepairStatus.REPAIRING);

        repairStatusService.changeStatus(repair, RepairStatus.COMPLETED, updatedBy);

        verify(repair).changeStatus(RepairStatus.COMPLETED, updatedBy);

        verify(asStatusHistoryService).record(asReception, "REPAIRING", "COMPLETED", null, changedBy);
    }

    @Test
    @DisplayName("허용되지 않은 상태 변경을 요청하면 예외가 발생한다.")
    void changeInvalidStatus() {
        when(repair.getStatus()).thenReturn(RepairStatus.DIAGNOSING);

        assertThrows(IllegalArgumentException.class, () -> repairStatusService.changeStatus(repair, RepairStatus.COMPLETED, updatedBy));

        verify(repair, never()).changeStatus(any(), anyString());
        verifyNoInteractions(memberRepository, asStatusHistoryService);
    }

    @Test
    @DisplayName("거절된 상태에서는 다른 상태로 변경할 수 없다.")
    void changeRejectedStatus() {
        when(repair.getStatus()).thenReturn(RepairStatus.REJECTED);

        assertThrows(IllegalArgumentException.class, () -> repairStatusService.changeStatus(repair, RepairStatus.REQUESTED, updatedBy));

        verify(repair, never()).changeStatus(any(), anyString());
        verifyNoInteractions(memberRepository, asStatusHistoryService);
    }

    @Test
    @DisplayName("완료된 상태에서는 다른 상태로 변경할 수 없다.")
    void changeCompletedStatus() {
        when(repair.getStatus()).thenReturn(RepairStatus.COMPLETED);

        assertThrows(IllegalArgumentException.class, () -> repairStatusService.changeStatus(repair, RepairStatus.REQUESTED, updatedBy));

        verify(repair, never()).changeStatus(any(), anyString());
        verifyNoInteractions(memberRepository, asStatusHistoryService);
    }

    @Test
    @DisplayName("현재 상태와 동일한 상태로 변경할 수 없다.")
    void changeSameStatus() {
        when(repair.getStatus()).thenReturn(RepairStatus.DIAGNOSING);

        assertThrows(IllegalArgumentException.class, () -> repairStatusService.changeStatus(repair, RepairStatus.DIAGNOSING, updatedBy));

        verify(repair, never()).changeStatus(any(), anyString());
        verifyNoInteractions(memberRepository, asStatusHistoryService);
    }
}
