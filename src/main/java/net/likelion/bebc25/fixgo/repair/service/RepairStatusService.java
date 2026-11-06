package net.likelion.bebc25.fixgo.repair.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.service.AsStatusHistoryService;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
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

        String fromStatus = currentStatus.name();
        String toStatus = newStatus.name();

        repair.changeStatus(newStatus);

        AsReception asReception = repair.getAsReception();

        asStatusHistoryService.record(
                asReception,
                fromStatus,
                toStatus,
                reason,
                changedBy
        );
    }
}
