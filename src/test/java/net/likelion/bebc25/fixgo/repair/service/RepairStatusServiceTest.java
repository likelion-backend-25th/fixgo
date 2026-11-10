package net.likelion.bebc25.fixgo.repair.service;

import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.service.AsStatusHistoryService;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBillType;
import net.likelion.bebc25.fixgo.payment.service.PaymentBillService;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.entity.repair.WarrantyResult;
import net.likelion.bebc25.fixgo.repair.exception.RepairException;
import net.likelion.bebc25.fixgo.repair.repository.RepairRepository;
import net.likelion.bebc25.fixgo.shipment.entity.ReturnMethod;
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

    @Mock
    private PaymentBillService paymentBillService;

    private RepairStatusService repairStatusService;

    @BeforeEach
    void setUp() {
        repairStatusService = new RepairStatusService(
                asStatusHistoryService,
                memberRepository,
                repairRepository,
                paymentBillService
        );
    }

    // 잠금 조회 및 현재 상태 설정
    private void prepareStatusChange(RepairStatus currentStatus) {
        when(repair.getId()).thenReturn(1L);

        when(repairRepository.findByIdWithLock(1L))
                .thenReturn(Optional.of(repair));

        when(repair.getStatus()).thenReturn(currentStatus);
    }

    // 정상 상태 변경에 필요한 추가 설정
    private void prepareValidStatusChange() {
        when(repair.getAsReception()).thenReturn(asReception);

        when(memberRepository.findByEmail(updatedBy))
                .thenReturn(Optional.of(changedBy));
    }

    @Test
    @DisplayName("점검 중 상태에서 견적 요청 상태로 변경할 수 있다.")
    void changeDiagnosingToRequested() {
        prepareStatusChange(RepairStatus.DIAGNOSING);
        prepareValidStatusChange();
        when(asReception.getStatus()).thenReturn(AsReceptionStatus.INSPECTION);

        repairStatusService.changeStatus(
                repair,
                RepairStatus.REQUESTED,
                updatedBy
        );

        verify(repairRepository).findByIdWithLock(1L);

        verify(repair).changeStatus(RepairStatus.REQUESTED);

        verify(asStatusHistoryService).record(
                asReception,
                "DIAGNOSING",
                "REQUESTED",
                null,
                changedBy
        );

        // 접수 상태도 함께 바뀌고 별도 이력이 남는다.
        verify(asReception).changeStatus(AsReceptionStatus.AWAITING_QUOTE_APPROVAL);
        verify(asStatusHistoryService).record(
                asReception,
                "INSPECTION",
                "AWAITING_QUOTE_APPROVAL",
                null,
                changedBy
        );
    }

    @Test
    @DisplayName("견적 요청 상태에서 승인 상태로 변경할 수 있다.")
    void changeRequestedToApproved() {
        prepareStatusChange(RepairStatus.REQUESTED);
        prepareValidStatusChange();
        when(asReception.getStatus()).thenReturn(AsReceptionStatus.AWAITING_QUOTE_APPROVAL);

        repairStatusService.changeStatus(
                repair,
                RepairStatus.APPROVED,
                updatedBy
        );

        verify(repair).changeStatus(RepairStatus.APPROVED);

        verify(asStatusHistoryService).record(
                asReception,
                "REQUESTED",
                "APPROVED",
                null,
                changedBy
        );

        // 접수 상태도 함께 바뀌고 별도 이력이 남는다.
        verify(asReception).changeStatus(AsReceptionStatus.AWAITING_REPAIR);
        verify(asStatusHistoryService).record(
                asReception,
                "AWAITING_QUOTE_APPROVAL",
                "AWAITING_REPAIR",
                null,
                changedBy
        );
    }

    @Test
    @DisplayName("견적 요청 상태에서 거절 상태로 변경할 수 있다.")
    void changeRequestedToRejected() {
        prepareStatusChange(RepairStatus.REQUESTED);
        prepareValidStatusChange();
        when(asReception.getStatus()).thenReturn(AsReceptionStatus.AWAITING_QUOTE_APPROVAL);

        repairStatusService.changeStatus(
                repair,
                RepairStatus.REJECTED,
                updatedBy
        );

        verify(repair).changeStatus(RepairStatus.REJECTED);

        verify(asStatusHistoryService).record(
                asReception,
                "REQUESTED",
                "REJECTED",
                null,
                changedBy
        );

        // 접수 상태도 함께 바뀌고 별도 이력이 남는다.
        verify(asReception).changeStatus(AsReceptionStatus.AWAITING_PAYMENT);
        verify(asStatusHistoryService).record(
                asReception,
                "AWAITING_QUOTE_APPROVAL",
                "AWAITING_PAYMENT",
                null,
                changedBy
        );
    }

    @Test
    @DisplayName("승인 상태에서 수리 중 상태로 변경할 수 있다.")
    void changeApprovedToRepairing() {
        prepareStatusChange(RepairStatus.APPROVED);
        prepareValidStatusChange();
        when(asReception.getStatus()).thenReturn(AsReceptionStatus.AWAITING_REPAIR);

        repairStatusService.changeStatus(
                repair,
                RepairStatus.REPAIRING,
                updatedBy
        );

        verify(repair).changeStatus(RepairStatus.REPAIRING);

        verify(asStatusHistoryService).record(
                asReception,
                "APPROVED",
                "REPAIRING",
                null,
                changedBy
        );

        // 접수 상태도 함께 바뀌고 별도 이력이 남는다.
        verify(asReception).changeStatus(AsReceptionStatus.REPAIR_IN_PROGRESS);
        verify(asStatusHistoryService).record(
                asReception,
                "AWAITING_REPAIR",
                "REPAIR_IN_PROGRESS",
                null,
                changedBy
        );
    }

    @Test
    @DisplayName("무상 수리 완료 시 청구서를 발행하지 않고 배송 대기 상태로 변경한다.")
    void changeRepairingToCompleted() {
        prepareStatusChange(RepairStatus.REPAIRING);
        prepareValidStatusChange();

        when(asReception.getStatus())
                .thenReturn(AsReceptionStatus.REPAIR_IN_PROGRESS);
        when(asReception.getReturnMethod())
                .thenReturn(ReturnMethod.DELIVERY);
        when(repair.getWarrantyResult())
                .thenReturn(WarrantyResult.FREE);

        repairStatusService.changeStatus(
                repair,
                RepairStatus.COMPLETED,
                updatedBy
        );

        verify(repair).changeStatus(RepairStatus.COMPLETED);

        verify(asReception).changeStatus(
                AsReceptionStatus.AWAITING_SHIPMENT
        );

        verify(asStatusHistoryService).record(
                asReception,
                "REPAIRING",
                "COMPLETED",
                null,
                changedBy
        );

        verify(asStatusHistoryService).record(
                asReception,
                "REPAIR_IN_PROGRESS",
                "AWAITING_SHIPMENT",
                null,
                changedBy
        );

        verifyNoInteractions(paymentBillService);
    }

    @Test
    @DisplayName("유상 수리 완료 시 수리비 청구서를 발행하고 접수를 결제 대기로 전환한다.")
    void completePaidRepair() {
        prepareStatusChange(RepairStatus.REPAIRING);
        prepareValidStatusChange();
        when(repair.getWarrantyResult()).thenReturn(WarrantyResult.PAID);
        when(repair.getFinalAmount()).thenReturn(30_000);
        when(asReception.getId()).thenReturn(2L);
        when(asReception.getStatus()).thenReturn(AsReceptionStatus.REPAIR_IN_PROGRESS);

        repairStatusService.changeStatus(repair, RepairStatus.COMPLETED, updatedBy);

        verify(paymentBillService).createBill(2L, 30_000L, PaymentBillType.REPAIR);
        verify(asReception).changeStatus(AsReceptionStatus.AWAITING_PAYMENT);
        verify(asStatusHistoryService).record(
                asReception, "REPAIR_IN_PROGRESS", "AWAITING_PAYMENT", null, changedBy
        );
    }

    @Test
    @DisplayName("접수 상태가 이미 목표 상태면 수리 이력만 남기고 접수 이력은 중복으로 남기지 않는다.")
    void skipReceptionHistoryWhenAlreadySynced() {
        prepareStatusChange(RepairStatus.APPROVED);
        prepareValidStatusChange();
        when(asReception.getStatus()).thenReturn(AsReceptionStatus.REPAIR_IN_PROGRESS);

        repairStatusService.changeStatus(repair, RepairStatus.REPAIRING, updatedBy);

        verify(asStatusHistoryService).record(asReception, "APPROVED", "REPAIRING", null, changedBy);
        verify(asReception, never()).changeStatus(any());
        verifyNoMoreInteractions(asStatusHistoryService);
    }

    @Test
    @DisplayName("허용되지 않은 상태 변경을 요청하면 예외가 발생한다.")
    void changeInvalidStatus() {
        prepareStatusChange(RepairStatus.DIAGNOSING);

        assertThrows(
                RepairException.class,
                () -> repairStatusService.changeStatus(
                        repair,
                        RepairStatus.COMPLETED,
                        updatedBy
                )
        );

        verify(repair, never()).changeStatus(any());
        verifyNoInteractions(memberRepository, asStatusHistoryService);
    }

    @Test
    @DisplayName("거절된 상태에서는 다른 상태로 변경할 수 없다.")
    void changeRejectedStatus() {
        prepareStatusChange(RepairStatus.REJECTED);

        assertThrows(
                RepairException.class,
                () -> repairStatusService.changeStatus(
                        repair,
                        RepairStatus.REQUESTED,
                        updatedBy
                )
        );

        verify(repair, never()).changeStatus(any());
        verifyNoInteractions(memberRepository, asStatusHistoryService);
    }

    @Test
    @DisplayName("완료된 상태에서는 다른 상태로 변경할 수 없다.")
    void changeCompletedStatus() {
        prepareStatusChange(RepairStatus.COMPLETED);

        assertThrows(
                RepairException.class,
                () -> repairStatusService.changeStatus(
                        repair,
                        RepairStatus.REQUESTED,
                        updatedBy
                )
        );

        verify(repair, never()).changeStatus(any());
        verifyNoInteractions(memberRepository, asStatusHistoryService);
    }

    @Test
    @DisplayName("현재 상태와 동일한 상태로 변경할 수 없다.")
    void changeSameStatus() {
        prepareStatusChange(RepairStatus.DIAGNOSING);

        assertThrows(
                RepairException.class,
                () -> repairStatusService.changeStatus(
                        repair,
                        RepairStatus.DIAGNOSING,
                        updatedBy
                )
        );

        verify(repair, never()).changeStatus(any());
        verifyNoInteractions(memberRepository, asStatusHistoryService);
    }

    @Test
    @DisplayName("존재하지 않는 수리 정보는 상태 변경할 수 없다.")
    void changeStatusWhenRepairNotFound() {
        when(repair.getId()).thenReturn(1L);

        when(repairRepository.findByIdWithLock(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                RepairException.class,
                () -> repairStatusService.changeStatus(
                        repair,
                        RepairStatus.REQUESTED,
                        updatedBy
                )
        );

        verifyNoInteractions(memberRepository, asStatusHistoryService);
    }
}
