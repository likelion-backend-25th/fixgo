package net.likelion.bebc25.fixgo.repair.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.admin.entity.LaborRate;
import net.likelion.bebc25.fixgo.admin.entity.RateType;
import net.likelion.bebc25.fixgo.admin.repository.LaborRateRepository;
import net.likelion.bebc25.fixgo.product.entity.Category;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.product.entity.ProductModel;
import net.likelion.bebc25.fixgo.product.repository.ProductModelRepository;
import net.likelion.bebc25.fixgo.repair.entity.Part;
import net.likelion.bebc25.fixgo.repair.entity.RepairPartUsage;
import net.likelion.bebc25.fixgo.repair.entity.RepairWork;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairDecision;
import net.likelion.bebc25.fixgo.repair.repository.RepairPartUsageRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairWorkRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepairCostService {

    private final RepairRepository repairRepository;
    private final RepairWorkRepository repairWorkRepository;
    private final RepairPartUsageRepository repairPartUsageRepository;
    private final LaborRateRepository laborRateRepository;
    private final ProductModelRepository productModelRepository;

    // 수리비 계산 및 저장
    @Transactional
    public int calculateRepairCost(Long repairId, String updatedBy) {

        Repair repair = repairRepository.findById(repairId).orElseThrow(() -> new IllegalArgumentException("수리 정보를 찾을 수 없습니다."));

        // A/S 접수 제품 조회
        Product product = repair.getAsReception().getProduct();

        // 제품 모델을 통해 제품군 조회
        ProductModel productModel = productModelRepository.findById(product.getProductModelId()).orElseThrow(() -> new IllegalArgumentException("제품 모델을 찾을 수 없습니다."));

        Category category = productModel.getCategory();

        int finalAmount;
        int laborRate = 0;
        int inspectionCost = 0;

        // 수리 불가능한 경우 점검비만 청구
        if (repair.getRepairDecision() == RepairDecision.UNREPAIRABLE) {

            inspectionCost = getLaborRate(category, RateType.INSPECTION_COST);
            finalAmount = inspectionCost;

        } else if (repair.getRepairDecision() == RepairDecision.REPAIRABLE) {

            // 건당 고정 공임비 조회
            laborRate = getLaborRate(category, RateType.LABOR_RATE);

            // 수리 작업 목록 조회
            List<RepairWork> repairWorks = repairWorkRepository.findByRepairId(repairId);

            List<Long> repairWorkIds = repairWorks.stream().map(RepairWork::getId).toList();

            // 실제 사용 부품 조회
            List<RepairPartUsage> usages = repairWorkIds.isEmpty() ? List.of() : repairPartUsageRepository.findByRepairWorkIdIn(repairWorkIds);

            // 부품비 계산
            long partsCost = 0L;

            for (RepairPartUsage usage : usages) {

                Part part = usage.getPart();

                long partCost = Math.multiplyExact(Math.multiplyExact((long) usage.getUnitPrice(), usage.getQuantity()), part.getStandardMinutes());

                partsCost = Math.addExact(partsCost, partCost);
            }

            finalAmount = Math.toIntExact(Math.addExact((long) laborRate, partsCost));

        } else {
            throw new IllegalArgumentException("수리 판정 정보가 올바르지 않습니다.");
        }

        // 계산 결과 저장
        repair.updateRepairCost(laborRate, inspectionCost, finalAmount);
        repair.updateAuditInfo(updatedBy);

        return finalAmount;
    }

    // 제품군 및 요금 종류별 최신 적용 요금 조회
    private int getLaborRate(Category category, RateType rateType) {
        LaborRate laborRate = laborRateRepository.findLatestRate(category, rateType, LocalDate.now(), PageRequest.of(0, 1)).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("해당 제품군의 요금 정보를 찾을 수 없습니다."));

        return laborRate.getCost();
    }
}
