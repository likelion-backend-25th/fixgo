package net.likelion.bebc25.fixgo.repair.service;

import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import org.springframework.stereotype.Service;

@Service
public class RepairStatusService {

    // 공통 수리 상태 변경
    public void changeStatus(Repair repair, RepairStatus newStatus, String updatedBy) {

        RepairStatus currentStatus = repair.getStatus();

        if (currentStatus == newStatus) {
            throw new IllegalArgumentException("이미 해당 상태로 처리되어 있습니다.");
        }

        boolean isValidTransition = switch (currentStatus) {
            case DIAGNOSING -> newStatus == RepairStatus.REQUESTED;
            case REQUESTED -> newStatus == RepairStatus.APPROVED || newStatus == RepairStatus.REJECTED;
            case APPROVED -> newStatus == RepairStatus.REPAIRING;
            case REPAIRING -> newStatus == RepairStatus.COMPLETED;
            case REJECTED, COMPLETED -> false;
        };

        if (!isValidTransition) {
            throw new IllegalArgumentException("허용되지 않은 상태 변경입니다. 현재 상태: " + currentStatus + ", 변경 요청 상태: " + newStatus);
        }

        repair.changeStatus(newStatus, updatedBy);
    }
}