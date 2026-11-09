package net.likelion.bebc25.fixgo.repair.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.admin.entity.LaborRate;
import net.likelion.bebc25.fixgo.admin.entity.RateType;
import net.likelion.bebc25.fixgo.admin.repository.LaborRateRepository;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.repository.AsReceptionRepository;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.product.entity.Category;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.product.entity.ProductModel;
import net.likelion.bebc25.fixgo.product.repository.ProductModelRepository;
import net.likelion.bebc25.fixgo.repair.dto.repair.EstimateItemResponse;
import net.likelion.bebc25.fixgo.repair.dto.repair.EstimateResponse;
import net.likelion.bebc25.fixgo.repair.entity.RepairWork;
import net.likelion.bebc25.fixgo.repair.entity.part.RepairPartUsage;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairDecision;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.entity.repair.WarrantyResult;
import net.likelion.bebc25.fixgo.repair.exception.RepairErrorCode;
import net.likelion.bebc25.fixgo.repair.exception.RepairException;
import net.likelion.bebc25.fixgo.repair.repository.RepairPartUsageRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairWorkRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepairCostService {

    private final RepairRepository repairRepository;
    private final RepairWorkRepository repairWorkRepository;
    private final RepairPartUsageRepository repairPartUsageRepository;
    private final LaborRateRepository laborRateRepository;
    private final ProductModelRepository productModelRepository;
    private final RepairStatusService repairStatusService;
    private final AsReceptionRepository asReceptionRepository;

    // 접수 ID로 수리비가 확정된 견적서를 조회
    public EstimateResponse getEstimate(Long receptionId) {
        Repair repair = repairRepository.findByAsReception_Id(receptionId)
                .orElseThrow(() -> new RepairException(
                        RepairErrorCode.REPAIR_NOT_FOUND,
                        "수리 정보를 찾을 수 없습니다."
                ));

        if (repair.getConfirmedAt() == null) {
            throw new RepairException(
                    RepairErrorCode.REPAIR_COST_NOT_CONFIRMED,
                    "아직 수리비가 확정되지 않았습니다."
            );
        }

        return EstimateResponse.from(repair, estimateItems(repair));
    }

    // 견적 항목은 작업, 그 작업의 부품, 공임, 점검 순서로 만든다.
    private List<EstimateItemResponse> estimateItems(Repair repair) {
        List<RepairWork> repairWorks = repairWorkRepository.findByRepairId(repair.getId());
        List<Long> repairWorkIds = repairWorks.stream()
                .map(RepairWork::getId)
                .toList();
        List<RepairPartUsage> usages = repairWorkIds.isEmpty()
                ? List.of()
                : repairPartUsageRepository.findByRepairWorkIdIn(repairWorkIds);
        Map<Long, List<RepairPartUsage>> usagesByWork = usages.stream()
                .collect(Collectors.groupingBy(usage -> usage.getRepairWork().getId()));

        List<EstimateItemResponse> items = new ArrayList<>();
        for (RepairWork repairWork : repairWorks) {
            items.add(EstimateItemResponse.work(repairWork));
            for (RepairPartUsage usage : usagesByWork.getOrDefault(repairWork.getId(), List.of())) {
                items.add(EstimateItemResponse.part(usage));
            }
        }
        if (repair.getLaborRate() > 0) {
            items.add(EstimateItemResponse.labor(repair.getLaborRate()));
        }
        if (repair.getInspectionCost() > 0) {
            items.add(EstimateItemResponse.inspection(repair.getInspectionCost()));
        }
        return items;
    }

    // 수리비 계산 및 저장
    @Transactional
    public Long calculateRepairCost(Long repairId) {

        Repair repair = repairRepository.findByIdWithLock(repairId)
                .orElseThrow(() -> new RepairException(
                        RepairErrorCode.REPAIR_NOT_FOUND,
                        "수리 정보를 찾을 수 없습니다."
                ));

        validateWarrantyDecided(repair);

        Product product = repair.getAsReception().getProduct();

        ProductModel productModel = productModelRepository
                .findById(product.getProductModelId())
                .orElseThrow(() -> new RepairException(
                        RepairErrorCode.PRODUCT_MODEL_NOT_FOUND,
                        "제품 모델을 찾을 수 없습니다."
                ));

        Category category = productModel.getCategory();

        int laborCost = 0;
        int inspectionCost = 0;
        int finalAmount;

        if (repair.getRepairDecision() == RepairDecision.UNREPAIRABLE) {

            inspectionCost = getLaborRate(
                    category,
                    RateType.INSPECTION_COST
            );

            finalAmount = inspectionCost;

        } else if (repair.getRepairDecision() == RepairDecision.REPAIRABLE) {

            int laborRate = getLaborRate(
                    category,
                    RateType.LABOR_RATE
            );

            List<RepairWork> repairWorks =
                    repairWorkRepository.findByRepairId(repairId);

            List<Long> repairWorkIds = repairWorks.stream()
                    .map(RepairWork::getId)
                    .toList();

            List<RepairPartUsage> usages = repairWorkIds.isEmpty()
                    ? List.of()
                    : repairPartUsageRepository
                    .findByRepairWorkIdIn(repairWorkIds);

            int partsCost = 0;
            long totalStandardMinutes = 0L;

            for (RepairPartUsage usage : usages) {

                int unitPrice = usage.getUnitPrice();
                int quantity = usage.getQuantity();
                int standardMinutes =
                        usage.getPart().getStandardMinutes();

                // 부품 비용 = 단가 × 사용 수량
                int usageCost = Math.multiplyExact(
                        unitPrice,
                        quantity
                );

                partsCost = Math.addExact(
                        partsCost,
                        usageCost
                );

                // 작업 시간 = 부품별 표준 시간 × 사용 수량
                long usageMinutes = Math.multiplyExact(
                        (long) standardMinutes,
                        quantity
                );

                totalStandardMinutes = Math.addExact(
                        totalStandardMinutes,
                        usageMinutes
                );
            }

            // 10분 단위 올림
            long laborUnits = (totalStandardMinutes + 9) / 10;

            // 공임 = 10분당 공임 × 작업 단위
            laborCost = Math.toIntExact(
                    Math.multiplyExact(
                            (long) laborRate,
                            laborUnits
                    )
            );

            // 최종 수리비 = 부품비 + 공임
            finalAmount = Math.addExact(
                    partsCost,
                    laborCost
            );

        } else {

            throw new RepairException(
                    RepairErrorCode.INVALID_REPAIR_DECISION,
                    "수리 판정 정보가 올바르지 않습니다."
            );
        }

        repair.updateRepairCost(
                laborCost,
                inspectionCost,
                finalAmount
        );

        return (long) finalAmount;
    }

    // 제품군 및 요금 종류별 최신 적용 요금 조회
    private int getLaborRate(Category category, RateType rateType) {

        LaborRate laborRate = laborRateRepository.findLatestRate(
                        category,
                        rateType,
                        LocalDate.now(),
                        PageRequest.of(0, 1)
                )
                .stream()
                .findFirst()
                .orElseThrow(() -> new RepairException(
                        RepairErrorCode.LABOR_RATE_NOT_FOUND,
                        "해당 제품군의 요금 정보를 찾을 수 없습니다."
                ));

        return laborRate.getCost();
    }

    // 수리비 확정
    @Transactional
    public void confirmRepairCost(Long repairId, CustomUserDetails userDetails) {

        // 로그인 사용자 권한 확인
        if (userDetails.getMember().getRole() != Role.ROLE_TECHNICIAN) {
            throw new RepairException(
                    RepairErrorCode.REPAIR_COST_TECHNICIAN_ONLY,
                    "기사 권한이 필요합니다."
            );
        }

        // 수리 정보 잠금 조회
        Repair repair = repairRepository.findByIdWithLock(repairId)
                .orElseThrow(() -> new RepairException(
                        RepairErrorCode.REPAIR_NOT_FOUND,
                        "수리 정보를 찾을 수 없습니다."
                ));

        // 담당 기사 확인
        if (!repair.getTechnician().getId().equals(userDetails.getId())) {
            throw new RepairException(
                    RepairErrorCode.NOT_ASSIGNED_TECHNICIAN,
                    "담당 기사만 수리비를 확정할 수 있습니다."
            );
        }

        // 수리 상태 확인
        if (repair.getStatus() != RepairStatus.DIAGNOSING) {
            throw new RepairException(
                    RepairErrorCode.REPAIR_COST_NOT_DIAGNOSING,
                    "점검 중인 건만 수리비를 확정할 수 있습니다."
            );
        }

        // 중복 확정 확인
        if (repair.getConfirmedAt() != null) {
            throw new RepairException(
                    RepairErrorCode.REPAIR_COST_ALREADY_CONFIRMED,
                    "이미 수리비가 확정되었습니다."
            );
        }

        validateWarrantyDecided(repair);

        // A/S 접수 정보 잠금 조회
        AsReception asReception = asReceptionRepository
                .findByIdWithLock(repair.getAsReception().getId())
                .orElseThrow(() -> new RepairException(
                        RepairErrorCode.AS_RECEPTION_NOT_FOUND,
                        "A/S 접수 정보를 찾을 수 없습니다."
                ));

        // 수리비 확정
        repair.confirmRepairCost();

        // 공통 수리 상태 변경 (접수 상태는 견적 승인 대기로 함께 바뀌고 이력이 남는다)
        repairStatusService.changeStatus(
                repair,
                RepairStatus.REQUESTED,
                userDetails.getUsername(),
                "수리비 확정"
        );
    }

    // 견적 거부 시 청구할 점검비 (제품군별 최신 점검비)
    public int getInspectionCost(Repair repair) {
        ProductModel productModel = productModelRepository
                .findById(repair.getAsReception().getProduct().getProductModelId())
                .orElseThrow(() -> new RepairException(
                        RepairErrorCode.PRODUCT_MODEL_NOT_FOUND,
                        "제품 모델을 찾을 수 없습니다."
                ));

        return getLaborRate(productModel.getCategory(), RateType.INSPECTION_COST);
    }

    // 무상 또는 유상 판정을 저장한 뒤에만 수리비를 처리할 수 있다.
    private void validateWarrantyDecided(Repair repair) {
        if (repair.getWarrantyResult() == WarrantyResult.UNDECIDED) {
            throw new RepairException(
                    RepairErrorCode.WARRANTY_RESULT_UNDECIDED,
                    "무상 또는 유상 판정을 먼저 저장해 주세요."
            );
        }
    }
}