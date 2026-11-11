package net.likelion.bebc25.fixgo.repair.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairHistoryResponse;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairPartUsageResponse;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairStatementResponse;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairWorkHistoryResponse;
import net.likelion.bebc25.fixgo.repair.entity.RepairWork;
import net.likelion.bebc25.fixgo.repair.entity.part.RepairPartUsage;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.repository.RepairHistoryRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairPartUsageRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairWorkRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepairHistoryService {

    private final RepairHistoryRepository repairHistoryRepository;
    private final RepairWorkRepository repairWorkRepository;
    private final RepairPartUsageRepository repairPartUsageRepository;

    public RepairHistoryResponse getRepairHistory(Long asReceptionId) {

        Repair repair = repairHistoryRepository.findByAsReceptionId(asReceptionId)
                .orElseThrow(() ->
                                     new IllegalArgumentException("수리 정보를 찾을 수 없습니다.")
                );

        return RepairHistoryResponse.from(repair, getRepairWorks(repair.getId()));
    }

    // 수리가 완료된 건에 한해 출력용 수리내역서를 조회한다.
    public RepairStatementResponse getRepairStatement(Long receptionId) {
        Repair repair = repairHistoryRepository.findByAsReceptionId(receptionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "수리 정보를 찾을 수 없습니다."
                ));

        if (repair.getStatus() != RepairStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "수리가 완료된 건만 내역서를 조회할 수 있습니다."
            );
        }

        return RepairStatementResponse.from(
                repair,
                getRepairWorks(repair.getId()),
                // 조회 시각을 내역서의 발행일로 표시한다.
                LocalDateTime.now()
        );
    }

    private List<RepairWorkHistoryResponse> getRepairWorks(Long repairId) {
        List<RepairWork> repairWorks = repairWorkRepository.findByRepair_Id(repairId);

        List<Long> repairWorkIds = repairWorks.stream()
                .map(RepairWork::getId)
                .toList();

        // 작업별로 쿼리를 실행하지 않도록 사용 부품을 한 번에 조회한다.
        List<RepairPartUsage> usages = repairWorkIds.isEmpty()
                ? List.of()
                : repairPartUsageRepository.findByRepairWorkIdIn(repairWorkIds);

        Map<Long, List<RepairPartUsageResponse>> usageMap = usages.stream()
                .map(RepairPartUsageResponse::from)
                .collect(Collectors.groupingBy(
                        RepairPartUsageResponse::repairWorkId
                ));

        return repairWorks.stream()
                .map(work -> RepairWorkHistoryResponse.from(
                        work,
                        usageMap.getOrDefault(work.getId(), List.of())
                ))
                .toList();
    }
}
