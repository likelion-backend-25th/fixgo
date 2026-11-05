package net.likelion.bebc25.fixgo.repair.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.service.AsStatusHistoryService;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class RepairStatusService {

    private final AsStatusHistoryService asStatusHistoryService;
    private final MemberRepository memberRepository;

    // 공통 수리 상태 변경
    @Transactional
    public void changeStatus(Repair repair, RepairStatus newStatus, String updatedBy) {
        changeStatus(repair, newStatus, updatedBy, null);
    }

    // 변경 사유를 포함한 수리 상태 변경
    @Transactional
    public void changeStatus(Repair repair, RepairStatus newStatus, String updatedBy, String reason) {
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

        Member changedBy = null;

        if (updatedBy != null && !updatedBy.isBlank()) {
            changedBy = memberRepository.findByEmail(updatedBy).orElseThrow(() -> new NoSuchElementException("변경한 직원을 찾을 수 없습니다."));
        }

        // 기존 상태 저장
        String fromStatus = currentStatus.name();
        String toStatus = newStatus.name();

        // 수리 상태 변경
        repair.changeStatus(newStatus, updatedBy);

        // 연결된 A/S 접수 정보
        AsReception asReception = repair.getAsReception();

        // 상태 변경 이력 저장
        asStatusHistoryService.record(asReception, fromStatus, toStatus, reason, changedBy);
    }
}