package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.repair.entity.RepairPresetPart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepairPresetPartRepository extends JpaRepository<RepairPresetPart, Long> {
    List<RepairPresetPart> findByRepairPresetId(Long repairPresetId);
    
    List<RepairPresetPart> findByPartId(Long repairPartId);
}
