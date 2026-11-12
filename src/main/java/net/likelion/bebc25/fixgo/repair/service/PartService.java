package net.likelion.bebc25.fixgo.repair.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.repair.dto.part.PartDto;
import net.likelion.bebc25.fixgo.repair.entity.MovementType;
import net.likelion.bebc25.fixgo.repair.entity.RepairWork;
import net.likelion.bebc25.fixgo.repair.entity.part.Part;
import net.likelion.bebc25.fixgo.repair.entity.part.PartStatus;
import net.likelion.bebc25.fixgo.repair.entity.part.PartStockHistory;
import net.likelion.bebc25.fixgo.repair.entity.part.RepairPartUsage;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PartService {
    private final PartRepository partRepository;
    private final RepairWorkRepository repairWorkRepository;
    private final RepairPartUsageRepository repairPartUsageRepository;
    private final PartStockHistoryRepository partStockHistoryRepository;
    private final RepairRepository repairRepository;


//    @Transactional
//    public PartDto.BriefResponse createPart(PartDto.CreateRequest request) {
//
//        Part part = partRepository.save(request.toEntity());
//
//        return PartDto.BriefResponse.from(part);
//    }

    // 부품 목록 조회
    public Page<PartDto.BriefResponse> searchParts(PartDto.SearchRequest request, Pageable pageable) {
        String partName = request.partName();
        if (partName != null && partName.isBlank()) {
            partName = null;
        }
        return partRepository.searchParts(
                partName,
                request.status(),
                request.category(),
                request.productModelId(),
                pageable
        ).map(PartDto.BriefResponse::from);
    }

    // 부품 상세 정보 조회
    public PartDto.DetailResponse getPartsById(Long partId) {
        return PartDto.DetailResponse.from(partRepository.findById(partId).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 부품입니다.")
        ));
    }

    // 부품 단가 변경
    @Transactional
    public void updateUnitPrice(PartDto.PriceUpdateRequest request) {
        Part part = partRepository.findById(request.id()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 부품입니다.")
        );

        if (request.unitPrice() < 0) {
            throw new IllegalArgumentException("단가를 0이하로 설정할 수 없습니다.");
        }

        part.updateUnitPrice(request.unitPrice());
    }

    //
    public void updateStandardMinutes(PartDto.StandardMinutesUpdateRequest request) {
        Part part = partRepository.findById(request.id()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 부품입니다.")
        );

        if (request.standardMinutes() < 0) {
            throw new IllegalArgumentException("기준 시간은 0이하로 설정할 수 없습니다.");
        }

        part.updateStandardMinutes(request.standardMinutes());
    }

    // 재고 개별 수정 (입고 or 노후화 등으로 인한 손실)
    @Transactional
    public void updateSingleStockQuantity(CustomUserDetails userDetails, PartDto.SingleStockUpdateRequest request) {
        // 수정 대상 부품 조회
        Part part = partRepository.findByIdAndStatusNot(request.id(), PartStatus.DISCONTINUED).orElseThrow(
                () -> new NoSuchElementException("존재하지 않거나 사용 불가능한 부품입니다.")
        );

        // 재고 수정
        int newStockQuantity;
        if (request.movementType().equals(MovementType.IN)) {
            newStockQuantity = part.getStockQuantity() + request.quantity();
        } else {
            if (request.quantity() > part.getStockQuantity()) {
                throw new IllegalArgumentException("재고가 부족합니다.");
            }
            newStockQuantity = part.getStockQuantity() - request.quantity();
        }
        part.updateStockQuantity(newStockQuantity);

        // 재고 수정 이후 상태값 변경
        if (part.getStatus().equals(PartStatus.ACTIVE) && part.getStockQuantity() == 0) {
            part.updateStatus(PartStatus.LOW_QUANTITY);
        } else if (!part.getStatus().equals(PartStatus.ACTIVE) && part.getStockQuantity() > 0) {
            part.updateStatus(PartStatus.ACTIVE);
        }

        // 수정 이력 저장
        PartStockHistory partStockHistory = new PartStockHistory(part, null, request.movementType(), request.quantity(), request.reason(), userDetails.getId());
        partStockHistoryRepository.save(partStockHistory);
    }

    // 재고 다량 수정 (수리 완료 이후 수리 과정에서 소모된 재고반영)
    @Transactional
    public void updateMultiStockQuantity(CustomUserDetails userDetails, Long repairId) {
        if (!repairRepository.existsByIdAndStatus(repairId, RepairStatus.COMPLETED)) {
            throw new NoSuchElementException("존재하지 않거나 아직 완료되지 않은 수리 건입니다.");
        }

        List<RepairWork> repairWorks = repairWorkRepository.findByRepair_Id(repairId);
        List<Long> repairWorkIds = repairWorks.stream().map(RepairWork::getId).toList();
        List<RepairPartUsage> repairPartUsages = repairPartUsageRepository.findByRepairWorkIdIn(repairWorkIds);

        for (RepairPartUsage repairPartUsage : repairPartUsages) {
            Part part = partRepository.findByIdAndStatus(repairPartUsage.getPart().getId(), PartStatus.ACTIVE).orElseThrow(
                    () -> new NoSuchElementException("존재하지 않거나 사용 불가능한 부품입니다.")
            );

            if (repairPartUsage.getQuantity() > part.getStockQuantity()) {
                throw new IllegalArgumentException("재고가 부족합니다.");
            }
            part.updateStockQuantity(part.getStockQuantity() - repairPartUsage.getQuantity());

            PartStockHistory partStockHistory = new PartStockHistory(part, repairPartUsage, MovementType.OUT, repairPartUsage.getQuantity(), null, userDetails.getId());
            partStockHistoryRepository.save(partStockHistory);
        }
    }
}
