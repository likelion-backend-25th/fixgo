package net.likelion.bebc25.fixgo.repair.service;

import lombok.RequiredArgsConstructor;
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
import net.likelion.bebc25.fixgo.repair.exception.RepairErrorCode;
import net.likelion.bebc25.fixgo.repair.exception.RepairException;
import net.likelion.bebc25.fixgo.repair.repository.RepairRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RepairStatusService {

    private final AsStatusHistoryService asStatusHistoryService;
    private final MemberRepository memberRepository;
    private final RepairRepository repairRepository;
    private final PaymentBillService paymentBillService;

    @Transactional
    public void changeStatus(Repair repair, RepairStatus newStatus, String updatedBy) {
        changeStatus(repair, newStatus, updatedBy, null);
    }

    @Transactional
    public void changeStatus(
            Repair repair,
            RepairStatus newStatus,
            String updatedBy,
            String reason
    ) {
        repair = repairRepository.findByIdWithLock(repair.getId())
                .orElseThrow(() -> new RepairException(
                        RepairErrorCode.REPAIR_NOT_FOUND,
                        "수리 정보를 찾을 수 없습니다."
                ));

        RepairStatus currentStatus = repair.getStatus();

        if (currentStatus == newStatus) {
            throw new RepairException(
                    RepairErrorCode.ALREADY_SAME_REPAIR_STATUS,
                    "이미 해당 상태로 처리되어 있습니다."
            );
        }

        boolean isValidTransition = switch (currentStatus) {
            case DIAGNOSING -> newStatus == RepairStatus.REQUESTED;
            case REQUESTED -> newStatus == RepairStatus.APPROVED
                    || newStatus == RepairStatus.REJECTED;
            case APPROVED -> newStatus == RepairStatus.REPAIRING;
            case REPAIRING -> newStatus == RepairStatus.COMPLETED;
            case REJECTED, COMPLETED -> false;
        };

        if (!isValidTransition) {
            throw new RepairException(
                    RepairErrorCode.INVALID_REPAIR_STATUS_TRANSITION,
                    "허용되지 않은 상태 변경입니다. 현재 상태: "
                            + currentStatus + ", 변경 요청 상태: " + newStatus
            );
        }

        Member changedBy = null;

        if (updatedBy != null && !updatedBy.isBlank()) {
            changedBy = memberRepository.findByEmail(updatedBy)
                    .orElseThrow(() -> new RepairException(
                            RepairErrorCode.CHANGED_BY_NOT_FOUND,
                            "변경한 직원을 찾을 수 없습니다."
                    ));
        }

        repair.changeStatus(newStatus);

        AsReception asReception = repair.getAsReception();

        // 수리 상태 이력. 기사 통계(수리 소요 시간)가 REPAIRING → COMPLETED 행을 사용하므로 유지한다.
        asStatusHistoryService.record(
                asReception,
                currentStatus.name(),
                newStatus.name(),
                reason,
                changedBy
        );

        // 접수 상태 동기화. 접수 상태 전환은 별도 행으로 남기며, 고객 포탈 진행이력은 이 행만 보여 준다.
        AsReceptionStatus receptionStatus = toReceptionStatus(newStatus);
        if (newStatus == RepairStatus.COMPLETED
                && repair.getWarrantyResult() == WarrantyResult.PAID
                && repair.getFinalAmount() > 0) {
            paymentBillService.createBill(
                    asReception.getId(),
                    (long) repair.getFinalAmount(),
                    PaymentBillType.REPAIR
            );
            receptionStatus = AsReceptionStatus.AWAITING_PAYMENT;
        }

        if (receptionStatus == null) {
            return;
        }

        AsReceptionStatus fromReceptionStatus = asReception.getStatus();
        if (fromReceptionStatus == receptionStatus) {
            return;
        }

        asReception.changeStatus(receptionStatus);

        asStatusHistoryService.record(
                asReception,
                fromReceptionStatus.name(),
                receptionStatus.name(),
                reason,
                changedBy
        );
    }

    /**
     * 수리 상태 변경에 따라 바뀌어야 하는 접수 상태. 접수 상태가 바뀌지 않으면 null.
     * 견적 거부 시에는 점검비를 결제해야 하므로 결제 대기로 보낸다.
     */
    static AsReceptionStatus toReceptionStatus(RepairStatus repairStatus) {
        return switch (repairStatus) {
            case REQUESTED -> AsReceptionStatus.AWAITING_QUOTE_APPROVAL;
            case APPROVED -> AsReceptionStatus.AWAITING_REPAIR;
            case REJECTED -> AsReceptionStatus.AWAITING_PAYMENT;
            case REPAIRING -> AsReceptionStatus.REPAIR_IN_PROGRESS;
            // 수리 완료 시 청구서를 발행한 건은 changeStatus에서 결제 대기로 전환한다.
            case DIAGNOSING, COMPLETED -> null;
        };
    }
}
