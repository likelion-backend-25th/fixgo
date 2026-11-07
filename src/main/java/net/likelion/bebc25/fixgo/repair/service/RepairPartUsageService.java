package net.likelion.bebc25.fixgo.repair.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairPartUsageRequest;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairPartUsageResponse;
import net.likelion.bebc25.fixgo.repair.entity.MovementType;
import net.likelion.bebc25.fixgo.repair.entity.RepairWork;
import net.likelion.bebc25.fixgo.repair.entity.part.Part;
import net.likelion.bebc25.fixgo.repair.entity.part.PartStatus;
import net.likelion.bebc25.fixgo.repair.entity.part.PartStockHistory;
import net.likelion.bebc25.fixgo.repair.entity.part.RepairPartUsage;
import net.likelion.bebc25.fixgo.repair.repository.PartRepository;
import net.likelion.bebc25.fixgo.repair.repository.PartStockHistoryRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairPartUsageRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairWorkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepairPartUsageService {

    private final RepairPartUsageRepository repairPartUsageRepository;
    private final RepairWorkRepository repairWorkRepository;
    private final PartRepository partRepository;
    private final PartStockHistoryRepository partStockHistoryRepository;

    /**
     * 실제 수리 부품 사용 등록
     * 1. 수리 작업 조회
     * 2. 부품 조회
     * 3. 재고 확인
     * 4. 실제 사용 부품 저장
     * 5. 재고 차감
     * 6. 출고 이력 저장
     */
    @Transactional
    public RepairPartUsageResponse createUsage(
            CustomUserDetails userDetails,
            RepairPartUsageRequest request
    ) {

        // 사용 수량 검증
        if (request.quantity() <= 0) {
            throw new IllegalArgumentException("부품 사용 수량은 1개 이상이어야 합니다.");
        }

        // 수리 작업 조회
        RepairWork repairWork = repairWorkRepository.findById(request.repairWorkId())
                .orElseThrow(() ->
                                     new IllegalArgumentException("수리 작업을 찾을 수 없습니다.")
                );

        // 부품 조회 (단종된 부품 제외)
        Part part = partRepository.findByIdAndStatusNot(
                        request.partId(),
                        PartStatus.DISCONTINUED
                )
                .orElseThrow(() ->
                                     new IllegalArgumentException("사용할 수 있는 부품을 찾을 수 없습니다.")
                );

        // 재고 확인
        if (part.getStockQuantity() < request.quantity()) {
            throw new IllegalArgumentException("부품 재고가 부족합니다.");
        }

        // 당시 부품 단가 저장
        int unitPrice = part.getUnitPrice();

        // 실제 사용 부품 저장
        RepairPartUsage usage = new RepairPartUsage(
                repairWork,
                part,
                request.quantity(),
                unitPrice
        );

        repairPartUsageRepository.save(usage);

        // 재고 차감
        int remainingStock = part.getStockQuantity() - request.quantity();
        part.updateStockQuantity(remainingStock);

        // 재고 출고 이력 저장
        PartStockHistory history = new PartStockHistory(
                part,
                usage,
                MovementType.OUT,
                -request.quantity(),
                "수리 부품 사용",
                userDetails.getId()
        );

        partStockHistoryRepository.save(history);

        return RepairPartUsageResponse.from(usage);
    }
}