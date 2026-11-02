package net.likelion.bebc25.fixgo.repair.mapper;

import net.likelion.bebc25.fixgo.repair.dto.repair.RepairRequestDto;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairResponseDto;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.WarrantyResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RepairMapper {

    // Repair Entity -> RepairResponseDto
    public RepairResponseDto toRepairResponseDto(Repair repair) {

        return RepairResponseDto.builder().id(repair.getId()).asRequestId(repair.getAsRequestId()).technicianId(repair.getTechnicianId()).diagnosis(repair.getDiagnosis()).workNote(repair.getWorkNote()).repairDecision(repair.getRepairDecision()).warrantyResult(repair.getWarrantyResult().name()).reason(repair.getReason()).laborRate(repair.getLaborRate()).inspectionCost(repair.getInspectionCost()).finalAmount(repair.getFinalAmount()).status(repair.getStatus().name()).reasonForRejection(repair.getReasonForRejection()).confirmedAt(repair.getConfirmedAt()).createdAt(repair.getCreatedAt()).updatedAt(repair.getUpdatedAt()).createdBy(repair.getCreatedBy()).updatedBy(repair.getUpdatedBy()).build();
    }

    // Repair Entity List -> RepairResponseDto List
    public List<RepairResponseDto> toRepairResponseDtoList(List<Repair> repairs) {
        return repairs.stream().map(this::toRepairResponseDto).toList();
    }

    // RepairRequestDto -> Repair Entity
    public void toRepairEntity(Repair repair, RepairRequestDto dto) {

        repair.updateRepairInfo(dto.getAsRequestId(), dto.getTechnicianId(), dto.getDiagnosis(), dto.getWorkNote(), dto.getRepairDecision(), WarrantyResult.valueOf(dto.getWarrantyResult()), dto.getReason(), dto.getLaborRate(), dto.getInspectionCost(), dto.getFinalAmount(), dto.getReasonForRejection());
    }
}
