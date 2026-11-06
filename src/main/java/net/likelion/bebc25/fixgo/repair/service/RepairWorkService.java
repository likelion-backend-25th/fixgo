package net.likelion.bebc25.fixgo.repair.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.repair.dto.repairWork.RepairWorkRequest;
import net.likelion.bebc25.fixgo.repair.dto.repairWork.RepairWorkResponse;
import net.likelion.bebc25.fixgo.repair.dto.repairWork.RepairWorkUpdate;
import net.likelion.bebc25.fixgo.repair.entity.RepairWork;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.repository.RepairRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairWorkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepairWorkService {

    private final RepairWorkRepository repairWorkRepository;
    private final RepairRepository repairRepository;

    // 작업 내역 등록
    @Transactional
    public RepairWorkResponse createRepairWork(RepairWorkRequest dto, String createdBy) {

        Repair repair = repairRepository.findById(dto.repairId())
                .orElseThrow(() -> new IllegalArgumentException("해당 수리 내역을 찾을 수 없습니다."));

        RepairWork repairWork = RepairWorkRequest.toEntity(dto, repair);

        return RepairWorkResponse.from(
                repairWorkRepository.save(repairWork)
        );
    }

    // 작업 내역 전체 조회
    public List<RepairWorkResponse> getAllRepairWorks() {

        return repairWorkRepository.findAll()
                .stream()
                .map(RepairWorkResponse::from)
                .toList();
    }

    // 작업 내역 단건 조회
    public RepairWorkResponse getRepairWork(Long id) {

        RepairWork repairWork = repairWorkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 작업 내역을 찾을 수 없습니다."));

        return RepairWorkResponse.from(repairWork);
    }

    // 특정 수리 건의 작업 내역 조회
    public List<RepairWorkResponse> getRepairWorksByRepairId(Long repairId) {

        return repairWorkRepository.findByRepairId(repairId)
                .stream()
                .map(RepairWorkResponse::from)
                .toList();
    }

    // 작업명 검색
    public List<RepairWorkResponse> searchRepairWorks(String workName) {

        return repairWorkRepository.findByWorkName(workName)
                .stream()
                .map(RepairWorkResponse::from)
                .toList();
    }

    // 작업 내역 수정
    @Transactional
    public RepairWorkResponse updateRepairWork(Long id, RepairWorkUpdate dto, String updatedBy) {

        RepairWork repairWork = repairWorkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 작업 내역을 찾을 수 없습니다."));

        repairWork.updateRepairWork(
                dto.repairPresetName(),
                dto.workName()
        );

        return RepairWorkResponse.from(repairWork);
    }

    // 작업 내역 삭제
    @Transactional
    public void deleteRepairWork(Long id) {

        RepairWork repairWork = repairWorkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 작업 내역을 찾을 수 없습니다."));

        repairWorkRepository.delete(repairWork);
    }
}