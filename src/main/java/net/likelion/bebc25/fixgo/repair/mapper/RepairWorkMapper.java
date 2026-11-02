package net.likelion.bebc25.fixgo.repair.mapper;

import net.likelion.bebc25.fixgo.repair.dto.repairWork.RepairWorkRequestDto;
import net.likelion.bebc25.fixgo.repair.dto.repairWork.RepairWorkResponseDto;
import net.likelion.bebc25.fixgo.repair.dto.repairWork.RepairWorkUpdateDto;
import net.likelion.bebc25.fixgo.repair.entity.RepairWork;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RepairWorkMapper {

    // 등록 DTO → Entity
    public RepairWork toEntity(RepairWorkRequestDto dto, Repair repair) {
        LocalDateTime now = LocalDateTime.now();

        return new RepairWork(repair, dto.getRepairPresetName(), dto.getWorkName(), now, now, "test", "test");
    }

    // Entity → Response DTO
    public RepairWorkResponseDto toResponseDto(RepairWork repairWork) {
        RepairWorkResponseDto dto = new RepairWorkResponseDto();

        dto.setId(repairWork.getId());
        dto.setRepairId(repairWork.getRepair().getId());
        dto.setRepairPresetName(repairWork.getRepairPresetName());
        dto.setWorkName(repairWork.getWorkName());
        dto.setCreatedAt(repairWork.getCreatedAt());
        dto.setUpdatedAt(repairWork.getUpdatedAt());
        dto.setCreatedBy(repairWork.getCreatedBy());
        dto.setUpdatedBy(repairWork.getUpdatedBy());

        return dto;
    }

    // 수정 DTO → Entity 반영
    public void updateEntity(RepairWork repairWork, RepairWorkUpdateDto dto) {
        repairWork.updateRepairWork(dto.getRepairPresetName(), dto.getWorkName());
    }
}