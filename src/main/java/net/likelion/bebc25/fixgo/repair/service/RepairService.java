package net.likelion.bebc25.fixgo.repair.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.repository.AsReceptionRepository;
import net.likelion.bebc25.fixgo.as.service.AsStatusHistoryService;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairMessageResponse;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairRequest;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairResponse;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.entity.repair.WarrantyResult;
import net.likelion.bebc25.fixgo.repair.repository.RepairRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepairService {

    private final RepairRepository repairRepository;
    private final AsReceptionRepository asReceptionRepository;
    private final MemberRepository memberRepository;
    private final RepairStatusService repairStatusService;
    private final AsStatusHistoryService asStatusHistoryService;
    private final PartService partService;
    private final ApplicationEventPublisher eventPublisher;

    // 수리 등록
    @Transactional
    public RepairMessageResponse createRepair(RepairRequest dto, String createdBy) {

        // A/S 접수 정보 조회
        AsReception asReception = asReceptionRepository.findByIdWithLock(dto.asReceptionId())
                .orElseThrow(() -> new IllegalArgumentException("A/S 접수 정보를 찾을 수 없습니다."));

        Member technician = requireAssignedTechnician(asReception, dto.technicianId());

        // 수리 생성 (초기 상태 DIAGNOSING)
        Repair repair = new Repair(
                asReception,
                technician,
                WarrantyResult.UNDECIDED, // 처음부터 무상으로 처리되던 로직 수정
                RepairStatus.DIAGNOSING
        );

        RepairRequest.toEntity(repair, dto, asReception, technician);

        // 수리 정보 저장
        repairRepository.save(repair);

        // 수리 등록 = 점검 시작. 접수 상태를 점검으로 바꾸고 이력을 남긴다.
        if (asReception.getStatus() == AsReceptionStatus.RECEIVED) {
            Member changedBy = createdBy == null ? null : memberRepository.findByEmail(createdBy).orElse(null);
            asReception.changeStatus(AsReceptionStatus.INSPECTION);
            asStatusHistoryService.record(
                    asReception,
                    AsReceptionStatus.RECEIVED.name(),
                    AsReceptionStatus.INSPECTION.name(),
                    "점검 시작",
                    changedBy
            );
        }

        return new RepairMessageResponse("수리 정보가 등록되었습니다.");
    }

    // 전체 수리 목록 조회
    public List<RepairResponse> getAllRepairs() {
        return repairRepository.findAllWithDetails()
                .stream()
                .map(RepairResponse::from)
                .toList();
    }

    // 수리 상세 조회
    public RepairResponse getRepair(Long id) {
        return RepairResponse.from(findRepair(id));
    }

    // 로그인한 기사 본인에게 배정된 수리 목록 조회
    public List<RepairResponse> getMyRepairs(Long technicianId) {
        return repairRepository.findByAsReception_Technician_Id(technicianId)
                .stream()
                .map(RepairResponse::from)
                .toList();
    }

    // 로그인한 기사 본인에게 배정된 수리 상세 조회
    public RepairResponse getMyRepair(Long id, Long technicianId) {

        Repair repair = repairRepository.findByIdAndAsReception_Technician_Id(id, technicianId)
                .orElseThrow(() -> new IllegalArgumentException("본인에게 배정된 수리 건이 아닙니다."));

        return RepairResponse.from(repair);
    }

    // 요청번호로 수리 상세 조회
    public RepairResponse getRepairByReceptionNumber(String receptionNumber) {

        Repair repair = repairRepository.findByAsReception_ReceptionNumber(receptionNumber)
                .orElseThrow(() -> new IllegalArgumentException("해당 접수번호의 수리 정보를 찾을 수 없습니다."));

        return RepairResponse.from(repair);
    }

    // 수리 정보 수정
    @Transactional
    public RepairResponse updateRepair(Long id, RepairRequest dto, String updatedBy) {

        Repair repair = findRepair(id);

        // A/S 접수 정보 조회
        AsReception asReception = asReceptionRepository.findByIdWithLock(dto.asReceptionId())
                .orElseThrow(() -> new IllegalArgumentException("A/S 접수 정보를 찾을 수 없습니다."));

        Member technician = requireAssignedTechnician(asReception, dto.technicianId());

        RepairRequest.toEntity(repair, dto, asReception, technician);

        return RepairResponse.from(repair);
    }

    private Member requireAssignedTechnician(AsReception reception, Long requestedTechnicianId) {
        Member technician = reception.getTechnician();
        if (technician == null || !technician.getId().equals(requestedTechnicianId)) {
            throw new IllegalArgumentException("접수에 배정된 기사와 일치해야 합니다. 담당 기사 변경은 접수에서 처리해 주세요.");
        }
        return technician;
    }

    // 수리 삭제
    @Transactional
    public void deleteRepair(Long id) {
        repairRepository.delete(findRepair(id));
    }

    // 수리 정보 조회 공통 메서드
    private Repair findRepair(Long id) {
        return repairRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("수리 정보를 찾을 수 없습니다."));
    }

    // 로그인한 기사가 담당하는 수리 건인지 확인한 후 수리 시작
    @Transactional
    public void startRepair(Long id, Long technicianId, String updatedBy) {

        Repair repair = repairRepository.findByIdAndAsReception_Technician_Id(id, technicianId)
                .orElseThrow(() -> new IllegalArgumentException("본인에게 배정된 수리 건이 아닙니다."));

        // 기존 상태 전이 로직 재사용 (접수 상태는 수리 중으로 함께 바뀐다)
        repairStatusService.changeStatus(repair, RepairStatus.REPAIRING, updatedBy, "수리 시작");
    }

    // 수리 상태 변경
    @Transactional
    public void changeRepairStatus(Long id, RepairStatus status, String updatedBy) {

        Repair repair = findRepair(id);

        repairStatusService.changeStatus(repair, status, updatedBy);
    }

    // 로그인한 기사가 담당하는 수리 건인지 확인한 후 수리 완료
    @Transactional
    public void completeRepair(Long id, CustomUserDetails userDetails) {

        Repair repair = repairRepository.findByIdAndAsReception_Technician_Id(id, userDetails.getId())
                .orElseThrow(() -> new IllegalArgumentException("본인에게 배정된 수리 건이 아닙니다."));

        if (repair.getStatus() != RepairStatus.REPAIRING) {
            throw new IllegalArgumentException("수리 중인 건만 완료할 수 있습니다.");
        }

        repairStatusService.changeStatus(repair, RepairStatus.COMPLETED, userDetails.getUsername(), "수리 완료");

        // 같은 트랜잭션에서 사용 부품 재고를 차감한다.
        partService.updateMultiStockQuantity(userDetails, id);

        // A/S 접수 및 고객 정보 조회
        AsReception asReception = repair.getAsReception();
        Customer customer = asReception.getCustomer();

        // 수리 완료 이메일 이벤트
        eventPublisher.publishEvent(
                new RepairCompletedEvent(
                        asReception.getId(),
                        asReception.getReceptionNumber(),
                        customer.getName(),
                        customer.getEmail(),
                        repair.getWarrantyResult() != WarrantyResult.FREE
                )
        );
    }

    // 수리 완료 이메일 발송 이벤트
    public record RepairCompletedEvent(
            Long asReceptionId,
            String receptionNumber,
            String customerName,
            String recipientEmail,
            boolean paymentRequired
    ) {}
}
