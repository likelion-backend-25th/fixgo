package net.likelion.bebc25.fixgo.repair.mapper;

import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairRequest;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairResponse;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.WarrantyResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RepairMapper {

    // Repair Entity -> RepairResponse
    public RepairResponse toRepairResponseDto(Repair repair) {

        return RepairResponse.builder().id(repair.getId()).asReceptionId(repair.getAsReception().getId()).technicianId(repair.getTechnician().getId()).diagnosis(repair.getDiagnosis()).workNote(repair.getWorkNote()).repairDecision(repair.getRepairDecision()).warrantyResult(repair.getWarrantyResult().name()).reason(repair.getReason()).laborRate(repair.getLaborRate()).inspectionCost(repair.getInspectionCost()).finalAmount(repair.getFinalAmount()).status(repair.getStatus().name()).reasonForRejection(repair.getReasonForRejection()).confirmedAt(repair.getConfirmedAt()).createdAt(repair.getCreatedAt()).updatedAt(repair.getUpdatedAt()).createdBy(repair.getCreatedBy()).updatedBy(repair.getUpdatedBy()).build();
    }

    // Repair Entity List -> RepairResponse List
    public List<RepairResponse> toRepairResponseDtoList(List<Repair> repairs) {
        return repairs.stream().map(this::toRepairResponseDto).toList();
    }

    // RepairRequest -> Repair Entity
    public void toRepairEntity(Repair repair, RepairRequest dto, AsReception asReception, Member technician) {
        repair.updateRepairInfo(asReception, technician, dto.getDiagnosis(), dto.getWorkNote(), dto.getRepairDecision(), WarrantyResult.valueOf(dto.getWarrantyResult()), dto.getReason(), dto.getLaborRate(), dto.getInspectionCost(), dto.getFinalAmount(), dto.getReasonForRejection());
    }
}
