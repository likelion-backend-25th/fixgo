package net.likelion.bebc25.fixgo.repair.service;

import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("수리 상태 전이 테스트")
class RepairStatusServiceTest {

    private final String updatedBy = "tester";
    @Mock
    private Repair repair;
    private RepairStatusService repairStatusService;

    @BeforeEach
    void setUp() {
        repairStatusService = new RepairStatusService();
    }

    @Test
    @DisplayName("점검 중 상태에서 견적 요청 상태로 변경할 수 있다.")
    void changeDiagnosingToRequested() {
        when(repair.getStatus()).thenReturn(RepairStatus.DIAGNOSING);

        repairStatusService.changeStatus(repair, RepairStatus.REQUESTED, updatedBy);

        verify(repair).changeStatus(RepairStatus.REQUESTED, updatedBy);
    }

    @Test
    @DisplayName("견적 요청 상태에서 승인 상태로 변경할 수 있다.")
    void changeRequestedToApproved() {
        when(repair.getStatus()).thenReturn(RepairStatus.REQUESTED);

        repairStatusService.changeStatus(repair, RepairStatus.APPROVED, updatedBy);

        verify(repair).changeStatus(RepairStatus.APPROVED, updatedBy);
    }

    @Test
    @DisplayName("견적 요청 상태에서 거절 상태로 변경할 수 있다.")
    void changeRequestedToRejected() {
        when(repair.getStatus()).thenReturn(RepairStatus.REQUESTED);

        repairStatusService.changeStatus(repair, RepairStatus.REJECTED, updatedBy);

        verify(repair).changeStatus(RepairStatus.REJECTED, updatedBy);
    }

    @Test
    @DisplayName("승인 상태에서 수리 중 상태로 변경할 수 있다.")
    void changeApprovedToRepairing() {
        when(repair.getStatus()).thenReturn(RepairStatus.APPROVED);

        repairStatusService.changeStatus(repair, RepairStatus.REPAIRING, updatedBy);

        verify(repair).changeStatus(RepairStatus.REPAIRING, updatedBy);
    }

    @Test
    @DisplayName("수리 중 상태에서 완료 상태로 변경할 수 있다.")
    void changeRepairingToCompleted() {
        when(repair.getStatus()).thenReturn(RepairStatus.REPAIRING);

        repairStatusService.changeStatus(repair, RepairStatus.COMPLETED, updatedBy);

        verify(repair).changeStatus(RepairStatus.COMPLETED, updatedBy);
    }

    @Test
    @DisplayName("허용되지 않은 상태 변경을 요청하면 예외가 발생한다.")
    void changeInvalidStatus() {
        when(repair.getStatus()).thenReturn(RepairStatus.DIAGNOSING);

        assertThrows(IllegalArgumentException.class, () -> repairStatusService.changeStatus(repair, RepairStatus.COMPLETED, updatedBy));

        verify(repair, never()).changeStatus(any(), anyString());
    }

    @Test
    @DisplayName("거절된 상태에서는 다른 상태로 변경할 수 없다.")
    void changeRejectedStatus() {
        when(repair.getStatus()).thenReturn(RepairStatus.REJECTED);

        assertThrows(IllegalArgumentException.class, () -> repairStatusService.changeStatus(repair, RepairStatus.REQUESTED, updatedBy));

        verify(repair, never()).changeStatus(any(), anyString());
    }

    @Test
    @DisplayName("완료된 상태에서는 다른 상태로 변경할 수 없다.")
    void changeCompletedStatus() {
        when(repair.getStatus()).thenReturn(RepairStatus.COMPLETED);

        assertThrows(IllegalArgumentException.class, () -> repairStatusService.changeStatus(repair, RepairStatus.REQUESTED, updatedBy));

        verify(repair, never()).changeStatus(any(), anyString());
    }

    @Test
    @DisplayName("현재 상태와 동일한 상태로 변경할 수 없다.")
    void changeSameStatus() {
        when(repair.getStatus()).thenReturn(RepairStatus.DIAGNOSING);

        assertThrows(IllegalArgumentException.class, () -> repairStatusService.changeStatus(repair, RepairStatus.DIAGNOSING, updatedBy));

        verify(repair, never()).changeStatus(any(), anyString());
    }
}
