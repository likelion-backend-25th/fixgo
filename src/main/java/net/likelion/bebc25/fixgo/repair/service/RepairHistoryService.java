package net.likelion.bebc25.fixgo.repair.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairHistoryResponse;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairPartUsageResponse;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairWorkHistoryResponse;
import net.likelion.bebc25.fixgo.repair.entity.RepairWork;
import net.likelion.bebc25.fixgo.repair.entity.part.RepairPartUsage;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.repository.RepairHistoryRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairPartUsageRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairWorkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        List<RepairWork> repairWorks =
                repairWorkRepository.findByRepairId(repair.getId());

        List<Long> repairWorkIds = repairWorks.stream()
                .map(RepairWork::getId)
                .toList();

        List<RepairPartUsage> usages = repairWorkIds.isEmpty()
                ? List.of()
                : repairPartUsageRepository.findByRepairWorkIdIn(repairWorkIds);

        Map<Long, List<RepairPartUsageResponse>> usageMap = usages.stream()
                .map(RepairPartUsageResponse::from)
                .collect(Collectors.groupingBy(
                        RepairPartUsageResponse::repairWorkId
                ));

        List<RepairWorkHistoryResponse> workResponses =
                repairWorks.stream()
                        .map(work -> RepairWorkHistoryResponse.from(
                                work,
                                usageMap.getOrDefault(work.getId(), List.of())
                        ))
                        .toList();

        return RepairHistoryResponse.from(repair, workResponses);
    }
}