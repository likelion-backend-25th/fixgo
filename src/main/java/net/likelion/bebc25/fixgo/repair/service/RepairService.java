package net.likelion.bebc25.fixgo.repair.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.service.AsStatusHistoryService;
import net.likelion.bebc25.fixgo.as.repository.AsReceptionRepository;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairMessageResponse;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairRequest;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairResponse;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.entity.repair.WarrantyResult;
import net.likelion.bebc25.fixgo.repair.repository.RepairRepository;
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

    // 수리 등록
    @Transactional
    public RepairMessageResponse createRepair(RepairRequest dto, String createdBy) {

        // A/S 접수 정보 조회
        AsReception asReception = asReceptionRepository.findById(dto.asReceptionId())
                .orElseThrow(() -> new IllegalArgumentException("A/S 접수 정보를 찾을 수 없습니다."));

        // 담당 기사 정보 조회
        Member technician = memberRepository.findById(dto.technicianId())
                .orElseThrow(() -> new IllegalArgumentException("담당 기사를 찾을 수 없습니다."));

        // 수리 생성 (초기 상태 DIAGNOSING)
        Repair repair = new Repair(
                asReception,
                technician,
                WarrantyResult.FREE,
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
        AsReception asReception = asReceptionRepository.findById(dto.asReceptionId())
                .orElseThrow(() -> new IllegalArgumentException("A/S 접수 정보를 찾을 수 없습니다."));

        // 담당 기사 정보 조회
        Member technician = memberRepository.findById(dto.technicianId())
                .orElseThrow(() -> new IllegalArgumentException("담당 기사를 찾을 수 없습니다."));

        RepairRequest.toEntity(repair, dto, asReception, technician);

        return RepairResponse.from(repair);
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
}
