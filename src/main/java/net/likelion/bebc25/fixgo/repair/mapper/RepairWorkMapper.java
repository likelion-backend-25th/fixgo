//package net.likelion.bebc25.fixgo.repair.mapper;
//
//import net.likelion.bebc25.fixgo.repair.dto.repairWork.RepairWorkRequest;
//import net.likelion.bebc25.fixgo.repair.dto.repairWork.RepairWorkResponse;
//import net.likelion.bebc25.fixgo.repair.dto.repairWork.RepairWorkUpdate;
//import net.likelion.bebc25.fixgo.repair.entity.RepairWork;
//import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//
//@Component
//public class RepairWorkMapper {
//
//    // 등록 DTO → Entity
//    public RepairWork toEntity(RepairWorkRequest dto, Repair repair) {
//        LocalDateTime now = LocalDateTime.now();
//
//        return new RepairWork(
//                repair,
//                dto.repairPresetName(),
//                dto.workName(),
//                now,
//                now,
//                "test",
//                "test"
//        );
//    }
//
//    // Entity → Response DTO
//    public RepairWorkResponse toResponseDto(RepairWork repairWork) {
//        RepairWorkResponse dto = new RepairWorkResponse();
//
//        dto.setId(repairWork.getId());
//        dto.setRepairId(repairWork.getRepair().getId());
//        dto.setRepairPresetName(repairWork.getRepairPresetName());
//        dto.setWorkName(repairWork.getWorkName());
//        dto.setCreatedAt(repairWork.getCreatedAt());
//        dto.setUpdatedAt(repairWork.getUpdatedAt());
//        dto.setCreatedBy(repairWork.getCreatedBy());
//        dto.setUpdatedBy(repairWork.getUpdatedBy());
//
//        return dto;
//    }
//
//    // 수정 DTO → Entity 반영
//    public void updateEntity(RepairWork repairWork, RepairWorkUpdate dto) {
//        repairWork.updateRepairWork(
//                dto.repairPresetName(),
//                dto.workName()
//        );
//    }
//}