//package net.likelion.bebc25.fixgo.repair.mapper;
//
//import net.likelion.bebc25.fixgo.as.entity.AsReception;
//import net.likelion.bebc25.fixgo.auth.entity.Member;
//import net.likelion.bebc25.fixgo.repair.dto.repair.RepairRequest;
//import net.likelion.bebc25.fixgo.repair.dto.repair.RepairResponse;
//import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
//import net.likelion.bebc25.fixgo.repair.entity.repair.WarrantyResult;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//public class RepairMapper {
//
//    // Repair Entity -> RepairResponse
//    public RepairResponse toRepairResponseDto(Repair repair) {
//
//        return new RepairResponse(
//                repair.getId(),
//                repair.getAsReception().getId(),
//                repair.getTechnician().getId(),
//                repair.getDiagnosis(),
//                repair.getWorkNote(),
//                repair.getRepairDecision(),
//                repair.getWarrantyResult().name(),
//                repair.getReason(),
//                repair.getLaborRate(),
//                repair.getInspectionCost(),
//                repair.getFinalAmount(),
//                repair.getStatus().name(),
//                repair.getReasonForRejection(),
//                repair.getConfirmedAt(),
//                repair.getCreatedAt(),
//                repair.getUpdatedAt(),
//                repair.getCreatedBy(),
//                repair.getUpdatedBy()
//        );
//    }
//
//    // Repair Entity List -> RepairResponse List
//    public List<RepairResponse> toRepairResponseDtoList(List<Repair> repairs) {
//        return repairs.stream().map(this::toRepairResponseDto).toList();
//    }
//
//    // RepairRequest -> Repair Entity
//    public void toRepairEntity(
//            Repair repair,
//            RepairRequest dto,
//            AsReception asReception,
//            Member technician
//    ) {
//        repair.updateRepairInfo(
//                asReception,
//                technician,
//                dto.diagnosis(),
//                dto.workNote(),
//                dto.repairDecision(),
//                WarrantyResult.valueOf(dto.warrantyResult()),
//                dto.reason(),
//                dto.laborRate(),
//                dto.inspectionCost(),
//                dto.finalAmount(),
//                dto.reasonForRejection()
//        );
//    }
//}
