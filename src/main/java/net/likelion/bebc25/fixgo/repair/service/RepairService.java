package net.likelion.bebc25.fixgo.repair.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairRequestDto;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairResponseDto;
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

    // 수리 등록
    @Transactional
    public Map<String, String> createRepair(RepairRequestDto dto) {

        Repair repair = new Repair(WarrantyResult.FREE, RepairStatus.REQUESTED);

        repairMapper.toRepairEntity(repair, dto);

        repairRepository.save(repair);

        Map<String, String> response = new HashMap<>();
        response.put("message", "수리 정보가 등록되었습니다.");

        return response;
    }

    // 전체 수리 목록 조회
    public List<RepairResponseDto> getAllRepairs() {
        return repairMapper.toRepairResponseDtoList(repairRepository.findAll());
    }

    // 수리 상세 조회
    public RepairResponseDto getRepair(Long id) {
        return repairMapper.toRepairResponseDto(findRepair(id));
    }

    // 수리 정보 수정
    @Transactional
    public RepairResponseDto updateRepair(Long id, RepairRequestDto dto) {

        Repair repair = findRepair(id);

        repairMapper.toRepairEntity(repair, dto);
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
}
