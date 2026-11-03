package net.likelion.bebc25.fixgo.repair.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.as.entity.AsRequest;
import net.likelion.bebc25.fixgo.as.repository.AsReceptionRepository;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairRequest;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairResponse;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.entity.repair.WarrantyResult;
import net.likelion.bebc25.fixgo.repair.mapper.RepairMapper;
import net.likelion.bebc25.fixgo.repair.repository.RepairRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepairService {

    private final RepairRepository repairRepository;
    private final RepairMapper repairMapper;
    private final AsReceptionRepository asRequestRepository;
    private final MemberRepository memberRepository;

    // 수리 등록
    @Transactional
    public Map<String, String> createRepair(RepairRequest dto) {

        // A/S 접수 정보 조회
        AsRequest asRequest = asRequestRepository.findById(dto.getAsRequestId()).orElseThrow(() -> new IllegalArgumentException("A/S 접수 정보를 찾을 수 없습니다."));

        // 담당 기사 정보 조회
        Member technician = memberRepository.findById(dto.getTechnicianId()).orElseThrow(() -> new IllegalArgumentException("담당 기사를 찾을 수 없습니다."));

        // 수리 생성 (초기 상태 DIAGNOSING)
        Repair repair = new Repair(asRequest, technician, WarrantyResult.FREE, RepairStatus.DIAGNOSING);

        repairMapper.toRepairEntity(repair, dto, asRequest, technician);

        repairRepository.save(repair);

        Map<String, String> response = new HashMap<>();
        response.put("message", "수리 정보가 등록되었습니다.");

        return response;
    }

    // 전체 수리 목록 조회
    public List<RepairResponse> getAllRepairs() {
        return repairMapper.toRepairResponseDtoList(repairRepository.findAll());
    }

    // 수리 상세 조회
    public RepairResponse getRepair(Long id) {
        return repairMapper.toRepairResponseDto(findRepair(id));
    }

    // 수리 정보 수정
    @Transactional
    public RepairResponse updateRepair(Long id, RepairRequest dto) {

        Repair repair = findRepair(id);

        // A/S 접수 정보 조회
        AsRequest asRequest = asRequestRepository.findById(dto.getAsRequestId()).orElseThrow(() -> new IllegalArgumentException("A/S 접수 정보를 찾을 수 없습니다."));

        // 담당 기사 정보 조회
        Member technician = memberRepository.findById(dto.getTechnicianId()).orElseThrow(() -> new IllegalArgumentException("담당 기사를 찾을 수 없습니다."));

        repairMapper.toRepairEntity(repair, dto, asRequest, technician);
        repair.updateAuditInfo("test");

        return repairMapper.toRepairResponseDto(repair);
    }

    // 수리 삭제
    @Transactional
    public void deleteRepair(Long id) {
        repairRepository.delete(findRepair(id));
    }

    // 수리 정보 조회 공통 메서드
    private Repair findRepair(Long id) {
        return repairRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("수리 정보를 찾을 수 없습니다."));
    }

    // 수리 상태 변경
    @Transactional
    public void changeRepairStatus(Long id, RepairStatus status) {

        Repair repair = findRepair(id);

        RepairStatus currentStatus = repair.getStatus();

        // 동일한 상태로 변경하는 경우
        if (currentStatus == status) {
            throw new IllegalArgumentException("이미 해당 상태로 처리되어 있습니다.");
        }

        // 상태 전이 검증
        boolean isValidTransition = switch (currentStatus) {
            case DIAGNOSING -> status == RepairStatus.REQUESTED;
            case REQUESTED -> status == RepairStatus.APPROVED || status == RepairStatus.REJECTED;
            case APPROVED -> status == RepairStatus.REPAIRING;
            case REPAIRING -> status == RepairStatus.COMPLETED;
            case REJECTED, COMPLETED -> false;
        };

        if (!isValidTransition) {
            throw new IllegalArgumentException("허용되지 않은 상태 변경입니다. 현재 상태: " + currentStatus + ", 변경 요청 상태: " + status);
        }

        repair.changeStatus(status, "test");
    }
}
