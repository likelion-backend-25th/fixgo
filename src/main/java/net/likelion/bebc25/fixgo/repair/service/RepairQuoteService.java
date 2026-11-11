package net.likelion.bebc25.fixgo.repair.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.repository.AsReceptionRepository;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBillType;
import net.likelion.bebc25.fixgo.payment.service.PaymentBillService;
import net.likelion.bebc25.fixgo.repair.dto.repair.CustomerQuoteView;
import net.likelion.bebc25.fixgo.repair.entity.RepairWork;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.exception.RepairErrorCode;
import net.likelion.bebc25.fixgo.repair.exception.RepairException;
import net.likelion.bebc25.fixgo.repair.repository.RepairPartUsageRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairWorkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 고객이 보는 견적과 고객의 견적 승인/거부를 담당한다.
 * 고객 행동이므로 이력의 변경자(changedBy)는 비워 둔다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepairQuoteService {

    private final RepairRepository repairRepository;
    private final RepairWorkRepository repairWorkRepository;
    private final RepairPartUsageRepository repairPartUsageRepository;
    private final AsReceptionRepository asReceptionRepository;
    private final RepairStatusService repairStatusService;
    private final RepairCostService repairCostService;
    private final PaymentBillService paymentBillService;

    /**
     * 고객 포탈 견적 영역. 수리 정보가 없거나 수리비가 아직 확정되지 않았으면 비어 있다.
     */
    public Optional<CustomerQuoteView> findCustomerView(Long asReceptionId) {
        return repairRepository.findByAsReception_Id(asReceptionId)
                .filter(repair -> repair.getConfirmedAt() != null)
                .map(repair -> CustomerQuoteView.of(repair, findItems(repair)));
    }

    /**
     * 견적 승인. 수리 상태는 승인, 접수 상태는 수리 대기가 된다.
     */
    @Transactional
    public void approveByCustomer(Long asReceptionId) {
        Repair repair = findPendingQuote(asReceptionId);
        repairStatusService.changeStatus(repair, RepairStatus.APPROVED, null, "고객 견적 승인");
    }

    /**
     * 견적 거부. 수리 상태는 거부, 접수 상태는 결제 대기가 되고 점검비 청구서가 발행된다.
     */
    @Transactional
    public void rejectByCustomer(Long asReceptionId) {
        Repair repair = findPendingQuote(asReceptionId);
        repairStatusService.changeStatus(repair, RepairStatus.REJECTED, null, "고객 견적 거부");

        long inspectionCost = repairCostService.getInspectionCost(repair);
        paymentBillService.createBill(asReceptionId, inspectionCost, PaymentBillType.INSPECTION);
    }

    /**
     * 접수를 먼저 잠가 같은 접수에 대한 승인/거부가 동시에 처리되지 않게 한 뒤, 승인 대기 중인 견적인지 확인한다.
     */
    private Repair findPendingQuote(Long asReceptionId) {
        AsReception reception = asReceptionRepository.findByIdWithLock(asReceptionId)
                .orElseThrow(() -> new RepairException(
                        RepairErrorCode.AS_RECEPTION_NOT_FOUND,
                        "A/S 접수 정보를 찾을 수 없습니다."
                ));

        Repair repair = repairRepository.findByAsReception_Id(asReceptionId)
                .orElseThrow(() -> new RepairException(
                        RepairErrorCode.REPAIR_NOT_FOUND,
                        "수리 정보를 찾을 수 없습니다."
                ));

        if (reception.getStatus() != AsReceptionStatus.AWAITING_QUOTE_APPROVAL
                || repair.getStatus() != RepairStatus.REQUESTED) {
            throw new RepairException(
                    RepairErrorCode.QUOTE_NOT_PENDING,
                    "승인 대기 중인 견적이 아닙니다."
            );
        }

        return repair;
    }

    private List<CustomerQuoteView.Item> findItems(Repair repair) {
        List<Long> repairWorkIds = repairWorkRepository.findByRepair_Id(repair.getId())
                .stream()
                .map(RepairWork::getId)
                .toList();

        if (repairWorkIds.isEmpty()) {
            return List.of();
        }

        return repairPartUsageRepository.findByRepairWorkIdIn(repairWorkIds)
                .stream()
                .map(CustomerQuoteView.Item::from)
                .toList();
    }
}
